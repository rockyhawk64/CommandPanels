package me.rockyhawk.commandpanels.commands.tabcomplete;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;


public class ImportTabComplete implements TabCompleter {
    Context ctx;
    public ImportTabComplete(Context pl) { this.ctx = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender.hasPermission("commandpanel.import")) {
            ArrayList<String> output = new ArrayList<>();
            if(args.length == 1){
                for(Panel panel : ctx.plugin.panelList){
                    if(panel.getFile() == null){ continue; }
                    output.add(panel.getFile().getName());
                }
            }
            return output;
        }
        return null;
    }
}