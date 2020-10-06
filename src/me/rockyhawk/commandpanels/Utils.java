package me.rockyhawk.commandpanels;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class Utils implements Listener {
    CommandPanels plugin;
    public Utils(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onPanelClick(InventoryClickEvent e) {
        //when clicked on a panel
        Player p = (Player)e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        if(!plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        ConfigurationSection cf = plugin.openPanels.getOpenPanel(p.getName()); //this is the panel cf section

        if(e.getSlotType().equals(InventoryType.SlotType.CONTAINER) && e.getRawSlot() <= Integer.parseInt(Objects.requireNonNull(cf.getString("rows")))*9-1){
            e.setCancelled(true);
            p.updateInventory();
            //this loops through all the items in the panel
            boolean foundSlot = false;
            for(String slot : Objects.requireNonNull(cf.getConfigurationSection("item")).getKeys(false)){
                if(slot.equals(Integer.toString(e.getSlot()))){
                    foundSlot = true;
                }
            }
            if(!foundSlot){
                return;
            }
            //loop through possible hasvalue/hasperm 1,2,3,etc
            String section = plugin.itemCreate.hasSection(cf.getConfigurationSection("item." + e.getSlot()), p);
            //this will remove any pending user inputs, if there is already something there from a previous item
            for(int o = 0; plugin.userInputStrings.size() > o; o++){
                if(plugin.userInputStrings.get(o)[0].equals(p.getName())){
                    plugin.userInputStrings.remove(o);
                    o=o-1;
                }
            }
            redirectPanel(p,cf,section,e.getSlot());
            if(cf.contains("item." + e.getSlot() + section + ".commands")) {
                List<String> commands = cf.getStringList("item." + e.getSlot() + section + ".commands");
                if (commands.size() != 0) {
                    //this will replace a sequence tag command with the commands from the sequence
                    List<String> commandsAfterSequence = commands;
                    for (int i = 0; commands.size() - 1 >= i; i++) {
                        if(commands.get(i).startsWith("sequence=")){
                            String locationOfSequence = plugin.papiNoColour(p,commands.get(i).split("\\s")[1]);
                            List<String> commandsSequence = cf.getStringList(locationOfSequence);
                            commandsAfterSequence.remove(i);
                            commandsAfterSequence.addAll(i,commandsSequence);
                        }
                    }
                    commands = commandsAfterSequence;
                    for (int i = 0; commands.size() - 1 >= i; i++) {
                        try {
                            if (commands.get(i).split("\\s")[0].equalsIgnoreCase("right=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("right=", "").trim());
                                commands.set(i, commands.get(i).replace("RIGHT=", "").trim());
                                if (e.isLeftClick() || (e.isShiftClick() && e.isLeftClick()) || (e.isShiftClick() && e.isRightClick())) {
                                    continue;
                                }
                            } else if (commands.get(i).split("\\s")[0].equalsIgnoreCase("rightshift=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("rightshift=", "").trim());
                                commands.set(i, commands.get(i).replace("RIGHTSHIFT=", "").trim());
                                if (e.isLeftClick() || (!e.isShiftClick() && e.isRightClick())) {
                                    continue;
                                }
                            }
                            if (commands.get(i).split("\\s")[0].equalsIgnoreCase("left=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("left=", "").trim());
                                commands.set(i, commands.get(i).replace("LEFT=", "").trim());
                                if (e.isRightClick() || (e.isShiftClick() && e.isRightClick()) || (e.isShiftClick() && e.isLeftClick())) {
                                    continue;
                                }
                            } else if (commands.get(i).split("\\s")[0].equalsIgnoreCase("leftshift=")) {
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("leftshift=", "").trim());
                                commands.set(i, commands.get(i).replace("LEFTSHIFT=", "").trim());
                                if (e.isRightClick() || (!e.isShiftClick() && e.isLeftClick())) {
                                    continue;
                                }
                            }
                            if (!e.isLeftClick() && !e.isRightClick()) {
                                continue;
                            }
                        } catch (Exception click) {
                            //skip if you can't do this
                        }
                        try {
                            commands.set(i, commands.get(i).replaceAll("%cp-clicked%", clicked.getType().toString()));
                        } catch (Exception mate) {
                            commands.set(i, commands.get(i).replaceAll("%cp-clicked%", "Air"));
                        }
                        //end custom PlaceHolders
                        int val = plugin.commandTags.commandPayWall(p,commands.get(i));
                        if(val == 0){
                            return;
                        }
                        if(val == 2){
                            plugin.commandTags.commandTags(p, commands.get(i));
                        }
                    }
                }
            }
        }
        //stop duplicate
        p.updateInventory();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(p.isOp() || p.hasPermission("*.*")){
            if(plugin.update) {
                p.sendMessage(ChatColor.WHITE + "CommandPanels " + ChatColor.DARK_RED + "is not running the latest version! A new version is available at");
                p.sendMessage(ChatColor.RED + "https://www.spigotmc.org/resources/command-panels-custom-guis.67788/");
            }
        }
    }

    public void redirectPanel(Player p, ConfigurationSection cf, String section, int slot){
        String tag = plugin.config.getString("config.format.tag") + " ";
        if(!cf.contains("item." + slot + section + ".redirect") || !cf.contains("item." + slot + section + ".redirect.panel")) {
            return;
        }
        String panelName = cf.getString("item." + slot + section + ".redirect.panel");
        ConfigurationSection panelConfig = null;
        for(String[] tempName : plugin.panelNames){
            if(tempName[0].equals(panelName)){
                panelConfig = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + plugin.panelFiles.get(Integer.parseInt(tempName[1])))).getConfigurationSection("panels." + panelName);
                break;
            }
        }
        if(panelConfig == null){
            p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.nopanel")));
            return;
        }
        if(cf.contains("item." + slot + section + ".redirect.replacements")){
            if(!panelConfig.getString("panels." + panelName + ".panelType").equalsIgnoreCase("temporary") && plugin.config.getBoolean("config.refresh-panels")){
                p.sendMessage(plugin.papi(tag + ChatColor.RED + panelName + " panel type needs to be temporary to replace elements."));
            }
            for(String sectionName : cf.getConfigurationSection("item." + slot + section + ".redirect.replacements").getKeys(false)){
                ConfigurationSection temp = cf.getConfigurationSection("item." + slot + section + ".redirect.replacements." + sectionName);
                panelConfig.set("panels." + panelName + ".item." + sectionName, temp);
            }
        }
        plugin.openVoids.openCommandPanel(p, p, panelName, panelConfig, false);
    }
}
