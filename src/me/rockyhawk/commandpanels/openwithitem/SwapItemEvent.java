package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SwapItemEvent implements Listener {
    CommandPanels plugin;
    public SwapItemEvent(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = e.getPlayer();
        try {
            if (plugin.hotbar.itemCheckExecute(e.getOffHandItem(), p, false, true)) {
                e.setCancelled(true);
                p.updateInventory();
            }
        }catch(Exception ignore){}
    }
}
