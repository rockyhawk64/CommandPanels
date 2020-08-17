package me.rockyhawk.commandPanels.completeTabs;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class cpTabComplete implements TabCompleter {
    commandpanels plugin;
    public cpTabComplete(commandpanels pl) { this.plugin = pl; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player && args.length == 1){
            Player p = ((Player) sender).getPlayer();
            if(label.equalsIgnoreCase("cp") || label.equalsIgnoreCase("cpanel") || label.equalsIgnoreCase("commandpanel")){
                ArrayList<String> apanels = new ArrayList<String>(); //all panels
                String tpanels; //tpanels is the temp to check through the files
                for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                    try {
                        YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                        String key;
                        tpanels = "";
                        if (!plugin.checkPanels(temp)) {
                            continue;
                        }
                        for (Iterator var10 = temp.getConfigurationSection("panels").getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                            key = (String) var10.next();
                            if (!key.startsWith(args[0])) {
                                //this will narrow down the panels to what the user types
                                continue;
                            }
                            if (sender.hasPermission("commandpanel.panel." + temp.getString("panels." + key + ".perm"))) {
                                if (temp.contains("panels." + key + ".disabled-worlds")) {
                                    List<String> disabledWorlds = temp.getStringList("panels." + key + ".disabled-worlds");
                                    if (!disabledWorlds.contains(p.getWorld().getName())) {
                                        apanels.add(key);
                                    }
                                } else {
                                    apanels.add(key);
                                }
                            }
                        }
                    }catch(Exception skip){
                        //ignore panel
                    }
                    //if file contains opened panel then start
                }
                return apanels;
            }
        }
        return null;
    }
}