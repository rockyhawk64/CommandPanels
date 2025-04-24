package me.rockyhawk.commandpanels.commands.opencommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenCommands implements Listener {
    Context ctx;
    public OpenCommands(Context pl) {
        this.ctx = pl;
    }

    // For custom commands that are used to open panels
    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent e) {
        try {
            for (Panel panel : ctx.plugin.panelList) {
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
                        String[] phEnds = ctx.placeholders.getPlaceholderEnds(panel,true); //start and end of placeholder
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
            ctx.debug.send(exc,e.getPlayer(), ctx);
        }
    }

    //this will require a server restart for new commands
    public void registerCommands(){
        File commandsLoc = new File("commands.yml");
        YamlConfiguration cmdCF;
        try {
            cmdCF = YamlConfiguration.loadConfiguration(commandsLoc);
        }catch(Exception e){
            //could not access the commands.yml file
            ctx.debug.send(e,null, ctx);
            return;
        }

        //remove old commandpanels commands
        for(String existingCommands : cmdCF.getConfigurationSection("aliases").getKeys(false)){
            try {
                if (cmdCF.getStringList("aliases." + existingCommands).get(0).equals("commandpanel")) {
                    cmdCF.set("aliases." + existingCommands, null);
                }
            }catch(Exception ignore){}
        }

        //make the command 'commandpanels' to identify it
        ArrayList<String> temp = new ArrayList<>();
        temp.add("commandpanel");

        for (Panel panel : ctx.plugin.panelList) {
            if(panel.getConfig().contains("panelType")){
                if(panel.getConfig().getStringList("panelType").contains("nocommandregister")){
                    continue;
                }
            }

            if(panel.getConfig().contains("commands")){
                List<String> panelCommands = panel.getConfig().getStringList("commands");
                for(String command : panelCommands){
                    cmdCF.set("aliases." + command.split("\\s")[0],temp);
                }
            }
        }

        try {
            cmdCF.save(commandsLoc);
        } catch (IOException var10) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not register custom commands!");
        }
    }
}
