package me.rockyhawk.commandpanels.ioclasses.nbt;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import org.bukkit.inventory.ItemStack;

public class NBTManager {
    CommandPanels plugin;
    public NBTManager(CommandPanels pl) {
        this.plugin = pl;
    }

    //commandpanel item NBT
    public boolean hasNBT(ItemStack item){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().contains(item, "CommandPanelsItem");
        }else{
            return new NBT_1_14(plugin).hasNBT(item,"CommandPanelsItem");
        }
    }

    public ItemStack setNBT(ItemStack item){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().set(item,1,"CommandPanelsItem");
        }else{
            return new NBT_1_14(plugin).addNBT(item,"CommandPanelsItem","1");
        }
    }

    //custom key NBT
    public String getNBT(ItemStack item, String key){
        String output = "";
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            try{
                output = new NBT_1_13().getString(item, key);
            }catch(NullPointerException ignore){}
        }else{
            output = new NBT_1_14(plugin).getNBT(item, key);
        }
        return output;
    }

    public ItemStack setNBT(ItemStack item, String key, String value){
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_13)){
            return new NBT_1_13().set(item,value,key);
        }else{
            return new NBT_1_14(plugin).addNBT(item,key,value);
        }
    }
}
