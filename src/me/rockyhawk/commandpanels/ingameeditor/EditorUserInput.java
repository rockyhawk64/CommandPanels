package me.rockyhawk.commandpanels.ingameeditor;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditorUserInput implements Listener {
    CommandPanels plugin;
    public EditorUserInput(CommandPanels pl) {
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
            YamlConfiguration cfile = null;
            ConfigurationSection cf = null;
            try {
                for (Panel panel : plugin.panelList) { //will loop through all the files in folder
                    if (panel.getName().equals(panelName)) {
                        cf = panel.getConfig();
                        panelFile = panel.getFile();
                        panelTitle = plugin.papi(cf.getString("title"));
                        break;
                    }
                }
            } catch (Exception fail) {
                //could not fetch all panel names (probably no panels exist)
                plugin.debug(fail);
                plugin.editorInputStrings.remove(temp);
                return;
            }
            if(e.getMessage().equalsIgnoreCase(plugin.config.getString("config.input-cancel"))){
                plugin.editorInputStrings.remove(temp);
                plugin.reloadPanelFiles();
                e.getPlayer().sendMessage(plugin.papi( Objects.requireNonNull(plugin.config.getString("config.input-cancelled"))));
                return;
            }
            plugin.editorInputStrings.remove(temp);
            if(section.startsWith("panel.")) {
                panelSectionCheck(p, section, panelName, panelTitle, cf, cfile, panelFile, e);
            }else if(section.startsWith("item:")){
                itemSectionCheck(p, section, panelName, cf, cfile, panelFile, e);
            }else if(section.startsWith("section.")){
                itemSectionSectionCheck(p, section, panelName, cf, cfile, panelFile, e);
            }
            plugin.reloadPanelFiles();
            if(section.startsWith("panel.")){
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        plugin.editorGuis.openEditorGui(p, 0); //I have to do this to run regular Bukkit voids in an ASYNC Event
                    }
                });
            }else if(section.startsWith("item:")) {
                final ConfigurationSection finalCF = cf;
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        plugin.createGUI.openGui(panelName, p, finalCF, 3,0); //I have to do this to run regular Bukkit voids in an ASYNC Event
                    }
                });
            }else if(section.startsWith("section.")){
                String itemSection = ChatColor.stripColor(section.replace("section." + section.split("\\.")[1] + ".", ""));
                final ConfigurationSection finalCF = cf.getConfigurationSection("item." + itemSection);
                if(section.contains("change")){
                    final String changeItemSection = itemSection.substring(0, itemSection.lastIndexOf("."));
                    final ConfigurationSection changeFinalCF = cf.getConfigurationSection("item." + changeItemSection);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.editorGuis.openItemSections(p,panelName,changeFinalCF,changeItemSection);
                        }
                    });
                }else{
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.editorGuis.openItemSections(p,panelName,finalCF,itemSection);
                        }
                    });
                }
            }
            return;
        }
    }
    boolean savePanelFile(ConfigurationSection cf, YamlConfiguration cfile, String panelName, File panelFile){
        try {
            cfile.set("panels." + panelName, cf);
            cfile.save(panelFile);
            return true;
        } catch (Exception io) {
            plugin.debug(io);
            return false;
        }
    }

    void panelSectionCheck(Player p, String section, String panelName, String panelTitle, ConfigurationSection cf, YamlConfiguration cfile, File panelFile, AsyncPlayerChatEvent e){
        switch (section) {
            case "panel.delete":
                if (e.getMessage().contains("y")) {
                    if(Objects.requireNonNull(cfile.getConfigurationSection("panels")).getKeys(false).size() != 1){
                        //if the file has more than one panel in it
                        if(savePanelFile(null, cfile, panelName, panelFile)){
                            p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Deleted Panel!"));
                        }else{
                            p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could Not Delete Panel!"));
                        }
                    }else {
                        //if the file only has one panel in it
                        if (panelFile.delete()) {
                            p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Deleted Panel!"));
                        }else{
                            p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could Not Delete Panel!"));
                        }
                    }
                }
                break;
            case "panel.perm":
                if(e.getMessage().contains(" ")){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Permission cannot contain spaces!"));
                    break;
                }
                cf.set("perm", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Permission required is now " + "commandpanel.panel." + e.getMessage()));
                break;
            case "panel.rows":
                try {
                    int rows = Integer.parseInt(e.getMessage());
                    if (rows >= 7 || rows <= 0) {
                        //if the number isn't between 1-6
                        p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Choose an integer between 1 to 6!"));
                        return;
                    }
                    cf.set("rows", rows);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set Panel to " + rows + " rows!"));
                } catch (Exception io) {
                    plugin.debug(io);
                }
                break;
            case "panel.title":
                if(panelTitle.equals(plugin.papi(e.getMessage()))){
                    p.sendMessage(plugin.papi(plugin.tag + e.getMessage() + ChatColor.RED + " is in use from another panel!"));
                    break;
                }
                cf.set("title", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set new Title to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "panel.name":
                if(e.getMessage().contains(" ")){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Panel name cannot contain spaces!"));
                    break;
                }
                if(panelName.equals(e.getMessage())){
                    p.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + e.getMessage() + " is in use from another panel!"));
                    break;
                }
                cfile.set("panels." + e.getMessage(), cfile.get("panels." + panelName));
                //I have put null there instead of cf because that will replicate cp = null to delete it
                savePanelFile(null, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set new Name to " + e.getMessage()));
                break;
            case "panel.empty":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("empty", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Empty materials have been removed."));
                    break;
                }
                String materialTemp = null;
                try {
                    materialTemp = Objects.requireNonNull(Material.matchMaterial(e.getMessage())).toString();
                }catch(NullPointerException ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + e.getMessage() + " is not a valid Material!"));
                }
                cf.set("empty", materialTemp);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set Empty material to " + materialTemp));
                break;
            case "panel.sound-on-open":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("sound-on-open", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Sounds have been removed."));
                    break;
                }
                String tempSound;
                try {
                    tempSound = Sound.valueOf(e.getMessage()).toString();
                }catch(Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + e.getMessage() + " is not a valid Sound!"));
                    return;
                }
                cf.set("sound-on-open", tempSound);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Sound when opening is now " + tempSound));
                break;
            case "panel.panelType.add":
                List<String> typeAdd = new ArrayList<>();
                if(cf.contains("panelType")){
                    typeAdd = cf.getStringList("panelType");
                }
                typeAdd.add(e.getMessage().toLowerCase());
                cf.set("panelType", typeAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new panel type: " + e.getMessage().toLowerCase()));
                break;
            case "panel.panelType.remove":
                List<String> typeRemove;
                if(cf.contains("panelType")){
                    typeRemove = cf.getStringList("panelType");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No types found to remove!"));
                    break;
                }
                try {
                    typeRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find type!"));
                    break;
                }
                if(typeRemove.size() == 0){
                    cf.set("panelType", null);
                }else{
                    cf.set("panelType", typeRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed panel type " + e.getMessage()));
                break;
            case "panel.commands.add":
                List<String> commandsAdd = new ArrayList<>();
                if(cf.contains("commands")){
                    commandsAdd = cf.getStringList("commands");
                }
                commandsAdd.add(e.getMessage());
                cf.set("commands", commandsAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new command: " + e.getMessage()));
                break;
            case "panel.commands.remove":
                List<String> commandsRemove;
                if(cf.contains("commands")){
                    commandsRemove = cf.getStringList("commands");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No commands found to remove!"));
                    break;
                }
                try {
                    commandsRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find command!"));
                    break;
                }
                if(commandsRemove.size() == 0){
                    cf.set("commands", null);
                }else{
                    cf.set("commands", commandsRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed command line " + e.getMessage()));
                break;
            case "panel.commands-on-open.add":
                List<String> commandsOnOpenAdd = new ArrayList<>();
                if(cf.contains("commands-on-open")){
                    commandsOnOpenAdd = cf.getStringList("commands-on-open");
                }
                commandsOnOpenAdd.add(e.getMessage());
                cf.set("commands-on-open", commandsOnOpenAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new command: " + e.getMessage()));
                break;
            case "panel.commands-on-open.remove":
                List<String> commandsOnOpenRemove;
                if(cf.contains("commands-on-open")){
                    commandsOnOpenRemove = cf.getStringList("commands-on-open");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No commands found to remove!"));
                    break;
                }
                try {
                    commandsOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find command!"));
                    break;
                }
                if(commandsOnOpenRemove.size() == 0){
                    cf.set("commands-on-open", null);
                }else{
                    cf.set("commands-on-open", commandsOnOpenRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed command line " + e.getMessage()));
                break;
            case "panel.disabled-worlds.add":
                List<String> disabledWorldsAdd = new ArrayList<>();
                if(cf.contains("disabled-worlds")){
                    disabledWorldsAdd = cf.getStringList("disabled-worlds");
                }
                disabledWorldsAdd.add(e.getMessage());
                cf.set("disabled-worlds", disabledWorldsAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new World: " + e.getMessage()));
                break;
            case "panel.disabled-worlds.remove":
                List<String> disabledWorldsRemove;
                if(cf.contains("disabled-worlds")){
                    disabledWorldsRemove = cf.getStringList("disabled-worlds");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No Worlds found to remove!"));
                    break;
                }
                try {
                    disabledWorldsRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find World!"));
                    break;
                }
                if(disabledWorldsRemove.size() == 0){
                    cf.set("disabled-worlds", null);
                }else{
                    cf.set("disabled-worlds", disabledWorldsRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed World line " + e.getMessage()));
                break;
            case "panel.hotbar.material":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("open-with-item", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Hotbar item have been removed."));
                    //after an open-with-item has been altered, reload after changes
                    plugin.reloadPanelFiles();
                    break;
                }
                cf.set("open-with-item.material", e.getMessage());
                if(!cf.contains("open-with-item.name")){
                    cf.set("open-with-item.name", panelName + " Item");
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set new Material to " + ChatColor.WHITE + e.getMessage()));
                //after an open-with-item has been altered, reload after changes
                plugin.reloadPanelFiles();
                break;
            case "panel.hotbar.stationary":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("open-with-item.stationary", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Hotbar item can now be moved."));
                    break;
                }
                try {
                    int loc = Integer.parseInt(e.getMessage());
                    if (loc >= 34 || loc <= -1) {
                        //if the number isn't between 1-9
                        p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Choose an integer between 0 to 33!"));
                        return;
                    }
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set Hotbar Location to " + loc + "!"));
                    //because it needs to convert 1-9 to 0-8 for in the panel
                    loc -= 1;
                    cf.set("open-with-item.stationary", loc);
                    savePanelFile(cf, cfile, panelName, panelFile);
                } catch (Exception io) {
                    plugin.debug(io);
                }
                break;
            case "panel.hotbar.name":
                cf.set("open-with-item.name",e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set new Name to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "panel.hotbar.lore.add":
                List<String> loreAdd = new ArrayList<>();
                if(cf.contains("open-with-item.lore")){
                    loreAdd = cf.getStringList("open-with-item.lore");
                }
                loreAdd.add(e.getMessage());
                cf.set("open-with-item.lore", loreAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new lore: " + e.getMessage()));
                break;
            case "panel.hotbar.lore.remove":
                List<String> loreRemove;
                if(cf.contains("open-with-item.lore")){
                    loreRemove = cf.getStringList("open-with-item.lore");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No lore found to remove!"));
                    break;
                }
                try {
                    loreRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find lore!"));
                    break;
                }
                if(loreRemove.size() == 0){
                    cf.set("open-with-item.lore", null);
                }else{
                    cf.set("open-with-item.lore", loreRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed lore line " + e.getMessage()));
                break;
            case "panel.hotbar.commands.add":
                List<String> commandAdd = new ArrayList<>();
                if(cf.contains("open-with-item.commands")){
                    commandAdd = cf.getStringList("open-with-item.commands");
                }
                commandAdd.add(e.getMessage());
                cf.set("open-with-item.commands", commandAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new command: " + e.getMessage()));
                break;
            case "panel.hotbar.commands.remove":
                List<String> commandRemove;
                if(cf.contains("open-with-item.commands")){
                    commandRemove = cf.getStringList("open-with-item.commands");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No commands found to remove!"));
                    break;
                }
                try {
                    commandRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find command!"));
                    break;
                }
                if(commandRemove.size() == 0){
                    cf.set("open-with-item.commands", null);
                }else{
                    cf.set("open-with-item.commands", commandRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed command line " + e.getMessage()));
                break;
        }
    }

    void itemSectionCheck(Player p, String section, String panelName, ConfigurationSection cf, YamlConfiguration cfile, File panelFile, AsyncPlayerChatEvent e){
        /*
        I am using : instead of . because the
        item sections could contain 18.hasperm <- the periods
        so using a different symbol will help to separate the section from
        everything else
         */
        String itemSlot = section.split(":")[1];
        String sectionChange = section.replace("item:" + itemSlot + ":","");
        switch (sectionChange) {
            case "name":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".name", "");
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Name is now default."));
                    break;
                }
                cf.set("item." + itemSlot + ".name", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set new name to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "head":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".material", plugin.getHeads.playerHeadString());
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Material is now default."));
                    break;
                }
                cf.set("item." + itemSlot + ".material", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set Material value to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "stack":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".stack", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Item has been unstacked."));
                    break;
                }
                try {
                    int rows = Integer.parseInt(e.getMessage());
                    if (rows >= 65 || rows <= 0) {
                        //if the number isn't between 1-64
                        p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Choose an integer between 1 to 64!"));
                        return;
                    }
                    cf.set("item." + itemSlot + ".stack", rows);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set stack to " + rows + "!"));
                } catch (Exception io) {
                    plugin.debug(io);
                }
                break;
            case "enchanted":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".enchanted", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Enchantments have been removed."));
                    break;
                }
                cf.set("item." + itemSlot + ".enchanted", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set new Enchantment to " + ChatColor.WHITE + e.getMessage()));
                break;
            case "potion":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".potion", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Potion effects have been removed."));
                    break;
                }
                cf.set("item." + itemSlot + ".potion", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set new Potion to " + e.getMessage().toUpperCase()));
                break;
            case "customdata":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".customdata", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Custom Model Data has been removed."));
                    break;
                }
                cf.set("item." + itemSlot + ".customdata", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Custom Model Data set to " + e.getMessage()));
                break;
            case "leatherarmor":
                if(e.getMessage().trim().equalsIgnoreCase("remove")){
                    cf.set("item." + itemSlot + ".leatherarmor", null);
                    savePanelFile(cf, cfile, panelName, panelFile);
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Leather armor colour has been removed."));
                    break;
                }
                cf.set("item." + itemSlot + ".leatherarmor", e.getMessage());
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Leather armor colour set to " + e.getMessage()));
                break;
            case "commands:add":
                List<String> commandsOnOpenAdd = new ArrayList<>();
                if(cf.contains("item." + itemSlot + ".commands")){
                    commandsOnOpenAdd = cf.getStringList("item." + itemSlot + ".commands");
                }
                commandsOnOpenAdd.add(e.getMessage());
                cf.set("item." + itemSlot + ".commands", commandsOnOpenAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new command: " + e.getMessage()));
                break;
            case "commands:remove":
                List<String> commandsOnOpenRemove;
                if(cf.contains("item." + itemSlot + ".commands")){
                    commandsOnOpenRemove = cf.getStringList("item." + itemSlot + ".commands");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No commands found to remove!"));
                    break;
                }
                try {
                    commandsOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find command!"));
                    plugin.debug(ex);
                    break;
                }
                if(commandsOnOpenRemove.size() == 0){
                    cf.set("item." + itemSlot + ".commands", null);
                }else{
                    cf.set("item." + itemSlot + ".commands", commandsOnOpenRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed command line " + e.getMessage()));
                break;
            case "lore:add":
                List<String> loreOnOpenAdd = new ArrayList<>();
                if(cf.contains("item." + itemSlot + ".lore")){
                    loreOnOpenAdd = cf.getStringList("item." + itemSlot + ".lore");
                }
                loreOnOpenAdd.add(e.getMessage());
                cf.set("item." + itemSlot + ".lore", loreOnOpenAdd);
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new lore: " + e.getMessage()));
                break;
            case "lore:remove":
                List<String> loreOnOpenRemove;
                if(cf.contains("item." + itemSlot + ".lore")){
                    loreOnOpenRemove = cf.getStringList("item." + itemSlot + ".lore");
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No lore found to remove!"));
                    break;
                }
                try {
                    loreOnOpenRemove.remove(Integer.parseInt(e.getMessage())-1);
                }catch (Exception ex){
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "Could not find lore!"));
                    plugin.debug(ex);
                    break;
                }
                if(loreOnOpenRemove.size() == 0){
                    cf.set("item." + itemSlot + ".lore", null);
                }else{
                    cf.set("item." + itemSlot + ".lore", loreOnOpenRemove);
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed lore line " + e.getMessage()));
                break;
            case "duplicate:add":
                if(cf.contains("item." + itemSlot + ".duplicate")){
                    cf.set("item." + itemSlot + ".duplicate", cf.getString("item." + itemSlot + ".duplicate") + "," + e.getMessage());
                }else{
                    cf.set("item." + itemSlot + ".duplicate", e.getMessage());
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added new duplicate item/s: " + e.getMessage()));
                break;
            case "duplicate:remove":
                if(cf.contains("item." + itemSlot + ".duplicate")){
                    if(cf.getString("item." + itemSlot + ".duplicate").contains(",")) {
                        try {
                            String[] duplicateItems = cf.getString("item." + itemSlot + ".duplicate").split(",");
                            StringBuilder items = new StringBuilder();
                            for(int s = 0; s < duplicateItems.length; s++){
                                if(Integer.parseInt(e.getMessage()) != s+1) {
                                    items.append(duplicateItems[s]);
                                    items.append(",");
                                }
                            }
                            cf.set("item." + itemSlot + ".duplicate", items.toString());
                        } catch (Exception ex) {
                            p.sendMessage(plugin.papi(plugin.tag + ChatColor.RED + "Could not delete or find item!"));
                            plugin.debug(ex);
                            break;
                        }
                        if(cf.getString("item." + itemSlot + ".duplicate").equals("")){
                            cf.set("item." + itemSlot + ".duplicate", null);
                        }
                    }else{
                        cf.set("item." + itemSlot + ".duplicate", null);
                    }
                }else{
                    p.sendMessage(plugin.papi( plugin.tag + ChatColor.RED + "No items found to remove!"));
                    break;
                }
                savePanelFile(cf, cfile, panelName, panelFile);
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed duplicate item/s: " + e.getMessage()));
                break;
        }
    }

    void itemSectionSectionCheck(Player p, String section, String panelName, ConfigurationSection cf, YamlConfiguration cfile, File panelFile, AsyncPlayerChatEvent e){
        String secondValue = section.split("\\.")[1];
        //section includes slot at front eg, 1.hasvalue
        String itemSection = ChatColor.stripColor(section.replace("section." + secondValue + ".", ""));
        String playerMessage = ChatColor.stripColor(e.getMessage()).toLowerCase();
        switch (secondValue) {
            case "add":
                cf.set("item." + itemSection + "." + playerMessage + ".output", "true");
                if(playerMessage.equals("hasperm")) {
                    cf.set("item." + itemSection + "." + playerMessage + ".perm", "admin");
                }else{
                    cf.set("item." + itemSection + "." + playerMessage + ".value", "10");
                    cf.set("item." + itemSection + "." + playerMessage + ".compare", "%cp-player-balance%");
                }
                cf.set("item." + itemSection + "." + playerMessage + ".material", "DIRT");
                cf.set("item." + itemSection + "." + playerMessage + ".name", "");
                savePanelFile(cf, cfile, panelName, panelFile);
                plugin.reloadPanelFiles();
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Added Section " + ChatColor.WHITE + playerMessage));
                break;
            case "remove":
                cf.set("item." + itemSection + "." + playerMessage, null);
                savePanelFile(cf, cfile, panelName, panelFile);
                plugin.reloadPanelFiles();
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Removed Section " + ChatColor.WHITE + playerMessage));
                break;
            case "change":
                cf.set("item." + itemSection + "." + playerMessage.split(":")[0], playerMessage.split(":")[1]);
                savePanelFile(cf, cfile, panelName, panelFile);
                plugin.reloadPanelFiles();
                p.sendMessage(plugin.papi( plugin.tag + ChatColor.GREEN + "Set " + playerMessage.split(":")[0] + " to " + ChatColor.WHITE + playerMessage.split(":")[1]));
                break;
        }
    }
}
