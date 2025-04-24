package me.rockyhawk.commandpanels.floodgate;

import me.rockyhawk.commandpanels.Context;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OpenFloodgateGUI implements Listener {
    private Context ctx;

    public OpenFloodgateGUI(Context pl) {
        this.ctx = pl;
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
        //replace for multi-line support in simpleform content title
        SimpleForm.Builder form = SimpleForm.builder()
                .title(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), e.getPanel().getConfig().getString("title"))))
                .content(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fgPanel.getString("simple")).replaceAll("\\\\n", "\n")));

        List<String> buttonCommands;
        try {
            buttonCommands = processButtons(fgPanel, form, e.getPanel(), e.getPlayer());
        }catch (Exception err){
            e.getPlayer().sendMessage(ctx.tag + ChatColor.RED + "FloodGate panel button config error");
            ctx.debug.send(err, e.getPlayer(), ctx);
            return;
        }

        form.validResultHandler((SimpleFormResponse response) -> {
            int clickedButtonId = response.clickedButtonId();
            String configKey = buttonCommands.get(clickedButtonId);

            String section = ctx.has.hasSection(e.getPanel(), PanelPosition.Top, fgPanel.getConfigurationSection(configKey), e.getPlayer());
            ConfigurationSection buttonConfig = fgPanel.getConfigurationSection(configKey + section);

            if(buttonConfig.contains("commands")) {
                ctx.commandRunner.runCommands(e.getPanel(), PanelPosition.Top, e.getPlayer(), buttonConfig.getStringList("commands"), null);
            }
        });

        fgPlayer.sendForm(form.build());
    }

    private List<String> processButtons(ConfigurationSection fgPanel, SimpleForm.Builder form, Panel panel, Player p) {
        return fgPanel.getKeys(false).stream()
                .filter(key -> key.matches("\\d+")) // Only process numeric keys
                .sorted(Comparator.comparingInt(Integer::parseInt)) // Numeric sorting
                .filter(key -> { // Filter out keys where "text" is missing
                    ConfigurationSection buttonConfig = fgPanel.getConfigurationSection(key);
                    if (buttonConfig == null) return false; // Ensure buttonConfig exists
                    String section = ctx.has.hasSection(panel, PanelPosition.Top, buttonConfig, p);
                    ConfigurationSection resolvedConfig = fgPanel.getConfigurationSection(key + section);
                    return resolvedConfig != null && resolvedConfig.contains("text"); // Ensure resolvedConfig and "text" exist
                })
                .map(key -> {
                    String section = ctx.has.hasSection(panel, PanelPosition.Top, fgPanel.getConfigurationSection(key), p);
                    ConfigurationSection buttonConfig = fgPanel.getConfigurationSection(key + section);

                    String buttonContent = ctx.tex.placeholders(panel, null, p, buttonConfig.getString("text").replaceAll("\\\\n", "\n"));
                    if (!buttonConfig.contains("icon")) {
                        form.button(buttonContent);
                    } else {
                        FormImage.Type type = FormImage.Type.valueOf(ctx.tex.placeholders(panel, null, p, buttonConfig.getString("icon.type")).toUpperCase());
                        String texture = ctx.tex.placeholders(panel, null, p, buttonConfig.getString("icon.texture"));
                        form.button(buttonContent, type, texture);
                    }

                    return key; // Return the key for the final list
                })
                .collect(Collectors.toList());
    }

    private void createAndSendCustomForm(PanelOpenedEvent e, FloodgatePlayer fgPlayer, ConfigurationSection fgPanel) {
        CustomForm.Builder form = CustomForm.builder()
                .title(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), e.getPanel().getConfig().getString("title")));

        List<String> commandsOrder = new ArrayList<>();
        fgPanel.getKeys(false).forEach(key -> {
            if (key.matches("\\d+")) {
                String section = ctx.has.hasSection(e.getPanel(), e.getPosition(), fgPanel.getConfigurationSection(key), e.getPlayer());
                ConfigurationSection fieldConfig = fgPanel.getConfigurationSection(key + section);
                if(!fieldConfig.contains("text")) {
                    //skip do not add the element if text value is missing
                    return;
                }

                try {
                    String type = "toggle";
                    if(fieldConfig.contains("type")) {
                        type = fieldConfig.getString("type").toLowerCase();
                    }
                    switch (type) {
                        case "toggle":
                            form.toggle(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text").replaceAll("\\\\n", "\n")),
                                    Boolean.parseBoolean(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("default"))));
                            commandsOrder.add(key);
                            break;
                        case "slider":
                            form.slider(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text").replaceAll("\\\\n", "\n")),
                                    Long.parseLong(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("min"))),
                                    Long.parseLong(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("max"))),
                                    Long.parseLong(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("step"))),
                                    Long.parseLong(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("default"))));
                            commandsOrder.add(key);
                            break;
                        case "input":
                            form.input(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text").replaceAll("\\\\n", "\n")),
                                    ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("placeholder")),
                                    ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("default")));
                            commandsOrder.add(key);
                            break;
                        case "dropdown":
                            form.dropdown(ctx.tex.placeholders(e.getPanel(), null, e.getPlayer(), fieldConfig.getString("text").replaceAll("\\\\n", "\n")),
                                    ctx.tex.placeholdersList(e.getPanel(), null, e.getPlayer(), fieldConfig.getStringList("options"), true));
                            commandsOrder.add(key);
                            break;
                    }
                }catch (Exception err){
                    e.getPlayer().sendMessage(ctx.tag + ChatColor.RED + "FloodGate panel button config error");
                    ctx.debug.send(err, e.getPlayer(), ctx);
                }
            }
        });

        form.validResultHandler((CustomFormResponse response) -> {
            for (String configKey : commandsOrder) {  // Iterate over each command configuration key
                if (!response.hasNext()) {
                    break;  // Safety check to prevent NoSuchElementException
                }
                String section = ctx.has.hasSection(e.getPanel(), e.getPosition(), fgPanel.getConfigurationSection(configKey), e.getPlayer());
                ConfigurationSection fieldConfig = fgPanel.getConfigurationSection(configKey + section);

                if(fieldConfig.contains("commands")) {
                    Object fieldValue = response.next();  // Retrieve the next response value
                    String value = String.valueOf(fieldValue);  // Convert the field value to String
                    List<String> commands = fieldConfig.getStringList("commands");  // Retrieve commands for this field
                    List<String> processedCommands = new ArrayList<>();
                    for (String command : commands) {
                        processedCommands.add(command.replaceAll("%cp-input%", value));  // Replace the placeholder in each command
                    }
                    ctx.commandRunner.runCommands(e.getPanel(), PanelPosition.Top, e.getPlayer(), processedCommands, null);  // Execute the processed commands
                }
            }
        });

        fgPlayer.sendForm(form.build());
    }
}
