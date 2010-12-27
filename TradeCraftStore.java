public abstract class TradeCraftStore {
    protected final TradeCraft plugin;
    protected final Sign sign;
    private final TradeCraftChestInfo chest;

    public TradeCraftStore(TradeCraft plugin, Sign sign, Chest chest) {
        this.plugin = plugin;
        this.sign = sign;
        this.chest = new TradeCraftChestInfo(chest);
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