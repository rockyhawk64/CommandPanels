package me.rockyhawk.commandpanels.api;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;

public class CommandPanelsAPI {
    CommandPanels plugin;
    public CommandPanelsAPI(CommandPanels pl) {
        this.plugin = pl;
    }

    //returns true if the player has a panel open
    public boolean isPanelOpen(Player p){
        return plugin.openPanels.hasPanelOpen(p.getName());
    }

    //get the name of a panel currently open, will return null if panel is not open
    public String getPanelName(Player p){
        return plugin.openPanels.getOpenPanelName(p.getName());
    }

    //will return item slots of hotbar stationary items
    public ArrayList<Integer> getHotbarItems(){
        return plugin.hotbar.getStationaryItemSlots();
    }

    //open a panel using a custom file or configuration
    public void openGUI(Player p, YamlConfiguration panelYaml, String panelName){
        ConfigurationSection panelSection = panelYaml.getConfigurationSection("panels." + panelName);
        plugin.openVoids.openCommandPanel(plugin.getServer().getConsoleSender(), p, panelName, panelSection, false);
    }
    public void openGUI(Player p, File panelFile, String panelName){
        ConfigurationSection panelSection = YamlConfiguration.loadConfiguration(panelFile).getConfigurationSection("panels." + panelName);
        plugin.openVoids.openCommandPanel(plugin.getServer().getConsoleSender(), p, panelName, panelSection, false);
    }
}
