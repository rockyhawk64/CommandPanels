package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class UtilsPanelsLoader implements Listener {
    CommandPanels plugin;
    public UtilsPanelsLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    //tell panel loader that player has opened panel
    @EventHandler
    public void onPlayerClosePanel(PlayerQuitEvent e){
        for(int i = 0; i < plugin.openPanels.openPanelsPN.size(); i++){
            if(plugin.openPanels.openPanelsPN.get(i)[0].equals(e.getPlayer().getName())){
                plugin.customCommand.removeCCP(plugin.openPanels.openPanelsPN.get(i)[1], e.getPlayer().getName());
                plugin.openPanels.openPanelsPN.remove(i);
                plugin.openPanels.openPanelsCF.remove(i);
                return;
            }
        }
    }

    //tell panel loader that player has closed the panel
    @EventHandler
    public void onPlayerClosePanel(InventoryCloseEvent e){
        //only do this if editor is disabled as it will disabled this code
        if(!Objects.requireNonNull(plugin.config.getString("config.ingame-editor")).equalsIgnoreCase("true")) {
            //this is put here to avoid conflicts, close panel if it is closed
            for (int i = 0; i < plugin.openPanels.openPanelsPN.size(); i++) {
                if (plugin.openPanels.openPanelsPN.get(i)[0].equals(e.getPlayer().getName())) {
                    plugin.customCommand.removeCCP(plugin.openPanels.openPanelsPN.get(i)[1], e.getPlayer().getName());
                    plugin.openPanels.openPanelsPN.remove(i);
                    plugin.openPanels.openPanelsCF.remove(i);
                    return;
                }
            }
        }
    }
}
