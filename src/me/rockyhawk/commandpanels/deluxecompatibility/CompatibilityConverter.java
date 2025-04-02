package me.rockyhawk.commandpanels.deluxecompatibility;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityConverter {
    CommandPanels plugin;

    public CompatibilityConverter(CommandPanels pl) {
        this.plugin = pl;
    }

    public YamlConfiguration tryConversion(String fileName, YamlConfiguration inputYaml){
        try {
            // Create converted YAML
            YamlConfiguration convertedYaml = new YamlConfiguration();
            ConfigurationSection panelsSection = convertedYaml.createSection("panels");

            List<String> allRequirementDetails = new ArrayList<>();

            // Check if the input has a 'menus' key for DeluxeMenus format
            if (inputYaml.contains("menus")) {
                // Multiple menus format
                ConfigurationSection menusSection = inputYaml.getConfigurationSection("menus");
                for (String menuName : menusSection.getKeys(false)) {
                    ConfigurationSection menuData = menusSection.getConfigurationSection(menuName);
                    List<String> requirements = convertMenu(menuData, menuName, panelsSection);
                    allRequirementDetails.addAll(requirements);
                }
            } else {
                // Single menu format
                List<String> requirements = convertMenu(inputYaml, ChatColor.stripColor(plugin.tex.colour(inputYaml.getString("menu_title"))), panelsSection);
                allRequirementDetails.addAll(requirements);
            }

            // Save the converted YAML to a new file
            return convertedYaml;
        } catch (Exception e) {
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Error in file: " + fileName);
            return null;
        }
    }

    private List<String> convertMenu(ConfigurationSection data, String panelName, ConfigurationSection panelsSection) {
        List<String> allRequirementDetails = new ArrayList<>();

        try {
            // Panel Settings
            ConfigurationSection panel = panelsSection.createSection(panelName);

            // Set basic panel properties
            panel.set("title", data.getString("menu_title", "&8" + panelName));
            panel.set("rows", (int)Math.ceil(data.getInt("size", 9) / 9.0));
            panel.set("perm", "default");
            panel.set("empty", data.getString("empty", "BLACK_STAINED_GLASS_PANE"));

            // Handle commands
            if (data.contains("open_command")) {
                if (data.isList("open_command")) {
                    panel.set("commands", data.getStringList("open_command"));
                } else {
                    List<String> commands = new ArrayList<>();
                    commands.add(data.getString("open_command"));
                    panel.set("commands", commands);
                }
            }

            if (data.contains("open_commands")) {
                panel.set("commands-on-open", checkTags(data.getStringList("open_commands")));
            }

            if (data.contains("close_commands")) {
                panel.set("commands-on-close", checkTags(data.getStringList("close_commands")));
            }

            if (data.contains("update_interval")) {
                panel.set("refresh-delay", data.getInt("update_interval") * 20);
            }

            // Items section
            ConfigurationSection itemSection = panel.createSection("item");

            // Process each item
            if (data.contains("items")) {
                ConfigurationSection itemsData = data.getConfigurationSection("items");
                Map<Integer, Map<Integer, Map<String, Object>>> items = new HashMap<>();

                for (String itemKey : itemsData.getKeys(false)) {
                    ConfigurationSection itemData = itemsData.getConfigurationSection(itemKey);

                    if (!itemData.contains("slot")) {
                        continue;
                    }

                    int slot = itemData.getInt("slot");
                    int priority = itemData.getInt("priority", 0);

                    // Initialize the slot if needed
                    if (!items.containsKey(slot)) {
                        items.put(slot, new HashMap<>());
                    }

                    // Create item data map
                    Map<String, Object> itemProperties = new HashMap<>();
                    itemProperties.put("material", itemData.getString("material", "STONE"));
                    itemProperties.put("stack", itemData.get("amount", itemData.get("dynamic_amount", 1)));

                    if (itemData.contains("display_name")) {
                        itemProperties.put("name", itemData.getString("display_name"));
                    }

                    if (itemData.contains("lore")) {
                        itemProperties.put("lore", itemData.getStringList("lore"));
                    }

                    if (itemData.contains("enchantments")) {
                        itemProperties.put("enchanted", itemData.get("enchantments"));
                    }

                    if (itemData.contains("model_data")) {
                        itemProperties.put("customdata", itemData.getInt("model_data"));
                    }

                    if (itemData.contains("click_commands")) {
                        itemProperties.put("commands", checkTags(itemData.getStringList("click_commands")));
                    }

                    // Store requirements for later processing
                    if (itemData.contains("view_requirement")) {
                        ConfigurationSection viewReqSection = itemData.getConfigurationSection("view_requirement");
                        if (viewReqSection.contains("requirements")) {
                            List<Map<?, ?>> viewRequirements = getRequirementsList(viewReqSection, "requirements");
                            itemProperties.put("view_requirements", viewRequirements);
                        }
                    }

                    if (itemData.contains("click_requirement")) {
                        ConfigurationSection clickReqSection = itemData.getConfigurationSection("click_requirement");
                        if (clickReqSection.contains("requirements")) {
                            List<Map<?, ?>> clickRequirements = getRequirementsList(clickReqSection, "requirements");
                            itemProperties.put("click_requirements", clickRequirements);
                        }
                    }

                    // Store the item properties
                    items.get(slot).put(priority, itemProperties);
                }

                // Process items by priority
                for (int slot : items.keySet()) {
                    Map<Integer, Map<String, Object>> slotItems = items.get(slot);

                    // Sort priorities in descending order
                    List<Integer> priorities = new ArrayList<>(slotItems.keySet());
                    priorities.sort((a, b) -> b - a);

                    for (int i = 0; i < priorities.size(); i++) {
                        int priority = priorities.get(i);
                        Map<String, Object> itemProps = slotItems.get(priority);

                        if (i == 0) {
                            // First priority is the main item
                            ConfigurationSection slotSection = itemSection.createSection(String.valueOf(slot));

                            // Set basic properties
                            slotSection.set("material", itemProps.get("material"));
                            slotSection.set("stack", itemProps.get("stack"));

                            if (itemProps.containsKey("name")) {
                                slotSection.set("name", itemProps.get("name"));
                            }

                            if (itemProps.containsKey("lore")) {
                                slotSection.set("lore", itemProps.get("lore"));
                            }

                            if (itemProps.containsKey("enchanted")) {
                                slotSection.set("enchanted", itemProps.get("enchanted"));
                            }

                            if (itemProps.containsKey("customdata")) {
                                slotSection.set("customdata", itemProps.get("customdata"));
                            }

                            if (itemProps.containsKey("commands")) {
                                slotSection.set("commands", itemProps.get("commands"));
                            }
                        } else {
                            // Higher priorities go into hasX
                            String hasKey = "has" + (i - 1);
                            ConfigurationSection slotSection = itemSection.getConfigurationSection(String.valueOf(slot));
                            ConfigurationSection hasSection = slotSection.createSection(hasKey);

                            // Set basic properties
                            hasSection.set("material", itemProps.get("material"));
                            hasSection.set("stack", itemProps.get("stack"));

                            if (itemProps.containsKey("name")) {
                                hasSection.set("name", itemProps.get("name"));
                            }

                            if (itemProps.containsKey("lore")) {
                                hasSection.set("lore", itemProps.get("lore"));
                            }

                            if (itemProps.containsKey("enchanted")) {
                                hasSection.set("enchanted", itemProps.get("enchanted"));
                            }

                            if (itemProps.containsKey("customdata")) {
                                hasSection.set("customdata", itemProps.get("customdata"));
                            }

                            if (itemProps.containsKey("commands")) {
                                hasSection.set("commands", itemProps.get("commands"));
                            }

                            // Process requirements
                            List<Map<?, ?>> mergedRequirements = new ArrayList<>();

                            if (itemProps.containsKey("view_requirements")) {
                                @SuppressWarnings("unchecked")
                                List<Map<?, ?>> viewReqs = (List<Map<?, ?>>) itemProps.get("view_requirements");
                                mergedRequirements.addAll(viewReqs);
                            }

                            if (itemProps.containsKey("click_requirements")) {
                                @SuppressWarnings("unchecked")
                                List<Map<?, ?>> clickReqs = (List<Map<?, ?>>) itemProps.get("click_requirements");
                                mergedRequirements.addAll(clickReqs);
                            }

                            if (!mergedRequirements.isEmpty()) {
                                convertRequirements(mergedRequirements, slotSection, hasKey, allRequirementDetails);
                            }
                        }
                    }
                }
            }

            return allRequirementDetails;
        } catch (Exception e) {
            return allRequirementDetails;
        }
    }

    private List<Map<?, ?>> getRequirementsList(ConfigurationSection section, String key) {
        if (section.isList(key)) {
            return section.getMapList(key);
        } else {
            // Handle the case where it might be a direct object
            return new ArrayList<>();
        }
    }

    private void convertRequirements(List<Map<?, ?>> requirements,
                                     ConfigurationSection panel,
                                     String hasSection,
                                     List<String> allRequirementDetails) {
        for (Map<?, ?> requirement : requirements) {
            if (requirement.isEmpty()) continue;

            Map.Entry<?, ?> entry = requirement.entrySet().iterator().next();
            if (!(entry.getValue() instanceof Map)) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> reqDetails = (Map<String, Object>) entry.getValue();

            if (!reqDetails.containsKey("type")) continue;

            String type = reqDetails.get("type").toString();
            String slotStr = panel.getName();
            ConfigurationSection hasKeySection = panel.getConfigurationSection(hasSection);

            if (type.contains("has permission")) {
                String permission = reqDetails.get("permission").toString();
                boolean not = type.contains("!");

                hasKeySection.set("value0", not ? "NOT %cp-player-name% HASPERM" : "%cp-player-name% HASPERM");
                hasKeySection.set("compare0", permission);

                allRequirementDetails.add("Slot " + slotStr + ": Requires permission '" + permission + "'");
            }

            if (type.contains("has money")) {
                double amount = Double.parseDouble(reqDetails.get("amount").toString());
                boolean not = type.contains("!");

                hasKeySection.set("value0", not ? "NOT %cp-player-balance% ISGREATER" : "%cp-player-balance% ISGREATER");
                hasKeySection.set("compare0", amount);

                allRequirementDetails.add("Slot " + slotStr + ": Requires at least $" + amount);
            }

            if (type.contains("string equals")) {
                String input = reqDetails.get("input").toString();
                String output = reqDetails.get("output").toString();
                boolean not = type.contains("!");

                hasKeySection.set("value0", not ? "NOT " + input : input);
                hasKeySection.set("compare0", output);

                allRequirementDetails.add("Slot " + slotStr + ": Requires '" + input + "' to equal '" + output + "'");
            }
        }
    }

    private List<String> checkTags(List<String> commands) {
        List<String> newCommands = new ArrayList<>();

        for (String command : commands) {
            String newCommand = command;

            // Check if command contains delay tag
            if (newCommand.contains("<delay=")) {
                int startIndex = newCommand.indexOf("<delay=") + 7; // Start of number
                int endIndex = newCommand.indexOf(">", startIndex); // End of number

                if (endIndex > startIndex) {
                    String delayValue = newCommand.substring(startIndex, endIndex); // Extract delay value
                    newCommand = "delay= " + delayValue + " " + newCommand.substring(0, startIndex - 7).trim();
                }
            }

            // Replace common tags
            newCommand = newCommand
                    .replace("[player]", "")
                    .replace("[console]", "console=")
                    .replace("[commandevent]", "sudo=")
                    .replace("[message]", "msg=")
                    .replace("[broadcast]", "broadcast=")
                    .replace("[openguimenu]", "open=")
                    .replace("[connect]", "server=")
                    .replace("[close]", "cpc")
                    .replace("[refresh]", "refresh")
                    .replace("[sound]", "sound=")
                    .replace("[takemoney]", "paywall=")
                    .replace("[takeexp]", "xp-paywall=")
                    .replace("[chat]", "send=")
                    .replace("%player_name%", "%cp-player-name%");

            newCommands.add(newCommand);
        }

        return newCommands;
    }
}