package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class CommandPanelsEditorTabComplete implements TabCompleter {
    CommandPanels plugin;
    public CommandPanelsEditorTabComplete(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && args.length == 1){
            Player p = ((Player) sender).getPlayer();
            ArrayList<String> apanels = new ArrayList<String>(); //all panels
            try {
                for(Panel panel : plugin.panelList) { //will loop through all the files in folder
                    if(!panel.getName().startsWith(args[0])){
                        //this will narrow down the panels to what the user types
                        continue;
                    }
                    if(sender.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
                        if(plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                            apanels.add(panel.getName());
                        }
                    }
                    //if file contains opened panel then start
                }
            }catch(Exception fail){
                //could not fetch all panel names (probably no panels exist)
            }
            return apanels;
        }
        return null;
    }
}