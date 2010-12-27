class TradeCraftListener extends PluginListener {

    private final TradeCraft plugin;

    TradeCraftListener(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public void onBlockRightClicked(
            Player player,
            Block blockClicked,
            Item itemInHand) {

        TradeCraftStore store = getStoreFromSignBlock(blockClicked);

        if (store == null) {
            return;
        }

        if (store.merchantName != null && player.getName().equals(store.merchantName)) {
            handleMerchantClick(player, store);
        } else {
            handlePatronClick(player, store);
        }

        return;
    }

    public boolean onBlockBreak(Player player, Block block) {
        TradeCraftStore store = getStoreFromSignOrChestBlock(block);

        if (store == null) {
            return false;
        }

        if (store.merchantName == null) {
            return false;
        }

        if (store.merchantName.equals(player.getName())) {
            // TODO: Don't let them destroy their own store unless they withdraw everything!
            return false;
        }

        // This sign or chest is part of a store that belongs to another player.
        // Don't let the current player destroy it.
        plugin.sendMessage(player, "You can't destroy signs or chests that belong to other players!");
        return true;
    }

    public boolean onSignChange(Player player, Sign sign) {
        String merchantName = getMerchantName(sign);

        if (merchantName == null) {
            return false;
        }

        if (merchantName.equals(player.getName())) {
            return false;
        }

        // The sign has some other player's name on it.
        // Don't let the current player create it.
        plugin.sendMessage(player, "You can't create signs with other players names on them!");
        return true;
    }

    private TradeCraftStore getStoreFromSignOrChestBlock(Block block) {
        if (block.getType() == TradeCraft.CHEST) {
            block = plugin.server.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
        }

        return getStoreFromSignBlock(block);
    }

    private TradeCraftStore getStoreFromSignBlock(Block block) {
        if (block.getType() != TradeCraft.WALL_SIGN) {
            return null;
        }

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        Sign sign = (Sign)plugin.server.getComplexBlock(x, y, z);

        // The sign at this location can be null if it was just destroyed.
        if (sign == null) {
            return null;
        }

        TradeCraftConfigurationInfo tradeInfo = getConfigurationInfo(sign);

        if (tradeInfo == null) {
            return null;
        }

        Block blockBelowSign = plugin.server.getBlockAt(x, y - 1, z);

        if (blockBelowSign.getType() != TradeCraft.CHEST) {
            return null;
        }

        Chest chest = (Chest)plugin.server.getComplexBlock(x, y - 1, z);
        TradeCraftChestInfo chestInfo = new TradeCraftChestInfo(chest);

        String merchantName = getMerchantName(sign);

        TradeCraftStore store = new TradeCraftStore();
        store.sign = sign;
        store.configurationInfo = tradeInfo;
        store.chest = chest;
        store.chestInfo = chestInfo;
        store.merchantName = merchantName;

        return store;
    }

    private TradeCraftConfigurationInfo getConfigurationInfo(Sign sign) {
        for (int i = 0; i < 4; i++) {
            String signText = sign.getText(i);

            TradeCraftConfigurationInfo info = plugin.configuration.get(signText);

            if (info != null) {
                return info;
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

    private void handleMerchantClick(Player player, TradeCraftStore store) {
        if (!store.chestInfo.containsOnlyOneItemType()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }

        int x = store.sign.getX();
        int y = store.sign.getY();
        int z = store.sign.getZ();

        if (store.chestInfo.total == 0) {
            int goldAmount = plugin.data.withdrawGold(x, y, z);
            if (goldAmount > 0) {
                populateChest(store.chest, TradeCraft.GOLD_INGOT, goldAmount);
                plugin.sendMessage(player, "Withdrew %1$d Gold.", goldAmount);
            } else {
                int itemAmount = plugin.data.withdraw(x, y, z);
                if (itemAmount > 0) {
                    populateChest(store.chest, store.configurationInfo.id, itemAmount);
                    plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, store.configurationInfo.name);
                } else {
                    plugin.sendMessage(player, "There is nothing to withdraw.");
                }
            }
        } else if (store.chestInfo.id == TradeCraft.GOLD_INGOT) {
            plugin.data.depositGold(x, y, z, store.chestInfo.total);
            plugin.sendMessage(player, "Deposited %1$d Gold.", store.chestInfo.total);
            populateChest(store.chest, 0, 0);
            int itemAmount = plugin.data.withdraw(x, y, z);
            if (itemAmount > 0) {
                populateChest(store.chest, store.configurationInfo.id, itemAmount);
                plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, store.configurationInfo.name);
            }
        } else if (store.chestInfo.id == store.configurationInfo.id) {
            plugin.data.deposit(x, y, z, store.chestInfo.total);
            populateChest(store.chest, 0, 0);
            plugin.sendMessage(player, "Deposited %1$d %2$s.", store.chestInfo.total, store.configurationInfo.name);
        } else {
            plugin.sendMessage(player, "You can't deposit that here!");
        }
    }

    private void handlePatronClick(Player player, TradeCraftStore store) {
        if (store.chestInfo.total == 0) {
            if (store.configurationInfo.buyAmount != 0) {
                plugin.sendMessage(player,
                        "You can buy %1$d %2$s items for %3$d gold.",
                        store.configurationInfo.buyAmount,
                        store.configurationInfo.name,
                        store.configurationInfo.buyValue);
            }

            if (store.configurationInfo.sellAmount != 0) {
                plugin.sendMessage(player,
                        "You can sell %1$d %2$s items for %3$d gold.",
                        store.configurationInfo.sellAmount,
                        store.configurationInfo.name,
                        store.configurationInfo.sellValue);
            }

            plugin.sendMessage(player, "The chest is empty.");
            return;
        }

        if (!store.chestInfo.containsOnlyOneItemType()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }

        if (store.chestInfo.id == TradeCraft.GOLD_INGOT) {
            makePurchase(player, store.configurationInfo, store.chestInfo, store.chest);
        } else if (store.chestInfo.id == store.configurationInfo.id) {
            makeSale(player, store.configurationInfo, store.chestInfo, store.chest);
        } else {
            plugin.sendMessage(player, "You can't sell that here!");
        }
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
