package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandPanelsEditor implements CommandExecutor {
    CommandPanels plugin;

    public CommandPanelsEditor(CommandPanels pl) {
        this.plugin = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.edit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Cannot execute command in Console!"));
                return true;
            }
            //editor website link
            if (args.length == 0) {
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.GREEN + "Access the web editor at the link below"));
                sender.sendMessage(plugin.tex.colour(ChatColor.YELLOW + "https://CommandPanels.net/editor"));
                return true;
            }
            //export the requested panel
            if (args.length == 1) {
                for (Panel panel : plugin.panelList) {
                    if (panel.getFile().getName().equals(args[0])) {
                        String filePath = panel.getFile().getAbsolutePath(); //remove file name extensions
                        String fileContents = readFileAsString(filePath);

                        // Get the relative file path from the root panels folder
                        Path panelsFolderPath = plugin.panelsf.toPath();
                        Path panelFilePath = panel.getFile().toPath();
                        Path relativePanelPath = panelsFolderPath.relativize(panelFilePath);
                        String relativePath = relativePanelPath.toString();

                        // Prepend "fileName: {name}" and "filePath: {relativePath}" to the YAML content
                        String yamlWithFileNameAndPath = "fileName: " + (relativePath.replaceFirst("[.][^.]+$", "")) + "\n" + fileContents;

                        // Create a clickable text component with the modified YAML content
                        BaseComponent[] components = new ComponentBuilder(plugin.tag +
                                net.md_5.bungee.api.ChatColor.GREEN + "Click here to copy " +
                                net.md_5.bungee.api.ChatColor.WHITE + panel.getFile().getName() +
                                net.md_5.bungee.api.ChatColor.GREEN + " to the clipboard!")
                                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, yamlWithFileNameAndPath))
                                .create();

                        // Send the clickable text to the player
                        Player player = (Player) sender;
                        player.spigot().sendMessage(components);
                        return true;
                    }
                }
                // Pass the panel name to the YamlFileHandler
                sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Could not find panel!"));
                return true;
            }
            //download the requested panel using an import
            if (args.length == 3) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        downloadPanel(sender,args[1],args[0], args[2]);
                        plugin.reloadPanelFiles();
                        plugin.hotbar.reloadHotbarSlots();
                    }
                }.run();
                return true;
            }
        }else{
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpe <parameters>"));
        return true;
    }

    private void downloadPanel(CommandSender sender, String userID, String fileName, String token) {
        //get custom editor URL
        String url = "https://firebasestorage.googleapis.com/v0/b/commandpanels-website.appspot.com/o/pastes%2F" + userID + "%2F" + fileName + "?alt=media&token=" + token;

        plugin.downloader.downloadPanel(sender, url, fileName);

    }

    private String readFileAsString(String filePath) {
        try {
            Path path = Paths.get(filePath);
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Handle the exception if the file cannot be read
            e.printStackTrace();
            return "";
        }
    }


}
