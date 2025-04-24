package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
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
import java.util.*;

public class LoadReloadCommand implements CommandExecutor {
    Context ctx;
    public LoadReloadCommand(Context pl) {
        this.ctx = pl;
        reloadPanelFiles();
    }

    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("cpr") || label.equalsIgnoreCase("commandpanelreload") || label.equalsIgnoreCase("cpanelr")) {
            if (sender.hasPermission("commandpanel.reload")) {
                //close all the panels
                for(String name : ctx.openPanels.openPanels.keySet()){
                    ctx.openPanels.closePanelForLoader(name, PanelPosition.Top);
                    try {
                        Bukkit.getPlayer(name).closeInventory();
                    }catch (Exception ignore){}
                }

                reloadPanelFiles();
                ctx.configHandler.onPluginLoad();

                //load all known player UUIDs for data
                ctx.panelDataPlayers.reloadAllPlayers();

                //check for duplicates
                checkDuplicatePanel(sender);

                //reloadHotbarSlots
                ctx.hotbar.reloadHotbarSlots();

                //reload tag
                ctx.tag = ctx.tex.colour(ctx.configHandler.config.getString("config.format.tag"));

                //add custom commands to commands.yml
                if(ctx.configHandler.isTrue("config.auto-register-commands")) {
                    registerCommands();
                }

                sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.reload")));
            }else{
                sender.sendMessage(ctx.tex.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            }
            return true;
        }
        sender.sendMessage(ctx.tex.colour(ctx.tag + ChatColor.RED + "Usage: /cpr"));
        return true;
    }

    public void reloadPanelFiles() {
        ctx.plugin.panelList.clear();
        ctx.plugin.openWithItem = false;
        //load panel files from panels folder
        fileNamesFromDirectory(new File(ctx.plugin.getDataFolder() + File.separator + "panels"));
    }

    //look through all files in all folders
    private void fileNamesFromDirectory(File directory) {
        for (String fileName : Objects.requireNonNull(directory.list())) {
            if(new File(directory + File.separator + fileName).isDirectory()){
                fileNamesFromDirectory(new File(directory + File.separator + fileName));
                continue;
            }

            try {
                int ind = fileName.lastIndexOf(".");
                if (!fileName.substring(ind).equalsIgnoreCase(".yml") && !fileName.substring(ind).equalsIgnoreCase(".yaml")) {
                    continue;
                }
            }catch (Exception ex){
                continue;
            }

            //if a yaml file is missing the 'panels' at the files root
            if(!checkPanels(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)))){
                //try converting the file
                YamlConfiguration convertedYaml = ctx.deluxeConverter.tryConversion(fileName, YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)));
                if(convertedYaml != null){
                    //the conversion was successful
                    for (String tempName : Objects.requireNonNull(convertedYaml.getConfigurationSection("panels")).getKeys(false)) {
                        ctx.plugin.panelList.add(new Panel(convertedYaml,tempName));
                        if(convertedYaml.contains("panels." + tempName + ".open-with-item")) {
                            ctx.plugin.openWithItem = true;
                        }
                    }
                }else{
                    //error in the file, was not a valid commandpanels file and/or could not be converted
                    ctx.plugin.getServer().getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " Error in: " + fileName);
                }
                continue;
            }
            //go ahead and add the panels in the file to the plugins loaded panels
            for (String tempName : Objects.requireNonNull(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).getConfigurationSection("panels")).getKeys(false)) {
                ctx.plugin.panelList.add(new Panel(new File((directory + File.separator + fileName)),tempName));
                if(YamlConfiguration.loadConfiguration(new File(directory + File.separator + fileName)).contains("panels." + tempName + ".open-with-item")) {
                    ctx.plugin.openWithItem = true;
                }
            }
        }
    }

    private boolean checkPanels(YamlConfiguration temp) {
        try {
            return temp.contains("panels");
        } catch (Exception var3) {
            return false;
        }
    }

    //check for duplicate panel names
    private boolean checkDuplicatePanel(CommandSender sender){
        List<String> apanels = new ArrayList<>();
        for(Panel panel : ctx.plugin.panelList){
            apanels.add(panel.getName());
        }

        //names is a list of the titles for the Panels
        Set<String> oset = new HashSet<>(apanels);
        if (oset.size() < apanels.size()) {
            //there are duplicate panel names
            ArrayList<String> opanelsTemp = new ArrayList<>();
            for(String tempName : apanels){
                if(opanelsTemp.contains(tempName)){
                    sender.sendMessage(ctx.tex.colour(ctx.tag) + ChatColor.RED + "Error duplicate panel name: " + tempName);
                    return false;
                }
                opanelsTemp.add(tempName);
            }
            return false;
        }
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