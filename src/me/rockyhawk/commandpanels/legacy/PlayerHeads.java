package me.rockyhawk.commandpanels.legacy;

import me.rockyhawk.commandpanels.CommandPanels;

public class PlayerHeads {
    CommandPanels plugin;
    public PlayerHeads(CommandPanels pl) {
        this.plugin = pl;
    }

    public boolean ifSkullOrHead(String material) {
        return material.equalsIgnoreCase("PLAYER_HEAD") || material.equalsIgnoreCase("SKULL_ITEM");
    }

    public String playerHeadString() {
        if(plugin.legacy.isLegacy()){
            return "SKULL_ITEM";
        }else{
            return "PLAYER_HEAD";
        }
    }
}
