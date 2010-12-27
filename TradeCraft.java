import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern ratePattern = Pattern.compile("\\s*(\\d+)\\s*:\\s*(\\d+)\\s*");

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

        etc.getLoader().addListener(
                PluginLoader.Hook.BLOCK_BROKEN,
                listener,
                this,
                PluginListener.Priority.MEDIUM);

        etc.getLoader().addListener(
                PluginLoader.Hook.SIGN_CHANGE,
                listener,
                this,
                PluginListener.Priority.MEDIUM);
    }

    void sendMessage(Player player, String format, Object... args) {
        String message = String.format(format, args);
        player.sendMessage(message);
    }

    TradeCraftStore getStoreFromSignOrChestBlock(Block block) {
        if (block.getType() == TradeCraft.CHEST) {
            block = server.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
        }

        return getStoreFromSignBlock(block);
    }

    TradeCraftStore getStoreFromSignBlock(Block block) {
        if (block.getType() != TradeCraft.WALL_SIGN) {
            return null;
        }

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        Sign sign = (Sign)server.getComplexBlock(x, y, z);

        // The sign at this location can be null if it was just destroyed.
        if (sign == null) {
            return null;
        }

        String itemName = getItemName(sign);

        if (itemName == null) {
            return null;
        }

        if (!configuration.isConfigured(itemName)) {
            return null;
        }

        Block blockBelowSign = server.getBlockAt(x, y - 1, z);

        if (blockBelowSign.getType() != TradeCraft.CHEST) {
            return null;
        }

        Chest chest = (Chest)server.getComplexBlock(x, y - 1, z);

        TradeCraftStore store;

        if (getOwnerName(sign) == null) {
            store = new TradeCraftInfiniteStore(this, sign, chest);
        } else {
            store = new TradeCraftPlayerOwnedStore(this, sign, chest);
        }

        return store;
    }

    String getItemName(Sign sign) {
        return getSpecialText(sign, "[", "]");
    }

    String getOwnerName(Sign sign) {
        return getSpecialText(sign, "-", "-");
    }

    private String getSpecialText(Sign sign, String prefix, String suffix) {
        for (int i = 0; i < 4; i++) {
            String signText = sign.getText(i);

            if (signText.startsWith(prefix) &&
                signText.endsWith(suffix) &&
                signText.length() > 2) {
                return signText.substring(1, signText.length() - 1);
            }
        }

        return null;
    }

    TradeCraftExchangeRate getExchangeRate(Sign sign, int lineNumber) {
        TradeCraftExchangeRate rate = new TradeCraftExchangeRate();

        String signText = sign.getText(lineNumber);

        Matcher matcher = ratePattern.matcher(signText);

        if (matcher.find()) {
            rate.amount = Integer.parseInt(matcher.group(1));
            rate.value = Integer.parseInt(matcher.group(2));
        }

        return rate;
    }
}
