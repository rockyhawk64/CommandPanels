package me.rockyhawk.commandpanels;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Converter {
    private final Context ctx;

    public Converter(Context ctx) {
        this.ctx = ctx;
    }

    // CONVERTER For CommandPanels v3 to v4
    // Will be removed at some point when v4 is further adopted
    // Does not include 1:! conversions as the two configs are incompatible in many ways

    public void convertPanels(CommandSender sender) {
        File oldPanelsDir = new File(ctx.plugin.getDataFolder(), "old_panels"); // e.g. plugins/CommandPanels/old_panels
        if (!oldPanelsDir.exists()) {
            ctx.text.sendError(sender, "Converts v3 -> v4 Panel config layouts");
            ctx.text.sendError(sender, "Old panels directory not found: " + oldPanelsDir.getPath());
            return;
        }

        File[] files = oldPanelsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            ctx.text.sendInfo(sender, "No old panel files found.");
            return;
        }

        for (File file : files) {
            try {
                YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection panelsSection = oldConfig.getConfigurationSection("panels");

                if (panelsSection == null) {
                    ctx.text.sendError(sender, "No panels section in file: " + file.getName());
                    continue;
                }

                for (String panelName : panelsSection.getKeys(false)) {
                    ConfigurationSection oldPanel = panelsSection.getConfigurationSection(panelName);
                    if (oldPanel == null) continue;

                    Map<String, Object> newPanel = convertPanel(panelName, oldPanel);

                    File outFile = new File(ctx.plugin.folder, panelName + ".yml");
                    YamlConfiguration newYaml = new YamlConfiguration();
                    for (Map.Entry<String, Object> entry : newPanel.entrySet()) {
                        newYaml.set(entry.getKey(), entry.getValue());
                    }
                    newYaml.save(outFile);

                    ctx.text.sendInfo(sender, "Converted panel: " + panelName + " -> " + outFile.getName());
                }
            } catch (IOException ex) {
                ctx.text.sendError(sender, "Failed to convert file: " + file.getName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertPanel(String panelName, ConfigurationSection oldPanel) {
        Map<String, Object> newPanel = new LinkedHashMap<>();

        newPanel.put("title", oldPanel.getString("title", "&fPanel"));
        newPanel.put("type", "inventory"); // hardcoded type unless we detect others
        newPanel.put("rows", oldPanel.getInt("rows", 6));

        // Commands from "commands" and "commands-on-open"
        List<String> openCommands = oldPanel.getStringList("commands-on-open");
        List<String> baseCommands = oldPanel.getStringList("commands");
        String newOpenCommand = null;

        if (!baseCommands.isEmpty()) {
            newOpenCommand = baseCommands.get(0); // Only use the first base command
        }

        List<String> newOpenCommands = new ArrayList<>(openCommands); // Add commands

        newPanel.put("command", newOpenCommand);
        newPanel.put("commands", newOpenCommands);

        // Create layout and items map
        Map<String, List<String>> layout = new LinkedHashMap<>();
        Map<String, Object> items = new LinkedHashMap<>();

        // Add custom items (named reusable entries)
        if (oldPanel.isConfigurationSection("custom-item")) {
            ConfigurationSection customItems = oldPanel.getConfigurationSection("custom-item");
            for (String itemKey : customItems.getKeys(false)) {
                items.put(itemKey, convertItem(customItems.getConfigurationSection(itemKey)));
            }
        }

        // Convert item slots to layout
        ConfigurationSection itemSection = oldPanel.getConfigurationSection("item");
        if (itemSection != null) {
            for (String slotKey : itemSection.getKeys(false)) {
                ConfigurationSection itemConfig = itemSection.getConfigurationSection(slotKey);

                String itemId = panelName + "_slot_" + slotKey;
                Map<String, Object> convertedItem = convertItem(itemConfig);

                if (itemConfig.contains("commands") || hasNestedConditions(itemConfig)) {
                    // Move commands into 'actions'
                    List<String> cmds = itemConfig.getStringList("commands");
                    if (!cmds.isEmpty()) {
                        Map<String, Object> leftClick = new HashMap<>();
                        List<String> cmdList = new ArrayList<>();
                        for (String cmd : cmds) {
                            // Do command tag convert attempts
                            cmdList.add(commandTagConverter(cmd));
                        }
                        leftClick.put("commands", cmdList);
                        convertedItem.put("actions", leftClick);
                    }
                }

                // Handle nested "has0", "has1" logic
                List<Map<String, Object>> logicVariants = extractConditionVariants(itemConfig);
                if (!logicVariants.isEmpty()) {
                    // Add base item as default (without conditions)
                    items.put(itemId + "_base", convertedItem);
                    layout.put(slotKey, new ArrayList<>(Collections.singletonList(itemId + "_base")));

                    int i = 0;
                    for (Map<String, Object> variant : logicVariants) {
                        String variantId = itemId + "_cond_" + i;
                        items.put(variantId, variant);
                        layout.get(slotKey).add(0, variantId); // priority first
                        i++;
                    }
                } else {
                    items.put(itemId, convertedItem);
                    layout.put(slotKey, new ArrayList<>(Collections.singletonList(itemId)));
                }
            }
        }
        if(oldPanel.contains("empty")){
            layout.put("fill", new ArrayList<>(Collections.singletonList("empty_item")));
            items.put("empty_item.material", oldPanel.getString("empty"));
        }

        newPanel.put("layout", layout);
        newPanel.put("items", items);

        return newPanel;
    }

    private Map<String, Object> convertItem(ConfigurationSection itemConfig) {
        Map<String, Object> item = new LinkedHashMap<>();

        if (itemConfig == null) return item;

        for (String key : itemConfig.getKeys(false)) {
            Object value = itemConfig.get(key);

            switch (key) {
                case "commands":
                    // Handled separately in layout
                    break;
                case "name":
                case "material":
                case "lore":
                case "enchanted":
                case "damage":
                case "stack":
                case "leatherarmor":
                    item.put(key, value);
                    break;
                default:
                    if (!key.startsWith("has")) {
                        item.put(key, value);
                    }
                    break;
            }
        }

        return item;
    }

    private String commandTagConverter(String command){
        command = command.replace("server=", "[server]");
        command = command.replace("open=", "[open]");
        command = command.replace("cpc", "[close]");
        command = command.replace("console=", "[console]");
        command = command.replace("refresh", "[refresh]");
        command = command.replace("data=", "[data]");
        command = command.replace("msg=", "[msg]");
        command = command.replace("teleport=", "[teleport]");
        return command;
    }

    private boolean hasNestedConditions(ConfigurationSection itemConfig) {
        for (String key : itemConfig.getKeys(false)) {
            if (key.startsWith("has")) return true;
        }
        return false;
    }

    private List<Map<String, Object>> extractConditionVariants(ConfigurationSection itemConfig) {
        List<Map<String, Object>> variants = new ArrayList<>();

        for (String key : itemConfig.getKeys(false)) {
            if (key.startsWith("has")) {
                ConfigurationSection conditionSection = itemConfig.getConfigurationSection(key);
                if (conditionSection == null) continue;

                Map<String, Object> variant = convertItem(conditionSection);

                String conditions = "";
                for (String subKey : conditionSection.getKeys(false)) {
                    if (subKey.startsWith("compare")) {
                        int index = Integer.parseInt(subKey.substring(7));
                        String valueKey = "value" + index;
                        if (conditionSection.contains(valueKey)) {
                            String compare = conditionSection.getString(subKey);
                            String value = conditionSection.getString(valueKey);
                            conditions = compare + " $EQUALS " + value;
                            break;
                        }
                    }
                }

                if (!conditions.isEmpty()) {
                    variant.put("conditions", conditions);
                }

                variants.add(variant);
            }
        }

        return variants;
    }
}