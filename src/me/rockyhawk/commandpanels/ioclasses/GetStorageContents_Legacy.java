package me.rockyhawk.commandpanels.ioclasses;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

//get contents legacy
public class GetStorageContents_Legacy {
    CommandPanels plugin;
    public GetStorageContents_Legacy(CommandPanels pl) {
        this.plugin = pl;
    }

    public ItemStack[] getStorageContents(Inventory i){
        return i.getContents();
    }

    public void setStorageContents(Player p, ItemStack[] i){
        p.getOpenInventory().getTopInventory().setContents(i);
    }
}
