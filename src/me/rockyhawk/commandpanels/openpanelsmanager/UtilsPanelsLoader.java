package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UtilsPanelsLoader implements Listener {
    CommandPanels plugin;
    public UtilsPanelsLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onPlayerClosePanel(InventoryCloseEvent e){
        for(int i = 0; i < plugin.openPanels.openPanelsPN.size(); i++){
            if(plugin.openPanels.openPanelsPN.get(i)[0].equals(e.getPlayer().getName())){
                plugin.openPanels.openPanelsPN.remove(i);
                plugin.openPanels.openPanelsCF.remove(i);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerClosePanel(PlayerQuitEvent e){
        for(int i = 0; i < plugin.openPanels.openPanelsPN.size(); i++){
            if(plugin.openPanels.openPanelsPN.get(i)[0].equals(e.getPlayer().getName())){
                plugin.openPanels.openPanelsPN.remove(i);
                plugin.openPanels.openPanelsCF.remove(i);
                return;
            }
        }
    }
}
