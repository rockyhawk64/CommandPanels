package me.rockyhawk.commandpanels.openpanelsmanager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class WorldPermissions {
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
