import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeCraft extends Plugin {

    // Some constants
    private static final String name = "TradeCraft";
    private static final String version = "1.1";
    private static final String configurationFileName = name + ".txt";

    // Stuff used to interact with the server.
    private final Logger log = Logger.getLogger("Minecraft");
    private final Server server = etc.getServer();
    private final TradeCraftListener listener = new TradeCraftListener();

    // Stores the information read from the configuration file.
    private final Map<String, TradeInfo> tradeInfo = new HashMap<String, TradeInfo>();

    // Patterns used to parse the configuration file.
    private static final Pattern commentPattern = Pattern.compile("^\\s*#.*$");
    private static final Pattern infoPattern = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // name
            "\\s*(\\d+)\\s*," + // id
            "\\s*(\\d+):(\\d+)\\s*$"); // amount:value

    // Some data value constants.
    private static final int CHEST = 54;
    private static final int WALL_SIGN = 68;
    private static final int GOLD_INGOT = 266;
    private static final int MIXED = -1;

    private static final int BLOCK_SIZE = 64;

    // Represents a line parsed from the configuration file.
    static class TradeInfo {
        public String name;
        public int id;
        public int amount;
        public int value;
    }

    // Represents what's inside a chest.
    static class ChestInfo {
        public int id;
        public int total;

        public void addItem(Item item) {
            if (total == 0) {
                id = item.getItemId();
            } else if (id != item.getItemId()) {
                id = MIXED;
            }
            total += item.getAmount();
        }
    }

    public void enable() {
    }

    public void disable() {
    }

    public void initialize() {
        log.info(name + " " + version + " initialized");

        loadConfiguration();

        etc.getLoader().addListener(
                PluginLoader.Hook.BLOCK_CREATED,
                listener,
                this,
                PluginListener.Priority.MEDIUM);
    }

    private void loadConfiguration() {
        try {
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
                info.amount = Integer.parseInt(infoMatcher.group(3));
                info.value = Integer.parseInt(infoMatcher.group(4));
                tradeInfo.put("[" + info.name.toUpperCase() + "]", info);
            }

            configurationFile.close();
        } catch (IOException e) {
            log.warning("Error reading TradeCraft.txt");
        }
    }

    private class TradeCraftListener extends PluginListener {

        public boolean onBlockCreate(
                Player player,
                Block blockPlaced,
                Block blockClicked,
                int itemInHand) {

            int blockClickedType = blockClicked.getType();

            if (blockClickedType != WALL_SIGN) {
                return false;
            }

            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            Sign sign = (Sign) server.getComplexBlock(x, y, z);
            TradeInfo currentTradeInfo = getTradeInfo(sign);

            if (currentTradeInfo == null) {
                return false;
            }

            Block block = (Block) server.getBlockAt(x, y - 1, z);

            if (block.getType() != CHEST) {
                return false;
            }

            Chest chest = (Chest) server.getComplexBlock(x, y - 1, z);
            ChestInfo chestInfo = getChestInfo(chest);

            if (chestInfo.total == 0) {
                player.sendMessage(
                        "Exchange rate for " + currentTradeInfo.name +
                        " is " + currentTradeInfo.amount + " items" +
                        " for " + currentTradeInfo.value + " gold.");
                player.sendMessage("The chest is empty.");
                return false;
            }

            if (chestInfo.id == MIXED) {
                player.sendMessage("The chest has more than one type of item in it!");
                return false;
            }

            if (chestInfo.id == GOLD_INGOT) {
                makePurchase(player, currentTradeInfo, chestInfo, chest);
            } else if (chestInfo.id == currentTradeInfo.id) {
                makeSale(player, currentTradeInfo, chestInfo, chest);
            } else {
                player.sendMessage("You can't sell that here!");
            }

            return false;
        }

        private TradeInfo getTradeInfo(Sign sign) {
            for (int i = 0; i < 4; i++) {
                String signText = sign.getText(i);
                TradeInfo currentTradeInfo = tradeInfo.get(signText.toUpperCase());

                if (currentTradeInfo != null) {
                    return currentTradeInfo;
                }
            }

            return null;
        }

        private ChestInfo getChestInfo(Chest chest) {
            ChestInfo info = new ChestInfo();

            for (hl realItem : chest.getArray()) {
                if (realItem != null) {
                    Item item = new Item(realItem);
                    info.addItem(item);
                }
            }

            return info;
        }

        private void makePurchase(
                Player player,
                TradeInfo currentTradeInfo,
                ChestInfo chestInfo,
                Chest chest) {

            int totalItems = chestInfo.total / currentTradeInfo.value * currentTradeInfo.amount;

            if (totalItems == 0) {
                player.sendMessage(
                        "You need to spend at least " +
                        currentTradeInfo.value +
                        " gold to get any items.");
                return;
            }

            player.sendMessage(
                    "You bought " + totalItems + " " +
                    currentTradeInfo.name +
                    " for " + chestInfo.total +
                    " gold.");

            populateChest(chest, currentTradeInfo.id, totalItems);
        }

        private void makeSale(
                Player player,
                TradeInfo currentTradeInfo,
                ChestInfo chestInfo,
                Chest chest) {

            int totalValue = chestInfo.total / currentTradeInfo.amount * currentTradeInfo.value;

            if (totalValue == 0) {
                player.sendMessage(
                        "You need to sell at least " +
                        currentTradeInfo.amount +
                        " to get any gold.");
                return;
            }

            player.sendMessage(
                    "You sold " + chestInfo.total + " " +
                    currentTradeInfo.name +
                    " for " + totalValue +
                    " gold.");

            populateChest(chest, GOLD_INGOT, totalValue);
        }

        private void populateChest(Chest chest, int id, int amount) {
            chest.clearContents();

            int blocks = amount / BLOCK_SIZE;

            for (int i = 0; i < blocks; i++) {
                chest.addItem(new Item(id, BLOCK_SIZE));
            }

            int remainder = amount % BLOCK_SIZE;

            if (remainder > 0) {
                chest.addItem(new Item(id, remainder));
            }

            chest.update();
        }
    }
}
