package me.rockyhawk.commandpanels.manager.session;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.events.PanelClosedEvent;
import me.rockyhawk.commandpanels.api.PanelInterface;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SessionHandler {
    Context ctx;
    public SessionHandler(Context pl) {
        this.ctx = pl;
    }

    /*
    This is used as A non-title reliant way to determine which panels are open for specific players
    The configuration section is opened directly
    into the correct panel, so there is no need for the panel name
    */
    public HashMap<String, PanelInterface> openPanels = new HashMap<>(); //player name and panel interface
    public HashSet<String> skipPanelClose = new HashSet<>(); //don't remove the player if they are in this list

    //this will return the panel CF based on the player, if it isn't there it returns null
    public Panel getOpenPanel(String playerName, PanelPosition position){
        for(Map.Entry<String, PanelInterface> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return entry.getValue().getPanel(position);
            }
        }
        return null;
    }

    //true if the player has the corresponding panel open in the location
    public boolean hasPanelOpen(String playerName, String panelName, PanelPosition position){
        for(Map.Entry<String, PanelInterface> entry : openPanels.entrySet()){
            try {
                if (entry.getKey().equals(playerName) && entry.getValue().getPanel(position).getName().equals(panelName)) {
                    return true;
                }
            }catch (NullPointerException ex){
                return false;
            }
        }
        return false;
    }

    //true if the player has a panel open
    public boolean hasPanelOpen(String playerName, PanelPosition position) {
        for(Map.Entry<String, PanelInterface> entry : openPanels.entrySet()){
            try {
                if(entry.getKey().equals(playerName) && entry.getValue().getPanel(position) != null){
                    return true;
                }
            }catch (NullPointerException ex){
                return false;
            }
        }
        return false;
    }

    //tell loader that a panel has been opened
    public void openPanelForLoader(String playerName, Panel panel, PanelPosition position){
        if(!openPanels.containsKey(playerName)){
            openPanels.put(playerName, new PanelInterface(playerName));
        }
        openPanels.get(playerName).setPanel(panel,position);
        openPanels.get(playerName).getPanel(position).isOpen = true;
        if(ctx.configHandler.isTrue("config.panel-snooper")) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Opened " + panel.getName() + " at " + position);
        }
    }

    //close all the panels for a player currently open
    public void closePanelForLoader(String playerName, PanelPosition position){
        //close if not panel
        if(!ctx.openPanels.openPanels.containsKey(playerName) || ctx.openPanels.skipPanelClose.contains(playerName)){
            return;
        }

        //snooper
        if(ctx.configHandler.isTrue("config.panel-snooper")) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Closed " + openPanels.get(playerName).getPanel(position).getName() + " at " + position);
        }

        //panel instance
        Panel panel = openPanels.get(playerName).getPanel(position);

        //run close commands once panel is closed
        panelCloseCommands(playerName,position,panel);

        //fire PanelClosedEvent
        PanelClosedEvent closedEvent = new PanelClosedEvent(Bukkit.getPlayer(playerName),panel,position);
        Bukkit.getPluginManager().callEvent(closedEvent);

        //Save the data file so changes are updated
        Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin, () -> {
            ctx.panelData.saveDataFile();
        });

        openPanels.get(playerName).setPanel(null,position);
        //remove if all panels closed or if top panel is closed
        if(openPanels.get(playerName).allClosed()){
            removePlayer(playerName);
        }else if(openPanels.get(playerName).getPanel(PanelPosition.Top) == null){
            removePlayer(playerName);
        }

        //fix up the inventory
        ctx.inventorySaver.restoreInventory(Bukkit.getPlayer(playerName),position);
    }

    //removes player from openPanels map
    public void removePlayer(String playerName){
        openPanels.get(playerName).setPanel(null,PanelPosition.Top);
        openPanels.get(playerName).setPanel(null,PanelPosition.Middle);
        openPanels.get(playerName).setPanel(null,PanelPosition.Bottom);
        openPanels.remove(playerName);
    }

    public void panelCloseCommands(String playerName,PanelPosition position, Panel panel){
        if (panel.getConfig().contains("commands-on-close")) {
            //execute commands on panel close
            try {
                ctx.commands.runCommands(panel,position,Bukkit.getPlayer(playerName),panel.getConfig().getStringList("commands-on-close"), null);
            }catch(Exception s){
                ctx.debug.send(s,null, ctx);
            }
        }
    }

    //if any CommandPanels items exist outside a panel, they will be removed
    public void deleteCommandPanelsItems(Player p){
        for(ItemStack itm : p.getInventory().getContents()){
            if(itm != null){
                if (ctx.nbt.hasNBT(itm, "CommandPanelsItem")) {
                    p.getInventory().remove(itm);
                }
            }
        }
    }
    //checks if an item is from cpanel or not
    public boolean isCommandPanelsItem(ItemStack itm){
        if(itm != null){
            return ctx.nbt.hasNBT(itm,"CommandPanelsItem");
        }
        return false;
    }
}
