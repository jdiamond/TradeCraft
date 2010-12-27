public class TradeCraftInfiniteStore extends TradeCraftStore {
    private final TradeCraftConfigurationInfo configurationInfo;

    public TradeCraftInfiniteStore(TradeCraft plugin, Sign sign, Chest chest) {
        super(plugin, sign, chest);

        String itemName = plugin.getItemName(sign);
        configurationInfo = plugin.configuration.get(itemName);
    }

    public boolean playerCanDestroy(Player player) {
        return true;
    }

    public boolean isOwnedByPlayer(Player player) {
        return false;
    }

    public int getItemType() {
        return configurationInfo.id;
    }

    public String getItemName() {
        return configurationInfo.name;
    }

    public boolean playerCanBuy() {
        return configurationInfo.buyAmount != 0;
    }

    public boolean playerCanSell() {
        return configurationInfo.sellAmount != 0;
    }

    public int getBuyAmount() {
        return configurationInfo.buyAmount;
    }

    public int getBuyValue() {
        return configurationInfo.buyValue;
    }

    public int getSellAmount() {
        return configurationInfo.sellAmount;
    }

    public int getSellValue() {
        return configurationInfo.sellValue;
    }

    public int getItemsInStore() {
        return Integer.MAX_VALUE;
    }

    public int getGoldInStore() {
        return Integer.MAX_VALUE;
    }

    public void depositItems(int amount) {
    }

    public void depositGold(int amount) {
    }

    public int withdrawItems() {
        return 0;
    }

    public int withdrawGold() {
        return 0;
    }

    public void updateItemAndGoldAmounts(int itemAdjustment, int goldAdjustment) {
    }
}
