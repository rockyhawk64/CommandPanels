package me.rockyhawk.commandpanels;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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
        if(!plugin.openPanels.hasPanelOpen(p.getName()) || e.getSlotType() == InventoryType.SlotType.OUTSIDE || e.getClick() == ClickType.DOUBLE_CLICK){
            return;
        }
        ConfigurationSection cf = plugin.openPanels.getOpenPanel(p.getName()); //this is the panel cf section

        if(e.getClickedInventory().getType() == InventoryType.CHEST){
            //loop through possible hasvalue/hasperm 1,2,3,etc

            //this loops through all the items in the panel
            boolean foundSlot = false;
            for(String slot : Objects.requireNonNull(cf.getConfigurationSection("item")).getKeys(false)){
                if(slot.equals(Integer.toString(e.getSlot()))){
                    foundSlot = true;
                }
            }
            if(!foundSlot){
                e.setCancelled(true);
                p.updateInventory();
                return;
            }

            String section = plugin.itemCreate.hasSection(cf.getConfigurationSection("item." + e.getSlot()), p);

            if(cf.contains("item." + e.getSlot() + section + ".itemType")){
                if(cf.getStringList("item." + e.getSlot() + section + ".itemType").contains("placeable")){
                    //skip if the item is a placeable
                    e.setCancelled(false);
                    return;
                }
            }

            e.setCancelled(true);
            p.updateInventory();

            //this will remove any pending user inputs, if there is already something there from a previous item
            for(int o = 0; plugin.userInputStrings.size() > o; o++){
                if(plugin.userInputStrings.get(o)[0].equals(p.getName())){
                    plugin.userInputStrings.remove(o);
                    o=o-1;
                }
            }

            if(cf.contains("item." + e.getSlot() + section + ".commands")) {
                List<String> commands = cf.getStringList("item." + e.getSlot() + section + ".commands");
                if (commands.size() != 0) {
                    //this will replace a sequence tag command with the commands from the sequence
                    List<String> commandsAfterSequence = commands;
                    for (int i = 0; commands.size() - 1 >= i; i++) {
                        if(commands.get(i).startsWith("sequence=")){
                            String locationOfSequence = commands.get(i).split("\\s")[1];
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
                        //start custom command placeholders
                        try {
                            commands.set(i, commands.get(i).replaceAll("%cp-clicked%", clicked.getType().toString()));
                        } catch (Exception mate) {
                            commands.set(i, commands.get(i).replaceAll("%cp-clicked%", "Air"));
                        }
                        //end custom command PlaceHolders
                        String command = plugin.papi(p,commands.get(i));
                        int val = plugin.commandTags.commandPayWall(p,command);
                        if(val == 0){
                            return;
                        }
                        if(val == 2){
                            plugin.commandTags.commandTags(p, command, commands.get(i));
                        }
                    }
                }
            }
        }
    }
}
