package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class PanelPermissions {
    CommandPanels plugin;
    public PanelPermissions(CommandPanels pl) {
        this.plugin = pl;
    }

    //if panel has the world enabled
    public boolean isPanelWorldEnabled(Player p, ConfigurationSection panelConfig){
        if(panelConfig.contains("disabled-worlds")){
            return !panelConfig.getStringList("disabled-worlds").contains(p.getWorld().getName());
        }
        if(panelConfig.contains("enabled-worlds")){
            return panelConfig.getStringList("enabled-worlds").contains(p.getWorld().getName());
        }
        return true;
    }
}
