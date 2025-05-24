package me.rockyhawk.commandpanels.commands.tabcomplete;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;


public class DataTabComplete implements TabCompleter {
    Context ctx;
    public DataTabComplete(Context pl) { this.ctx = pl; }

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
                output.addAll(ctx.panelData.dataPlayers.getPlayerNames());
                String[] finalArgs = args;
                output.removeIf(s -> !s.toLowerCase().startsWith(finalArgs[1]));
                if (!args[0].equalsIgnoreCase("get")) output.add("all");
            }else if(args.length == 3){
                //the clear function is here as it is the only subcommand without 3 args
                if (args[0].equalsIgnoreCase("clear")) return new ArrayList<>();
                try {
                    Set<String> set = new HashSet<>();
                    for (String player : ctx.panelData.dataPlayers.getPlayerNames()) {
                        String profile = ctx.panelData.dataPlayers.getDataProfile(player);
                        set.addAll(ctx.panelData.dataConfig.getConfigurationSection(profile + ".data").getKeys(false));
                    }
                    String[] finalArgs = args;
                    set.removeIf(s -> !s.toLowerCase().startsWith(finalArgs[2]));
                    return new ArrayList<>(set);
                } catch (Exception ex) {
                    return new ArrayList<>();
                }
            }
            return output;
        }
        return null;
    }
}