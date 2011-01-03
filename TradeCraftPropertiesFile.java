public class TradeCraftPropertiesFile {

    private final PropertiesFile propertiesFile;

    public TradeCraftPropertiesFile() {
        propertiesFile = new PropertiesFile(TradeCraft.pluginName + ".properties");
    }

    public boolean getAdminRequiredToCreateInfiniteShops() {
        return propertiesFile.getBoolean("admin-required-to-create-infinite-shops", false);
    }
}
