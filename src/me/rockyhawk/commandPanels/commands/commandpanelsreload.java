package me.rockyhawk.commandPanels.commands;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;

import java.io.File;

public class commandpanelsreload implements CommandExecutor {
    commandpanels plugin;
    public commandpanelsreload(commandpanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (label.equalsIgnoreCase("cpr") || label.equalsIgnoreCase("commandpanelreload") || label.equalsIgnoreCase("cpanelr")) {
            if (sender.hasPermission("commandpanel.reload")) {
                plugin.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));
                plugin.reloadPanelFiles();
                tag = plugin.config.getString("config.format.tag") + " ";
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.reload")));
                return true;
            }else{
                sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
                return true;
            }
        }
        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cpr"));
        return true;
    }
}