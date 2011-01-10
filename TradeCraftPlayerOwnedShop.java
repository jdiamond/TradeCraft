public class TradeCraftPlayerOwnedShop extends TradeCraftItemShop {
    private final String ownerName;
    private final String itemName;
    private final int itemType;
    private final TradeCraftExchangeRate buyRate;
    private final TradeCraftExchangeRate sellRate;

    public TradeCraftPlayerOwnedShop(TradeCraft plugin, Sign sign, Chest chest) {
        super(plugin, sign, chest);

        ownerName = plugin.data.getOwnerOfSign(sign);
        itemName = plugin.getItemName(sign);
        itemType = plugin.configuration.get(itemName).id;
        buyRate = plugin.getExchangeRate(sign, 1);
        sellRate = plugin.getExchangeRate(sign, 2);
    }

    public boolean playerCanDestroy(Player player) {
        return isOwnedByPlayer(player);
    }

    public boolean shopCanBeWithdrawnFrom() {
        return getItemsInShop() > 0 || getGoldInShop() > 0;
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

    public int getItemsInShop() {
        return plugin.data.getItemAmount(sign);
    }

    public int getGoldInShop() {
        return plugin.data.getGoldAmount(sign);
    }

    public void depositItems(int amount) {
        plugin.data.depositItems(ownerName, sign, itemType, amount);
    }

    public void depositGold(int amount) {
        plugin.data.depositGold(ownerName, sign, amount);
    }

    public int withdrawItems() {
        return plugin.data.withdrawItems(sign);
    }

    public int withdrawGold() {
        return plugin.data.withdrawGold(sign);
    }

    public void updateItemAndGoldAmounts(int itemAdjustment, int goldAdjustment) {
        plugin.data.updateItemAndGoldAmounts(sign, itemAdjustment, goldAdjustment);
    }
}
