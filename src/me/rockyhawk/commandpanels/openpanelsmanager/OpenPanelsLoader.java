package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public List<ConfigurationSection> openPanelsCF = new ArrayList<>(); //panel config section
    public List<String[]> openPanelsPN = new ArrayList<>(); //PLayer Name, Panel Name

    //this will skip certain panels from closing for players
    public List<String> skipPanels = new ArrayList<>(); //PLayer Name

    //this will return the panel CF based on the player, if it isn't there it returns null
    public ConfigurationSection getOpenPanel(String playerName){
        for(int i = 0; i < openPanelsCF.size(); i++){
            if(openPanelsPN.get(i)[0].equals(playerName)){
                return openPanelsCF.get(i);
            }
        }
        return null;
    }

    //this will return the panel CF based on the player, if it isn't there it returns null
    public String getOpenPanelName(String playerName){
        for(int i = 0; i < openPanelsCF.size(); i++){
            if(openPanelsPN.get(i)[0].equals(playerName)){
                return openPanelsPN.get(i)[1];
            }
        }
        return null;
    }

    //true if the player has a panel open
    public boolean hasPanelOpen(String playerName, String panelName){
        for(String[] temp : openPanelsPN){
            if(temp[0].equals(playerName) && temp[1].equals(panelName)){
                return true;
            }
        }
        return false;
    }

    //true if the player has a panel open
    public boolean hasPanelOpen(String playerName) {
        for (String[] temp : openPanelsPN) {
            if (temp[0].equals(playerName)) {
                return true;
            }
        }
        return false;
    }

    //tell loader that a panel has been opened
    public void openPanelForLoader(String playerName, String panelName, ConfigurationSection cf){
        //just to make sure there are no duplicates
        for(int i = 0; i < openPanelsCF.size(); i++){
            if(openPanelsPN.get(i)[0].equals(playerName) && !openPanelsPN.get(i)[1].equals(playerName)){
                openPanelsCF.remove(i);
                openPanelsPN.remove(i);
                i--;
            }
        }
        openPanelsCF.add(cf);
        openPanelsPN.add(new String[]{playerName,panelName});
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).trim().equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Opened " + panelName);
            }
        }
    }

    //close all of the panels for a player currently open
    public void closePanelsForLoader(String playerName){
        for(int i = 0; i < openPanelsPN.size(); i++){
            if(openPanelsPN.get(i)[0].equals(playerName)){
                panelCloseCommands(playerName,openPanelsPN.get(i)[1]);
                checkNBTItems(Bukkit.getPlayer(playerName));
                plugin.customCommand.removeCCP(openPanelsPN.get(i)[1], playerName);
                openPanelsCF.remove(i);
                openPanelsPN.remove(i);
                i--;
            }
        }
    }

    //tell loader that the panel is closed
    public void closePanelForLoader(String playerName, String panelName){
        for(int i = 0; i < openPanelsPN.size(); i++){
            if(Arrays.equals(openPanelsPN.get(i), new String[]{playerName, panelName})){
                panelCloseCommands(playerName,panelName);
                checkNBTItems(Bukkit.getPlayer(playerName));
                plugin.customCommand.removeCCP(panelName, playerName);
                openPanelsCF.remove(i);
                openPanelsPN.remove(i);
                break;
            }
        }
        if (plugin.config.contains("config.panel-snooper")) {
            if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).equalsIgnoreCase("true")) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + playerName + " Closed " + panelName);
            }
        }
    }

    public void panelCloseCommands(String playerName, String panelName){
        for(int i = 0; i < openPanelsCF.size(); i++){
            if(Arrays.equals(openPanelsPN.get(i), new String[]{playerName, panelName})){
                if (openPanelsCF.get(i).contains("commands-on-close")) {
                    //execute commands on panel close
                    try {
                        List<String> commands = openPanelsCF.get(i).getStringList("commands-on-close");
                        for (String command : commands) {
                            int val = plugin.commandTags.commandPayWall(Bukkit.getPlayer(openPanelsPN.get(i)[0]),command);
                            if(val == 0){
                                break;
                            }
                            if(val == 2){
                                plugin.commandTags.commandTags(Bukkit.getPlayer(openPanelsPN.get(i)[0]), plugin.papi(Bukkit.getPlayer(openPanelsPN.get(i)[0]),command), command);
                            }
                        }
                    }catch(Exception s){
                        plugin.debug(s);
                    }
                }
                return;
            }
        }
    }

    //ensure the player has not duplicated items
    public void checkNBTItems(Player p){
        try {
            for(ItemStack playerItem : p.getInventory().getContents()){
                //ensure the item is not a panel item
                try {
                    if (NBTEditor.getString(playerItem, "plugin").equalsIgnoreCase("CommandPanels")) {
                        p.getInventory().removeItem(playerItem);
                    }
                }catch(Exception ignore){}
            }
        }catch(Exception e){
            //oof
        }
    }
}
