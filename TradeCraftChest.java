import java.util.ArrayList;
import java.util.List;

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

    public void clear() {
        chest.clearContents();
    }

    public void add(int id, int amount) {
        int maxStackSize = TradeCraft.getMaxStackSize(id);
        int blocks = amount / maxStackSize;

        for (int i = 0; i < blocks; i++) {
            chest.addItem(new Item(id, maxStackSize));
        }

        int remainder = amount % maxStackSize;

        if (remainder > 0) {
            chest.addItem(new Item(id, remainder));
        }
    }

    public void update() {
    }

    public void populateChest(int id, int amount) {
        clear();
        add(id, amount);
        update();
    }

    public int getAmountOfCurrencyInChest() {
        int amount = 0;
        for (Item item : chest.getContents()) {
            if (item != null) {
                if (item.getItemId() == Item.Type.GoldIngot.getId()) {
                    amount += item.getAmount();
                }
            }
        }
        return amount;
    }

    public List<Item> getNonCurrencyItems() {
        List<Item> items = new ArrayList<Item>();
        for (Item item : chest.getContents()) {
            if (item != null) {
                if (item.getType() != Item.Type.GoldIngot) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}
