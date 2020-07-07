package me.rockyhawk.commandPanels.ingameEditor;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class editorUserInput implements Listener {
    commandpanels plugin;
    public editorUserInput(commandpanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPlayerChatEditor(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        for(String[] temp : plugin.editorInputStrings) {
            //[0] is player name [1] is panel name [2] is section type
            if (!temp[0].equals(p.getName())) {
                continue;
            }
            e.setCancelled(true);
            String panelName = temp[1];
            String panelTitle = temp[1];
            File panelFile = null;
            String section = temp[2];
            YamlConfiguration cf = null;
            try {
                for (String tempFile : plugin.panelFiles) { //will loop through all the files in folder
                    YamlConfiguration tempConf = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + tempFile));
                    if (!plugin.checkPanels(tempConf)) {
                        continue;
                    }
                    for (String key : Objects.requireNonNull(tempConf.getConfigurationSection("panels")).getKeys(false)) {
                        if (key.equals(panelName)) {
                            cf = tempConf;
                            panelFile = new File(plugin.panelsf + File.separator + tempFile);
                            panelTitle = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(tempConf.getString("panels." + key + ".title")));
                            break;
                        }
                    }
                    //if file contains opened panel then start
                }
            } catch (Exception fail) {
                //could not fetch all panel names (probably no panels exist)
            }
            if(e.getMessage().equalsIgnoreCase(plugin.config.getString("config.input-cancel"))){
                plugin.editorInputStrings.remove(temp);
                plugin.reloadPanelFiles();
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(plugin.config.getString("config.input-cancelled"))));
                return;
            }
            if(section.startsWith("panel.")) {
                panelSectionCheck(p, section, panelName, panelTitle, cf, panelFile, e);
            }else if(section.startsWith("item.")){
                itemSectionCheck(p, section, panelName, cf, panelFile, e);
            }
            plugin.editorInputStrings.remove(temp);
            plugin.reloadPanelFiles();
            if(section.startsWith("panel.")){
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        plugin.openEditorGui(p, 0); //I have to do this to run regular Bukkit voids in an ASYNC Event
                    }
                });
            }else if(section.startsWith("item.")) {
                final YamlConfiguration finalCF = cf;
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        plugin.openGui(panelName, p, finalCF, 3,0); //I have to do this to run regular Bukkit voids in an ASYNC Event
                    }
                });
            }
            return;
        }
    }
    boolean savePanelFile(YamlConfiguration cf, File panelFile){
        try {
            cf.save(panelFile);
            return true;
        } catch (Exception io) {
            plugin.debug(io);
            return false;
        }
    }
    void panelSectionCheck(Player p, String section, String panelName, String panelTitle, YamlConfiguration cf, File panelFile, AsyncPlayerChatEvent e){
        String tag = plugin.config.getString("config.format.tag") + " ";
        switch (section) {
            case "panel.delete":
                if (e.getMessage().contains("y")) {
                    if(Objects.requireNonNull(cf.getConfigurationSection("panels")).getKeys(false).size() != 1){
                        //if the file has more than one panel in it
                        cf.set("panels." + panelName, null);
                        if(savePanelFile(cf, panelFile)){
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Deleted Panel!"));
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Could Not Delete Panel!"));
                        }
                    }else {
                        //if the file only has one panel in it
                        if (panelFile.delete()) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Deleted Panel!"));
                        }else{
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Could Not Delete Panel!"));
                        }
                    }
                }
                break;
            case "panel.perm":
                if(e.getMessage().contains(" ")){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Permission cannot contain spaces!"));
                    break;
                }
                cf.set("panels." + panelName + ".perm", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Permission required is now " + "commandpanel.panel." + e.getMessage()));
                break;
            case "panel.rows":
                try {
                    int rows = Integer.parseInt(e.getMessage());
                    if (rows >= 7 || rows <= 0) {
                        //if the number isn't between 1-6
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Choose an integer between 1 to 6!"));
                        return;
                    }
                    cf.set("panels." + panelName + ".rows", rows);
                    cf.save(panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set Panel to " + rows + " rows!"));
                } catch (Exception io) {
                    plugin.debug(io);
                }
                break;
            case "panel.title":
                if(panelTitle.equals(ChatColor.translateAlternateColorCodes('&',e.getMessage()))){
                    p.sendMessage(plugin.papi(p,tag + e.getMessage() + ChatColor.RED + " is in use from another panel!"));
                    break;
                }
                cf.set("panels." + panelName + ".title", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set new Title to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "panel.name":
                if(e.getMessage().contains(" ")){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Panel name cannot contain spaces!"));
                    break;
                }
                if(panelName.equals(e.getMessage())){
                    p.sendMessage(plugin.papi(p,tag + ChatColor.RED + e.getMessage() + " is in use from another panel!"));
                    break;
                }
                cf.set("panels." + e.getMessage(), cf.get("panels." + panelName));
                cf.set("panels." + panelName, null);
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set new Name to " + e.getMessage()));
                break;
            case "panel.empty":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".empty", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Empty materials have been removed."));
                    break;
                }
                String materialTemp = null;
                try {
                    materialTemp = Objects.requireNonNull(Material.matchMaterial(e.getMessage())).toString();
                }catch(NullPointerException ex){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + e.getMessage() + " is not a valid Material!"));
                }
                cf.set("panels." + panelName + ".empty", materialTemp);
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set Empty material to " + materialTemp));
                break;
            case "panel.sound-on-open":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".sound-on-open", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Sounds have been removed."));
                    break;
                }
                String tempSound;
                try {
                    tempSound = Sound.valueOf(e.getMessage()).toString();
                }catch(Exception ex){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + e.getMessage() + " is not a valid Sound!"));
                    return;
                }
                cf.set("panels." + panelName + ".sound-on-open", tempSound);
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Sound when opening is now " + tempSound));
                break;
            case "panel.command":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".command", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Custom commands have been removed."));
                    break;
                }
                cf.set("panels." + panelName + ".command", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set new custom commands to " + ChatColor.WHITE + "/" + e.getMessage().trim().replace(" ", " /")));
                break;
            case "panel.commands-on-open.add":
                List<String> commandsOnOpenAdd = new ArrayList<>();
                if(cf.contains("panels." + panelName + ".commands-on-open")){
                    commandsOnOpenAdd = cf.getStringList("panels." + panelName + ".commands-on-open");
                }
                commandsOnOpenAdd.add(e.getMessage());
                cf.set("panels." + panelName + ".commands-on-open", commandsOnOpenAdd);
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Added new command: " + e.getMessage()));
                break;
            case "panel.commands-on-open.remove":
                List<String> commandsOnOpenRemove;
                if(cf.contains("panels." + panelName + ".commands-on-open")){
                    commandsOnOpenRemove = cf.getStringList("panels." + panelName + ".commands-on-open");
                }else{
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "No commands found to remove!"));
                    break;
                }
                try {
                    commandsOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Could not find command!"));
                    break;
                }
                if(commandsOnOpenRemove.size() == 0){
                    cf.set("panels." + panelName + ".commands-on-open", null);
                }else{
                    cf.set("panels." + panelName + ".commands-on-open", commandsOnOpenRemove);
                }
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Removed command line " + e.getMessage()));
                break;
        }
    }
    void itemSectionCheck(Player p, String section, String panelName, YamlConfiguration cf, File panelFile, AsyncPlayerChatEvent e){
        String tag = plugin.config.getString("config.format.tag") + " ";
        String itemSlot = section.split("\\.")[1];
        String sectionChange = section.replace("item." + itemSlot + ".","");
        switch (sectionChange) {
            case "name":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".name", "");
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Name is now default."));
                    break;
                }
                cf.set("panels." + panelName + ".item." + itemSlot + ".name", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set new name to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "head":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".material", "PLAYER_HEAD");
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Material is now default."));
                    break;
                }
                cf.set("panels." + panelName + ".item." + itemSlot + ".material", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set Material value to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "stack":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".stack", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Item has been unstacked."));
                    break;
                }
                try {
                    int rows = Integer.parseInt(e.getMessage());
                    if (rows >= 65 || rows <= 0) {
                        //if the number isn't between 1-64
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Choose an integer between 1 to 64!"));
                        return;
                    }
                    cf.set("panels." + panelName + ".item." + itemSlot + ".stack", rows);
                    cf.save(panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set stack to " + rows + "!"));
                } catch (Exception io) {
                    plugin.debug(io);
                }
                break;
            case "enchanted":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".enchanted", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Enchantments have been removed."));
                    break;
                }
                cf.set("panels." + panelName + ".item." + itemSlot + ".enchanted", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set new Enchantment to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "potion":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".potion", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Potion effects have been removed."));
                    break;
                }
                cf.set("panels." + panelName + ".item." + itemSlot + ".potion", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Set new Potion to " + e.getMessage().toUpperCase()));
                break;
            case "panel.customdata":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".customdata", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Custom Model Data has been removed."));
                    break;
                }
                cf.set("panels." + panelName + ".item." + itemSlot + ".customdata", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Custom Model Data set to " + e.getMessage()));
                break;
            case "leatherarmor":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".leatherarmor", null);
                    savePanelFile(cf, panelFile);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Leather armor colour has been removed."));
                    break;
                }
                cf.set("panels." + panelName + ".item." + itemSlot + ".leatherarmor", e.getMessage());
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Leather armor colour set to " + e.getMessage()));
                break;
            case "commands.add":
                List<String> commandsOnOpenAdd = new ArrayList<>();
                if(cf.contains("panels." + panelName + ".item." + itemSlot + ".commands")){
                    commandsOnOpenAdd = cf.getStringList("panels." + panelName + ".item." + itemSlot + ".commands");
                }
                commandsOnOpenAdd.add(e.getMessage());
                cf.set("panels." + panelName + ".item." + itemSlot + ".commands", commandsOnOpenAdd);
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Added new command: " + e.getMessage()));
                break;
            case "commands.remove":
                List<String> commandsOnOpenRemove;
                if(cf.contains("panels." + panelName + ".item." + itemSlot + ".commands")){
                    commandsOnOpenRemove = cf.getStringList("panels." + panelName + ".item." + itemSlot + ".commands");
                }else{
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "No commands found to remove!"));
                    break;
                }
                try {
                    commandsOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Could not find command!"));
                    break;
                }
                if(commandsOnOpenRemove.size() == 0){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".commands", null);
                }else{
                    cf.set("panels." + panelName + ".item." + itemSlot + ".commands", commandsOnOpenRemove);
                }
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Removed command line " + e.getMessage()));
                break;
            case "lore.add":
                List<String> loreOnOpenAdd = new ArrayList<>();
                if(cf.contains("panels." + panelName + ".item." + itemSlot + ".lore")){
                    loreOnOpenAdd = cf.getStringList("panels." + panelName + ".item." + itemSlot + ".lore");
                }
                loreOnOpenAdd.add(e.getMessage());
                cf.set("panels." + panelName + ".item." + itemSlot + ".lore", loreOnOpenAdd);
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Added new lore: " + e.getMessage()));
                break;
            case "lore.remove":
                List<String> loreOnOpenRemove;
                if(cf.contains("panels." + panelName + ".item." + itemSlot + ".lore")){
                    loreOnOpenRemove = cf.getStringList("panels." + panelName + ".item." + itemSlot + ".lore");
                }else{
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "No lore found to remove!"));
                    break;
                }
                try {
                    loreOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.RED + "Could not find lore!"));
                    break;
                }
                if(loreOnOpenRemove.size() == 0){
                    cf.set("panels." + panelName + ".item." + itemSlot + ".lore", null);
                }else{
                    cf.set("panels." + panelName + ".item." + itemSlot + ".lore", loreOnOpenRemove);
                }
                savePanelFile(cf, panelFile);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', tag + ChatColor.GREEN + "Removed lore line " + e.getMessage()));
                break;
        }
    }
}
