package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

public class Commandpanelversion implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelversion(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";

        if (label.equalsIgnoreCase("cpv") || label.equalsIgnoreCase("commandpanelversion") || label.equalsIgnoreCase("cpanelv")) {
            if (sender.hasPermission("commandpanel.version")) {
                //version command
                sender.sendMessage(plugin.papi(tag));
                sender.sendMessage(ChatColor.GREEN + "Version " + ChatColor.GRAY + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
                sender.sendMessage(ChatColor.GREEN + "Command " + ChatColor.GRAY + "/cp");
                return true;
            }else{
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cpv"));
        return true;
    }
}
