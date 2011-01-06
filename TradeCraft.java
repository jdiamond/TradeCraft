import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeCraft extends Plugin {

    // The plugin name.
    static final String pluginName = "TradeCraft";

    // The plugin version. The first part is the version of hMod this is built against.
    // The second part is the release number built against that version of hMod.
    // A "+" at the end means this is a development version that hasn't been released yet.
    static final String version = "133.1+";

    private static final Pattern ratePattern = Pattern.compile("\\s*(\\d+)\\s*:\\s*(\\d+)\\s*");

    // Stuff used to interact with the server.
    final Logger log = Logger.getLogger("Minecraft");
    final Server server = etc.getServer();

    // Objects used by the plugin.
    final TradeCraftPropertiesFile properties = new TradeCraftPropertiesFile();
    final TradeCraftConfigurationFile configuration = new TradeCraftConfigurationFile(this);
    final TradeCraftDataFile data = new TradeCraftDataFile(this);

    private final TradeCraftListener listener = new TradeCraftListener(this);

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

        etc.getLoader().addListener(
                PluginLoader.Hook.COMMAND,
                listener,
                this,
                PluginListener.Priority.LOW);
    }

    void sendMessage(Player player, String format, Object... args) {
        String message = String.format(format, args);
        player.sendMessage(message);
    }

    public boolean playerIsInGroup(Player player, String group) {
        if (group.equals("*")) {
            return true;
        }
        return player.isInGroup(group);
    }

    TradeCraftShop getShopFromSignOrChestBlock(Block block) {
        if (block.getType() == Block.Type.Chest.getType()) {
            block = server.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
        }

        return getShopFromSignBlock(block);
    }

    TradeCraftShop getShopFromSignBlock(Block block) {
        if (block.getType() != Block.Type.WallSign.getType()) {
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

        if (blockBelowSign.getType() != Block.Type.Chest.getType()) {
            return null;
        }

        Chest chest = (Chest)server.getComplexBlock(x, y - 1, z);

        TradeCraftShop shop;

        if (getOwnerName(sign) == null) {
            if (!properties.getInfiniteShopsEnabled()) {
                return null;
            }
            shop = new TradeCraftInfiniteShop(this, sign, chest);
        } else {
            if (!properties.getPlayerOwnedShopsEnabled()) {
                return null;
            }
            shop = new TradeCraftPlayerOwnedShop(this, sign, chest);
        }

        return shop;
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

    static int getMaxStackSize(int itemType) {
        switch (Item.Type.fromId(itemType)) {
        case Apple:
        case GoldenApple:
        case Pork:
        case GrilledPork:
        case Bread:
        case Bucket:
        case WaterBucket:
        case LavaBucket:
        case MilkBucket:
        case WoodSword:
        case WoodSpade:
        case WoodPickaxe:
        case WoodAxe:
        case WoodHoe:
        case StoneSword:
        case StoneSpade:
        case StonePickaxe:
        case StoneAxe:
        case StoneHoe:
        case IronSword:
        case IronSpade:
        case IronPickaxe:
        case IronAxe:
        case IronHoe:
        case DiamondSword:
        case DiamondSpade:
        case DiamondPickaxe:
        case DiamondAxe:
        case DiamondHoe:
        case GoldSword:
        case GoldSpade:
        case GoldPickaxe:
        case GoldAxe:
        case GoldHoe:
        case LeatherHelmet:
        case LeatherChestplate:
        case LeatherLeggings:
        case LeatherBoots:
        case IronHelmet:
        case IronChestplate:
        case IronLeggings:
        case IronBoots:
        case DiamondHelmet:
        case DiamondChestplate:
        case DiamondLeggings:
        case DiamondBoots:
        case GoldHelmet:
        case GoldChestplate:
        case GoldLeggings:
        case GoldBoots:
           return 1;
        case SnowBall:
            return 16;
        }
        return 64;
    }
}
