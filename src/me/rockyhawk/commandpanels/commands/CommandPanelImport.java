package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandPanelImport implements CommandExecutor {
    CommandPanels plugin;

    public CommandPanelImport(CommandPanels pl) {
        this.plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.import")) {
            if(!plugin.config.getString("config.enable-import-command").equalsIgnoreCase("true")){
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.disabled")));
                return true;
            }
            if (args.length == 2) {
                //import command
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.downloader.downloadPanel(sender, args[1], args[0]);
                        plugin.reloadPanelFiles();
                        plugin.hotbar.reloadHotbarSlots();
                    }
                }.run();
                return true;
            }
        } else {
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpi <file name> <url>"));
        return true;
    }
}
