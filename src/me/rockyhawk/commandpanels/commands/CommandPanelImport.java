package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

public class CommandPanelImport implements CommandExecutor {
    CommandPanels plugin;
    public CommandPanelImport(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.import")) {
            if (args.length == 2) {
                //import command
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        downloadPanel(sender,args[1],args[0]);
                        plugin.reloadPanelFiles();
                    }
                }.run();
                return true;
            }
        }else{
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpi <file name> <url>"));
        return true;
    }

    private void downloadPanel(CommandSender sender, String url, String fileName) {
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        try {
            URL fileUrl = new URL(url);
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(new File(plugin.panelsf, fileName + ".yml"));
            byte[] data = new byte[1024];

            int count;
            while((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
            sender.sendMessage(plugin.tag + ChatColor.GREEN + "Finished downloading.");
        } catch (Exception var22) {
            sender.sendMessage(plugin.tag + ChatColor.RED + "Could not download panel.");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException var21) {
                this.plugin.getLogger().log(Level.SEVERE, null, var21);
            }

            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException var20) {
                this.plugin.getLogger().log(Level.SEVERE, null, var20);
            }

        }

    }
}
