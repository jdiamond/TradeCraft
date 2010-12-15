class TradeCraftChestInfo {
    public int id;
    public int total;

    public TradeCraftChestInfo(Chest chest) {
        for (hn realItem : chest.getArray()) {
            if (realItem != null) {
                Item item = new Item(realItem);
                addItem(item);
            }
        }
    }

    private void addItem(Item item) {
        if (total == 0) {
            id = item.getItemId();
        } else if (id != item.getItemId()) {
            id = TradeCraft.MIXED;
        }
        total += item.getAmount();
    }

    public boolean containsOnlyOneItemType() {
        return id != TradeCraft.MIXED;
    }
}