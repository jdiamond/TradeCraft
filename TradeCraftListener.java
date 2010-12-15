class TradeCraftListener extends PluginListener {

    private final TradeCraft plugin;

    TradeCraftListener(TradeCraft plugin) {
        this.plugin = plugin;
    }

	public void onBlockRightClicked(
            Player player,
            Block blockClicked,
            Item itemInHand) {

        if (blockClicked.getType() != TradeCraft.WALL_SIGN) {
            return;
        }

        int x = blockClicked.getX();
        int y = blockClicked.getY();
        int z = blockClicked.getZ();

        Sign sign = (Sign)plugin.server.getComplexBlock(x, y, z);
        TradeCraftConfigurationInfo tradeInfo = getTradeInfo(sign);

        if (tradeInfo == null) {
            return;
        }

        Block block = (Block)plugin.server.getBlockAt(x, y - 1, z);

        if (block.getType() != TradeCraft.CHEST) {
            return;
        }

        Chest chest = (Chest)plugin.server.getComplexBlock(x, y - 1, z);
        TradeCraftChestInfo chestInfo = new TradeCraftChestInfo(chest);

        String merchantName = getMerchantName(sign);

        if (merchantName == null) {
            if (chestInfo.total == 0) {
                if (tradeInfo.buyAmount != 0) {
                    plugin.sendMessage(player,
                                "You can buy %1$d %2$s items for %3$d gold.",
                                tradeInfo.buyAmount,
                                tradeInfo.name,
                                tradeInfo.buyValue);
                }

                if (tradeInfo.sellAmount != 0) {
                    plugin.sendMessage(player,
                                "You can sell %1$d %2$s items for %3$d gold.",
                                tradeInfo.sellAmount,
                                tradeInfo.name,
                                tradeInfo.sellValue);
                }

                plugin.sendMessage(player, "The chest is empty.");
                return;
            }

            if (!chestInfo.containsOnlyOneItemType()) {
                plugin.sendMessage(player, "The chest has more than one type of item in it!");
                return;
            }

            if (chestInfo.id == TradeCraft.GOLD_INGOT) {
                makePurchase(player, tradeInfo, chestInfo, chest);
            } else if (chestInfo.id == tradeInfo.id) {
                makeSale(player, tradeInfo, chestInfo, chest);
            } else {
                plugin.sendMessage(player, "You can't sell that here!");
            }
        } else {
            if (player.getName().equals(merchantName)) {
                if (!chestInfo.containsOnlyOneItemType()) {
                    plugin.sendMessage(player, "The chest has more than one type of item in it!");
                    return;
                }
                if (chestInfo.total == 0) {
                    int goldAmount = plugin.data.withdrawGold(x, y, z);
                    if (goldAmount > 0) {
                        populateChest(chest, TradeCraft.GOLD_INGOT, goldAmount);
                        plugin.sendMessage(player, "Withdrew %1$d Gold.", goldAmount);
                    } else {
                        int itemAmount = plugin.data.withdraw(x, y, z);
                        if (itemAmount > 0) {
                            populateChest(chest, tradeInfo.id, itemAmount);
                            plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, tradeInfo.name);
                        } else {
                            plugin.sendMessage(player, "There is nothing to withdraw.");
                        }
                    }
                } else if (chestInfo.id == TradeCraft.GOLD_INGOT) {
                    plugin.data.depositGold(x, y, z, chestInfo.total);
                    plugin.sendMessage(player, "Deposited %1$d Gold.", chestInfo.total);
                    populateChest(chest, 0, 0);
                    int itemAmount = plugin.data.withdraw(x, y, z);
                    if (itemAmount > 0) {
                        populateChest(chest, tradeInfo.id, itemAmount);
                        plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, tradeInfo.name);
                    }
                } else if (chestInfo.id == tradeInfo.id) {
                    plugin.data.deposit(x, y, z, chestInfo.total);
                    populateChest(chest, 0, 0);
                    plugin.sendMessage(player, "Deposited %1$d %2$s.", chestInfo.total, tradeInfo.name);
                } else {
                    plugin.sendMessage(player, "You can't deposit that here!");
                }
            }
        }

        return;
    }

    private TradeCraftConfigurationInfo getTradeInfo(Sign sign) {
        for (int i = 0; i < 4; i++) {
            String signText = sign.getText(i);
            TradeCraftConfigurationInfo currentTradeInfo = plugin.configuration.get(signText);

            if (currentTradeInfo != null) {
                return currentTradeInfo;
            }
        }

        return null;
    }

    private String getMerchantName(Sign sign) {
        for (int i = 0; i < 4; i++) {
            String signText = sign.getText(i);

            if (signText.startsWith("-") && signText.endsWith("-")) {
                return signText.substring(1, signText.length() - 1);
            }
        }

        return null;

    }

    private void makePurchase(
            Player player,
            TradeCraftConfigurationInfo tradeInfo,
            TradeCraftChestInfo chestInfo,
            Chest chest) {

        if (tradeInfo.buyAmount == 0) {
            plugin.sendMessage(player, "You can't buy that here!");
            return;
        }

        int totalItems = chestInfo.total / tradeInfo.buyValue * tradeInfo.buyAmount;

        if (totalItems == 0) {
            plugin.sendMessage(player,
                        "You need to spend at least %1$d gold to get any items.",
                        tradeInfo.buyValue);
            return;
        }

        plugin.sendMessage(player,
                    "You bought %1$d %2$s for %3$d gold.",
                    totalItems,
                    tradeInfo.name,
                    chestInfo.total);

        populateChest(chest, tradeInfo.id, totalItems);
    }

    private void makeSale(
            Player player,
            TradeCraftConfigurationInfo tradeInfo,
            TradeCraftChestInfo chestInfo,
            Chest chest) {

        if (tradeInfo.sellAmount == 0) {
            plugin.sendMessage(player, "You can't sell that here!");
            return;
        }

        int totalValue = chestInfo.total / tradeInfo.sellAmount * tradeInfo.sellValue;

        if (totalValue == 0) {
            plugin.sendMessage(player,
                        "You need to sell at least %1$d %2$s to get any gold.",
                        tradeInfo.sellAmount,
                        tradeInfo.name);
            return;
        }

        plugin.sendMessage(player,
                    "You sold %1$d %2$s for %3$d gold.",
                    chestInfo.total,
                    tradeInfo.name,
                    totalValue);

        populateChest(chest, TradeCraft.GOLD_INGOT, totalValue);
    }

    private void populateChest(Chest chest, int id, int amount) {
        chest.clearContents();

        int blocks = amount / TradeCraft.MAX_STACK_SIZE;

        for (int i = 0; i < blocks; i++) {
            chest.addItem(new Item(id, TradeCraft.MAX_STACK_SIZE));
        }

        int remainder = amount % TradeCraft.MAX_STACK_SIZE;

        if (remainder > 0) {
            chest.addItem(new Item(id, remainder));
        }

        chest.update();
    }
}