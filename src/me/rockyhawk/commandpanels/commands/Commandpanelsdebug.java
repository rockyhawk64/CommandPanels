package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Commandpanelsdebug implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelsdebug(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.debug")) {
            if (args.length == 0) {
                //command /cpd
                if(!(sender instanceof Player)) {
                    plugin.debug.consoleDebug = !plugin.debug.consoleDebug;
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Global Debug Mode: " + plugin.debug.consoleDebug));
                    return true;
                }

                Player p = (Player)sender;
                if(plugin.debug.isEnabled(p)){
                    plugin.debug.debugSet.remove(p);
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Personal Debug Mode Disabled!"));
                }else{
                    plugin.debug.debugSet.add(p);
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Personal Debug Mode Enabled!"));
                }
            }else{
                //START
                if(args.length == 1) {
                    if(args[0].equals("converteverything")){
                        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Converting Everything"));
                        for(Panel panel : plugin.panelList){
                            convertPanel(panel.getName(), sender);
                        }
                        return true;
                    }
                    //temporary panel converter from 3.16.x.x to 3.17.x.x
                    sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Converting panel " + args[0]));
                    convertPanel(args[0], sender);
                    return true;
                }
                //END
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpd"));
            }
        }else{
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
        }
        return true;
    }

    //temporary converter
    public void convertPanel(String name, CommandSender sender){
        Panel panel = null;
        for(Panel pan : plugin.panelList){
            if(pan.getName().equals(name)){
                panel = pan;
            }
        }
        if(panel == null){
            return;
        }
        Map<String,Object> oldKeys = panel.getConfig().getValues(true);
        Map<String,Object> newKeys = new HashMap<>();
        for(Map.Entry<String,Object> key : oldKeys.entrySet()){
            ArrayList<String> newKey = new ArrayList<>();
            String[] subKeys = key.getKey().split("\\.");
            for(String subKey : subKeys){
                if(subKey.startsWith("hasvalue")){
                    newKey.add(addNumber("hasvalue",subKey));
                    continue;
                }
                if(subKey.startsWith("hasperm")){
                    newKey.add(addNumber("hasperm",subKey));
                    continue;
                }
                if(subKey.equals("perm")){
                    if(subKeys.length != 1) {
                        newKey.add("value0");
                        continue;
                    }
                }
                if(subKey.startsWith("hasgreater")){
                    newKey.add(addNumber("hasgreater",subKey));
                    continue;
                }
                if(subKey.equals("value")){
                    newKey.add("value0");
                    continue;
                }
                if(subKey.equals("compare")){
                    newKey.add("compare0");
                    continue;
                }
                newKey.add(subKey);
            }
            newKeys.put(String.join(".", newKey),key.getValue());
        }
        YamlConfiguration newConfig = new YamlConfiguration();
        if(new File(panel.getFile().getPath().replaceFirst("panels","CONVERTED_PANELS")).exists()){
            newConfig = YamlConfiguration.loadConfiguration(new File(panel.getFile().getPath().replaceFirst("panels","CONVERTED_PANELS")));
        }
        for(Map.Entry<String,Object> key : newKeys.entrySet()){
            if(key.getValue() instanceof ConfigurationSection){
                continue;
            }
            newConfig.set("panels." + panel.getName() + "." + key.getKey(),key.getValue());
        }
        try {
            newConfig.save(new File(panel.getFile().getPath().replaceFirst("panels","CONVERTED_PANELS")));
            sender.sendMessage(ChatColor.WHITE + panel.getFile().getName() + ChatColor.GREEN + " File saved to the CommandPanels plugin folder, please check it is correct before overwriting it.");
        } catch (IOException s) {
            plugin.debug(s,null);
        }
    }
    public String addNumber(String word,String str){
        if(str.equals(word)){
            return (str + "0");
        }
        int number = Integer.parseInt(str.replace(word,""));
        number += 1;
        return (word + number);
    }
}