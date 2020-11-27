package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

public class Commandpanelsdebug implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelsdebug(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cpd") || label.equalsIgnoreCase("commandpaneldebug") || label.equalsIgnoreCase("cpaneld")) {
            if (sender.hasPermission("commandpanel.debug")) {
                if (args.length == 0) {
                    //command /cpd
                    plugin.debug = !plugin.debug;
                    if(plugin.debug){
                        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.GREEN + "Debug Mode Enabled!"));
                    }else{
                        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.GREEN + "Debug Mode Disabled!"));
                    }
                }else{
                    sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cpd"));
                }
            }else{
                sender.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cpd"));
        return true;
    }
}