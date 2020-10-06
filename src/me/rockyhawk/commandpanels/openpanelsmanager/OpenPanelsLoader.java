package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public boolean hasPanelOpen(String playerName){
        for(String[] temp : openPanelsPN){
            if(temp[0].equals(playerName)){
                return true;
            }
        }
        return false;
    }

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
    }

    public void closePanelForLoader(String playerName, String panelName){
        for(int i = 0; i < openPanelsCF.size(); i++){
            if(Arrays.equals(openPanelsPN.get(i), new String[]{playerName, panelName})){
                openPanelsCF.remove(i);
                openPanelsPN.remove(i);
                return;
            }
        }
    }
}
