package me.rockyhawk.commandpanels.generatepanels;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class TabCompleteGenerate implements TabCompleter {
    CommandPanels plugin;
    public TabCompleteGenerate(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && args.length == 1){
            if(label.equalsIgnoreCase("cpg") || label.equalsIgnoreCase("cpanelg") || label.equalsIgnoreCase("commandpanelgenerate")){
                if(sender.hasPermission("commandpanel.generate")) {
                    ArrayList<String> apanels = new ArrayList<String>(); //all panels
                    apanels.add("1");
                    apanels.add("2");
                    apanels.add("3");
                    apanels.add("4");
                    apanels.add("5");
                    apanels.add("6");
                    return apanels;
                }
            }
        }
        return null;
    }
}