public class TradeCraftPlayerOwnedStore extends TradeCraftStore {
    private final String ownerName;
    private final String itemName;
    private final int itemType;
    private final TradeCraftExchangeRate buyRate;
    private final TradeCraftExchangeRate sellRate;

    public TradeCraftPlayerOwnedStore(TradeCraft plugin, Sign sign, Chest chest) {
        super(plugin, sign, chest);

        ownerName = plugin.getOwnerName(sign);
        itemName = plugin.getItemName(sign);
        itemType = plugin.configuration.get(itemName).id;
        buyRate = plugin.getExchangeRate(sign, 1);
        sellRate = plugin.getExchangeRate(sign, 2);
    }

    public boolean playerCanDestroy(Player player) {
        return isOwnedByPlayer(player);
    }

    public boolean isOwnedByPlayer(Player player) {
        return ownerName != null && player.getName().equals(ownerName);
    }

    public int getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public boolean playerCanBuy() {
        return buyRate.amount != 0;
    }

    public boolean playerCanSell() {
        return sellRate.amount != 0;
    }

    public int getBuyAmount() {
        return buyRate.amount;
    }

    public int getBuyValue() {
        return buyRate.value;
    }

    public int getSellAmount() {
        return sellRate.amount;
    }

    public int getSellValue() {
        return sellRate.value;
    }

    public int getItemsInStore() {
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        return plugin.data.getItemAmount(x, y, z);
    }

    public int getGoldInStore() {
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        return plugin.data.getGoldAmount(x, y, z);
    }

    public void depositItems(int amount) {
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        plugin.data.depositItems(x, y, z, amount);
    }

    public void depositGold(int amount) {
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        plugin.data.depositGold(x, y, z, amount);
    }

    public int withdrawItems() {
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        return plugin.data.withdrawItems(x, y, z);
    }

    public int withdrawGold() {
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        return plugin.data.withdrawGold(x, y, z);
    }

    public void updateItemAndGoldAmounts(int itemAdjustment, int goldAdjustment) {
        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        plugin.data.updateItemAndGoldAmounts(x, y, z, itemAdjustment, goldAdjustment);
    }
}
