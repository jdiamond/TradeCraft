class TradeCraftChestInfo {
    private Chest chest;
    public int id;
    public int total;

    public TradeCraftChestInfo(Chest chest) {
        this.chest = chest;
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

    public void populateChest(int id, int amount) {
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
