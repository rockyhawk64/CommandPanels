package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelClosedEvent;
import me.rockyhawk.commandpanels.classresources.customheads.SavedCustomHead;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class UtilsPanelsLoader implements Listener {
    CommandPanels plugin;
    public UtilsPanelsLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    //tell panel loader that player has opened panel
    @EventHandler
    public void onPlayerClosePanel(PlayerQuitEvent e){
        plugin.openPanels.closePanelForLoader(e.getPlayer().getName(),PanelPosition.Top);
        Player p = e.getPlayer();
        p.updateInventory();
        plugin.openPanels.deleteCommandPanelsItems(p);
    }

    //tell panel loader that player has closed the panel (there is also one of these in EditorUtils)
    @EventHandler
    public void onPlayerClosePanel(InventoryCloseEvent e){
        String playerName = e.getPlayer().getName();

        //close if not panel
        if(!plugin.openPanels.openPanels.containsKey(playerName) || plugin.openPanels.skipPanelClose.contains(playerName)){
            return;
        }

        //check for panelType unclosable (unclosable is Top only)
        if(plugin.openPanels.getOpenPanel(playerName,PanelPosition.Top).getConfig().contains("panelType")){
            if(plugin.openPanels.getOpenPanel(playerName,PanelPosition.Top).getConfig().getStringList("panelType").contains("unclosable")){
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        //end the old panel session and copy a new one
                        if(plugin.openPanels.getOpenPanel(playerName,PanelPosition.Top) == null){
                            return;
                        }
                        plugin.openPanels.getOpenPanel(playerName,PanelPosition.Top).isOpen = false;
                        Panel reopenedPanel = plugin.openPanels.getOpenPanel(playerName,PanelPosition.Top).copy();
                        //re-add placeholders as they are not transferred in the Panel object
                        reopenedPanel.placeholders.keys = plugin.openPanels.getOpenPanel(playerName,PanelPosition.Top).placeholders.keys;
                        reopenedPanel.open(Bukkit.getPlayer(playerName), PanelPosition.Top);
                    }
                });
                return;
            }
        }

        //run commands-on-close for panels
        if(plugin.openPanels.hasPanelOpen(e.getPlayer().getName(),PanelPosition.Bottom)){
            plugin.openPanels.panelCloseCommands(playerName,PanelPosition.Bottom,plugin.openPanels.getOpenPanel(playerName,PanelPosition.Bottom));
        }
        if(plugin.openPanels.hasPanelOpen(e.getPlayer().getName(),PanelPosition.Middle)){
            plugin.openPanels.panelCloseCommands(playerName,PanelPosition.Middle,plugin.openPanels.getOpenPanel(playerName,PanelPosition.Middle));
        }

        //close panels and run commands for Top panel
        plugin.openPanels.closePanelForLoader(e.getPlayer().getName(),PanelPosition.Top);

        //clear cached textures list until length limit is reached
        Iterator<Map.Entry<String, SavedCustomHead>> iterator = plugin.customHeads.savedCustomHeads.entrySet().iterator();
        while (plugin.customHeads.savedCustomHeads.size() > 2000 && iterator.hasNext()) {
            iterator.next(); // Move to next entry
            iterator.remove(); // Remove the entry
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryClickEvent e){
        //this will check to ensure an item is not from CommandPanels on inventory open
        Player p = (Player)e.getWhoClicked();
        if(!plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top)){
            if(e.getCurrentItem() == null){return;}
            if(e.getCurrentItem().getType() == Material.AIR){return;}
            if(plugin.openPanels.isCommandPanelsItem(e.getCurrentItem())){
                p.getInventory().remove(e.getCurrentItem());
            }
        }
    }

    //if the regular InventoryOpenEvent is called
    @EventHandler(priority = EventPriority.HIGHEST)
    public void vanillaOpenedEvent(InventoryOpenEvent e){
        if(e.isCancelled()) {
            if (plugin.openPanels.hasPanelOpen(e.getPlayer().getName(),PanelPosition.Top)) {
                Panel closedPanel = plugin.openPanels.getOpenPanel(e.getPlayer().getName(),PanelPosition.Top);

                //manually remove player with no skip checks
                plugin.openPanels.removePlayer(e.getPlayer().getName());

                //fire PanelClosedEvent
                PanelClosedEvent closedEvent = new PanelClosedEvent(Bukkit.getPlayer(e.getPlayer().getName()),closedPanel, PanelPosition.Top);
                Bukkit.getPluginManager().callEvent(closedEvent);

                //do message
                if (plugin.config.contains("config.panel-snooper")) {
                    if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).equalsIgnoreCase("true")) {
                        Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + e.getPlayer().getName() + "'s Panel was Force Closed");
                    }
                }
            }
        }
    }
}
