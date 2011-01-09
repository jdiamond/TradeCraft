public class TradeCraftPropertiesFile {

    private final PropertiesFile propertiesFile;

    public TradeCraftPropertiesFile() {
        propertiesFile = new PropertiesFile(TradeCraft.pluginName + ".properties");
    }

    public boolean getInfiniteShopsEnabled() {
        return propertiesFile.getBoolean("infinite-shops-enabled", true);
    }

    public boolean getPlayerOwnedShopsEnabled() {
        return propertiesFile.getBoolean("player-owned-shops-enabled", true);
    }

    public String getGroupRequiredToCreateInfiniteShops() {
        return propertiesFile.getString("group-required-to-create-infinite-shops", "*");
    }

    public String getGroupRequiredToCreatePlayerOwnedShops() {
        return propertiesFile.getString("group-required-to-create-player-owned-shops", "*");
    }

    public String getGroupRequiredToBuyFromShops() {
        return propertiesFile.getString("group-required-to-buy-from-shops", "*");
    }

    public String getGroupRequiredToSellToShops() {
        return propertiesFile.getString("group-required-to-sell-to-shops", "*");
    }

    public boolean getEnableDebugMessages() {
        return propertiesFile.getBoolean("enable-debug-messages", false);
    }
}
