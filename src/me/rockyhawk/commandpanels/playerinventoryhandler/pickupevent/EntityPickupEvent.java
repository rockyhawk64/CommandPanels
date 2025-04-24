package me.rockyhawk.commandpanels.playerinventoryhandler.pickupevent;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupEvent implements Listener {

    Context ctx;
    public EntityPickupEvent(Context pl) {
        this.ctx = pl;
    }
    @EventHandler
    public void onPickup(EntityPickupItemEvent e){
        if(e.getEntity() instanceof HumanEntity) {
            Player p = (Player)e.getEntity();
            //move the item into the players inventory instead of the panel
            if (ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Middle) || ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Bottom)) {
                ctx.inventorySaver.addItem(p,e.getItem().getItemStack());
                e.getItem().remove();
                e.setCancelled(true);
            }
        }
    }
}
