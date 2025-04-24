package me.rockyhawk.commandpanels.manager.session;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.events.PanelClosedEvent;
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

public class SessionUtils implements Listener {
    Context ctx;
    public SessionUtils(Context pl) {
        this.ctx = pl;
    }

    //tell panel loader that player has opened panel
    @EventHandler
    public void onPlayerClosePanel(PlayerQuitEvent e){
        ctx.openPanels.closePanelForLoader(e.getPlayer().getName(), PanelPosition.Top);
        Player p = e.getPlayer();
        p.updateInventory();
        ctx.openPanels.deleteCommandPanelsItems(p);
    }

    //tell panel loader that player has closed the panel (there is also one of these in EditorUtils)
    @EventHandler
    public void onPlayerClosePanel(InventoryCloseEvent e){
        String playerName = e.getPlayer().getName();

        //close if not panel
        if(!ctx.openPanels.openPanels.containsKey(playerName) || ctx.openPanels.skipPanelClose.contains(playerName)){
            return;
        }

        //check for panelType unclosable (unclosable is Top only)
        if(ctx.openPanels.getOpenPanel(playerName,PanelPosition.Top).getConfig().contains("panelType")){
            if(ctx.openPanels.getOpenPanel(playerName,PanelPosition.Top).getConfig().getStringList("panelType").contains("unclosable")){
                ctx.plugin.getServer().getScheduler().scheduleSyncDelayedTask(ctx.plugin, new Runnable() {
                    public void run() {
                        //end the old panel session and copy a new one
                        if(ctx.openPanels.getOpenPanel(playerName,PanelPosition.Top) == null){
                            return;
                        }
                        ctx.openPanels.getOpenPanel(playerName,PanelPosition.Top).isOpen = false;
                        Panel reopenedPanel = ctx.openPanels.getOpenPanel(playerName,PanelPosition.Top).copy();
                        //re-add placeholders as they are not transferred in the Panel object
                        reopenedPanel.placeholders.keys = ctx.openPanels.getOpenPanel(playerName,PanelPosition.Top).placeholders.keys;
                        reopenedPanel.open(Bukkit.getPlayer(playerName), PanelPosition.Top);
                    }
                });
                return;
            }
        }

        //run commands-on-close for panels
        if(ctx.openPanels.hasPanelOpen(e.getPlayer().getName(),PanelPosition.Bottom)){
            ctx.openPanels.panelCloseCommands(playerName,PanelPosition.Bottom, ctx.openPanels.getOpenPanel(playerName,PanelPosition.Bottom));
        }
        if(ctx.openPanels.hasPanelOpen(e.getPlayer().getName(),PanelPosition.Middle)){
            ctx.openPanels.panelCloseCommands(playerName,PanelPosition.Middle, ctx.openPanels.getOpenPanel(playerName,PanelPosition.Middle));
        }

        //close panels and run commands for Top panel
        ctx.openPanels.closePanelForLoader(e.getPlayer().getName(),PanelPosition.Top);
    }

    @EventHandler
    public void onInventoryItemClick(InventoryClickEvent e){
        //this will check to ensure an item is not from CommandPanels on inventory open
        Player p = (Player)e.getWhoClicked();
        if(!ctx.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top)){
            if(e.getCurrentItem() == null){return;}
            if(e.getCurrentItem().getType() == Material.AIR){return;}
            if(ctx.openPanels.isCommandPanelsItem(e.getCurrentItem())){
                p.getInventory().remove(e.getCurrentItem());
            }
        }
    }

    //if the regular InventoryOpenEvent is called
    @EventHandler(priority = EventPriority.HIGHEST)
    public void vanillaOpenedEvent(InventoryOpenEvent e){
        if(e.isCancelled()) {
            if (ctx.openPanels.hasPanelOpen(e.getPlayer().getName(),PanelPosition.Top)) {
                Panel closedPanel = ctx.openPanels.getOpenPanel(e.getPlayer().getName(),PanelPosition.Top);

                //manually remove player with no skip checks
                ctx.openPanels.removePlayer(e.getPlayer().getName());

                //fire PanelClosedEvent
                PanelClosedEvent closedEvent = new PanelClosedEvent(Bukkit.getPlayer(e.getPlayer().getName()),closedPanel, PanelPosition.Top);
                Bukkit.getPluginManager().callEvent(closedEvent);

                //do message
                if(ctx.configHandler.isTrue("config.panel-snooper")) {
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + e.getPlayer().getName() + "'s Panel was Force Closed");
                }
            }
        }
    }
}
