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

    public ItemStack addNBT(ItemStack item){
        NamespacedKey key = new NamespacedKey(plugin, "CommandPanelsItem");
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
        item.setItemMeta(itemMeta);
        return item;
    }

    public boolean hasNBT(ItemStack item){
        NamespacedKey key = new NamespacedKey(plugin, "CommandPanelsItem");
        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }
}
