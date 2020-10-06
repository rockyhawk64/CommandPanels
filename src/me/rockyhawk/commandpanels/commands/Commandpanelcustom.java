package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.io.File;
import java.util.*;

public class Commandpanelcustom implements Listener {
    CommandPanels plugin;
    public Commandpanelcustom(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent e) {
        try {
            if (plugin.panelsf.list() == null || Objects.requireNonNull(plugin.panelsf.list()).length == 0) {
                return;
            }
        }catch(Exception b){
            return;
        }
        ConfigurationSection tempFile;

        for(String[] panelName  : plugin.panelNames){
            tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + panelName[0]);
            if(tempFile.contains("command")) {
                List<String> panelCommands = Arrays.asList(tempFile.getString("command").split("\\s"));
                if(panelCommands.contains(e.getMessage().replace("/",""))){
                    e.setCancelled(true);
                    plugin.openVoids.openCommandPanel(e.getPlayer(),e.getPlayer(),panelName[0],tempFile,false);
                }
            }
        }
    }
}
