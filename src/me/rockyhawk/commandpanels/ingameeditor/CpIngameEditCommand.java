package me.rockyhawk.commandpanels.ingameeditor;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Objects;

public class CpIngameEditCommand implements CommandExecutor {
    CommandPanels plugin;

    public CpIngameEditCommand(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("commandpanel.edit")){
            sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            return true;
        }
        if(Objects.requireNonNull(plugin.config.getString("config.ingame-editor")).equalsIgnoreCase("false")){
            //this will cancel every /cpe command if ingame-editor is set to false
            sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Editor disabled!"));
            return true;
        }
        if(!(sender instanceof Player)) {
            sender.sendMessage(plugin.tex.colour( plugin.tag + ChatColor.RED + "Please execute command as a Player!"));
            return true;
        }
        Player p = (Player)sender;
        //below is going to go through the files and find the right one
        if (args.length == 1) { //check to make sure the person hasn't just left it empty
            for(Panel panel  : plugin.panelList){
                if(panel.getName().equals(args[0])) {
                    //below will start the command, once it got the right file and panel
                    plugin.createGUI.openGui(panel.copy(), p,3,0);
                    return true;
                }
            }
        }
        if (args.length == 0) {
            plugin.editorGuis.openEditorGui(p,0);
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpe <panel>"));
        return true;
    }
}
