package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.Context;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class PanelDownloader {

    Context ctx;

    public PanelDownloader(Context pl) {
        this.ctx = pl;
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
                sender.sendMessage(ctx.tag + ChatColor.RED + "Invalid URL. Using file:// is not supported.");
                return;
            }
        } catch (UnsupportedEncodingException e) {
            sender.sendMessage(ctx.tag + ChatColor.RED + "UTF-8 support not found.");
            return;
        }

        // Create the file object and get its canonical path
        File file = new File(ctx.configHandler.panelsFolder, fileName);
        try {
            String canonicalPath = file.getCanonicalPath();
            if (!canonicalPath.startsWith(ctx.configHandler.panelsFolder.getCanonicalPath())) {
                sender.sendMessage(ctx.tag + ChatColor.RED + "Invalid file name or URL.");
                return;
            }
        } catch (IOException e) {
            sender.sendMessage(ctx.tag + ChatColor.RED + "Invalid file name or URL.");
            return;
        }

        //download panel from page contents and add to plugin
        try {
            URL fileUrl = new URL(url);
            in = new BufferedInputStream(fileUrl.openStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                baos.write(data, 0, count);
            }

            String yamlData = baos.toString(StandardCharsets.UTF_8.name());
            YamlConfiguration yamlConfig = new YamlConfiguration();

            try {
                yamlConfig.loadFromString(yamlData);
            } catch (InvalidConfigurationException e) {
                // Handle invalid YAML data
                sender.sendMessage(ctx.tag + ChatColor.RED + "Downloaded data is not a valid YAML file.");
                return;
            }

            // If parsing is successful, save the YAML data to the file
            File outputFile = new File(ctx.configHandler.panelsFolder, fileName);
            try (FileOutputStream outputFileOut = new FileOutputStream(outputFile)) {
                outputFileOut.write(yamlData.getBytes(StandardCharsets.UTF_8));
            }

            if (sender instanceof Player) {
                YamlConfiguration panels = YamlConfiguration.loadConfiguration(file);
                if (panels.getConfigurationSection("panels").getKeys(false).size()>1) {
                    sender.sendMessage(ctx.tag + ChatColor.GREEN + "Finished downloading panel "
                            + ChatColor.YELLOW + "'" + fileName + "'");
                } else {

                    BaseComponent[] components = new ComponentBuilder(ctx.tag +
                            net.md_5.bungee.api.ChatColor.GREEN + "Finished downloading " +
                            ChatColor.UNDERLINE + "'" + fileName + "'.\n" +
                            ChatColor.YELLOW + ChatColor.UNDERLINE + " Click Here to open the panel.")
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/cp " +
                                    panels.getConfigurationSection("panels").getKeys(false).toArray()[0]))
                            .create();
                    Player player =(Player) sender;
                    player.spigot().sendMessage(components);

                }

            } else {
                sender.sendMessage(ctx.tag + ChatColor.GREEN + "Finished downloading panel " +
                         ChatColor.YELLOW+ "'" + fileName + "'");
            }
        } catch (Exception var22) {
            sender.sendMessage(ctx.tag + ChatColor.RED + "Could not download panel.");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException var21) {
                Bukkit.getLogger().log(Level.SEVERE, null, var21);
            }

            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException var20) {
                Bukkit.getLogger().log(Level.SEVERE, null, var20);
            }
        }
    }

}
