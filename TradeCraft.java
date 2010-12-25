import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeCraft extends Plugin {

    // The plugin name.
    private static final String name = "TradeCraft";

    // The plugin version. The first part is the version of hMod this is built against.
    // The second part is the release number built against that version of hMod.
    // A "+" at the end means this is a development version that hasn't been released yet.
    private static final String version = "132.1";

    // Stuff used to interact with the server.
    private final Logger log = Logger.getLogger("Minecraft");
    private final Server server = etc.getServer();

    // Objects used by the plugin.
    private final TradeCraftConfigurationFile configuration = new TradeCraftConfigurationFile();
    private final TradeCraftListener listener = new TradeCraftListener();

    // Some data value constants.
    private static final int CHEST = 54;
    private static final int WALL_SIGN = 68;
    private static final int GOLD_INGOT = 266;
    private static final int MIXED = -1;

    // The maximum number of items that can be stacked in one slot.
    private static final int MAX_STACK_SIZE = 64;

    public void enable() {
    }

    public void disable() {
    }

    public void initialize() {
        log.info(name + " " + version + " initialized");

        configuration.load();

        etc.getLoader().addListener(
                PluginLoader.Hook.BLOCK_RIGHTCLICKED,
                listener,
                this,
                PluginListener.Priority.MEDIUM);
    }

    private void sendMessage(Player player, String format, Object... args) {
        String message = String.format(format, args);
        player.sendMessage(message);
    }

    // Represents the TradeCraft.txt configuration file.
    private class TradeCraftConfigurationFile {
        private static final String configurationFileName = name + ".txt";
        private final Map<String, TradeInfo> tradeInfo = new HashMap<String, TradeInfo>();

        // Patterns used to parse the configuration file.
        private final Pattern commentPattern = Pattern.compile("^\\s*#.*$");
        private final Pattern infoPattern = Pattern.compile(
                "^\\s*([^,]+)\\s*," + // name
                "\\s*(\\d+)\\s*," + // id
                "\\s*(\\d+)\\s*:\\s*(\\d+)\\s*" + // buyAmount:buyValue
                "(?:,\\s*(\\d+)\\s*:\\s*(\\d+))?\\s*$"); // sellAmount:sellValue

        private void load() {
            try {
                tradeInfo.clear();

                BufferedReader configurationFile = new BufferedReader(new FileReader(configurationFileName));

                int lineNumber = 0;
                String line;

                while ((line = configurationFile.readLine()) != null) {
                    lineNumber += 1;

                    if (line.trim().equals("")) {
                        continue;
                    }

                    Matcher commentMatcher = commentPattern.matcher(line);

                    if (commentMatcher.matches()) {
                        continue;
                    }

                    Matcher infoMatcher = infoPattern.matcher(line);

                    if (!infoMatcher.matches()) {
                        log.warning(
                                "Failed to parse line number " + lineNumber +
                                " in " + configurationFileName +
                                ": " + line);
                        continue;
                    }

                    TradeInfo info = new TradeInfo();
                    info.name = infoMatcher.group(1);
                    info.id = Integer.parseInt(infoMatcher.group(2));
                    info.sellAmount = info.buyAmount = Integer.parseInt(infoMatcher.group(3));
                    info.sellValue = info.buyValue = Integer.parseInt(infoMatcher.group(4));
                    
                    if (infoMatcher.group(5) != null) {
                    	info.sellAmount = Integer.parseInt(infoMatcher.group(5));
                    	info.sellValue = Integer.parseInt(infoMatcher.group(6));
                    }
                    
                    tradeInfo.put("[" + info.name.toUpperCase() + "]", info);
                }

                configurationFile.close();
            } catch (IOException e) {
                log.warning("Error reading " + configurationFileName);
            }
        }

        public TradeInfo get(String name) {
            return tradeInfo.get(name.toUpperCase());
        }
    }

    // Represents a line parsed from the configuration file.
    static class TradeInfo {
        public String name;
        public int id;
        public int buyAmount;
        public int buyValue;
        public int sellAmount;
        public int sellValue;
    }

    // This is where most of the work is done.
    private class TradeCraftListener extends PluginListener {

        public void onBlockRightClicked(
                Player player,
                Block blockClicked,
                Item itemInHand) {

            if (blockClicked.getType() != WALL_SIGN) {
                return;
            }

            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            Sign sign = (Sign)server.getComplexBlock(x, y, z);
            TradeInfo tradeInfo = getTradeInfo(sign);

            if (tradeInfo == null) {
                return;
            }

            Block block = (Block)server.getBlockAt(x, y - 1, z);

            if (block.getType() != CHEST) {
                return;
            }

            Chest chest = (Chest)server.getComplexBlock(x, y - 1, z);
            ChestInfo chestInfo = new ChestInfo(chest);

            if (chestInfo.total == 0) {
            	if (tradeInfo.buyAmount != 0) {
	                sendMessage(player,
	                            "You can buy %1$d %2$s items for %3$d gold.",
	                            tradeInfo.buyAmount,
	                            tradeInfo.name,
	                            tradeInfo.buyValue);
            	}
                
                if (tradeInfo.sellAmount != 0) {
	                sendMessage(player,
	                            "You can sell %1$d %2$s items for %3$d gold.",
	                            tradeInfo.sellAmount,
	                            tradeInfo.name,
	                            tradeInfo.sellValue);
                }

                sendMessage(player, "The chest is empty.");
                return;
            }

            if (!chestInfo.containsOnlyOneItemType()) {
                sendMessage(player, "The chest has more than one type of item in it!");
                return;
            }

            if (chestInfo.id == GOLD_INGOT) {
                makePurchase(player, tradeInfo, chestInfo, chest);
            } else if (chestInfo.id == tradeInfo.id) {
                makeSale(player, tradeInfo, chestInfo, chest);
            } else {
                sendMessage(player, "You can't sell that here!");
            }

            return;
        }

        private TradeInfo getTradeInfo(Sign sign) {
            for (int i = 0; i < 4; i++) {
                String signText = sign.getText(i);
                TradeInfo currentTradeInfo = configuration.get(signText);

                if (currentTradeInfo != null) {
                    return currentTradeInfo;
                }
            }

            return null;
        }

        private void makePurchase(
                Player player,
                TradeInfo tradeInfo,
                ChestInfo chestInfo,
                Chest chest) {

        	if (tradeInfo.buyAmount == 0) {
                sendMessage(player, "You can't buy that here!");
                return;
        	}
        	
            int totalItems = chestInfo.total / tradeInfo.buyValue * tradeInfo.buyAmount;

            if (totalItems == 0) {
                sendMessage(player,
                            "You need to spend at least %1$d gold to get any items.",
                            tradeInfo.buyValue);
                return;
            }

            sendMessage(player,
                        "You bought %1$d %2$s for %3$d gold.",
                        totalItems,
                        tradeInfo.name,
                        chestInfo.total);

            populateChest(chest, tradeInfo.id, totalItems);
        }

        private void makeSale(
                Player player,
                TradeInfo tradeInfo,
                ChestInfo chestInfo,
                Chest chest) {

        	if (tradeInfo.sellAmount == 0) {
                sendMessage(player, "You can't sell that here!");
                return;
        	}

        	int totalValue = chestInfo.total / tradeInfo.sellAmount * tradeInfo.sellValue;

            if (totalValue == 0) {
                sendMessage(player,
                            "You need to sell at least %1$d %2$s to get any gold.",
                            tradeInfo.sellAmount,
                            tradeInfo.name);
                return;
            }

            sendMessage(player,
                        "You sold %1$d %2$s for %3$d gold.",
                        chestInfo.total,
                        tradeInfo.name,
                        totalValue);

            populateChest(chest, GOLD_INGOT, totalValue);
        }

        private void populateChest(Chest chest, int id, int amount) {
            chest.clearContents();

            int blocks = amount / MAX_STACK_SIZE;

            for (int i = 0; i < blocks; i++) {
                chest.addItem(new Item(id, MAX_STACK_SIZE));
            }

            int remainder = amount % MAX_STACK_SIZE;

            if (remainder > 0) {
                chest.addItem(new Item(id, remainder));
            }

            chest.update();
        }
    }

    // Represents what's inside a chest.
    static class ChestInfo {
        public int id;
        public int total;

        public ChestInfo(Chest chest) {
            for (Item item : chest.getContents()) {
                addItem(item);
            }
        }

        private void addItem(Item item) {
            if (item == null) {
                return;
            }
            if (total == 0) {
                id = item.getItemId();
            } else if (id != item.getItemId()) {
                id = MIXED;
            }
            total += item.getAmount();
        }

        public boolean containsOnlyOneItemType() {
            return id != MIXED;
        }
    }
}
