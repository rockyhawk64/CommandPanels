package me.rockyhawk.commandpanels.session.floodgate;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.floodgate.FloodgatePanelBuilder;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloodgatePanel extends Panel {
    private final String simpleSubtitle;
    private final String floodgateType;
    private final Map<String, FloodgateComponent> components = new HashMap<>();
    private final Map<String, List<String>> order = new HashMap<>();

    public FloodgatePanel(String name, YamlConfiguration config) {
        super(name, config);

        this.simpleSubtitle = config.getString("subtitle", "");
        this.floodgateType = config.getString("floodgate-type", "simple");

        ConfigurationSection orderSection = config.getConfigurationSection("layout");
        if (orderSection != null) {
            for (String key : orderSection.getKeys(false)) {
                order.put(key, orderSection.getStringList(key));
            }
        }

        ConfigurationSection componentSection = config.getConfigurationSection("items");
        if (componentSection != null) {
            for (String key : componentSection.getKeys(false)) {
                ConfigurationSection configSection = componentSection.getConfigurationSection(key);
                if (configSection != null) {
                    // Store item id as string key in hashmap AND store id in PanelItem
                    FloodgateComponent component = FloodgateComponent.fromSection(key, configSection);
                    if(component == null) continue;
                    components.put(component.getId(), component);
                }
            }
        }
    }

    @Override
    public void open(Context ctx, Player player, SessionManager.PanelOpenType openType){
        if (Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            return;
        }

        // Run panel commands
        if(openType != SessionManager.PanelOpenType.REFRESH) {
            CommandRunner runner = new CommandRunner(ctx);
            runner.runCommands(this, player, this.getCommands());
        }

        // Build and open panel
        PanelBuilder builder = new FloodgatePanelBuilder(ctx, player);
        builder.open(this, openType);
    }
    public String getSubtitle() { return simpleSubtitle; }
    public String getFloodgateType() { return floodgateType; }
    public Map<String, FloodgateComponent> getComponents() { return components; }
    public Map<String, List<String>> getOrder() { return order; }
}
