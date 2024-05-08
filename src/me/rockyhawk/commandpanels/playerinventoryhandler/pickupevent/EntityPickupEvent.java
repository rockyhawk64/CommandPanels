package me.rockyhawk.commandpanels.playerinventoryhandler.pickupevent;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupEvent implements Listener {

    CommandPanels plugin;
    public EntityPickupEvent(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPickup(EntityPickupItemEvent e){
        if(e.getEntity() instanceof HumanEntity) {
            Player p = (Player)e.getEntity();
            //move the item into the players inventory instead of the panel
            if (plugin.openPanels.hasPanelOpen(p.getName(), PanelPosition.Middle) || plugin.openPanels.hasPanelOpen(p.getName(), PanelPosition.Bottom)) {
                plugin.inventorySaver.addItem(p,e.getItem().getItemStack());
                e.getItem().remove();
                e.setCancelled(true);
            }
        }
    }
}
