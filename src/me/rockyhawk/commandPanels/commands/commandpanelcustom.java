package me.rockyhawk.commandPanels.commands;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class commandpanelcustom implements Listener {
    commandpanels plugin;
    public commandpanelcustom(commandpanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent e) {
        String panels;
        String tag = plugin.config.getString("config.format.tag") + " ";
        Player p = e.getPlayer();
        File panelsf = new File(plugin.getDataFolder() + File.separator + "panels");
        try {
            if (panelsf.list() == null || Objects.requireNonNull(panelsf.list()).length == 0) {
                return;
            }
        }catch(Exception b){
            return;
        }
        YamlConfiguration cf = null; //this is the file to use for any panel.* requests
        ArrayList<String> apanels = new ArrayList<String>(); //all panels from all files (panel names)
        String tpanels; //tpanels is the temp to check through the files
        String panel = null;
        for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
            YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            String key;
            tpanels = "";
            if(!plugin.checkPanels(temp)){
                p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + ": File with no Panels found!"));
                return;
            }
            for (Iterator var10 = Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false).iterator(); var10.hasNext(); tpanels = tpanels + key + " ") {
                key = (String) var10.next();
                apanels.add(key);
            }
            tpanels = tpanels.trim();
            //check if the requested panel is in the file (then set the config to that panel file)
            for(int i = 0; i < tpanels.split("\\s").length;i++){
                if(temp.contains("panels." + tpanels.split("\\s")[i] + ".command")) {
                    for(int c = 0; c < temp.getString("panels." + tpanels.split("\\s")[i] + ".command").split("\\s").length;c++) {
                        if (("/" + temp.getString("panels." + tpanels.split("\\s")[i] + ".command").split("\\s")[c]).equalsIgnoreCase(e.getMessage())) {
                            cf = temp;
                            panels = tpanels;
                            panels = panels.trim();
                            panel = panels.split("\\s")[i];
                            break;
                        }
                    }
                }
            }
        }
        if(panel == null){
            return;
        }
        e.setCancelled(true);
        try {
            plugin.openCommandPanel(p,p,panel,cf,false);
        }catch(Exception er){
            //do nothing
            p.sendMessage(plugin.papi(tag + ChatColor.RED + "Error opening panel!"));
        }
    }
}
