package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
        plugin.openPanels.closePanelsForLoader(e.getPlayer().getName());
    }

    //tell panel loader that player has closed the panel (there is also one of these in EditorUtils)
    @EventHandler
    public void onPlayerClosePanel(InventoryCloseEvent e){
        //only do this if editor is disabled as it will disabled this code
        if(!Objects.requireNonNull(plugin.config.getString("config.ingame-editor")).equalsIgnoreCase("true")) {
            //this is put here to avoid conflicts, close panel if it is closed
            if(plugin.openPanels.skipPanels.contains(e.getPlayer().getName())){
                plugin.openPanels.skipPanels.remove(e.getPlayer().getName());
                return;
            }
            //loop panels
            for (int i = 0; i < plugin.openPanels.openPanelsPN.size(); i++) {
                if (plugin.openPanels.openPanelsPN.get(i)[0].equals(e.getPlayer().getName())) {
                    plugin.openPanels.closePanelForLoader(e.getPlayer().getName(),plugin.openPanels.openPanelsPN.get(i)[1]);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryClickEvent e){
        //this will check to ensure an item is not from CommandPanels on inventory open
        try {
            plugin.openPanels.checkNBTItems((Player) e.getWhoClicked());
        }catch(Exception ex){
            plugin.debug(ex);
        }
    }
}
