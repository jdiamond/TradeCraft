public abstract class TradeCraftStore {
    protected final TradeCraft plugin;
    protected final Sign sign;
    private final TradeCraftChestInfo chest;

    public TradeCraftStore(TradeCraft plugin, Sign sign, Chest chest) {
        this.plugin = plugin;
        this.sign = sign;
        this.chest = new TradeCraftChestInfo(chest);
    }

    public void handleRightClick(Player player) {
        if (isOwnedByPlayer(player)) {
            handleOwnerClick(player);
        } else {
            handlePatronClick(player);
        }
    }

    private void handleOwnerClick(Player player) {
        if (!chestContentsAreOK()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }

        if (getChestItemCount() == 0) {
            int goldAmount = withdrawGold();
            if (goldAmount > 0) {
                populateChest(Item.Type.GoldIngot.getId(), goldAmount);
                plugin.sendMessage(player, "Withdrew %1$d gold.", goldAmount);
            } else {
                int itemAmount = withdrawItems();
                if (itemAmount > 0) {
                    populateChest(getItemType(), itemAmount);
                    plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, getItemName());
                } else {
                    plugin.sendMessage(player, "There is nothing to withdraw.");
                }
            }
        } else if (getChestItemType() == Item.Type.GoldIngot.getId()) {
            depositGold(getChestItemCount());
            plugin.sendMessage(player, "Deposited %1$d gold.", getChestItemCount());
            populateChest(0, 0);
            int itemAmount = withdrawItems();
            if (itemAmount > 0) {
                populateChest(getItemType(), itemAmount);
                plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, getItemName());
            }
        } else if (getChestItemType() == getItemType()) {
            depositItems(getChestItemCount());
            populateChest(0, 0);
            plugin.sendMessage(player, "Deposited %1$d %2$s.", getChestItemCount(), getItemName());
        } else {
            plugin.sendMessage(player, "You can't deposit that here!");
        }
    }

    private void handlePatronClick(Player player) {
        if (getChestItemCount() == 0) {
            if (playerCanBuy()) {
                plugin.sendMessage(player,
                        "You can buy %1$d %2$s for %3$d gold.",
                        getBuyAmount(),
                        getItemName(),
                        getBuyValue());
            }

            if (playerCanSell()) {
                plugin.sendMessage(player,
                        "You can sell %1$d %2$s for %3$d gold.",
                        getSellAmount(),
                        getItemName(),
                        getSellValue());
            }

            plugin.sendMessage(player, "The chest is empty.");
            return;
        }

        if (!chestContentsAreOK()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }

        if (getChestItemType() == Item.Type.GoldIngot.getId()) {
            playerWantsToBuy(player);
        } else if (getChestItemType() == getItemType()) {
            playerWantsToSell(player);
        } else {
            plugin.sendMessage(player, "You can't sell that here!");
        }
    }

    private void playerWantsToBuy(Player player) {
        if (!playerCanBuy()) {
            plugin.sendMessage(player, "You can't buy that here!");
            return;
        }

        int goldPlayerWantsToSpend = getChestItemCount();
        int amountPlayerWantsToBuy = goldPlayerWantsToSpend * getBuyAmount() / getBuyValue();

        if (amountPlayerWantsToBuy == 0) {
            plugin.sendMessage(player,
                        "You need to spend at least %1$d gold to get any %2$s.",
                        getBuyValue(),
                        getItemName());
            return;
        }

        if (amountPlayerWantsToBuy > getItemsInStore()) {
            plugin.sendMessage(player,
                    "Cannot buy. This store only has %1$d %2$s.",
                    getItemsInStore(),
                    getItemName());
            return;
        }

        updateItemAndGoldAmounts(-amountPlayerWantsToBuy, goldPlayerWantsToSpend);
        populateChest(getItemType(), amountPlayerWantsToBuy);

        plugin.sendMessage(player,
                    "You bought %1$d %2$s for %3$d gold.",
                    amountPlayerWantsToBuy,
                    getItemName(),
                    goldPlayerWantsToSpend);
    }

    private void playerWantsToSell(Player player) {
        if (!playerCanSell()) {
            plugin.sendMessage(player, "You can't sell that here!");
            return;
        }

        int amountPlayerWantsToSell = getChestItemCount();
        int goldPlayerShouldReceive = amountPlayerWantsToSell * getSellValue() / getSellAmount();

        if (goldPlayerShouldReceive == 0) {
            plugin.sendMessage(player,
                        "You need to sell at least %1$d %2$s to get any gold.",
                        getSellAmount(),
                        getItemName());
            return;
        }

        if (goldPlayerShouldReceive > getGoldInStore()) {
            plugin.sendMessage(player,
                    "Cannot sell. This store only has %1$d gold.",
                    getGoldInStore());
            return;
        }

        updateItemAndGoldAmounts(amountPlayerWantsToSell, -goldPlayerShouldReceive);
        populateChest(Item.Type.GoldIngot.getId(), goldPlayerShouldReceive);

        plugin.sendMessage(player,
                    "You sold %1$d %2$s for %3$d gold.",
                    amountPlayerWantsToSell,
                    getItemName(),
                    goldPlayerShouldReceive);
    }

    public int getChestItemType() {
        return chest.id;
    }

    public int getChestItemCount() {
        return chest.total;
    }

    public boolean chestContentsAreOK() {
        return chest.containsOnlyOneItemType();
    }

    public void populateChest(int id, int amount) {
        chest.populateChest(id, amount);
    }

    public abstract boolean playerCanDestroy(Player player);

    public abstract boolean storeCanBeWithdrawnFrom();

    public abstract boolean isOwnedByPlayer(Player player);

    public abstract int getItemType();

    public abstract String getItemName();

    public abstract boolean playerCanBuy();

    public abstract boolean playerCanSell();

    public abstract int getBuyAmount();

    public abstract int getBuyValue();

    public abstract int getSellAmount();

    public abstract int getSellValue();

    public abstract int getItemsInStore();

    public abstract int getGoldInStore();

    public abstract void depositItems(int amount);

    public abstract void depositGold(int amount);

    public abstract int withdrawItems();

    public abstract int withdrawGold();

    public abstract void updateItemAndGoldAmounts(int itemAdjustment, int goldAdjustment);
}