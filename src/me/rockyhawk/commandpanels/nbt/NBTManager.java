package me.rockyhawk.commandpanels.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rockyhawk.commandpanels.CommandPanels;
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

    public ItemStack setData(ItemStack item, String key, String value){
        NBT.modify(item, nbt -> {
            nbt.setString(key, value);
        });
        return item;
    }

    public boolean hasData(ItemStack item, String key){
        NBTItem nbti = new NBTItem(item);
        return nbti.hasTag(key);
    }

    public String getData(ItemStack item, String key){
        NBTItem nbti = new NBTItem(item);
        if(!nbti.hasNBTData()) return "";
        //nbti.getOrDefault(key, "");
        return nbti.getOrDefault(key, "");
    }
}