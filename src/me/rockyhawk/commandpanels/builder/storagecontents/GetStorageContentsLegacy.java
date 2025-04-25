package me.rockyhawk.commandpanels.builder.storagecontents;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

//get contents legacy
public class GetStorageContentsLegacy {
    public ItemStack[] getStorageContents(Inventory i){
        return i.getContents();
    }

    public void setStorageContents(Player p, ItemStack[] i){
        p.getOpenInventory().getTopInventory().setContents(i);
    }
}