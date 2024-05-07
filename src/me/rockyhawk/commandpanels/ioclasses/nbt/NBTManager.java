package me.rockyhawk.commandpanels.ioclasses.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class NBTManager {
    CommandPanels plugin;
    public NBTManager(CommandPanels pl) {
        this.plugin = pl;
    }

    public boolean hasSameNBT(ItemStack one, ItemStack two){
        NBTItem nbtitem1 = new NBTItem(one);
        NBTItem nbtitem2 = new NBTItem(two);

        return nbtitem1.equals(nbtitem2);
    }

    public ItemStack setNBT(ItemStack item, String key, String value){
        NBT.modify(item, nbt -> {
            nbt.setString(key, value);
        });
        return item;
    }

    public boolean hasNBT(ItemStack item, String key){
        NBTItem nbti = new NBTItem(item);
        return nbti.hasTag(key);
    }

    public String getNBT(ItemStack item, String key){
        NBTItem nbti = new NBTItem(item);
        if(!nbti.hasNBTData()) return "";
        return nbti.getString(key);
    }

    public void applyNBTRecursively(String path, ConfigurationSection section, ItemStack s, Player p, Panel panel, PanelPosition position) {
        for (String key : section.getKeys(true)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;
            if (section.isConfigurationSection(key)) {
                // Recursive call for nested sections
                applyNBTRecursively(fullPath, section.getConfigurationSection(key), s, p, panel, position);
            } else {
                // Set the NBT tag at this level
                String value = plugin.tex.attachPlaceholders(panel, position, p, section.getString(key));
                s = plugin.nbt.setNBT(s, fullPath, value);
            }
        }
    }
}