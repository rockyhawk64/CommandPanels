package me.rockyhawk.commandPanels.commands;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

public class commandpanelversion implements CommandExecutor {
    commandpanels plugin;
    public commandpanelversion(commandpanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";

        if (label.equalsIgnoreCase("cpv") || label.equalsIgnoreCase("commandpanelversion") || label.equalsIgnoreCase("cpanelv")) {
            if (sender.hasPermission("commandpanel.version")) {
                //version command
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag));
                sender.sendMessage(ChatColor.GREEN + "Version " + ChatColor.GRAY + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GREEN + "Developer " + ChatColor.GRAY + "RockyHawk");
                sender.sendMessage(ChatColor.GREEN + "Command " + ChatColor.GRAY + "/cp");
                return true;
            }else{
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cpv"));
        return true;
    }
}
