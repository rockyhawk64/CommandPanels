package me.rockyhawk.commandpanels.legacy;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class LegacyVersion {
    CommandPanels plugin;
    public LegacyVersion(CommandPanels pl) {
        this.plugin = pl;
    }

    //true if 1.12 or below
    public boolean isLegacy() {
        boolean output = false;
        ArrayList<String> legacyVersions = new ArrayList();
        legacyVersions.add("1.8");
        legacyVersions.add("1.9");
        legacyVersions.add("1.10");
        legacyVersions.add("1.11");
        legacyVersions.add("1.12");
        for(String key : legacyVersions){
            if (Bukkit.getVersion().contains(key)) {
                output = true;
                break;
            }
        }
        return  output;
    }
}
