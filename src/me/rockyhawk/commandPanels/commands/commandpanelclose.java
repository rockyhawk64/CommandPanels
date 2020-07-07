package me.rockyhawk.commandPanels.commands;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class commandpanelclose implements CommandExecutor {
    commandpanels plugin;
    public commandpanelclose(commandpanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Please execute command as a Player!"));
            return true;
        }
        Player p = (Player)sender;

        if (cmd.getName().equalsIgnoreCase("cpc") || cmd.getName().equalsIgnoreCase("commandpanelclose") && sender instanceof Player || cmd.getName().equalsIgnoreCase("cpanelc") && sender instanceof Player) {
            p.closeInventory();
            return true;
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&',tag + ChatColor.RED + "Usage: /cpc"));
        return true;
    }
}
