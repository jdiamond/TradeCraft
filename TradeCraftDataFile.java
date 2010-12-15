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
    private static final Pattern infoPattern = Pattern.compile(
            "^\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)" + // x,y,z
            "\\s*=\\s*" +
            "(\\d+)\\s*,\\s*(\\d+)\\s*$"); // itemAmount,goldAmount

    private final TradeCraft plugin;
    private final Map<String, TradeCraftDataInfo> data = new HashMap<String, TradeCraftDataInfo>();

    TradeCraftDataFile(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public synchronized void load() {
        if (!new File(fileName).exists()) {
            plugin.log.warning("No " + fileName + " file to read");
            return;
        }

        try {
            data.clear();

            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber += 1;

                Matcher infoMatcher = infoPattern.matcher(line);

                if (!infoMatcher.matches()) {
                    plugin.log.warning(
                            "Failed to parse line number " + lineNumber +
                            " in " + fileName +
                            ": " + line);
                    continue;
                }

                int x = Integer.parseInt(infoMatcher.group(1));
                int y = Integer.parseInt(infoMatcher.group(2));
                int z = Integer.parseInt(infoMatcher.group(3));
                int itemAmount = Integer.parseInt(infoMatcher.group(4));
                int goldAmount = Integer.parseInt(infoMatcher.group(5));

                String key = getKey(x, y, z);

                TradeCraftDataInfo info = new TradeCraftDataInfo();
                info.itemAmount = itemAmount;
                info.goldAmount = goldAmount;

                data.put(key, info);
            }

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
                writer.write(key + "=" + info.itemAmount + "," + info.goldAmount);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            plugin.log.warning("error writing " + fileName);
        }
    }

    public synchronized void deposit(int x, int y, int z, int itemAmount) {
        String key = getKey(x, y, z);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            info.itemAmount += itemAmount;
        } else {
            TradeCraftDataInfo info = new TradeCraftDataInfo();
            info.itemAmount = itemAmount;
            data.put(key, info);
        }
        save();
    }

    public synchronized void depositGold(int x, int y, int z, int goldAmount) {
        String key = getKey(x, y, z);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            info.goldAmount += goldAmount;
        } else {
            TradeCraftDataInfo info = new TradeCraftDataInfo();
            info.goldAmount = goldAmount;
            data.put(key, info);
        }
        save();
    }

    public synchronized int withdraw(int x, int y, int z) {
        String key = getKey(x, y, z);
        TradeCraftDataInfo info = data.get(key);
        int itemAmount = info.itemAmount;
        if (itemAmount != 0) {
            info.itemAmount = 0;
            save();
        }
        return itemAmount;
    }

    public synchronized int withdrawGold(int x, int y, int z) {
        String key = getKey(x, y, z);
        TradeCraftDataInfo info = data.get(key);
        int goldAmount = info.goldAmount;
        if (goldAmount != 0) {
            info.goldAmount = 0;
            save();
        }
        return goldAmount;
    }

    private String getKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}