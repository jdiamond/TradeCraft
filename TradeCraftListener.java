class TradeCraftListener extends PluginListener {

    private final TradeCraft plugin;

    public TradeCraftListener(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public void onBlockRightClicked(Player player, Block blockClicked, Item itemInHand) {
        TradeCraftShop shop = plugin.getShopFromSignBlock(player, blockClicked);

        if (shop == null) {
            return;
        }

        shop.handleRightClick(player);
    }

    public boolean onBlockBreak(Player player, Block block) {
        TradeCraftShop shop = plugin.getShopFromSignOrChestBlock(player, block);

        if (shop == null) {
            return false;
        }

        if (shop.playerCanDestroy(player)) {
            if (!shop.shopCanBeWithdrawnFrom()) {
                return false;
            }
 
            plugin.sendMessage(player, "All items and gold must be withdrawn before you can destroy this sign or chest!");

            return true;
        }

        plugin.sendMessage(player, "You can't destroy this sign or chest!");

        return true;
    }

    public boolean onSignChange(Player player, Sign sign) {

        String ownerName = plugin.getOwnerName(sign);

        if (ownerName == null) {
            String itemName = plugin.getItemName(sign);

            if (itemName == null) {
                return false;
            }

            if (plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToCreateInfiniteShops())) {
                return false;
            }

            plugin.sendMessage(player, "You can't create infinite shops!");

            return true;
        }

        if (!plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToCreatePlayerOwnedShops())) {
            plugin.sendMessage(player, "You can't create player-owned shops!");
            return true;
        }

        if (player.getName().startsWith(ownerName)) {
            plugin.data.setOwnerOfSign(player.getName(), sign);
            return false;
        }

        plugin.sendMessage(player, "You can't create signs with other players names on them!");

        return true;
    }

    public boolean onCommand(Player player, String[] split) {
        if (split[0].toLowerCase().equals("/tradecraft")) {
            if (split.length == 1) {
                displayHelp(player);
                return true;
            } else {
                String command = split[1].toLowerCase();
                if (command.equals("version")) {
                    displayVersion(player);
                    return true;
                } else if (command.equals("items")) {
                    displayItems(player);
                    return true;
                } else if (command.equals("security")) {
                    displaySecurity(player);
                    return true;
                }
            }
        }
        return false;
    }

    private void displayHelp(Player player) {
        plugin.sendMessage(player, "/tradecraft version");
        plugin.sendMessage(player, "  - show the current version number");
        plugin.sendMessage(player, "/tradecraft items");
        plugin.sendMessage(player, "  - show item names that can appear on signs");
        plugin.sendMessage(player, "/tradecraft security");
        plugin.sendMessage(player, "  - show your permissions");
    }

    private void displayVersion(Player player) {
        plugin.sendMessage(player, "TradeCraft version %1$s", TradeCraft.version);
    }

    private void displayItems(Player player) {
        String[] names = plugin.configuration.getNames();
        StringBuilder sb = new StringBuilder(); 
        for (String name : names) {
            if (sb.length() + name.length() > 60) {
                plugin.sendMessage(player, sb.toString());
                sb = new StringBuilder();
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        if (sb.length() > 0) {
            plugin.sendMessage(player, sb.toString());
        }
    }

    private void displaySecurity(Player player) {
        plugin.sendMessage(player,
                "Can create infinite shops: %s",
                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToCreateInfiniteShops()));
        plugin.sendMessage(player,
                "Can create player-owned shops: %s",
                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToCreatePlayerOwnedShops()));
        plugin.sendMessage(player,
                "Can buy from shops: %s",
                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToBuyFromShops()));
        plugin.sendMessage(player,
                "Can sell to shops: %s",
                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToSellToShops()));
    }
}
