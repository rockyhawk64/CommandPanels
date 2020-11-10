package me.rockyhawk.commandpanels.customcommands;

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

        try {
            for (String[] panelName : plugin.panelNames) {
                tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + panelName[0]);
                if (tempFile.contains("commands")) {
                    List<String> panelCommands = tempFile.getStringList("commands");
                    for(String cmd : panelCommands){
                        if(cmd.equalsIgnoreCase(e.getMessage().replace("/", ""))){
                            e.setCancelled(true);
                            plugin.openVoids.openCommandPanel(e.getPlayer(), e.getPlayer(), panelName[0], tempFile, false);
                            return;
                        }

                        boolean correctCommand = true;
                        ArrayList<String[]> placeholders = new ArrayList<>(); //should read placeholder,argument
                        String[] args = cmd.split("\\s");
                        String[] executedCommand = e.getMessage().replace("/", "").split("\\s"); //command split into args
                        if(args.length != executedCommand.length){
                            continue;
                        }

                        for(int i = 0; i < cmd.split("\\s").length; i++){
                            if(args[i].startsWith("%cp-")){
                                placeholders.add(new String[]{args[i], executedCommand[i]});
                            }else if(!args[i].equals(executedCommand[i])){
                                correctCommand = false;
                            }
                        }

                        if(correctCommand){
                            e.setCancelled(true);
                            for(String[] placeholder : placeholders){
                                plugin.customCommand.addCCP(panelName[0],e.getPlayer().getName(),placeholder[0],placeholder[1]);
                            }
                            plugin.openVoids.openCommandPanel(e.getPlayer(), e.getPlayer(), panelName[0], tempFile, false);
                            return;
                        }
                    }
                }
            }
        }catch(NullPointerException exc){
            //this is placed to prevent null exceptions if the commandpanels reload command has file changes
            plugin.debug(exc);
        }
    }
}
