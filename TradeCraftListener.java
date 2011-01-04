class TradeCraftListener extends PluginListener {

    private final TradeCraft plugin;

    public TradeCraftListener(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public void onBlockRightClicked(Player player, Block blockClicked, Item itemInHand) {
        TradeCraftShop shop = plugin.getShopFromSignBlock(blockClicked);

        if (shop == null) {
            return;
        }

        shop.handleRightClick(player);
    }

    public boolean onBlockBreak(Player player, Block block) {
        TradeCraftShop shop = plugin.getShopFromSignOrChestBlock(block);

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

            String group = plugin.properties.getGroupRequiredToCreateInfiniteShops();

            if (group.equals("*")) {
                return false;
            }

            if (player.isInGroup(group)) {
                return false;
            }

            plugin.log.info("Player \"" + player.getName() + "\" is not in group \"" + group + "\"");
            plugin.sendMessage(player, "You can't create infinite shops!");

            return true;
        }

        String group = plugin.properties.getGroupRequiredToCreatePlayerOwnedShops();

        if (!group.equals("*")) {
            if (!player.isInGroup(group)) {
                plugin.log.info("Player \"" + player.getName() + "\" is not in group \"" + group + "\"");
                plugin.sendMessage(player, "You can't create player-owned shops!");
                return true;
            }
        }

        if (player.getName().startsWith(ownerName)) {
            plugin.data.setOwnerOfSign(player.getName(), sign);
            return false;
        }

        plugin.sendMessage(player, "You can't create signs with other players names on them!");

        return true;
    }
}
