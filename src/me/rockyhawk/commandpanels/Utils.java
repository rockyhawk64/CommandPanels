package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interactives.input.PlayerInput;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.ArrayList;
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

        if(e.getSlotType() == InventoryType.SlotType.OUTSIDE){
            //if the panel is clicked on the outside area of the GUI
            if (panel.getConfig().contains("outside-commands")) {
                try {
                    plugin.commandRunner.runCommands(panel,PanelPosition.Top,p, panel.getConfig().getStringList("outside-commands"),e.getClick());
                }catch(Exception s){
                    plugin.debug(s,p);
                }
            }
            return;
        }

        PanelPosition position = PanelPosition.Top;
        if(e.getClickedInventory().getType() == InventoryType.PLAYER) {
            //cancel the event and return, stops items going from players inventory to the panels
            if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
                e.setCancelled(true);
                return;
            }

            //do player or panel inventory checks
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
        String foundSlot = null;
        for(String item : Objects.requireNonNull(panel.getConfig().getConfigurationSection("item")).getKeys(false)){
            String slot = plugin.tex.placeholdersNoColour(panel, position, p, item);
            if (slot.equals(Integer.toString(clickedSlot))) {
                foundSlot = item;
                break;
            }
        }

        // If we didn't find the slot directly, check if it's a duplicate
        if(foundSlot == null){
            // Loop through all items to check their duplicate configurations
            for(String item : Objects.requireNonNull(panel.getConfig().getConfigurationSection("item")).getKeys(false)){
                String section = plugin.has.hasSection(panel, position, panel.getConfig().getConfigurationSection("item." + item), p);

                // Check if this item has a duplicate configuration
                if(panel.getConfig().contains("item." + item + section + ".duplicate")) {
                    String duplicateValue = panel.getConfig().getString("item." + item + section + ".duplicate");

                    // Check if the clicked slot is in the duplicate configuration
                    if(isSlotInDuplicate(clickedSlot, duplicateValue)) {
                        foundSlot = item;
                        break;
                    }
                }
            }
        }

        if(foundSlot == null){
            e.setCancelled(true);
            return;
        }

        //get the section of the slot that was clicked
        String section = plugin.has.hasSection(panel,position,panel.getConfig().getConfigurationSection("item." + foundSlot), p);

        if(panel.getConfig().contains("item." + foundSlot + section + ".itemType")){
            if(panel.getConfig().getStringList("item." + foundSlot + section + ".itemType").contains("placeable")){
                //skip if the item is a placeable
                e.setCancelled(false);
                return;
            }
        }

        //updates the inventory to stop item glitches
        e.setCancelled(true);
        p.updateInventory();

        //if an item has an area for input instead of commands - Now with click type support
        if(panel.getConfig().contains("item." + foundSlot + section + ".player-input")) {
            List<String> playerInputs = panel.getConfig().getStringList("item." + foundSlot + section + ".player-input");
            List<String> filteredPlayerInputs = new ArrayList<>();

            //Check for cancel player input events.
            List<String> cancelCommands = null;
            if(panel.getConfig().contains("item." + foundSlot + section + ".player-input-cancel")){
                cancelCommands = panel.getConfig().getStringList("item." + foundSlot + section + ".player-input-cancel");
            }

            // Check for valid click types in player inputs
            ClickType click = e.getClick();
            boolean validClickFound = false;

            for(String playerInput : playerInputs) {
                String validInput = plugin.commandRunner.hasCorrectClick(playerInput, click);
                if(!validInput.isEmpty()) {
                    filteredPlayerInputs.add(validInput);
                    validClickFound = true;
                }
            }

            // Only process player input if we have valid inputs for this click type
            if(validClickFound) {
                plugin.inputUtils.playerInput.put(p, new PlayerInput(panel, filteredPlayerInputs, cancelCommands, click));
                plugin.inputUtils.sendInputMessage(panel, position, p);
            }
        }

        // Run commands section.
        if(panel.getConfig().contains("item." + foundSlot + section + ".commands")) {
            List<String> commands = panel.getConfig().getStringList("item." + foundSlot + section + ".commands");
            if (!commands.isEmpty()) {
                for (int i = 0; commands.size() > i; i++) {
                    try {
                        commands.set(i, commands.get(i).replaceAll("%cp-clicked%", e.getCurrentItem().getType().toString()));
                    } catch (Exception mate) {
                        commands.set(i, commands.get(i).replaceAll("%cp-clicked%", "AIR"));
                    }
                }
                if (panel.getConfig().contains("item." + foundSlot + section + ".multi-paywall")) {
                    plugin.commandRunner.runMultiPaywall(panel, position, p,
                            panel.getConfig().getStringList("item." + foundSlot + section + ".multi-paywall"),
                            commands, e.getClick());
                } else {
                    plugin.commandRunner.runCommands(panel, position, p, commands, e.getClick());
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

    //Helper method to see if the slot is a duplicate so it can copy commands.
    private boolean isSlotInDuplicate(int slot, String duplicateConfig) {
        if(duplicateConfig == null) return false;

        String[] dupeItems = duplicateConfig.split(",");

        for(String dupeItem : dupeItems) {
            dupeItem = dupeItem.trim(); // Remove any whitespace

            if(dupeItem.contains("-")) {
                // This is a range
                String[] range = dupeItem.split("-");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);

                if(slot >= min && slot <= max) {
                    return true;
                }
            } else {
                // This is a single slot
                try {
                    int dupeSlot = Integer.parseInt(dupeItem);
                    if(dupeSlot == slot) {
                        return true;
                    }
                } catch(NumberFormatException ignored) {}
            }
        }

        return false;
    }
}
