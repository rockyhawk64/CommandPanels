package me.rockyhawk.commandPanels.commands;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

public class commandpanelsdebug implements CommandExecutor {
    commandpanels plugin;
    public commandpanelsdebug(commandpanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (label.equalsIgnoreCase("cpd") || label.equalsIgnoreCase("commandpaneldebug") || label.equalsIgnoreCase("cpaneld")) {
            if (sender.hasPermission("commandpanel.debug")) {
                if (args.length == 0) {
                    //command /cpd
                    plugin.debug = !plugin.debug;
                    if(plugin.debug){
                        sender.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Debug Mode Enabled!"));
                    }else{
                        sender.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Debug Mode Disabled!"));
                    }
                }else{
                    sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cpd"));
                }
                return true;
            }else{
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cpd"));
        return true;
    }
}