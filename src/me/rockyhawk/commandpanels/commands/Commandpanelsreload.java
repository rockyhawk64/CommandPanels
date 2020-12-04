package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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
                plugin.reloadPanelFiles();
                if(new File(plugin.getDataFolder() + File.separator + "temp.yml").delete()){
                    //empty
                }
                plugin.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "config.yml"));

                //check for duplicates
                plugin.checkDuplicatePanel(sender);

                //add custom commands
                registerCommands();

                plugin.tag = plugin.papi(plugin.config.getString("config.format.tag") + " ");
                sender.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.reload")));
            }else{
                sender.sendMessage(plugin.papi(plugin.tag + plugin.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Usage: /cpr"));
        return true;
    }

    //this will require a server restart for new commands
    public void registerCommands(){
        ConfigurationSection tempFile;
        File commandsLoc = new File("commands.yml");
        YamlConfiguration cmdCF = YamlConfiguration.loadConfiguration(commandsLoc);
        //remove old commandpanels commands
        for(String existingCommands : cmdCF.getConfigurationSection("aliases").getKeys(false)){
            if(cmdCF.getStringList("aliases." + existingCommands).get(0).equals("commandpanel")){
                cmdCF.set("aliases." + existingCommands,null);
            }
        }
        //make the command 'commandpanels' to identify it
        ArrayList<String> temp = new ArrayList<>();
        temp.add("commandpanel");

        for (String[] panelName : plugin.panelNames) {
            tempFile = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(panelName[1])))).getConfigurationSection("panels." + panelName[0]);

            if(tempFile.contains("commands")){
                List<String> panelCommands = tempFile.getStringList("commands");
                for(String command : panelCommands){
                    cmdCF.set("aliases." + command.split("\\s")[0],temp);
                }
            }

            try {
                cmdCF.save(commandsLoc);
            } catch (IOException var10) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not register custom commands!");
            }
        }
    }
}