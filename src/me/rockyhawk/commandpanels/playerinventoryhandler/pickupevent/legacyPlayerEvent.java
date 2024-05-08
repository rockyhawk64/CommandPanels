package me.rockyhawk.commandpanels.playerinventoryhandler.pickupevent;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class legacyPlayerEvent implements Listener {

    CommandPanels plugin;
    public legacyPlayerEvent(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
            Player p = e.getPlayer();
            //move the item into the players inventory instead of the panel
            if (plugin.openPanels.hasPanelOpen(p.getName(), PanelPosition.Middle) || plugin.openPanels.hasPanelOpen(p.getName(), PanelPosition.Bottom)) {
                plugin.inventorySaver.addItem(p,e.getItem().getItemStack());
                e.getItem().remove();
                e.setCancelled(true);
            }
    }
}
