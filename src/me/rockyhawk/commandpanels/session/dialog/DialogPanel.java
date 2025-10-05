package me.rockyhawk.commandpanels.session.dialog;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.dialog.DialogPanelBuilder;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.floodgate.FloodgatePanel;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.*;

public class DialogPanel extends Panel {
    private final String columns;
    private final String escapable;
    private final String exitButton;
    private final String floodgate;
    private final Map<String, DialogComponent> components = new HashMap<>();
    private final Map<String, List<String>> order = new HashMap<>();

    public DialogPanel(String name, YamlConfiguration config) {
        super(name, config);

        this.floodgate = config.getString("floodgate", "");
        this.columns = config.getString("columns", "1");
        this.escapable = config.getString("escapable", "true");
        this.exitButton = config.getString("has-exit-button", "false");

        ConfigurationSection order = config.getConfigurationSection("layout");
        if (order != null) {
            for (String key : order.getKeys(false)) {
                this.order.put(key, order.getStringList(key));
            }
        }

        ConfigurationSection componentSection = config.getConfigurationSection("items");
        if (componentSection != null) {
            for (String key : componentSection.getKeys(false)) {
                ConfigurationSection configSection = componentSection.getConfigurationSection(key);
                if (configSection != null) {
                    // Store item id as string key in hashmap AND store id in PanelItem
                    DialogComponent component = DialogComponent.fromSection(key, configSection);
                    if(component == null) continue;
                    components.put(component.getId(), component);
                }
            }
        }
    }

    @Override
    public void open(Context ctx, Player player, boolean isNewPanelSession){
        // Check for floodgate panel if bedrock player
        Panel panel = ctx.plugin.panels.get(floodgate);
        if (Bukkit.getPluginManager().getPlugin("floodgate") != null) {
            if (panel instanceof FloodgatePanel floodgatePanel &&
                    FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                floodgatePanel.open(ctx, player, true);
                return;
            }
        }

        if(isNewPanelSession) {
            // Update panel data values
            updatePanelData(ctx, player);

            // Run panel commands
            CommandRunner runner = new CommandRunner(ctx);
            runner.runCommands(this, player, this.getCommands());
        }

        // Build and open panel
        PanelBuilder builder = new DialogPanelBuilder(ctx, player);
        builder.open(this);
    }

    public Map<String, DialogComponent> getComponents() { return components; }
    public Map<String, List<String>> getOrder() { return order; }

    public String getColumns() {
        return columns;
    }

    public String getEscapable() {
        return escapable;
    }

    public String getExitButton() {
        return exitButton;
    }
}
