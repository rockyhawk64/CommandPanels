package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

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
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpd"));
            }
        }else{
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
        }
        return true;
    }
}