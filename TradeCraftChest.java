class TradeCraftChest {
    private Chest chest;
    public int id;
    public int total;

    public TradeCraftChest(Chest chest) {
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
            id = -1;
        }
        total += item.getAmount();
    }

    public boolean containsOnlyOneItemType() {
        return id != -1;
    }

    public void populateChest(int id, int amount) {
        chest.clearContents();

        int maxStackSize = TradeCraft.getMaxStackSize(id);
        int blocks = amount / maxStackSize;

        for (int i = 0; i < blocks; i++) {
            chest.addItem(new Item(id, maxStackSize));
        }

        int remainder = amount % maxStackSize;

        if (remainder > 0) {
            chest.addItem(new Item(id, remainder));
        }

        chest.update();
    }
}
