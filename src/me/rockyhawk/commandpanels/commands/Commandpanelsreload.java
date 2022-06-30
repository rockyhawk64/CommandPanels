package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Commandpanelsreload implements CommandExecutor {
    CommandPanels plugin;
    public Commandpanelsreload(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cpr") || label.equalsIgnoreCase("commandpanelreload") || label.equalsIgnoreCase("cpanelr")) {
            if (sender.hasPermission("commandpanel.reload")) {
                //close all the panels
                for(String name : plugin.openPanels.openPanels.keySet()){
                    plugin.openPanels.closePanelForLoader(name, PanelPosition.Top);
                    try {
                        Bukkit.getPlayer(name).closeInventory();
                    }catch (Exception ignore){}
                }

                plugin.reloadPanelFiles();
                if(new File(plugin.getDataFolder() + File.separator + "temp.yml").delete()){
                    //empty
                }
                plugin.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));
                plugin.blockConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "blocks.yml"));

                //check for duplicates
                plugin.checkDuplicatePanel(sender);

                //reloadHotbarSlots
                plugin.hotbar.reloadHotbarSlots();

                //reload tag
                plugin.tag = plugin.tex.colour(plugin.config.getString("config.format.tag"));

                //add custom commands to commands.yml
                if(plugin.config.getString("config.auto-register-commands").equalsIgnoreCase("true")) {
                    registerCommands();
                }

                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.reload")));
            }else{
                sender.sendMessage(plugin.tex.colour(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.tex.colour(plugin.tag + ChatColor.RED + "Usage: /cpr"));
        return true;
    }

    //this will require a server restart for new commands
    public void registerCommands(){
        File commandsLoc = new File("commands.yml");
        YamlConfiguration cmdCF;
        try {
            cmdCF = YamlConfiguration.loadConfiguration(commandsLoc);
        }catch(Exception e){
            //could not access the commands.yml file
            plugin.debug(e,null);
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

        for (Panel panel : plugin.panelList) {
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