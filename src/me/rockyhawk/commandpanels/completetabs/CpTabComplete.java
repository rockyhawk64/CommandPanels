package me.rockyhawk.commandpanels.completetabs;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class CpTabComplete implements TabCompleter {
    CommandPanels plugin;
    public CpTabComplete(CommandPanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && args.length == 1){
            Player p = ((Player) sender).getPlayer();
            if(label.equalsIgnoreCase("cp") || label.equalsIgnoreCase("cpanel") || label.equalsIgnoreCase("commandpanel")){
                ArrayList<String> apanels = new ArrayList<String>(); //all panels
                for(Panel panel : plugin.panelList) { //will loop through all the files in folder
                    try {
                        if (!panel.getName().startsWith(args[0])) {
                            //this will narrow down the panels to what the user types
                            continue;
                        }
                        if (sender.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))) {
                            if(panel.getConfig().contains("panelType")) {
                                if (panel.getConfig().getStringList("panelType").contains("nocommand")) {
                                    //do not allow command with nocommand
                                    continue;
                                }
                            }
                            if(plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                                apanels.add(panel.getName());
                            }
                        }
                    }catch(Exception skip){
                        //ignore panel
                    }
                }
                return apanels;
            }
        }
        return null;
    }
}