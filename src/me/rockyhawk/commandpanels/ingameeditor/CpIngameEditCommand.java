package me.rockyhawk.commandpanels.ingameeditor;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.util.*;

public class CpIngameEditCommand implements CommandExecutor {
    CommandPanels plugin;

    public CpIngameEditCommand(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if(!sender.hasPermission("commandpanel.edit")){
            sender.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.perms")));
            return true;
        }
        if(Objects.requireNonNull(plugin.config.getString("config.ingame-editor")).equalsIgnoreCase("false")){
            //this will cancel every /cpe command if ingame-editor is set to false
            sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Editor disabled!"));
            return true;
        }
        if(!(sender instanceof Player)) {
            sender.sendMessage(plugin.papi( tag + ChatColor.RED + "Please execute command as a Player!"));
            return true;
        }
        ConfigurationSection cf = null; //this is the file to use for any panel.* requests
        String panelName = "";
        //below is going to go through the files and find the right one
        if (args.length != 0) { //check to make sure the person hasn't just left it empty
            for(String[] panels  : plugin.panelNames){
                if(panels[0].equals(args[0])) {
                    cf = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panels[1])))).getConfigurationSection("panels." + panels[0]);
                    panelName = panels[0];
                    break;
                }
            }
        }
        //below will start the command, once it got the right file and panel
        if (cmd.getName().equalsIgnoreCase("cpe") || cmd.getName().equalsIgnoreCase("commandpaneledit") || cmd.getName().equalsIgnoreCase("cpanele")) {
            Player p = (Player) sender;
            if (args.length == 0) {
                plugin.editorGuis.openEditorGui(p,0);
                return true;
            }
            if (args.length == 1) {
                //open editor window here
                plugin.createGUI.openGui(panelName, p, cf,3,0);
                return true;
            }
        }
        sender.sendMessage(plugin.papi(tag + ChatColor.RED + "Usage: /cpe <panel>"));
        return true;
    }
}
