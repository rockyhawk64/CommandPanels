package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class Commandpanelclose implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelclose(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Please execute command as a Player!"));
            return true;
        }
        Player p = (Player)sender;

        if (cmd.getName().equalsIgnoreCase("cpc") || cmd.getName().equalsIgnoreCase("commandpanelclose") && sender instanceof Player || cmd.getName().equalsIgnoreCase("cpanelc") && sender instanceof Player) {
            p.closeInventory();
            return true;
        }
        p.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cpc"));
        return true;
    }
}
