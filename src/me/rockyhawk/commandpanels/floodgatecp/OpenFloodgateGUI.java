package me.rockyhawk.commandpanels.floodgatecp;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelOpenedEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.ArrayList;
import java.util.List;

public class OpenFloodgateGUI implements Listener {
    private CommandPanels plugin;

    public OpenFloodgateGUI(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onPanelOpen(PanelOpenedEvent e) {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(e.getPlayer().getUniqueId()) ||
                !e.getPanel().getConfig().contains("floodgate")) return;

        FloodgatePlayer fgPlayer = FloodgateApi.getInstance().getPlayer(e.getPlayer().getUniqueId());
        ConfigurationSection fgPanel = e.getPanel().getConfig().getConfigurationSection("floodgate");

        if (fgPanel.contains("simple")) {
            createAndSendSimpleForm(e, fgPlayer, fgPanel);
        } else {
            createAndSendCustomForm(e, fgPlayer, fgPanel);
        }

        e.setCancelled(true);
    }

    private void createAndSendSimpleForm(PanelOpenedEvent e, FloodgatePlayer fgPlayer, ConfigurationSection fgPanel) {
        SimpleForm.Builder form = SimpleForm.builder()
                .title(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), e.getPanel().getConfig().getString("title"))))
                .content(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fgPanel.getString("simple"))));

        List<String> buttonCommands;
        try {
            buttonCommands = processButtons(fgPanel, form, e.getPanel(), e.getPlayer());
        }catch (Exception err){
            e.getPlayer().sendMessage(plugin.tag + ChatColor.RED + "FloodGate panel button config error");
            plugin.debug(err, e.getPlayer());
            return;
        }

        form.validResultHandler((SimpleFormResponse response) -> {
            int clickedButtonId = response.clickedButtonId();
            String configKey = buttonCommands.get(clickedButtonId);
            if(fgPanel.contains(configKey + ".commands")) {
                plugin.commandTags.runCommands(e.getPanel(), PanelPosition.Top, e.getPlayer(), fgPanel.getStringList(configKey + ".commands"));
            }
        });

        fgPlayer.sendForm(form.build());
    }

    private List<String> processButtons(ConfigurationSection fgPanel, SimpleForm.Builder form, Panel panel, Player p) {
        List<String> commandsOrder = new ArrayList<>();
        fgPanel.getKeys(false).stream()
                .filter(key -> key.matches("\\d+"))
                .sorted()
                .forEach(key -> {
                    ConfigurationSection buttonConfig = fgPanel.getConfigurationSection(key);
                    if (buttonConfig == null) return;

                    String buttonContent = plugin.tex.placeholders(panel, null, p, buttonConfig.getString("text"));
                    if (!buttonConfig.contains("icon")) {
                        form.button(buttonContent);
                    } else {
                        FormImage.Type type = FormImage.Type.valueOf(plugin.tex.placeholders(panel, null, p, buttonConfig.getString("icon.type")).toUpperCase());
                        String texture = plugin.tex.placeholders(panel, null, p, buttonConfig.getString("icon.texture"));
                        form.button(buttonContent, type, texture);
                    }
                    commandsOrder.add(key);
                });
        return commandsOrder;
    }

    private void createAndSendCustomForm(PanelOpenedEvent e, FloodgatePlayer fgPlayer, ConfigurationSection fgPanel) {
        CustomForm.Builder form = CustomForm.builder()
                .title(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), e.getPanel().getConfig().getString("title")));

        List<String> commandsOrder = new ArrayList<>();
        fgPanel.getKeys(false).forEach(key -> {
            if (key.matches("\\d+")) {
                ConfigurationSection fieldConfig = fgPanel.getConfigurationSection(key);
                try {
                    String type = "toggle";
                    if(fieldConfig.contains("type")) {
                        type = fieldConfig.getString("type").toLowerCase();
                    }
                    switch (type) {
                        case "toggle":
                            form.toggle(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text")),
                                    Boolean.parseBoolean(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("default"))));
                            commandsOrder.add(key);
                            break;
                        case "slider":
                            form.slider(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text")),
                                    Long.parseLong(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("min"))),
                                    Long.parseLong(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("max"))),
                                    Long.parseLong(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("step"))),
                                    Long.parseLong(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("default"))));
                            commandsOrder.add(key);
                            break;
                        case "input":
                            form.input(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text")),
                                    plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("placeholder")),
                                    plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("default")));
                            commandsOrder.add(key);
                            break;
                        case "dropdown":
                            form.dropdown(plugin.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text")),
                                    plugin.tex.placeholdersList(e.getPanel(), null, e.getPlayer(), fieldConfig.getStringList("options"), true));
                            commandsOrder.add(key);
                            break;
                    }
                }catch (Exception err){
                    e.getPlayer().sendMessage(plugin.tag + ChatColor.RED + "FloodGate panel button config error");
                    plugin.debug(err, e.getPlayer());
                }
            }
        });

        form.validResultHandler((CustomFormResponse response) -> {
            for (String configKey : commandsOrder) {  // Iterate over each command configuration key
                if (!response.hasNext()) {
                    break;  // Safety check to prevent NoSuchElementException
                }
                if(fgPanel.contains(configKey + ".commands")) {
                    Object fieldValue = response.next();  // Retrieve the next response value
                    String value = String.valueOf(fieldValue);  // Convert the field value to String
                    List<String> commands = fgPanel.getStringList(configKey + ".commands");  // Retrieve commands for this field
                    List<String> processedCommands = new ArrayList<>();
                    for (String command : commands) {
                        processedCommands.add(command.replaceAll("%cp-input%", value));  // Replace the placeholder in each command
                    }
                    plugin.commandTags.runCommands(e.getPanel(), PanelPosition.Top, e.getPlayer(), processedCommands);  // Execute the processed commands
                }
            }
        });

        fgPlayer.sendForm(form.build());
    }
}
