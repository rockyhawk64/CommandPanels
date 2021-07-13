package me.rockyhawk.commandpanels.ioclasses.nbt;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NBT_1_14 {
    CommandPanels plugin;
    public NBT_1_14(CommandPanels pl) {
        this.plugin = pl;
    }
    //NBT class for Minecraft versions 1.14+

    public ItemStack addNBT(ItemStack item, String key, String value){
        NamespacedKey ns_key = new NamespacedKey(plugin, key);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(ns_key, PersistentDataType.STRING, value);
        item.setItemMeta(itemMeta);
        return item;
    }

    public boolean hasNBT(ItemStack item, String key){
        NamespacedKey ns_key = new NamespacedKey(plugin, key);
        try {
            ItemMeta itemMeta = item.getItemMeta();
            return itemMeta.getPersistentDataContainer().has(ns_key, PersistentDataType.STRING);
        }catch (Exception e){
            return false;
        }
    }

    public String getNBT(ItemStack item, String key){
        NamespacedKey ns_key = new NamespacedKey(plugin, key);
        try {
            ItemMeta itemMeta = item.getItemMeta();
            return itemMeta.getPersistentDataContainer().get(ns_key, PersistentDataType.STRING);
        }catch (Exception e){
            return "";
        }
    }
}
