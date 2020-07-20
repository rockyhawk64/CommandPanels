package me.rockyhawk.commandPanels.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.error") + ": Panel with syntax error found!"));
                    return true;
                }
                for (String key : temp.getConfigurationSection("panels").getKeys(false)) {
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.nopanel")));
            return true;
        }
        //below will start the command, once it got the right file and panel
        if (cmd.getName().equalsIgnoreCase("cp") || cmd.getName().equalsIgnoreCase("commandpanel") || cmd.getName().equalsIgnoreCase("cpanel")) {
            //if command executed from console
            if(!(sender instanceof Player)) {
                if(args.length == 2){
                    if(!args[1].equals("item")){
                        boolean nfound = true;
                        for (int i = 0; panels.split("\\s").length - 1 >= i; ++i) {
                            if (args[0].equalsIgnoreCase(panels.split("\\s")[i])) {
                                panels = panels.split("\\s")[i];
                                nfound = false;
                            }
                        }
                        Player sp;
                        try {
                            sp = plugin.getServer().getPlayer(args[1]);
                        }catch(Exception e){
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.notitem")));
                            return true;
                        }
                        if (nfound) {
                            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(sp, plugin.config.getString("config.format.nopanel"))));
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.nopanel")));
                            }
                            return true;
                        }else if (!checkconfig(panels, sp, cf)) {
                            //if the config is missing an element (message will be sent to user via the public boolean)
                            return true;
                        }
                        if (sender.hasPermission("commandpanel.panel." + cf.getString("panels." + panels + ".perm"))) {
                            if(sender.hasPermission("commandpanel.other")) {
                                try {
                                    if (cf.contains("panels." + panels + ".disabled-worlds")) {
                                        List<String> disabledWorlds = (List<String>) cf.getList("panels." + panels + ".disabled-worlds");
                                        if (disabledWorlds.contains(sp.getWorld().getName())) {
                                            //panel cannot be used in the players world!
                                            if (plugin.config.getString("config.disabled-world-message").equalsIgnoreCase("true")) {
                                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Panel is disabled in players world!"));
                                            }
                                            return true;
                                        }
                                    }
                                }catch(NullPointerException offlinePlayer){
                                    //SKIP because player is offline
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.notitem")));
                                    return true;
                                }
                                try {
                                    if (cf.contains("panels." + panels + ".sound-on-open")) {
                                        //play sound when panel is opened
                                        if(!cf.getString("panels." + panels + ".sound-on-open").equalsIgnoreCase("off")) {
                                            try {
                                                sp.playSound(sp.getLocation(), Sound.valueOf(cf.getString("panels." + panels + ".sound-on-open").toUpperCase()), 1F, 1F);
                                            } catch (Exception s) {
                                                sp.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + plugin.papi(sp, plugin.config.getString("config.format.error") + " " + "sound-on-open: " + cf.getString("panels." + panels + ".sound-on-open"))));
                                            }
                                        }
                                    }
                                    if (cf.contains("panels." + panels + ".commands-on-open")) {
                                        //execute commands on panel open
                                        try {
                                            List<String> commands = (List<String>) cf.getList("panels." + panels + ".commands-on-open");
                                            for (int i = 0; commands.size() - 1 >= i; i++) {
                                                int val = plugin.commandPayWall(sp,commands.get(i));
                                                if(val == 0){
                                                    break;
                                                }
                                                if(val == 2){
                                                    plugin.commandTags(sp, commands.get(i));
                                                }
                                            }
                                        }catch(Exception s){
                                            sp.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + plugin.papi(sp, plugin.config.getString("config.format.error") + " " + "commands-on-open: " + cf.getString("panels." + panels + ".commands-on-open"))));
                                        }
                                    }
                                    plugin.openGui(panels, sp, cf,1,0);
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.GREEN + "Panel Opened for " + sp.getDisplayName()));
                                } catch (Exception r) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.notitem")));
                                }
                            }else{
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                            }
                            return true;
                        }

                        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(sp, plugin.config.getString("config.format.perms"))));
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                            return true;
                        }
                    }else{
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cp <panel> [item] [player]"));
                        return true;
                    }
                }else if(args.length == 3){
                    if (args[1].equals("item")) {
                        boolean nfound = true;
                        for (int i = 0; panels.split("\\s").length - 1 >= i; ++i) {
                            if (args[0].equalsIgnoreCase(panels.split("\\s")[i])) {
                                panels = panels.split("\\s")[i];
                                nfound = false;
                            }
                        }
                        Player sp;
                        try {
                            sp = plugin.getServer().getPlayer(args[2]);
                        }catch(Exception e){
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.notitem")));
                            return true;
                        }
                        if (nfound) {
                            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(sp, plugin.config.getString("config.format.nopanel"))));
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.nopanel")));
                            }
                            return true;
                        }

                        if (sender.hasPermission("commandpanel.item." + cf.getString("panels." + panels + ".perm")) && cf.contains("panels." + panels + ".open-with-item")) {
                            if(cf.contains("panels." + panels + ".disabled-worlds")){
                                List<String> disabledWorlds = (List<String>) cf.getList("panels." + panels + ".disabled-worlds");
                                if(disabledWorlds.contains(sp.getWorld().getName())){
                                    //panel cannot be used in the players world!
                                    if(Objects.requireNonNull(plugin.config.getString("config.disabled-world-message")).equalsIgnoreCase("true")){
                                        sp.sendMessage(ChatColor.RED + "Panel is disabled in this world!");
                                    }
                                    return true;
                                }
                            }
                            ItemStack s;
                            try {
                                s = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(cf.getString("panels." + panels + ".open-with-item.material")))), 1);
                            }catch(Exception n){
                                if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(sp, plugin.config.getString("config.format.error") + " open-with-item: material")));
                                } else {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.error") + " open-with-item: material"));
                                }
                                return true;
                            }
                            plugin.setName(s, cf.getString("panels." + panels + ".open-with-item.name"), cf.getList("panels." + panels + ".open-with-item.lore"),sp,true);
                            if(sender.hasPermission("commandpanel.other")) {
                                try {
                                    if(cf.contains("panels." + panels + ".open-with-item.stationary")) {
                                        sp.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panels + ".open-with-item.stationary"))), s);
                                    }else{
                                        sp.getInventory().addItem(s);
                                    }
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.GREEN + "Item Given to " + plugin.getServer().getPlayer(args[2]).getDisplayName()));
                                } catch (Exception r) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.notitem")));
                                }
                            }else{
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                            }
                            return true;
                        }
                        if (!cf.contains("panels." + panels + ".open-with-item")) {
                            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(sp, plugin.config.getString("config.format.noitem"))));
                                return true;
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.noitem")));
                                return true;
                            }
                        }
                        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(sp, plugin.config.getString("config.format.perms"))));
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                            return true;
                        }
                    }else{
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cp <panel> item [player]"));
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Please execute command directed to a Player!"));
                    return true;
                }
            }
            Player p = (Player) sender;
            //names is a list of the titles for the Panels
            Set<String> oset = new HashSet<String>(opanels);
            if (oset.size() < opanels.size()) {
                //there are duplicate panel names
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.error") + " panels: You cannot have duplicate panel names!")));
                if(plugin.debug){
                    ArrayList<String> opanelsTemp = new ArrayList<String>();
                    for(String tempName : opanels){
                        if(opanelsTemp.contains(tempName)){
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + " The duplicate panel is: " + tempName));
                            return true;
                        }
                        opanelsTemp.add(tempName);
                    }
                }
                return true;
            }
            Set<String> set = new HashSet<String>(apanels);
            if (set.size() < apanels.size()) {
                //there are duplicate panel titles
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.error") + " title: You cannot have duplicate title names!")));
                if(plugin.debug){
                    ArrayList<String> apanelsTemp = new ArrayList<String>();
                    for(String tempName : apanels){
                        if(apanelsTemp.contains(tempName)){
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + " The duplicate title is: " + tempName));
                            return true;
                        }
                        apanelsTemp.add(tempName);
                    }
                }
                return true;
            }

            if (args.length == 1) {
                boolean nfound = true;

                for (int i = 0; panels.split("\\s").length - 1 >= i; ++i) {
                    if (args[0].equalsIgnoreCase(panels.split("\\s")[i])) {
                        panels = panels.split("\\s")[i];
                        nfound = false;
                    }
                }
                if (nfound) {
                    if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.nopanel"))));
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.nopanel")));
                    }
                    return true;
                }else if (!checkconfig(panels, p, cf)) {
                    //if the config is missing an element (message will be sent to user via the public boolean)
                    return true;
                }
                if (p.hasPermission("commandpanel.panel." + cf.getString("panels." + panels + ".perm"))) {
                    if(cf.contains("panels." + panels + ".disabled-worlds")){
                        List<String> disabledWorlds = (List<String>) cf.getList("panels." + panels + ".disabled-worlds");
                        if(disabledWorlds.contains(p.getWorld().getName())){
                            //panel cannot be used in the players world!
                            if(plugin.config.getString("config.disabled-world-message").equalsIgnoreCase("true")){
                                p.sendMessage(ChatColor.RED + "Panel is disabled in this world!");
                            }
                            return true;
                        }
                    }
                    if (cf.contains("panels." + panels + ".sound-on-open")) {
                        //play sound when panel is opened
                        if(!cf.getString("panels." + panels + ".sound-on-open").equalsIgnoreCase("off")) {
                            try {
                                p.playSound(p.getLocation(), Sound.valueOf(cf.getString("panels." + panels + ".sound-on-open").toUpperCase()), 1F, 1F);
                            } catch (Exception s) {
                                plugin.debug(s);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + plugin.papi(p, plugin.config.getString("config.format.error") + " " + "sound-on-open: " + cf.getString("panels." + panels + ".sound-on-open"))));
                            }
                        }
                    }
                    if (cf.contains("panels." + panels + ".commands-on-open")) {
                        //execute commands on panel open
                        try {
                            List<String> commands = (List<String>) cf.getList("panels." + panels + ".commands-on-open");
                            for (int i = 0; commands.size() - 1 >= i; i++) {
                                int val = plugin.commandPayWall(p,commands.get(i));
                                if(val == 0){
                                    break;
                                }
                                if(val == 2){
                                    plugin.commandTags(p, commands.get(i));
                                }
                            }
                        }catch(Exception s){
                            plugin.debug(s);
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + plugin.papi(p, plugin.config.getString("config.format.error") + " " + "commands-on-open: " + cf.getString("panels." + panels + ".commands-on-open"))));
                        }
                    }
                    plugin.openGui(panels, p, cf, 1,0);
                    return true;
                }

                if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.perms"))));
                    return true;
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                    return true;
                }
            }
            if (args.length == 2) {
                if (args[1].equals("item")) {
                    boolean nfound = true;

                    for (int i = 0; panels.split("\\s").length - 1 >= i; ++i) {
                        if (args[0].equalsIgnoreCase(panels.split("\\s")[i])) {
                            panels = panels.split("\\s")[i];
                            nfound = false;
                        }
                    }

                    if (nfound) {
                        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.nopanel"))));
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.nopanel")));
                        }
                        return true;
                    }

                    if (p.hasPermission("commandpanel.item." + cf.getString("panels." + panels + ".perm")) && cf.contains("panels." + panels + ".open-with-item")) {
                        if(cf.contains("panels." + panels + ".disabled-worlds")){
                            List<String> disabledWorlds = (List<String>) cf.getList("panels." + panels + ".disabled-worlds");
                            if(disabledWorlds.contains(p.getWorld().getName())){
                                //panel cannot be used in the players world!
                                if(plugin.config.getString("config.disabled-world-message").equalsIgnoreCase("true")){
                                    p.sendMessage(ChatColor.RED + "Panel is disabled in this world!");
                                }
                                return true;
                            }
                        }
                        ItemStack s;
                        try {
                            s = new ItemStack(Material.matchMaterial(cf.getString("panels." + panels + ".open-with-item.material")), 1);
                        }catch(Exception n){
                            plugin.debug(n);
                            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.error") + " open-with-item: material")));
                            } else {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.error") + " open-with-item: material"));
                            }
                            return true;
                        }
                        plugin.setName(s, cf.getString("panels." + panels + ".open-with-item.name"), cf.getList("panels." + panels + ".open-with-item.lore"),p,true);
                        if(cf.contains("panels." + panels + ".open-with-item.stationary")) {
                            p.getInventory().setItem(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panels + ".open-with-item.stationary"))), s);
                        }else{
                            p.getInventory().addItem(s);
                        }
                        return true;
                    }
                    if (!cf.contains("panels." + panels + ".open-with-item")) {
                        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.noitem"))));
                            return true;
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.noitem")));
                            return true;
                        }
                    }
                    if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.perms"))));
                        return true;
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                        return true;
                    }
                }else { //player name here eg /cp example RockyHawk to get rockyhawk to open panel
                    boolean nfound = true;

                    for (int i = 0; panels.split("\\s").length - 1 >= i; ++i) {
                        if (args[0].equalsIgnoreCase(panels.split("\\s")[i])) {
                            panels = args[0];
                            nfound = false;
                        }
                    }
                    if (nfound) {
                        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.nopanel"))));
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.nopanel")));
                        }
                        return true;
                    }else if (!checkconfig(panels, p, cf)) {
                        //if the config is missing an element (message will be sent to user via the public boolean)
                        return true;
                    }
                    if (p.hasPermission("commandpanel.panel." + cf.getString("panels." + panels + ".perm"))) {
                        if(p.hasPermission("commandpanel.other")) {
                            if(cf.contains("panels." + panels + ".disabled-worlds")){
                                List<String> disabledWorlds = (List<String>) cf.getList("panels." + panels + ".disabled-worlds");
                                if(disabledWorlds.contains(p.getWorld().getName())){
                                    //panel cannot be used in the players world!
                                    if(plugin.config.getString("config.disabled-world-message").equalsIgnoreCase("true")){
                                        p.sendMessage(ChatColor.RED + "Panel is disabled in this world!");
                                    }
                                    return true;
                                }
                            }
                            try {
                                plugin.openGui(panels, plugin.getServer().getPlayer(args[1]), cf,1,0);
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.GREEN + "Panel Opened for " + plugin.getServer().getPlayer(args[1]).getDisplayName()));
                            } catch (Exception r) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.notitem")));
                            }
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                        }
                        return true;
                    }

                    if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.perms"))));
                        return true;
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                        return true;
                    }
                }
            }
            if (args.length == 3) {
                //if the command is /cp example item <player> to give other player item
                if (args[1].equals("item")) {
                    boolean nfound = true;

                    for (int i = 0; panels.split("\\s").length - 1 >= i; ++i) {
                        if (args[0].equalsIgnoreCase(panels.split("\\s")[i])) {
                            panels = panels.split("\\s")[i];
                            nfound = false;
                        }
                    }

                    if (nfound) {
                        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.nopanel"))));
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.nopanel")));
                        }
                        return true;
                    }

                    if (p.hasPermission("commandpanel.item." + cf.getString("panels." + panels + ".perm")) && cf.contains("panels." + panels + ".open-with-item")) {
                        if(cf.contains("panels." + panels + ".disabled-worlds")){
                            List<String> disabledWorlds = (List<String>) cf.getList("panels." + panels + ".disabled-worlds");
                            if(disabledWorlds.contains(p.getWorld().getName())){
                                //panel cannot be used in the players world!
                                if(plugin.config.getString("config.disabled-world-message").equalsIgnoreCase("true")){
                                    p.sendMessage(ChatColor.RED + "Panel is disabled in this world!");
                                }
                                return true;
                            }
                        }
                        ItemStack s;
                        try {
                            s = new ItemStack(Material.matchMaterial(cf.getString("panels." + panels + ".open-with-item.material")), 1);
                        }catch(Exception n){
                            plugin.debug(n);
                            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.error") + " open-with-item: material")));
                            } else {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.error") + " open-with-item: material"));
                            }
                            return true;
                        }
                        plugin.setName(s, cf.getString("panels." + panels + ".open-with-item.name"), cf.getList("panels." + panels + ".open-with-item.lore"),p,true);
                        if(p.hasPermission("commandpanel.other")) {
                            try {
                                if(cf.contains("panels." + panels + ".open-with-item.stationary")) {
                                   p.getInventory().setItem(Integer.parseInt(cf.getString("panels." + panels + ".open-with-item.stationary")), s);
                                }else{
                                    p.getInventory().addItem(s);
                                }
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.GREEN + "Item Given to " + plugin.getServer().getPlayer(args[2]).getDisplayName()));
                            } catch (Exception r) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.notitem")));
                            }
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                        }
                        return true;
                    }
                    if (!cf.contains("panels." + panels + ".open-with-item")) {
                        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.noitem"))));
                            return true;
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.noitem")));
                            return true;
                        }
                    }
                    if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + PlaceholderAPI.setPlaceholders(p, plugin.config.getString("config.format.perms"))));
                        return true;
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                        return true;
                    }
                }
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cp <panel> [player:item] [player]"));
        return true;
    }

    boolean checkconfig(String panels, Player p, YamlConfiguration pconfig) {
        //if it is missing a section specified it will return false
        String tag = plugin.config.getString("config.format.tag") + " ";
        if(!pconfig.contains("panels." + panels)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.nopanel"))));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".perm")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.error") + " perm: Missing config section!")));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".rows")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.error") + " rows: Missing config section!")));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".title")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.error") + " title: Missing config section!")));
            return false;
        }
        if(!pconfig.contains("panels." + panels + ".item")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.papi(p, plugin.config.getString("config.format.error") + " item: Missing config section!")));
            return false;
        }
        return true;
    }
}
