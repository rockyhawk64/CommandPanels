package me.rockyhawk.commandpanels.completetabs;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;


public class DataTabComplete implements TabCompleter {
    CommandPanels plugin;
    public DataTabComplete(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("commandpanel.data")) {
            ArrayList<String> output = new ArrayList<>();
            if (args.length>=1 && args[0].equalsIgnoreCase("-s")) {

                    args = Arrays.copyOfRange(args, 1, args.length);
            }
            if(args.length == 1){
                String arg1 = args[0].toLowerCase();
                if ("set".startsWith(arg1))output.add("set");
                if ("add".startsWith(arg1))output.add("add");
                if ("get".startsWith(arg1))output.add("get");
                if ("remove".startsWith(arg1))output.add("remove");
                if ("clear".startsWith(arg1))output.add("clear");
            }else if(args.length == 2){
                if ("all".startsWith(args[1].toLowerCase()) && !args[0].equalsIgnoreCase("get")) output.add("all");
                if ("online".startsWith(args[1].toLowerCase()) && !args[0].equalsIgnoreCase("get")) output.add("online");
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    String name = player.getName();
                    // don't worry about it saying it may throw a NPE
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) {
                        //this will narrow down the panels to what the user types
                        output.add(name);
                    }

                }
            }else if(args.length == 3){
                if (!args[0].equalsIgnoreCase("remove")) return new ArrayList<>();
                //the clear function is here as it is the only subcommand with 3 args
                try {

                    if (!args[1].equalsIgnoreCase("all") && !args[1].equalsIgnoreCase("online"))
                        return new ArrayList<>(plugin.panelData.dataConfig.getConfigurationSection("playerData." + plugin.panelDataPlayers.getOffline(args[1])).getKeys(false));

                    else {
                        Set<String> set = new HashSet<>();
                        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            if (!player.isOnline()&&args[1].equalsIgnoreCase("online")) continue;
                            set.addAll(plugin.panelData.dataConfig.getConfigurationSection("playerData." + plugin.panelDataPlayers.getOffline(player.getName())).getKeys(false));

                        }
                        String[] finalArgs = args;
                        set.removeIf(s -> !s.toLowerCase().startsWith(finalArgs[2]));
                        return new ArrayList<>(set);
                    }
                } catch (Exception ex) {
                    return new ArrayList<>();
                }
            }
            return output;
        }
        return null;
    }
}