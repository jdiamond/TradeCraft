import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TradeCraftConfigurationFile {

    private static final String fileName = TradeCraft.pluginName + ".txt";

    private static final Pattern commentPattern = Pattern.compile("^\\s*#.*$");
    private static final Pattern infoPattern = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // name
            "\\s*(\\d+)\\s*," + // id
            "\\s*(\\d+)\\s*:\\s*(\\d+)\\s*" + // buyAmount:buyValue
            "(?:,\\s*(\\d+)\\s*:\\s*(\\d+))?\\s*$"); // sellAmount:sellValue

    private final TradeCraft plugin;
    private final Map<String, TradeCraftConfigurationInfo> infos = new HashMap<String, TradeCraftConfigurationInfo>();

    TradeCraftConfigurationFile(TradeCraft plugin) {
        this.plugin = plugin;
    }

    void load() {
        try {
            infos.clear();

            BufferedReader configurationFile = new BufferedReader(new FileReader(fileName));

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
                    plugin.log.warning(
                            "Failed to parse line number " + lineNumber +
                            " in " + fileName +
                            ": " + line);
                    continue;
                }

                TradeCraftConfigurationInfo info = new TradeCraftConfigurationInfo();
                info.name = infoMatcher.group(1);
                info.id = Integer.parseInt(infoMatcher.group(2));
                info.sellAmount = info.buyAmount = Integer.parseInt(infoMatcher.group(3));
                info.sellValue = info.buyValue = Integer.parseInt(infoMatcher.group(4));

                if (infoMatcher.group(5) != null) {
                    info.sellAmount = Integer.parseInt(infoMatcher.group(5));
                    info.sellValue = Integer.parseInt(infoMatcher.group(6));
                }

                infos.put(info.name.toUpperCase(), info);
            }

            configurationFile.close();
        } catch (IOException e) {
            plugin.log.warning("Error reading " + fileName);
        }
    }

    public boolean isConfigured(String name) {
        return infos.containsKey(name.toUpperCase());
    }

    public TradeCraftConfigurationInfo get(String name) {
        return infos.get(name.toUpperCase());
    }
}