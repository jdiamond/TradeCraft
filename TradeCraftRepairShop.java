import java.util.List;

public class TradeCraftRepairShop extends TradeCraftShop {

    public TradeCraftRepairShop(TradeCraft plugin, Sign sign, Chest chest) {
        super(plugin, sign, chest);
    }

    public void handleRightClick(Player player) {
        int gold = chest.getAmountOfCurrencyInChest();
        List<Item> items = chest.getNonCurrencyItems();
        int repairCost = plugin.properties.getRepairCost();

        if (gold == 0 && items.size() == 0) {
            plugin.sendMessage(player, "It costs %d gold to repair an item.", repairCost);
            return;
        }

        int actualCost = items.size() * repairCost;

        if (items.size() == 0) {
            plugin.sendMessage(player, "With this much gold, you can repair %d items.", gold / repairCost);
            return;
        }

        if (gold < actualCost) {
            if (gold > 0) {
                plugin.sendMessage(player, "That's not enough gold.");
            }
            plugin.sendMessage(player, "You need %d gold to repair all this.", actualCost);
            return;
        }

        chest.clear();

        for (Item item : items) {
            chest.add(item.getType().getId(), 1);
        }

        chest.add(Item.Type.GoldIngot.getId(), (gold - actualCost));

        chest.update();

        plugin.sendMessage(player, "You repaired %d items for %d gold.", items.size(), actualCost);
    }

    public boolean playerCanDestroy(Player player) {
        return true;
    }

    public boolean shopCanBeWithdrawnFrom() {
        return false;
    }
}
