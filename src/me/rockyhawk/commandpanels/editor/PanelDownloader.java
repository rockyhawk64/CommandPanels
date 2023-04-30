package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

public class PanelDownloader {

    CommandPanels plugin;
    public PanelDownloader(CommandPanels pl) { this.plugin = pl; }
    public void downloadPanel(CommandSender sender, String url, String fileName) {
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        //add extension if not already added
        if(!fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) {
            fileName = fileName + ".yml";
        }

        //download panel from page contents and add to plugin
        try {
            URL fileUrl = new URL(url);
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(new File(plugin.panelsf, fileName));
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
