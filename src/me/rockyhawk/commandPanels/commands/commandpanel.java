package me.rockyhawk.commandPanels.commands;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class commandpanel implements CommandExecutor {
    commandpanels plugin;

    public commandpanel(commandpanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        YamlConfiguration cf = null; //this is the file to use for any panel.* requests
        String panels = "";
        ArrayList<String> apanels = new ArrayList<String>(); //all panels from all files (titles of panels)
        ArrayList<String> opanels = new ArrayList<String>(); //all panels from all files (raw names of panels)
        boolean found = false;
        //below is going to go through the files and find the right one
        if (args.length != 0) { //check to make sure the person hasn't just left it empty
            for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String key : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    apanels.add(temp.getString("panels." + key + ".title"));
                    opanels.add(key);
                    if (args[0].equalsIgnoreCase(key)){
                        found = true;
                        panels = key;
                        cf = temp;
                    }
                }
            }
        }else{
            plugin.helpMessage(sender);
            return true;
        }
        if(!found){
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.nopanel")));
            return true;
        }
        //below will start the command, once it got the right file and panel
        if (cmd.getName().equalsIgnoreCase("cp") || cmd.getName().equalsIgnoreCase("commandpanel") || cmd.getName().equalsIgnoreCase("cpanel")) {
            boolean nfound = true;
            for (int i = 0; panels.split("\\s").length - 1 >= i; ++i) {
                if (args[0].equalsIgnoreCase(panels.split("\\s")[i])) {
                    panels = panels.split("\\s")[i];
                    nfound = false;
                }
            }
            if (nfound) {
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.nopanel")));
                return true;
            }else if (!checkconfig(panels, sender, cf)) {
                //if the config is missing an element (message will be sent to user via the public boolean)
                return true;
            }
            checkDuplicatePanel(sender,opanels,apanels);
            if(!(sender instanceof Player)) {
                //do console command command
                if(args.length == 2){
                    if(!args[1].equals("item")){
                        plugin.openVoids.openCommandPanel(sender,plugin.getServer().getPlayer(args[1]),panels,cf,true);
                        return true;
                    }else{
                        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cp <panel> [item] [player]"));
                        return true;
                    }
                }else if(args.length == 3){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender,plugin.getServer().getPlayer(args[2]),panels,cf,true);
                        return true;
                    }else{
                        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cp <panel> item [player]"));
                        return true;
                    }
                } else {
                    sender.sendMessage(plugin.papi( tag + ChatColor.RED + "Please execute command directed to a Player!"));
                    return true;
                }
            }else{
                //get player
                Player p = (Player) sender;
                //do player command
                if (args.length == 1) {
                    plugin.openVoids.openCommandPanel(sender, p, panels, cf,false);
                    return true;
                }else if(args.length == 2){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender, p, panels, cf, false);
                    }else{
                        plugin.openVoids.openCommandPanel(sender, plugin.getServer().getPlayer(args[1]), panels, cf,true);
                    }
                    return true;
                }else if(args.length == 3){
                    plugin.openVoids.giveHotbarItem(sender, plugin.getServer().getPlayer(args[2]), panels, cf,true);
                    return true;
                }
            }
        }
        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cp <panel> [player:item] [player]"));
        return true;
    }

    boolean checkDuplicatePanel(CommandSender sender, ArrayList<String> opanels, ArrayList<String> apanels){
        String tag = plugin.config.getString("config.format.tag") + " ";
        //names is a list of the titles for the Panels
        Set<String> oset = new HashSet<String>(opanels);
        if (oset.size() < opanels.size()) {
            //there are duplicate panel names
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " panels: You cannot have duplicate panel names!"));
            if(plugin.debug){
                ArrayList<String> opanelsTemp = new ArrayList<String>();
                for(String tempName : opanels){
                    if(opanelsTemp.contains(tempName)){
                        sender.sendMessage(plugin.papi(tag + ChatColor.RED + " The duplicate panel is: " + tempName));
                        return false;
                    }
                    opanelsTemp.add(tempName);
                }
            }
            return false;
        }
        Set<String> set = new HashSet<String>(apanels);
        if (set.size() < apanels.size()) {
            //there are duplicate panel titles
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " title: You cannot have duplicate title names!"));
            if(plugin.debug){
                ArrayList<String> apanelsTemp = new ArrayList<String>();
                for(String tempName : apanels){
                    if(apanelsTemp.contains(tempName)){
                        sender.sendMessage(plugin.papi(tag + ChatColor.RED + " The duplicate title is: " + tempName));
                        return false;
                    }
                    apanelsTemp.add(tempName);
                }
            }
            return false;
        }
        return true;
    }

    boolean checkconfig(String panels, CommandSender sender, YamlConfiguration pconfig) {
        //if it is missing a section specified it will return false
        String tag = plugin.config.getString("config.format.tag") + " ";
        if(!pconfig.contains("panels." + panels)) {
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.nopanel")));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".perm")) {
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " perm: Missing config section!"));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".rows")) {
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " perm: Missing config section!"));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".title")) {
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " perm: Missing config section!"));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".item")) {
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " perm: Missing config section!"));
            return false;
        }
        return true;
    }
}
