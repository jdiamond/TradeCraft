import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TradeCraftDataFile {

	private static final String fileName = TradeCraft.pluginName + ".data";
    private static final Pattern infoPattern1 = Pattern.compile(
            "^\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)" + // x,y,z
            "\\s*=\\s*" +
            "(\\d+)\\s*,\\s*(\\d+)\\s*$"); // itemAmount,goldAmount
    private static final Pattern infoPattern2 = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // ownerName
            "\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*," + // x,y,z
            "\\s*(\\d+)\\s*," + // itemType
            "\\s*(\\d+)\\s*," + // itemAmount
            "\\s*(\\d+)\\s*$"); // goldAmount

    private final TradeCraft plugin;
    private final Map<String, TradeCraftDataInfo> data = new HashMap<String, TradeCraftDataInfo>();

    TradeCraftDataFile(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public synchronized void load() {
        if (!new File(fileName).exists()) {
            plugin.log.info("No " + fileName + " file to read");
            return;
        }

        try {
            data.clear();

            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber += 1;

                Matcher infoMatcher2 = infoPattern2.matcher(line);

                if (infoMatcher2.matches()) {
                    String ownerName = infoMatcher2.group(1);
                    int x = Integer.parseInt(infoMatcher2.group(2));
                    int y = Integer.parseInt(infoMatcher2.group(3));
                    int z = Integer.parseInt(infoMatcher2.group(4));
                    int itemType = Integer.parseInt(infoMatcher2.group(5));
                    int itemAmount = Integer.parseInt(infoMatcher2.group(6));
                    int goldAmount = Integer.parseInt(infoMatcher2.group(7));

                    String key = getKey(x, y, z);

                    TradeCraftDataInfo info = new TradeCraftDataInfo();
                    info.ownerName = ownerName;
                    info.itemType = itemType;
                    info.itemAmount = itemAmount;
                    info.goldAmount = goldAmount;

                    data.put(key, info);
                } else {
                    Matcher infoMatcher1 = infoPattern1.matcher(line);

                    if (!infoMatcher1.matches()) {
                        plugin.log.warning(
                                "Failed to parse line number " + lineNumber +
                                " in " + fileName +
                                ": " + line);
                        continue;
                    }

                    int x = Integer.parseInt(infoMatcher1.group(1));
                    int y = Integer.parseInt(infoMatcher1.group(2));
                    int z = Integer.parseInt(infoMatcher1.group(3));
                    int itemAmount = Integer.parseInt(infoMatcher1.group(4));
                    int goldAmount = Integer.parseInt(infoMatcher1.group(5));

                    String key = getKey(x, y, z);

                    TradeCraftDataInfo info = new TradeCraftDataInfo();
                    info.ownerName = "unknown";
                    info.itemAmount = itemAmount;
                    info.goldAmount = goldAmount;

                    data.put(key, info);
                }
            }

            plugin.log.info("Loaded " + data.size() + " shops");
            reader.close();
        } catch (IOException e) {
            plugin.log.warning("Error reading " + fileName);
        }
    }

    public synchronized void save() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            for (String key : data.keySet()) {
                TradeCraftDataInfo info = data.get(key);
                writer.write(info.ownerName + "," +
                             key + "," +
                             info.itemType + "," +
                             info.itemAmount + "," +
                             info.goldAmount);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            plugin.log.warning("Error writing " + fileName);
        }
    }

    public synchronized void setOwnerOfSign(String ownerName, Sign sign) {
        depositGold(ownerName, sign, 0);
    }

    public synchronized String getOwnerOfSign(Sign sign) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            return info.ownerName;
        }
        return null;
    }

    public synchronized int getItemAmount(Sign sign) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            return info.itemAmount;
        }
        return 0;
    }

    public synchronized int getGoldAmount(Sign sign) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            return info.goldAmount;
        }
        return 0;
    }

    public synchronized void depositItems(String ownerName, Sign sign, int itemType, int itemAmount) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            info.ownerName = ownerName; // For old entries that don't have the name.
            info.itemType = itemType; // For old entries that don't have the type.
            info.itemAmount += itemAmount;
        } else {
            TradeCraftDataInfo info = new TradeCraftDataInfo();
            info.ownerName = ownerName;
            info.itemType = itemType;
            info.itemAmount = itemAmount;
            data.put(key, info);
        }
        save();
    }

    public synchronized void depositGold(String ownerName, Sign sign, int goldAmount) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            info.ownerName = ownerName; // For old entries that don't have the name.
            info.goldAmount += goldAmount;
        } else {
            TradeCraftDataInfo info = new TradeCraftDataInfo();
            info.ownerName = ownerName;
            info.goldAmount = goldAmount;
            data.put(key, info);
        }
        save();
    }

    public synchronized int withdrawItems(Sign sign) {
        String key = getKeyFromSign(sign);
        if (!data.containsKey(key)) {
            return 0;
        }
        TradeCraftDataInfo info = data.get(key);
        int itemAmount = info.itemAmount;
        if (itemAmount != 0) {
            info.itemAmount = 0;
            save();
        }
        return itemAmount;
    }

    public synchronized int withdrawGold(Sign sign) {
        String key = getKeyFromSign(sign);
        if (!data.containsKey(key)) {
            return 0;
        }
        TradeCraftDataInfo info = data.get(key);
        int goldAmount = info.goldAmount;
        if (goldAmount != 0) {
            info.goldAmount = 0;
            save();
        }
        return goldAmount;
    }

    public synchronized void updateItemAndGoldAmounts(Sign sign, int itemAdjustment, int goldAdjustment) {
        String key = getKeyFromSign(sign);
        if (!data.containsKey(key)) {
            return;
        }
        TradeCraftDataInfo info = data.get(key);
        info.itemAmount += itemAdjustment;
        info.goldAmount += goldAdjustment;
        save();
    }

    private String getKeyFromSign(Sign sign) {
        return getKey(sign.getX(), sign.getY(), sign.getZ());
    }

    private String getKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}