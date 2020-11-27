package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;

import java.io.File;

public class Commandpanelsreload implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelsreload(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cpr") || label.equalsIgnoreCase("commandpanelreload") || label.equalsIgnoreCase("cpanelr")) {
            if (sender.hasPermission("commandpanel.reload")) {
                plugin.reloadPanelFiles();
                if(new File(plugin.getDataFolder() + File.separator + "temp.yml").delete()){
                    //empty
                }
                plugin.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));

                //check for duplicates
                plugin.checkDuplicatePanel(sender);

                plugin.tag = plugin.papi(plugin.config.getString("config.format.tag") + " ");
                sender.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.reload")));
            }else{
                sender.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cpr"));
        return true;
    }
}