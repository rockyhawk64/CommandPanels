package me.rockyhawk.commandPanels.commands;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class commandpanelslist implements CommandExecutor {
    commandpanels plugin;
    public commandpanelslist(commandpanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (label.equalsIgnoreCase("cpl") || label.equalsIgnoreCase("commandpanellist") || label.equalsIgnoreCase("cpanell")) {
            if (sender.hasPermission("commandpanel.list")) {
                //command /cpl
                //check to make sure the panels isn't empty
                try {
                    if (plugin.panelFiles == null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "No panels found!"));
                        return true;
                    }
                }catch(Exception b){
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "No panels found!"));
                    return true;
                }
                ArrayList<String> apanels = new ArrayList<String>(); //all panels from all files (panel names)
                String tpanels; //tpanels is the temp to check through the files
                for (int f = 0; plugin.panelFiles.size() > f; f++) { //will loop through all the files in folder
                    String key;
                    YamlConfiguration temp;
                    tpanels = "";
                    temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(f)));
                    apanels.add("%file%" + plugin.panelFiles.get(f));
                    if(!plugin.checkPanels(temp)){
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.error") + ": File with no Panels found!"));
                        return true;
                    }
                    for (Iterator var10 = temp.getConfigurationSection("panels").getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                        key = (String) var10.next();
                        apanels.add(key);
                    }
                }
                int page = 1;
                int skip = 0;
                if(args.length == 1){
                    try {
                        page = Integer.parseInt(args[0]);
                        skip = page*9-9;
                    }catch (Exception e){
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Inaccessible Page"));
                    }
                }
                for (int f = skip; apanels.size() > f; f++) {
                    if(!apanels.get(f).contains("%file%")){
                        skip++;
                    }else{
                        break;
                    }
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.DARK_AQUA + "Panels: (Page " + page + ")"));
                for (int f = skip; apanels.size() > f; f++) {
                    if(apanels.get(f).contains("%file%")){
                        if(skip+9 <= f){
                            sender.sendMessage(ChatColor.AQUA + "Type /cpl " + (page+1) + " to read next page");
                            break;
                        }
                        sender.sendMessage(ChatColor.DARK_GREEN + apanels.get(f).replaceAll("%file%","").replaceAll(".yml",""));
                    }else{
                        sender.sendMessage(ChatColor.GREEN + "- " + apanels.get(f));
                    }
                }
                return true;
            }else{
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cpl"));
        return true;
    }
}