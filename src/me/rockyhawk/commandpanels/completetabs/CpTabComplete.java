package me.rockyhawk.commandpanels.completetabs;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class CpTabComplete implements TabCompleter {
    CommandPanels plugin;

    public CpTabComplete(CommandPanels pl) {
        this.plugin = pl;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && args.length >= 1)
            if (label.equalsIgnoreCase("cp") || label.equalsIgnoreCase("cpanel") || label.equalsIgnoreCase("commandpanel")) {
                Player p = ((Player) sender).getPlayer();
                if (args.length == 1) {
                    ArrayList<String> apanels = new ArrayList<String>(); //all panels
                    for (Panel panel : plugin.panelList) { //will loop through all the files in folder
                        try {
                            if (!panel.getName().startsWith(args[0])) {
                                //this will narrow down the panels to what the user types
                                continue;
                            }
                            if (sender.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
                                if (panel.getConfig().contains("panelType")) {
                                    if (panel.getConfig().getStringList("panelType").contains("nocommand")) {
                                        //do not allow command with nocommand
                                        continue;
                                    }
                                }
                                if (plugin.panelPerms.isPanelWorldEnabled(p, panel.getConfig())) {
                                    apanels.add(panel.getName());
                                }
                            }
                        } catch (Exception skip) {
                            //ignore panel
                        }
                    }
                    return apanels;
                }

                if (args.length == 2 || args.length == 3) {

                    List<String> aplayers = new ArrayList<>();

                    if ("all".startsWith(args[(args.length == 2 ? 1 : 2)].toLowerCase())) aplayers.add("all");

                    if (args.length == 2 && "item".startsWith(args[1].toLowerCase())) aplayers.add("item");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        String name = player.getName();
                        if (name.toLowerCase().startsWith(args[(args.length == 2 ? 1 : 2)])) {
                            //this will narrow down the panels to what the user types
                            aplayers.add(name);
                        }

                    }

                    return aplayers;
                }


            }
        return null;
    }
}