package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

public class Commandpanelresources implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelresources(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";

        if (label.equalsIgnoreCase("cpa") || label.equalsIgnoreCase("commandpaneladdons") || label.equalsIgnoreCase("cpanela")) {
            if (sender.hasPermission("commandpanel.addons")) {
                //version command
                sender.sendMessage(plugin.papi(tag));
                sender.sendMessage(ChatColor.GREEN + "Version " + ChatColor.GRAY + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GREEN + "Find Addons " + ChatColor.GRAY + "https://commandpanels.org/resources/");
                return true;
            }else{
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cpa"));
        return true;
    }
}
