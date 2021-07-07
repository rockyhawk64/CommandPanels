package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelClosedEvent;
import me.rockyhawk.commandpanels.ioclasses.nbt.NBT_1_13;
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
    public HashMap<String, Panel> openPanels = new HashMap<>(); //player name and panel
    public HashSet<String> skipPanelClose = new HashSet<>(); //don't remove the player if they are in this list

    //this will return the panel CF based on the player, if it isn't there it returns null
    public Panel getOpenPanel(String playerName){
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return entry.getValue();
            }
        }
        return null;
    }

    //this will return the panel CF based on the player, if it isn't there it returns null
    public String getOpenPanelName(String playerName){
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return entry.getValue().getName();
            }
        }
        return null;
    }

    //true if the player has a panel open
    public boolean hasPanelOpen(String playerName, String panelName){
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName) && entry.getValue().getName().equals(panelName)){
                return true;
            }
        }
        return false;
    }

    //true if the player has a panel open
    public boolean hasPanelOpen(String playerName) {
        for(Map.Entry<String, Panel> entry : openPanels.entrySet()){
            if(entry.getKey().equals(playerName)){
                return true;
            }
        }
        return false;
    }

    //tell loader that a panel has been opened
    public void openPanelForLoader(String playerName, Panel panel){
        openPanels.put(playerName, panel);
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).trim().equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Opened " + panel.getName());
            }
        }
    }

    //close all of the panels for a player currently open
    public void closePanelForLoader(String playerName){
        if(!openPanels.containsKey(playerName) || skipPanelClose.contains(playerName)){
            return;
        }
        panelCloseCommands(playerName,openPanels.get(playerName));
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Closed " + openPanels.get(playerName).getName());
            }
        }

        //fire PanelClosedEvent
        PanelClosedEvent closedEvent = new PanelClosedEvent(Bukkit.getPlayer(playerName),openPanels.get(playerName));
        Bukkit.getPluginManager().callEvent(closedEvent);

        openPanels.remove(playerName);
    }

    public void panelCloseCommands(String playerName, Panel panel){
        if (panel.getConfig().contains("commands-on-close")) {
            //execute commands on panel close
            try {
                List<String> commands = panel.getConfig().getStringList("commands-on-close");
                for (String command : commands) {
                    int val = plugin.commandTags.commandPayWall(Bukkit.getPlayer(playerName),command);
                    if(val == 0){
                        break;
                    }
                    if(val == 2){
                        plugin.commandTags.runCommand(panel,Bukkit.getPlayer(playerName), command);
                    }
                }
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
