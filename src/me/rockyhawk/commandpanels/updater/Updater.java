package me.rockyhawk.commandpanels.updater;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Updater {
    CommandPanels plugin;
    public Updater(CommandPanels pl) {
        this.plugin = pl;
    }

    public String githubNewUpdate(boolean sendMessages){
        HttpURLConnection connection;
        String gitVersion;
        if(plugin.getDescription().getVersion().contains("-")){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GREEN + " Running a custom version.");
            }
            return null;
        }
        try{
            connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/rockyhawk64/CommandPanels/master/resource/plugin.yml").openConnection();
            connection.connect();
            gitVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine().split("\\s")[1];
            if(gitVersion.contains("-")){
                if(sendMessages) {
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Cannot check for update.");
                }
                return null;
            }

            //if update is true there is a new update
            boolean update = false;
            if(!gitVersion.equals(plugin.getDescription().getVersion())){
                update = true;
            }

            if(update){
                if(sendMessages) {
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.AQUA + " An update for CommandPanels is available.");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + " Download CommandPanels " + ChatColor.GOLD + gitVersion + ChatColor.WHITE + " here:");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.AQUA + " https://www.spigotmc.org/resources/command-panels-custom-guis.67788/");
                    Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.GOLD + " ================================================");
                }
                return gitVersion;
            }
        }catch(IOException e){
            if(sendMessages) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Error checking for updates online.");
            }
            plugin.debug(e);
        }
        return null;
    }
}
