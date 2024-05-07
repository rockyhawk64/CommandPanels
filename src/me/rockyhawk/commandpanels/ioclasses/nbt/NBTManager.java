package me.rockyhawk.commandpanels.ioclasses.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
}