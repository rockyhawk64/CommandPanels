package me.rockyhawk.commandpanels.legacy;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.GetStorageContents;
import me.rockyhawk.commandpanels.ioclasses.GetStorageContents_Legacy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class LegacyVersion {
    CommandPanels plugin;
    public LegacyVersion(CommandPanels pl) {
        this.plugin = pl;
    }

    //true if 1.15 or below
    public boolean isLegacyStorageContents() {
        boolean output = false;
        ArrayList<String> legacyVersions = new ArrayList<>();
        legacyVersions.add("1.8");
        legacyVersions.add("1.9");
        legacyVersions.add("1.10");
        legacyVersions.add("1.11");
        legacyVersions.add("1.12");
        legacyVersions.add("1.13");
        legacyVersions.add("1.14");
        legacyVersions.add("1.15");
        for(String key : legacyVersions){
            if (Bukkit.getVersion().contains(key)) {
                output = true;
                break;
            }
        }
        return  output;
    }

    //true if 1.12 or below
    public boolean isLegacy() {
        boolean output = false;
        ArrayList<String> legacyVersions = new ArrayList<>();
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

    public ItemStack[] getStorageContents(Inventory i){
        if(plugin.legacy.isLegacy()){
            return new GetStorageContents_Legacy(plugin).getStorageContents(i);
        }else{
            return new GetStorageContents(plugin).getStorageContents(i);
        }
    }

    public void setStorageContents(Player p, ItemStack[] i){
        if(plugin.legacy.isLegacyStorageContents()){
            new GetStorageContents_Legacy(plugin).setStorageContents(p,i);
        }else{
            new GetStorageContents(plugin).setStorageContents(p,i);
        }
    }
}
