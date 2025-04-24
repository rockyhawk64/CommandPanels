package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class ImportCommand implements CommandExecutor {
    Context ctx;

    public ImportCommand(Context pl) {
        this.ctx = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.import")) {
            if(!ctx.configHandler.isTrue("config.enable-import-command")){
                sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.disabled")));
                return true;
            }
            if (args.length == 2) {
                //import command
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ctx.downloader.downloadPanel(sender, args[1], args[0]);
                        ctx.reloader.reloadPanelFiles();
                        ctx.hotbar.reloadHotbarSlots();
                    }
                }.run();
                return true;
            }
        } else {
            sender.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            return true;
        }
        sender.sendMessage(ctx.text.colour(ctx.tag + ChatColor.RED + "Usage: /cpi <file name> <url>"));
        return true;
    }
}
