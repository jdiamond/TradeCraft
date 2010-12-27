import java.util.logging.Logger;

public class TradeCraft extends Plugin {

    // The plugin name.
    static final String pluginName = "TradeCraft";

    // The plugin version. The first part is the version of hMod this is built against.
    // The second part is the release number built against that version of hMod.
    // A "+" at the end means this is a development version that hasn't been released yet.
    private static final String version = "132.1+";

    // Stuff used to interact with the server.
    final Logger log = Logger.getLogger("Minecraft");
    final Server server = etc.getServer();

    // Objects used by the plugin.
    final TradeCraftConfigurationFile configuration = new TradeCraftConfigurationFile(this);
    final TradeCraftDataFile data = new TradeCraftDataFile(this);
    private final TradeCraftListener listener = new TradeCraftListener(this);

    // Some data value constants.
    static final int CHEST = 54;
    static final int WALL_SIGN = 68;
    static final int GOLD_INGOT = 266;
    static final int MIXED = -1;

    // The maximum number of items that can be stacked in one slot.
    static final int MAX_STACK_SIZE = 64;

    public void enable() {
    }

    public void disable() {
    }

    public void initialize() {
        log.info(pluginName + " " + version + " initialized");

        configuration.load();
        data.load();

        etc.getLoader().addListener(
                PluginLoader.Hook.BLOCK_RIGHTCLICKED,
                listener,
                this,
                PluginListener.Priority.MEDIUM);
    }

    void sendMessage(Player player, String format, Object... args) {
        String message = String.format(format, args);
        player.sendMessage(message);
    }
}
