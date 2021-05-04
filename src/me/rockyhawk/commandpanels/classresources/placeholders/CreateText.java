package me.rockyhawk.commandpanels.classresources.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateText {
    CommandPanels plugin;
    public CreateText(CommandPanels pl) {
        this.plugin = pl;
    }

    //CommandPanels send message function
    public void sendMessage(Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(papi(plugin.tag + message));
        }
    }

    //CommandPanels send message function without the tag
    public void sendString(Player p, String message){
        if(!message.equals("")) {
            p.sendMessage(papi(message));
        }
    }

    //papi except if it is a String List
    public List<String> papiNoColour(Panel panel, Player p, List<String> setpapi) {
        try {
            int tempInt = 0;
            for (String temp : setpapi) {
                setpapi.set(tempInt, plugin.placeholders.setCpPlaceholders(panel, p, temp));
                tempInt += 1;
            }
            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                OfflinePlayer offp = plugin.getServer().getOfflinePlayer(p.getUniqueId());
                setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
            }
        }catch(Exception ignore){
            //this will be ignored as it is probably a null
            return null;
        }
        return setpapi;
    }

    //papi except if it is a String List
    public List<String> papi(Panel panel, Player p, List<String> setpapi, boolean placeholder) {
        try {
            if(placeholder) {
                int tempInt = 0;
                for (String temp : setpapi) {
                    setpapi.set(tempInt, plugin.placeholders.setCpPlaceholders(panel, p, temp));
                    tempInt += 1;
                }
                if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    OfflinePlayer offp = plugin.getServer().getOfflinePlayer(p.getUniqueId());
                    setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
                }
            }
        }catch(Exception ignore){
            //this will be ignored as it is probably a null
            return null;
        }
        int tempInt = 0;
        //change colour
        for(String temp : setpapi){
            try {
                setpapi.set(tempInt, plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', temp)));
            }catch(NullPointerException ignore){
            }
            tempInt += 1;
        }
        return setpapi;
    }

    //regular string papi, but only colours so Player doesn't need to be there
    public String papi(String setpapi) {
        try {
            setpapi = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    //string papi with no colours
    public String papiNoColour(Panel panel, Player p, String setpapi) {
        try {
            setpapi = plugin.placeholders.setCpPlaceholders(panel, p,setpapi);
            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                OfflinePlayer offp = plugin.getServer().getOfflinePlayer(p.getUniqueId());
                setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
            }
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }

    //regular string papi
    public String papi(Panel panel, Player p, String setpapi) {
        try {
            setpapi = plugin.placeholders.setCpPlaceholders(panel, p,setpapi);
            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                OfflinePlayer offp = plugin.getServer().getOfflinePlayer(p.getUniqueId());
                setpapi = PlaceholderAPI.setPlaceholders(offp, setpapi);
            }
            setpapi = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', setpapi));
            return setpapi;
        }catch(NullPointerException e){
            return setpapi;
        }
    }
}
