package me.rockyhawk.commandpanels.completetabs;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;


public class ImportTabComplete implements TabCompleter {
    CommandPanels plugin;
    public ImportTabComplete(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("commandpanel.import")) {
            ArrayList<String> output = new ArrayList<>();
            if(args.length == 1){
                for(Panel panel : plugin.panelList){
                    output.add(panel.getFile().getName());
                }
            }
            return output;
        }
        return null;
    }
}