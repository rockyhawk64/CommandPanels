package me.rockyhawk.commandpanels.openwithitem;

import de.jeff_media.chestsort.api.ChestSortEvent;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
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
        //if player is null it is not necessary
        if(e.getPlayer() == null){
            return;
        }
        //cancel if a panel is opened at all
        if(plugin.openPanels.hasPanelOpen(e.getPlayer().getName(), PanelPosition.Top)){
            e.setCancelled(true);
            return;
        }
        //hotbar item code below
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //If the ChestSort plugin triggers an event
        try {
            if (e.getInventory().getType() == InventoryType.PLAYER) {
                for (int slot : plugin.hotbar.stationaryItems.get(e.getPlayer().getUniqueId()).list.keySet()) {
                    e.setUnmovable(slot);
                }
            }
        }catch(NullPointerException ex){
            plugin.debug(ex, (Player) e.getPlayer());
        }
    }
}
