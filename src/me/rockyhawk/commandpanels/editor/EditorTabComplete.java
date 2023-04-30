package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;


public class EditorTabComplete implements TabCompleter {
    CommandPanels plugin;
    public EditorTabComplete(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("commandpanel.edit")) {
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