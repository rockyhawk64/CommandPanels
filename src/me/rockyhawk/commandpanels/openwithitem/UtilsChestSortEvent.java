package me.rockyhawk.commandpanels.openwithitem;

import de.jeff_media.chestsort.api.ChestSortEvent;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

public class UtilsChestSortEvent implements Listener {
    Context ctx;
    public UtilsChestSortEvent(Context pl) {
        this.ctx = pl;
    }
    @EventHandler
    public void onChestSortEvent(ChestSortEvent e){
        //if player is null it is not necessary
        if(e.getPlayer() == null){
            return;
        }
        //cancel if a panel is opened at all
        if(ctx.openPanels.hasPanelOpen(e.getPlayer().getName(), PanelPosition.Top)){
            e.setCancelled(true);
            return;
        }
        //hotbar item code below
        if(!ctx.plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //If the ChestSort plugin triggers an event
        try {
            if (e.getInventory().getType() == InventoryType.PLAYER) {
                for (String slot : ctx.hotbar.stationaryItems.get(e.getPlayer().getUniqueId()).list.keySet()) {
                    e.setUnmovable(Integer.parseInt(slot));
                }
            }
        }catch(NullPointerException ex){
            ctx.debug.send(ex, (Player) e.getPlayer(), ctx);
        }
    }
}
