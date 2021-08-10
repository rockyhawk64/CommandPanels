package me.rockyhawk.commandpanels.completetabs;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class DataTabComplete implements TabCompleter {
    CommandPanels plugin;
    public DataTabComplete(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("commandpanel.data")) {
            ArrayList<String> output = new ArrayList<>();
            if(args.length == 1){
                output.add("set");
                output.add("add");
                output.add("get");
                output.add("remove");
                output.add("clear");
            }else if(args.length == 2){
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getName().startsWith(args[1])) {
                        continue;
                    }
                    output.add(p.getName());
                }
            }else if(args.length == 3){
                //the clear function is here as it is the only subcommand with 3 args
                try {
                    return new ArrayList<>(plugin.panelData.dataConfig.getConfigurationSection("playerData." + plugin.panelData.getOffline(args[1])).getKeys(false));
                } catch (Exception ex) {
                    return null;
                }
            }
            return output;
        }
        return null;
    }
}