package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelClosedEvent;
import me.rockyhawk.commandpanels.ioclasses.NBTEditor;
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
        plugin.openPanels.closePanelForLoader(e.getPlayer().getName());
        Player p = e.getPlayer();
        p.updateInventory();
        for(ItemStack itm : p.getInventory().getContents()){
            if(itm != null){
                if (NBTEditor.contains(itm, "CommandPanels")) {
                    p.getInventory().remove(itm);
                }
            }
        }
    }

    //tell panel loader that player has closed the panel (there is also one of these in EditorUtils)
    @EventHandler
    public void onPlayerClosePanel(InventoryCloseEvent e){
        //only do this if editor is disabled as it will disabled this code
        if(!Objects.requireNonNull(plugin.config.getString("config.ingame-editor")).equalsIgnoreCase("true")) {
            //this is put here to avoid conflicts, close panel if it is closed
            plugin.openPanels.closePanelForLoader(e.getPlayer().getName());
        }
    }

    @EventHandler
    public void onInventoryItemClick(InventoryClickEvent e){
        //this will check to ensure an item is not from CommandPanels on inventory open
        Player p = (Player)e.getWhoClicked();
        if(!plugin.openPanels.hasPanelOpen(p.getName())){
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
            if (plugin.openPanels.hasPanelOpen(e.getPlayer().getName())) {
                Panel closedPanel = plugin.openPanels.getOpenPanel(e.getPlayer().getName());

                //manually remove player with no skip checks
                plugin.openPanels.openPanels.remove(e.getPlayer().getName());

                //fire PanelClosedEvent
                PanelClosedEvent closedEvent = new PanelClosedEvent(Bukkit.getPlayer(e.getPlayer().getName()),closedPanel);
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
