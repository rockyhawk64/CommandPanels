package me.rockyhawk.commandpanels.customcommands;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class Commandpanelcustom implements Listener {
    CommandPanels plugin;
    public Commandpanelcustom(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent e) {
        try {
            for (Panel panel : plugin.panelList) {
                if (panel.getConfig().contains("commands")) {
                    List<String> panelCommands = panel.getConfig().getStringList("commands");
                    for(String cmd : panelCommands){
                        if(cmd.equalsIgnoreCase(e.getMessage().replace("/", ""))){
                            e.setCancelled(true);
                            panel.copy().open(e.getPlayer(), PanelPosition.Top);
                            return;
                        }

                        boolean correctCommand = true;
                        ArrayList<String[]> placeholders = new ArrayList<>(); //should read placeholder,argument
                        String[] phEnds = plugin.placeholders.getPlaceholderEnds(panel,true); //start and end of placeholder
                        String[] command = cmd.split("\\s");
                        String[] message = e.getMessage().replace("/", "").split("\\s"); //command split into args

                        if(command.length != message.length){
                            continue;
                        }

                        for(int i = 0; i < cmd.split("\\s").length; i++){
                            if(command[i].startsWith(phEnds[0])){
                                placeholders.add(new String[]{command[i].replace(phEnds[0],"").replace(phEnds[1],""), message[i]});
                            }else if(!command[i].equals(message[i])){
                                correctCommand = false;
                            }
                        }

                        if(correctCommand){
                            e.setCancelled(true);
                            Panel openPanel = panel.copy();
                            for(String[] placeholder : placeholders){
                                openPanel.placeholders.addPlaceholder(placeholder[0],placeholder[1]);
                            }
                            openPanel.open(e.getPlayer(),PanelPosition.Top);
                            return;
                        }
                    }
                }
            }
        }catch(NullPointerException exc){
            //this is placed to prevent null exceptions if the commandpanels reload command has file changes
            plugin.debug(exc,e.getPlayer());
        }
    }
}
