package me.rockyhawk.commandpanels.ioclasses.nbt;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.inventory.ItemStack;

public class NBTManager {
    CommandPanels plugin;
    public NBTManager(CommandPanels pl) {
        this.plugin = pl;
    }

    //commandpanel item NBT
    public boolean hasNBT(ItemStack item){
        NBTItem nbti = new NBTItem(item);
        return nbti.hasTag("CommandPanelsItem");
    }

    public ItemStack setNBT(ItemStack item){
        NBT.modify(item, nbt -> {
            nbt.setString("CommandPanelsItem", "1");
        });
        return item;
    }

    //custom key NBT
    public String getNBT(ItemStack item, String key){
        NBTItem nbti = new NBTItem(item);
        if(!nbti.hasNBTData()) return "";
        return nbti.getString(key);
    }

    public ItemStack setNBT(ItemStack item, String key, String value){
        NBT.modify(item, nbt -> {
            nbt.setString(key, value);
        });
        return item;
    }
}
