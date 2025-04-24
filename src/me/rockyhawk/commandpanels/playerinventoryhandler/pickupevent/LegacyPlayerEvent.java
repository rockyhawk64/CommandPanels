package me.rockyhawk.commandpanels.playerinventoryhandler.pickupevent;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class LegacyPlayerEvent implements Listener {

    Context ctx;
    public LegacyPlayerEvent(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
            Player p = e.getPlayer();
            //move the item into the players inventory instead of the panel
            if (ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Middle) || ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Bottom)) {
                ctx.inventorySaver.addItem(p,e.getItem().getItemStack());
                e.getItem().remove();
                e.setCancelled(true);
            }
    }
}
