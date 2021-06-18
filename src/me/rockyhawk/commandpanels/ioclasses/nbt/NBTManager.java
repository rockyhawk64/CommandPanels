package me.rockyhawk.commandpanels.ioclasses.nbt;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import org.bukkit.inventory.ItemStack;

public class NBTManager {
    CommandPanels plugin;
    public NBTManager(CommandPanels pl) {
        this.plugin = pl;
    }

    public boolean hasNBT(ItemStack item){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().contains(item, "CommandPanelsItem");
        }else{
            return new NBT_1_14(plugin).hasNBT(item);
        }
    }

    public ItemStack setNBT(ItemStack item){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().set(item,1,"CommandPanelsItem");
        }else{
            return new NBT_1_14(plugin).addNBT(item);
        }
    }
}
