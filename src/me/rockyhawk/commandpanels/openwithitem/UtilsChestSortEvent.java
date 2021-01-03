package me.rockyhawk.commandpanels.openwithitem;

import de.jeff_media.ChestSortAPI.ChestSortEvent;
import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

public class UtilsChestSortEvent implements Listener {
    CommandPanels plugin;
    public UtilsChestSortEvent(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onChestSortEvent(ChestSortEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //If the ChestSort plugin triggers an event
        plugin.getServer().getConsoleSender().sendMessage(e.getInventory().getType().toString());
        if(e.getInventory().getType() == InventoryType.PLAYER){
            for(int slot : plugin.hotbar.getStationaryItemSlots()){
                e.setUnmovable(slot);
            }
        }
    }
}
