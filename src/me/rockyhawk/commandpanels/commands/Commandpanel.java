package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;

public class Commandpanel implements CommandExecutor {
    CommandPanels plugin;

    public Commandpanel(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ConfigurationSection cf = null; //this is the file to use for any panel.* requests
        String panelName = "";
        //below is going to go through the files and find the right one
        if (args.length != 0) { //check to make sure the person hasn't just left it empty
            for(String[] panels  : plugin.panelNames){
                if(panels[0].equals(args[0])) {
                    cf = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panels[1])))).getConfigurationSection("panels." + panels[0]);
                    panelName = panels[0];
                    break;
                }
            }
        }else{
            plugin.helpMessage(sender);
            return true;
        }
        if(cf == null){
            sender.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.nopanel")));
            return true;
        }
        if(cf.contains("panelType")) {
            if (cf.getStringList("panelType").contains("nocommand")) {
                //do not allow command with noCommand
                return true;
            }
        }
        //below will start the command, once it got the right file and panel
        if (cmd.getName().equalsIgnoreCase("cp") || cmd.getName().equalsIgnoreCase("commandpanel") || cmd.getName().equalsIgnoreCase("cpanel")) {
            if(!(sender instanceof Player)) {
                //do console command command
                if(args.length == 2){
                    if(!args[1].equals("item")){
                        if(plugin.openPanels.hasPanelOpen(plugin.getServer().getPlayer(args[1]).getName())) {
                            plugin.openPanels.skipPanels.add(plugin.getServer().getPlayer(args[1]).getName());
                        }
                        plugin.openVoids.openCommandPanel(sender,plugin.getServer().getPlayer(args[1]),panelName,cf,true);
                    }else{
                        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cp <panel> [item] [player]"));
                    }
                    return true;
                }else if(args.length == 3){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender,plugin.getServer().getPlayer(args[2]),cf,true);
                    }else{
                        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cp <panel> item [player]"));
                    }
                    return true;
                } else {
                    sender.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Please execute command directed to a Player!"));
                    return true;
                }
            }else{
                //get player
                Player p = (Player) sender;
                //do player command
                if (args.length == 1) {
                    if(plugin.openPanels.hasPanelOpen(p.getName())) {
                        plugin.openPanels.skipPanels.add(p.getName());
                    }
                    plugin.openVoids.openCommandPanel(sender, p, panelName, cf,false);
                    return true;
                }else if(args.length == 2){
                    if (args[1].equals("item")) {
                        plugin.openVoids.giveHotbarItem(sender, p, cf, false);
                    }else{
                        if(plugin.openPanels.hasPanelOpen(plugin.getServer().getPlayer(args[1]).getName())) {
                            plugin.openPanels.skipPanels.add(plugin.getServer().getPlayer(args[1]).getName());
                        }
                        plugin.openVoids.openCommandPanel(sender, plugin.getServer().getPlayer(args[1]), panelName, cf,true);
                    }
                    return true;
                }else if(args.length == 3){
                    plugin.openVoids.giveHotbarItem(sender, plugin.getServer().getPlayer(args[2]), cf,true);
                    return true;
                }
            }
        }
        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cp <panel> [player:item] [player]"));
        return true;
    }
}
