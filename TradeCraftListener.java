class TradeCraftListener extends PluginListener {

    private final TradeCraft plugin;

    TradeCraftListener(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public void onBlockRightClicked(Player player, Block blockClicked, Item itemInHand) {
        TradeCraftStore store = getStoreFromSignBlock(blockClicked);

        if (store == null) {
            return;
        }

        if (store.isOwnedByPlayer(player)) {
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

        if (store.playerCanDestroy(player)) {
            return false;
        }

        plugin.sendMessage(player, "You can't destroy this sign or chest!");

        return true;
    }

    public boolean onSignChange(Player player, Sign sign) {
        String merchantName = plugin.getMerchantName(sign);

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

        String itemName = plugin.getItemName(sign);

        if (itemName == null) {
            return null;
        }

        if (!plugin.configuration.isConfigured(itemName)) {
            return null;
        }

        Block blockBelowSign = plugin.server.getBlockAt(x, y - 1, z);

        if (blockBelowSign.getType() != TradeCraft.CHEST) {
            return null;
        }

        Chest chest = (Chest)plugin.server.getComplexBlock(x, y - 1, z);

        TradeCraftStore store;

        if (plugin.getMerchantName(sign) == null) {
            store = new TradeCraftInfiniteStore(plugin, sign, chest);
        } else {
            store = new TradeCraftPlayerOwnedStore(plugin, sign, chest);
        }

        return store;
    }

    private void handleMerchantClick(Player player, TradeCraftStore store) {
        if (!store.chestContentsAreOK()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }

        if (store.getChestItemCount() == 0) {
            int goldAmount = store.withdrawGold();
            if (goldAmount > 0) {
                store.populateChest(TradeCraft.GOLD_INGOT, goldAmount);
                plugin.sendMessage(player, "Withdrew %1$d gold.", goldAmount);
            } else {
                int itemAmount = store.withdrawItems();
                if (itemAmount > 0) {
                    store.populateChest(store.getItemType(), itemAmount);
                    plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, store.getItemName());
                } else {
                    plugin.sendMessage(player, "There is nothing to withdraw.");
                }
            }
        } else if (store.getChestItemType() == TradeCraft.GOLD_INGOT) {
            store.depositGold(store.getChestItemCount());
            plugin.sendMessage(player, "Deposited %1$d gold.", store.getChestItemCount());
            store.populateChest(0, 0);
            int itemAmount = store.withdrawItems();
            if (itemAmount > 0) {
                store.populateChest(store.getItemType(), itemAmount);
                plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, store.getItemName());
            }
        } else if (store.getChestItemType() == store.getItemType()) {
            store.depositItems(store.getChestItemCount());
            store.populateChest(0, 0);
            plugin.sendMessage(player, "Deposited %1$d %2$s.", store.getChestItemCount(), store.getItemName());
        } else {
            plugin.sendMessage(player, "You can't deposit that here!");
        }
    }

    private void handlePatronClick(Player player, TradeCraftStore store) {
        if (store.getChestItemCount() == 0) {
            if (store.playerCanBuy()) {
                plugin.sendMessage(player,
                        "You can buy %1$d %2$s for %3$d gold.",
                        store.getBuyAmount(),
                        store.getItemName(),
                        store.getBuyValue());
            }

            if (store.playerCanSell()) {
                plugin.sendMessage(player,
                        "You can sell %1$d %2$s for %3$d gold.",
                        store.getSellAmount(),
                        store.getItemName(),
                        store.getSellValue());
            }

            plugin.sendMessage(player, "The chest is empty.");
            return;
        }

        if (!store.chestContentsAreOK()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }

        if (store.getChestItemType() == TradeCraft.GOLD_INGOT) {
            playerWantsToBuy(player, store);
        } else if (store.getChestItemType() == store.getItemType()) {
            playerWantsToSell(player, store);
        } else {
            plugin.sendMessage(player, "You can't sell that here!");
        }
    }


    private void playerWantsToBuy(Player player, TradeCraftStore store) {
        if (!store.playerCanBuy()) {
            plugin.sendMessage(player, "You can't buy that here!");
            return;
        }

        int goldPlayerWantsToSpend = store.getChestItemCount();
        int amountPlayerWantsToBuy = goldPlayerWantsToSpend * store.getBuyAmount() / store.getBuyValue();

        if (amountPlayerWantsToBuy == 0) {
            plugin.sendMessage(player,
                        "You need to spend at least %1$d gold to get any %2$s.",
                        store.getBuyValue(),
                        store.getItemName());
            return;
        }

        if (amountPlayerWantsToBuy > store.getItemsInStore()) {
            plugin.sendMessage(player,
                    "Cannot buy. This store only has %1$d %2$s.",
                    store.getItemsInStore(),
                    store.getItemName());
            return;
        }

        store.updateItemAndGoldAmounts(-amountPlayerWantsToBuy, goldPlayerWantsToSpend);
        store.populateChest(store.getItemType(), amountPlayerWantsToBuy);

        plugin.sendMessage(player,
                    "You bought %1$d %2$s for %3$d gold.",
                    amountPlayerWantsToBuy,
                    store.getItemName(),
                    goldPlayerWantsToSpend);
    }


    private void playerWantsToSell(Player player, TradeCraftStore store) {
        if (!store.playerCanSell()) {
            plugin.sendMessage(player, "You can't sell that here!");
            return;
        }

        int amountPlayerWantsToSell = store.getChestItemCount();
        int goldPlayerShouldReceive = amountPlayerWantsToSell * store.getSellValue() / store.getSellAmount();

        if (goldPlayerShouldReceive == 0) {
            plugin.sendMessage(player,
                        "You need to sell at least %1$d %2$s to get any gold.",
                        store.getSellAmount(),
                        store.getItemName());
            return;
        }

        if (goldPlayerShouldReceive > store.getGoldInStore()) {
            plugin.sendMessage(player,
                    "Cannot sell. This store only has %1$d gold.",
                    store.getGoldInStore());
            return;
        }

        store.updateItemAndGoldAmounts(amountPlayerWantsToSell, -goldPlayerShouldReceive);
        store.populateChest(TradeCraft.GOLD_INGOT, goldPlayerShouldReceive);

        plugin.sendMessage(player,
                    "You sold %1$d %2$s for %3$d gold.",
                    amountPlayerWantsToSell,
                    store.getItemName(),
                    goldPlayerShouldReceive);
    }

}
