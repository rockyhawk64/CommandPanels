package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelClosedEvent;
import me.rockyhawk.commandpanels.api.PanelsInterface;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class OpenPanelsLoader {
    CommandPanels plugin;
    public OpenPanelsLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    /*
    This is used as a less laggy and non title reliant way to determine which panels are open for specific players
    The configuration section is opened directly
    into the correct panel, so there is no need for the panel name
    */
    public HashMap<String, PanelsInterface> openPanels = new HashMap<>(); //player name and panel interface
    public HashSet<String> skipPanelClose = new HashSet<>(); //don't remove the player if they are in this list

    //this will return the panel CF based on the player, if it isn't there it returns null
    public Panel getOpenPanel(String playerName, PanelPosition position){
        for(Map.Entry<String, PanelsInterface> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return entry.getValue().getPanel(position);
            }
        }
        return null;
    }

    //true if the player has the corresponding panel open in the location
    public boolean hasPanelOpen(String playerName, String panelName, PanelPosition position){
        for(Map.Entry<String, PanelsInterface> entry : openPanels.entrySet()){
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
        for(Map.Entry<String, PanelsInterface> entry : openPanels.entrySet()){
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
            openPanels.put(playerName, new PanelsInterface(playerName));
        }
        openPanels.get(playerName).setPanel(panel,position);
        openPanels.get(playerName).getPanel(position).isOpen = true;
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).trim().equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Opened " + panel.getName() + " at " + position);
            }
        }
    }

    //close all of the panels for a player currently open
    public void closePanelForLoader(String playerName, PanelPosition position){
        //close if not panel
        if(!plugin.openPanels.openPanels.containsKey(playerName) || plugin.openPanels.skipPanelClose.contains(playerName)){
            return;
        }

        //snooper
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Closed " + openPanels.get(playerName).getPanel(position).getName() + " at " + position);
            }
        }

        //panel instance
        Panel panel = openPanels.get(playerName).getPanel(position);

        //run close commands once panel is closed
        panelCloseCommands(playerName,position,panel);

        //fire PanelClosedEvent
        PanelClosedEvent closedEvent = new PanelClosedEvent(Bukkit.getPlayer(playerName),panel,position);
        Bukkit.getPluginManager().callEvent(closedEvent);

        openPanels.get(playerName).setPanel(null,position);
        //remove if all panels closed or if top panel is closed
        if(openPanels.get(playerName).allClosed()){
            removePlayer(playerName);
        }else if(openPanels.get(playerName).getPanel(PanelPosition.Top) == null){
            removePlayer(playerName);
        }

        //fix up the inventory
        plugin.inventorySaver.restoreInventory(Bukkit.getPlayer(playerName),position);
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
                plugin.commandTags.runCommands(panel,position,Bukkit.getPlayer(playerName),panel.getConfig().getStringList("commands-on-close"));
            }catch(Exception s){
                plugin.debug(s,null);
            }
        }
    }

    public boolean isNBTInjected(ItemStack itm){
        if(itm != null){
            return plugin.nbt.hasNBT(itm);
        }
        return false;
    }
}
