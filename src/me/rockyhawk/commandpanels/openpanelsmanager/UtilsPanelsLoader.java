package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelClosedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

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
        for(ItemStack itm : p.getInventory().getContents()){
            if(itm != null){
                if (plugin.nbt.hasNBT(itm)) {
                    p.getInventory().remove(itm);
                }
            }
        }
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
                        plugin.openPanels.getOpenPanel(playerName,PanelPosition.Top).open(Bukkit.getPlayer(playerName), PanelPosition.Top);
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
    }

    @EventHandler
    public void onInventoryItemClick(InventoryClickEvent e){
        //this will check to ensure an item is not from CommandPanels on inventory open
        Player p = (Player)e.getWhoClicked();
        if(!plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top)){
            for(ItemStack itm : p.getInventory().getContents()){
                if(plugin.openPanels.isNBTInjected(itm)){
                    p.getInventory().remove(itm);
                }
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
