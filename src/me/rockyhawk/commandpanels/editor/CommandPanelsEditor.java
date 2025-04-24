package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class CommandPanelsEditor implements CommandExecutor {
    Context ctx;

    public CommandPanelsEditor(Context pl) {
        this.ctx = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("commandpanel.edit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Cannot execute command in Console!"));
                return true;
            }
            //editor website link
            if (args.length == 0) {
                sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.GREEN + "Access the web editor at the link below"));
                sender.sendMessage(ctx.tex.colour(ChatColor.YELLOW + "https://CommandPanels.net/editor"));
                return true;
            }
            //export the requested panel
            if (args.length == 1) {
                for (Panel panel : ctx.plugin.panelList) {
                    if(panel.getFile() == null){ continue; }
                    if (panel.getFile().getName().equals(args[0])) {
                        String filePath = panel.getFile().getAbsolutePath(); //remove file name extensions
                        String fileContents = readFileAsString(filePath);

                        // Get the relative file path from the root panels folder
                        String yamlWithFileNameAndPath = getYamlText(panel, fileContents);
                        byte[] contentBytes = yamlWithFileNameAndPath.getBytes(StandardCharsets.UTF_8);

                        //65535 is maximum value that can be represented by an unsigned 16-bit binary number
                        if (contentBytes.length > 65535) {
                            // If the content is too large, notify the player to use a different method
                            sender.sendMessage(ctx.tag + ChatColor.RED +
                                    "Content too long to paste in chat. Please copy and paste the panel file into the editor manually.");
                        } else {
                            // Safe to send
                            BaseComponent[] components = new ComponentBuilder(ctx.tag +
                                    net.md_5.bungee.api.ChatColor.GREEN + "Click here to copy " +
                                    net.md_5.bungee.api.ChatColor.WHITE + panel.getFile().getName() +
                                    net.md_5.bungee.api.ChatColor.GREEN + " to the clipboard!")
                                    .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, yamlWithFileNameAndPath))
                                    .create();

                            Player player = (Player) sender;
                            player.spigot().sendMessage(components);
                        }

                        return true;
                    }
                }
                // Pass the panel name to the YamlFileHandler
                sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Could not find panel!"));
                return true;
            }
            //download the requested panel using an import
            if (args.length == 3) {
                downloadPanel(sender,args[1],args[0], args[2]).thenAccept((ignored) -> {
                    ctx.reloader.reloadPanelFiles();
                    ctx.hotbar.reloadHotbarSlots();
                });

                return true;
            }
        }else{
            sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            return true;
        }
        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Usage: /cpe <parameters>"));
        return true;
    }

    //Get the relative file path
    private String getYamlText(Panel panel, String fileContents) {
        Path panelsFolderPath = ctx.configHandler.panelsFolder.toPath();
        Path panelFilePath = panel.getFile().toPath();
        Path relativePanelPath = panelsFolderPath.relativize(panelFilePath);
        String relativePath = relativePanelPath.toString();

        // Prepend "fileName: {name}" and "filePath: {relativePath}" to the YAML content
        return "fileName: " + (relativePath.replaceFirst("[.][^.]+$", "")) + "\n" + fileContents;
    }

    private CompletableFuture<Void> downloadPanel(CommandSender sender, String userID, String fileName, String token) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        //get custom editor URL
        String url = "https://firebasestorage.googleapis.com/v0/b/commandpanels-website.appspot.com/o/pastes%2F" + userID + "%2F" + fileName + "?alt=media&token=" + token;
        Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin, () -> {
            ctx.downloader.downloadPanel(sender, url, fileName);
            future.complete(null);
        });

        return future;
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
