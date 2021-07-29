package me.rockyhawk.commandpanels.classresources.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateText {
    CommandPanels plugin;
    public CreateText(CommandPanels pl) {
        this.plugin = pl;
    }

    //CommandPanels send message function with all placeholders
    public void sendMessage(Panel panel,PanelPosition position, Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(placeholders(panel,position, p,plugin.tag + message));
        }
    }

    //CommandPanels send message function
    public void sendMessage(Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(colour(plugin.tag + message));
        }
    }

    //CommandPanels send message function without the tag
    public void sendString(Panel panel,PanelPosition position, Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(placeholders(panel,position, p,message));
        }
    }

    //CommandPanels send message function without the tag
    public void sendString(Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(colour(message));
        }
    }

    //papi except if it is a String List
    public List<String> placeholdersNoColour(Panel panel,PanelPosition position, Player p, List<String> setpapi) {
        try {
            int tempInt = 0;
            for (String temp : setpapi) {
                setpapi.set(tempInt, attachPlaceholders(panel,position, p, temp));
                tempInt += 1;
            }
        }catch(Exception ignore){
            //this will be ignored as it is probably a null
            return null;
        }
        return setpapi;
    }

    //papi except if it is a String List
    public List<String> placeholdersList(Panel panel,PanelPosition position, Player p, List<String> setpapi, boolean placeholder) {
        try {
            if(placeholder) {
                int tempInt = 0;
                for (String temp : setpapi) {
                    setpapi.set(tempInt, attachPlaceholders(panel,position, p, temp));
                    tempInt += 1;
                }
            }
        }catch(Exception ignore){}
        int tempInt = 0;
        //change colour
        for(String temp : setpapi){
            try {
                setpapi.set(tempInt, plugin.hex.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', temp)));
            }catch(NullPointerException ignore){}
            tempInt += 1;
        }
        return setpapi;
    }

    //regular string papi, but only colours so Player doesn't need to be there
    public String colour(String setpapi) {
        try {
            setpapi = plugin.hex.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    //string papi with no colours
    public String placeholdersNoColour(Panel panel, PanelPosition position, Player p, String setpapi) {
        try {
            setpapi = attachPlaceholders(panel,position, p,setpapi);
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    //regular string papi
    public String placeholders(Panel panel, PanelPosition position, Player p, String setpapi) {
        try {
            setpapi = attachPlaceholders(panel,position, p,setpapi);
            setpapi = plugin.hex.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    public String attachPlaceholders(Panel panel, PanelPosition position, Player p, String input){
        //do all the placeholders in order to fill into text
        input = plugin.placeholders.setPlaceholders(panel,position, p, input, false);
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            OfflinePlayer offp = plugin.getServer().getOfflinePlayer(p.getUniqueId());
            input = PlaceholderAPI.setPlaceholders(offp, input);
        }
        input = plugin.placeholders.setPlaceholders(panel,position, p, input, true);
        return input;
    }
}
