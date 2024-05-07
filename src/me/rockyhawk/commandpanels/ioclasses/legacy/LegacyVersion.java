package me.rockyhawk.commandpanels.ioclasses.legacy;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.storagecontents.GetStorageContents;
import me.rockyhawk.commandpanels.ioclasses.storagecontents.GetStorageContents_Legacy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LegacyVersion {
    CommandPanels plugin;
    public MinecraftVersions MAJOR_VERSION; //The major version of the server (eg, converts 1.20.5 to 1.20)
    public int MINOR_VERSION; //The minor version of the server (1.20.5 to 5)
    public LegacyVersion(CommandPanels pl) {
        this.plugin = pl;
        String VERSION = plugin.getServer().getBukkitVersion().split("-")[0];
        MAJOR_VERSION = MinecraftVersions.get(extractMajorVersion(VERSION));
        MINOR_VERSION = extractMinorVersion(VERSION);
    }

    public String extractMajorVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length < 2) {
            // Handle the case where there aren't enough parts for a "major.major.minor" format.
            return version;  // Return the original version.
        }
        return parts[0] + "." + parts[1];
    }
    public Integer extractMinorVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length > 2) {
            // Take only the third part and convert it to an integer.
            try {
                return Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;  // Return 0 if there are no parts beyond the first two.
    }


    public ItemStack[] getStorageContents(Inventory i){
        if(MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
            return new GetStorageContents_Legacy(plugin).getStorageContents(i);
        }else{
            return new GetStorageContents(plugin).getStorageContents(i);
        }
    }

    public void setStorageContents(Player p, ItemStack[] i){
        if(MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)){
            new GetStorageContents_Legacy(plugin).setStorageContents(p,i);
        }else{
            new GetStorageContents(plugin).setStorageContents(p,i);
        }
    }
}
