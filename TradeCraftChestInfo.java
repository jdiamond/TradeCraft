class TradeCraftChestInfo {
    public int id;
    public int total;

    public TradeCraftChestInfo(Chest chest) {
        for (Item item : chest.getContents()) {
            if (item != null) {
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
