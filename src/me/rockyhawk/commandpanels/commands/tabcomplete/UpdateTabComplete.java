package me.rockyhawk.commandpanels.commands.tabcomplete;

import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;


public class UpdateTabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("commandpanel.refresh")) {
            ArrayList<String> output = new ArrayList<>();
            if(args.length == 1){
                for(Player player : Bukkit.getOnlinePlayers()){
                    output.add(player.getName());
                }
            }else if(args.length == 2){
                output.add("ALL");
                for(PanelPosition pp : PanelPosition.values()){
                    output.add(pp.toString());
                }
            }
            return output;
        }
        return null;
    }
}