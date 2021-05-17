package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class Utils implements Listener {
    CommandPanels plugin;
    public Utils(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onItemDrag(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(!plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        if(e.getInventory().getType() != InventoryType.PLAYER){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPanelClick(InventoryClickEvent e) {
        //when clicked on a panel
        Player p = (Player)e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        if(!plugin.openPanels.hasPanelOpen(p.getName()) || e.getSlotType() == InventoryType.SlotType.OUTSIDE || e.getClick() == ClickType.DOUBLE_CLICK){
            return;
        }
        Panel panel = plugin.openPanels.getOpenPanel(p.getName()); //this is the panel cf section

        if(e.getSlot() == -999){return;}
        if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            e.setCancelled(true);
            return;
        }
        if(e.getClickedInventory().getType() == InventoryType.PLAYER){
            return;
        }

        //loop through possible hasvalue/hasperm 1,2,3,etc

        //this loops through all the items in the panel
        boolean foundSlot = false;
        for(String slot : Objects.requireNonNull(panel.getConfig().getConfigurationSection("item")).getKeys(false)){
            if(slot.equals(Integer.toString(e.getSlot()))){
                foundSlot = true;
            }
        }
        if(!foundSlot){
            e.setCancelled(true);
            return;
        }

        String section = plugin.itemCreate.hasSection(panel,panel.getConfig().getConfigurationSection("item." + e.getSlot()), p);

        if(panel.getConfig().contains("item." + e.getSlot() + section + ".itemType")){
            if(panel.getConfig().getStringList("item." + e.getSlot() + section + ".itemType").contains("placeable")){
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

        if(panel.getConfig().contains("item." + e.getSlot() + section + ".commands")) {
            List<String> commands = panel.getConfig().getStringList("item." + e.getSlot() + section + ".commands");
            if (commands.size() != 0) {
                //this will replace a sequence tag command with the commands from the sequence
                List<String> commandsAfterSequence = commands;
                for (int i = 0; commands.size() - 1 >= i; i++) {
                    if(commands.get(i).startsWith("sequence=")){
                        String locationOfSequence = commands.get(i).split("\\s")[1];
                        List<String> commandsSequence = panel.getConfig().getStringList(locationOfSequence);
                        commandsAfterSequence.remove(i);
                        commandsAfterSequence.addAll(i,commandsSequence);
                    }
                }
                commands = commandsAfterSequence;
                for (int i = 0; commands.size() - 1 >= i; i++) {
                    try {
                        switch(commands.get(i).split("\\s")[0]){
                            case "right=":{
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("right=", "").trim());
                                if (e.getClick() != ClickType.RIGHT) {
                                    continue;
                                }
                                break;
                            }
                            case "rightshift=":{
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("rightshift=", "").trim());
                                if (e.getClick() != ClickType.SHIFT_RIGHT) {
                                    continue;
                                }
                                break;
                            }
                            case "left=":{
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("left=", "").trim());
                                if (e.getClick() != ClickType.LEFT) {
                                    continue;
                                }
                                break;
                            }
                            case "leftshift=":{
                                //if commands is for right clicking, remove the 'right=' and continue
                                commands.set(i, commands.get(i).replace("leftshift=", "").trim());
                                if (e.getClick() != ClickType.SHIFT_LEFT) {
                                    continue;
                                }
                                break;
                            }
                            case "middle=":{
                                commands.set(i, commands.get(i).replace("middle=", "").trim());
                                if (e.getClick() != ClickType.MIDDLE) {
                                    continue;
                                }
                                break;
                            }
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

                    //make the command
                    String command = plugin.tex.papi(panel,p,commands.get(i));

                    int val = plugin.commandTags.commandPayWall(p,command);
                    if(val == 0){
                        return;
                    }
                    if(val == 2){
                        plugin.commandTags.runCommand(panel, p, commands.get(i));
                    }
                }
            }
        }
    }
}
