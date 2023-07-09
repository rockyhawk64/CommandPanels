package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.CommandPanels;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class PanelDownloader {

    CommandPanels plugin;

    public PanelDownloader(CommandPanels pl) {
        this.plugin = pl;
    }

    public void downloadPanel(CommandSender sender, String url, String fileName) {
        BufferedInputStream in = null;
        FileOutputStream fout = null;

        //add extension if not already added
        if (!fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) {
            fileName = fileName + ".yml";
        }

        //Check if fileName contains file://
        try {
            if (URLDecoder.decode(url, StandardCharsets.UTF_8.toString()).contains("file://")) {
                sender.sendMessage(plugin.tag + ChatColor.RED + "Invalid URL. Using file:// is not supported.");
                return;
            }
        } catch (UnsupportedEncodingException e) {
            sender.sendMessage(plugin.tag + ChatColor.RED + "UTF-8 support not found.");
            return;
        }

        // Create the file object and get its canonical path
        File file = new File(plugin.panelsf, fileName);
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!canonicalPath.startsWith(plugin.panelsf.getCanonicalPath())) {
                sender.sendMessage(plugin.tag + ChatColor.RED + "Invalid file name or URL.");
                return;
            }
        } catch (IOException e) {
            sender.sendMessage(plugin.tag + ChatColor.RED + "Invalid file name or URL.");
            return;
        }

        //download panel from page contents and add to plugin
        try {
            URL fileUrl = new URL(url);
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(file);
            byte[] data = new byte[1024];

            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
            if (sender instanceof Player) {
                YamlConfiguration panels = YamlConfiguration.loadConfiguration(file);
                if (panels.getConfigurationSection("panels").getKeys(false).size()>1) {
                    sender.sendMessage(plugin.tag + ChatColor.GREEN + "Finished downloading," +
                            ChatColor.UNDERLINE +ChatColor.YELLOW+ " Panel '" + fileName + "'");
                } else {

                    BaseComponent[] components = new ComponentBuilder(plugin.tag +
                            net.md_5.bungee.api.ChatColor.GREEN + "Finished downloading, " +
                            ChatColor.UNDERLINE + "Panel '" + fileName + "'.\n" +
                            ChatColor.YELLOW + ChatColor.UNDERLINE + " Click Here to open the panel.")
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cp " +
                                    panels.getConfigurationSection("panels").getKeys(false).toArray()[0]))
                            .create();
                    Player player =(Player) sender;
                    player.spigot().sendMessage(components);

                }

            } else {
                sender.sendMessage(plugin.tag + ChatColor.GREEN + "Finished downloading, " +
                        ChatColor.UNDERLINE +ChatColor.YELLOW+ "Panel '" + fileName + "'");
            }
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
