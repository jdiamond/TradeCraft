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
}
