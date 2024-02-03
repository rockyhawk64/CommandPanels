package me.rockyhawk.commandpanels.completetabs;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;


public class UpdateTabComplete implements TabCompleter {
    CommandPanels plugin;
    public UpdateTabComplete(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("commandpanel.refresh")) {
            ArrayList<String> output = new ArrayList<>();
            if (args.length>=1 && args[0].equalsIgnoreCase("-s")) {

                    args = Arrays.copyOfRange(args, 1, args.length);
            }
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