package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interactives.input.PlayerInput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

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
                    plugin.commandTags.runCommands(panel,PanelPosition.Top,p, panel.getConfig().getStringList("outside-commands"),e.getClick());
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

        //if an item has an area for input instead of commands
        if(panel.getConfig().contains("item." + clickedSlot + section + ".player-input")) {
            plugin.inputUtils.playerInput.put(p,new PlayerInput(panel,panel.getConfig().getStringList("item." + clickedSlot + section + ".player-input")));
            plugin.inputUtils.sendMessage(panel,position,p);
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

                for (int i = 0; commands.size() > i; i++) {
                    try {
                        commands.set(i, commands.get(i).replaceAll("%cp-clicked%", e.getCurrentItem().getType().toString()));
                    } catch (Exception mate) {
                        commands.set(i, commands.get(i).replaceAll("%cp-clicked%", "AIR"));
                    }
                }

                plugin.commandTags.runCommands(panel,position,p,commands,e.getClick());
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
    // START - PATCH
    // @author thelonelywolf@https://github.com/TheLonelyWolf1
    // @date 06 August 2021
    
    // Calculate amount of EXP needed to level up
    public static int getExpToLevelUp(int level){
        if(level <= 15){
            return 2*level+7;
        } else if(level <= 30){
            return 5*level-38;
        } else {
            return 9*level-158;
        }
    }
 
    // Calculate total experience up to a level
    public static int getExpAtLevel(int level){
        if(level <= 16){
            return (int) (Math.pow(level,2) + 6*level);
        } else if(level <= 31){
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }
 
    // Calculate player's current EXP amount
    public static int getPlayerExp(Player player){
        int exp = 0;
        int level = player.getLevel();
     
        // Get the amount of XP in past levels
        exp += getExpAtLevel(level);
     
        // Get amount of XP towards next level
        exp += Math.round(getExpToLevelUp(level) * player.getExp());
     
        return exp;
    }
    
    // Give or take EXP
    public static int changePlayerExp(Player player, int exp){
     // Get player's current exp
     int currentExp = getPlayerExp(player);
     
     // Reset player's current exp to 0
     player.setExp(0);
     player.setLevel(0);
     
     // Give the player their exp back, with the difference
     int newExp = currentExp - exp;
     player.giveExp(newExp);
     
     // Return the player's new exp amount
     return newExp;
    }
     // END - PATCH
}
