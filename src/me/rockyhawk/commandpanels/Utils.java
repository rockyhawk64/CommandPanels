package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.commandtags.PaywallOutput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
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
        if(!plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top)){
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
        int clickedSlot = e.getSlot();
        ItemStack clicked = e.getCurrentItem();

        if(!plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Top) || e.getClick() == ClickType.DOUBLE_CLICK){
            return;
        }

        //set the panel to the top panel
        Panel panel = plugin.openPanels.getOpenPanel(p.getName(),PanelPosition.Top);

        if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            e.setCancelled(true);
        }

        if(e.getSlotType() == InventoryType.SlotType.OUTSIDE){
            //if the panel is clicked on the outside area of the GUI
            if (panel.getConfig().contains("outside-commands")) {
                try {
                    plugin.commandTags.runCommands(panel,PanelPosition.Top,p, panel.getConfig().getStringList("outside-commands"));
                }catch(Exception s){
                    plugin.debug(s,p);
                }
            }
            return;
        }

        PanelPosition position = PanelPosition.Top;
        if(e.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (e.getSlotType() == InventoryType.SlotType.CONTAINER) {
                if(plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Middle)) {
                    position = PanelPosition.Middle;
                    clickedSlot -= 9;
                }else{
                    e.setCancelled(itemsUnmovable(panel));
                    return;
                }
            } else{
                if(plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Bottom)) {
                    position = PanelPosition.Bottom;
                    //this is set to cancelled as if the command is to close the panel and there is a hotbar item in the same slot
                    //it will also trigger the hotbar item after the panel is closed
                    e.setCancelled(true);
                }else{
                    e.setCancelled(itemsUnmovable(panel));
                    return;
                }
            }
        }

        //the panels proper position
        panel = plugin.openPanels.getOpenPanel(p.getName(),position);

        //this loops through all the items in the panel
        boolean foundSlot = false;
        for(String slot : Objects.requireNonNull(panel.getConfig().getConfigurationSection("item")).getKeys(false)){
            if (slot.equals(Integer.toString(clickedSlot))) {
                foundSlot = true;
                break;
            }
        }
        if(!foundSlot){
            e.setCancelled(true);
            return;
        }

        //get the section of the slot that was clicked
        String section = plugin.itemCreate.hasSection(panel,position,panel.getConfig().getConfigurationSection("item." + clickedSlot), p);

        if(panel.getConfig().contains("item." + clickedSlot + section + ".itemType")){
            if(panel.getConfig().getStringList("item." + clickedSlot + section + ".itemType").contains("placeable")){
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

        if(panel.getConfig().contains("item." + clickedSlot + section + ".commands")) {
            List<String> commands = panel.getConfig().getStringList("item." + clickedSlot + section + ".commands");
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
                    String command = plugin.tex.placeholders(panel,position,p,commands.get(i));

                    PaywallOutput val = plugin.commandTags.commandPayWall(panel,p,command);
                    if(val == PaywallOutput.Blocked){
                        return;
                    }
                    if(val == PaywallOutput.NotApplicable){
                        plugin.commandTags.runCommand(panel,position, p, commands.get(i));
                    }
                }
            }
        }
    }

    private boolean itemsUnmovable(Panel panel){
        if(panel.getConfig().isSet("panelType")){
            //cancel event and return to signal no commands and no movement will occur
            return panel.getConfig().getStringList("panelType").contains("unmovable");
        }
        return false;
    }
}
