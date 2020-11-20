package me.rockyhawk.commandpanels.ingameeditor;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class EditorUtils implements Listener {
    public YamlConfiguration tempEdit;
    public ArrayList<String> inventoryItemSettingsOpening = new ArrayList<>();
    CommandPanels plugin;
    public EditorUtils(CommandPanels pl) {
        this.plugin = pl;
        this.tempEdit = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + "temp.yml"));
    }
    @EventHandler
    public void onClickMainEdit(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        //if the inventory isn't the editor main window
        try {
            if (Objects.requireNonNull(e.getClickedInventory()).getType() != InventoryType.CHEST) {
                return;
            }
        }catch(NullPointerException nu){return;}
        if(!p.getOpenInventory().getTitle().equals(ChatColor.stripColor(plugin.papi("Command Panels Editor"))) || plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        if(e.getClickedInventory() != e.getView().getTopInventory()){
            return;
        }
        e.setCancelled(true);
        ArrayList<String> panelNames = new ArrayList<String>(); //all panels from ALL files (panel names)
        ArrayList<String> panelTitles = new ArrayList<String>(); //all panels from ALL files (panel titles)
        ArrayList<ConfigurationSection> panelYaml = new ArrayList<ConfigurationSection>(); //all panels from ALL files (panel yaml files)
        try {
            for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                String key;
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    key = s;
                    panelNames.add(plugin.papi( key));
                    panelTitles.add(plugin.papi( Objects.requireNonNull(temp.getString("panels." + key + ".title"))));
                    panelYaml.add(temp.getConfigurationSection("panels." + key));
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(e.getSlot() == 48){
            //previous page button
            try {
                if (Objects.requireNonNull(e.getCurrentItem()).getType() == Material.PAPER) {
                    plugin.editorGuis.openEditorGui(p, -1);
                    p.updateInventory();
                    return;
                }
            }catch(NullPointerException ignored){}
        }
        if(e.getSlot() == 49){
            //sunflower page index
            if(Objects.requireNonNull(e.getCurrentItem()).getType() == Material.SLIME_BALL){
                p.updateInventory();
                return;
            }
        }
        if(e.getSlot() == 50){
            //next page button
            try{
                if(Objects.requireNonNull(e.getCurrentItem()).getType() == Material.PAPER){
                    plugin.editorGuis.openEditorGui(p, 1);
                    p.updateInventory();
                    return;
                }
            }catch(NullPointerException ignored){}
        }
        if(e.getSlot() == 45){
            //exit button
            p.closeInventory();
            p.updateInventory();
            return;
        }
        if(e.getSlot() <= 44){
            //if panel slots are selected
            try{
                if(Objects.requireNonNull(e.getCurrentItem()).getType() != Material.AIR){
                    if(e.getClick().isLeftClick() && !e.getClick().isShiftClick()){
                        //if left click
                        int count = 0;
                        for(String panelName : panelNames){
                            if(panelName.equals(ChatColor.stripColor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName()))){
                                plugin.createGUI.openGui(panelName, p, panelYaml.get(count),3,0);
                                return;
                            }
                            count +=1;
                        }
                    }else{
                        //if right click
                        int count = 0;
                        for(String panelName : panelNames){
                            if(panelName.equals(ChatColor.stripColor(Objects.requireNonNull(e.getCurrentItem().getItemMeta()).getDisplayName()))){
                                plugin.editorGuis.openPanelSettings(p,panelName,panelYaml.get(count));
                                return;
                            }
                            count +=1;
                        }
                        p.updateInventory();
                    }
                }
            }catch(NullPointerException ignored){}
        }
        p.updateInventory();
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(e.getInventory().getType() != InventoryType.CHEST){
            return;
        }
        if(!p.getOpenInventory().getTitle().contains("Editing Panel:") || plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        String panelName = ""; //all panels from ALL files (panel names)
        String fileName = ""; //all panels from ALL files (panel names)
        YamlConfiguration file = new YamlConfiguration(); //all panels from ALL files (panel yaml files)
        boolean found = false;
        try {
            //neew to loop through files to get file names
            for(String fileTempName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                String key;
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    key = s;
                    if (e.getView().getTitle().equals("Editing Panel: " + key)) {
                        panelName = key;
                        fileName = fileTempName;
                        file = temp;
                        found = true;
                        break;
                    }
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(!found){
            return;
        }
        //this basically just determines if something is dragged.
        try {
            if (tempEdit.contains("panels." + panelName + ".temp." + p.getName() + ".material")) {
                if (e.getOldCursor().getType() != Material.matchMaterial(Objects.requireNonNull(tempEdit.getString("panels." + panelName + ".temp." + p.getName() + ".material")))) {
                    clearTemp(p, panelName);
                    return;
                }
            }
        }catch(Exception ex){
            return;
        }
        //I cannot use load temp because the Event type is different, also I need to loop through all the items
        if(tempEdit.contains("panels." + panelName + ".temp." + p.getName())){
            try {
                for (int slot : e.getInventorySlots()) {
                    file.set("panels." + panelName + ".item." + slot, tempEdit.get("panels." + panelName + ".temp." + p.getName()));
                    //stacks can't be saved to file because it is not accurate in drag drop cases
                    if(file.contains("panels." + panelName + ".item." + slot + ".stack")){
                        file.set("panels." + panelName + ".item." + slot + ".stack",null);
                    }
                    saveFile(fileName, file, true);
                }
            }catch(NullPointerException nu){
                plugin.debug(nu);
            }
        }
    }
    @EventHandler
    public void onInventoryEdit(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        if(e.getInventory().getType() != InventoryType.CHEST){
            return;
        }
        if(!p.getOpenInventory().getTitle().contains("Editing Panel:") || plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        String panelName = "";
        String fileName = "";
        YamlConfiguration file = new YamlConfiguration();
        boolean found = false;
        try {
            //neew to loop through files to get file names
            for(String tempName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + tempName));
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    if (e.getView().getTitle().equals("Editing Panel: " + s)) {
                        panelName = s;
                        fileName = tempName;
                        file = temp;
                        found = true;
                        break;
                    }
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(!found){
            return;
        }
        //change file below
        /*
        All item settings will be custom saved and carried over. When inventory is closed, then figure out where items are and allocate the settings to those items...
        This is so there is no errors with item amounts and also so settings can be moved around easily

        Load temp item if the item is dropped into the panel

        Save temp item if the item is picked up from inside the panel
        */
        if(e.getClick().isShiftClick() && e.getClickedInventory() == e.getView().getTopInventory()){
            if(e.getInventory().getItem(e.getSlot()) == null) {
                return;
            }
            onEditPanelClose(p,e.getInventory(),e.getView());
            inventoryItemSettingsOpening.add(p.getName());
            //refresh the yaml config
            file = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            plugin.editorGuis.openItemSettings(p,panelName,file.getConfigurationSection("panels." + panelName + ".item." + e.getSlot()), String.valueOf(e.getSlot()));
            p.updateInventory();
            return;
        }
        if(tempEdit.contains("panels." + panelName + ".temp." + p.getName() + ".material")) {
            if(!plugin.getHeads.ifSkullOrHead(Objects.requireNonNull(e.getCursor()).getType().toString())) {
                //if the material doesn't match and also isn't a PLAYER_HEAD
                if (e.getCursor().getType() != Material.matchMaterial(Objects.requireNonNull(tempEdit.getString("panels." + panelName + ".temp." + p.getName() + ".material")))) {
                    clearTemp(p, panelName);
                }
            }
        }
        if(e.getSlot() == -999){
            if(e.getCurrentItem() == null) {
                clearTemp(p, panelName);
            }
            return;
        }
        if(e.getClickedInventory().getType() != InventoryType.CHEST){
            clearTemp(p, panelName);
            return;
        }
        if(e.getAction() == InventoryAction.CLONE_STACK){
            saveTempItem(e, p, file, panelName);
            saveFile(fileName,file,true);
        }else if(e.getAction() == InventoryAction.PLACE_ALL){
            loadTempItem(e, p, file, fileName, panelName);
            clearTemp(p, panelName);
            saveFile(fileName,file,true);
        }else if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR){
            saveTempItem(e, p, file, panelName);
            saveFile(fileName,file,true);
            removeOldItem(e, p, file, fileName, panelName);
        }else if(e.getAction() == InventoryAction.DROP_ALL_CURSOR){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.DROP_ALL_SLOT){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.DROP_ONE_CURSOR){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.DROP_ONE_SLOT){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.HOTBAR_SWAP){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.PLACE_SOME){
            loadTempItem(e, p, file, fileName, panelName);
            saveFile(fileName,file,true);
        }else if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.PICKUP_ALL){
            saveTempItem(e, p, file, panelName);
            saveFile(fileName,file,true);
            removeOldItem(e, p, file, fileName, panelName);
        }else if(e.getAction() == InventoryAction.PICKUP_HALF){
            saveTempItem(e, p, file, panelName);
            saveFile(fileName,file,true);
        }else if(e.getAction() == InventoryAction.PICKUP_ONE){
            saveTempItem(e, p, file, panelName);
            saveFile(fileName,file,true);
        }else if(e.getAction() == InventoryAction.PICKUP_SOME){
            saveTempItem(e, p, file, panelName);
            saveFile(fileName,file,true);
        }else if(e.getAction() == InventoryAction.PLACE_ONE){
            loadTempItem(e, p, file, fileName, panelName);
            saveFile(fileName,file,true);
        }
    }

    @EventHandler
    public void onPlayerClosePanel(InventoryCloseEvent e){
        //this is put here to avoid conflicts, close panel if it is closed
        for(int i = 0; i < plugin.openPanels.openPanelsPN.size(); i++){
            if(plugin.openPanels.openPanelsPN.get(i)[0].equals(e.getPlayer().getName())){
                plugin.customCommand.removeCCP(plugin.openPanels.openPanelsPN.get(i)[1], e.getPlayer().getName());
                if (plugin.config.contains("config.panel-snooper")) {
                    if (Objects.requireNonNull(plugin.config.getString("config.panel-snooper")).trim().equalsIgnoreCase("true")) {
                        Bukkit.getConsoleSender().sendMessage("[CommandPanels] " + e.getPlayer().getName() + " Closed " + plugin.openPanels.openPanelsPN.get(i)[1]);
                    }
                }
                plugin.openPanels.openPanelsPN.remove(i);
                plugin.openPanels.openPanelsCF.remove(i);
                return;
            }
        }
        //do editor settings if it is not a regular panel
        if(inventoryItemSettingsOpening.contains(e.getPlayer().getName())) {
            inventoryItemSettingsOpening.remove(e.getPlayer().getName());
            return;
        }
        onEditPanelClose((Player) e.getPlayer(), e.getInventory(), e.getView());
    }

    @EventHandler
    public void onPanelSettings(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        String tag = plugin.config.getString("config.format.tag") + " ";
        try {
            if (Objects.requireNonNull(e.getClickedInventory()).getType() != InventoryType.CHEST) {
                return;
            }
        }catch(Exception outOf){
            //skip as player clicked outside the inventory
            return;
        }
        if(!p.getOpenInventory().getTitle().contains("Panel Settings:") || plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        e.setCancelled(true);
        String panelName = ""; //all panels from ALL files (panel names)
        boolean found = false;
        boolean hotbarItems = false;
        try {
            //neew to loop through files to get file names
            for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String key : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)){
                    if(e.getView().getTitle().equals("Panel Settings: " + key)){
                        panelName = key;
                        if(temp.contains("panels." + panelName + ".open-with-item")){
                            hotbarItems = true;
                        }
                        found = true;
                        break;
                    }
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(!found){
            return;
        }
        if(e.getSlot() == 1){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.perm"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Permission"));
            p.closeInventory();
        }
        if(e.getSlot() == 3){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.title"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Title"));
            p.closeInventory();
        }
        if(e.getSlot() == 5){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.sound-on-open"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Sound ID"));
            p.closeInventory();
        }
        if(e.getSlot() == 7){
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.commands.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Command"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.commands.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Command to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 21){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.delete"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Are you sure? (yes/no)"));
            p.closeInventory();
        }
        if(e.getSlot() == 23){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.rows"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Row Amount (1 to 6)"));
            p.closeInventory();
        }
        if(e.getSlot() == 13){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.empty"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Material ID"));
            p.closeInventory();
        }
        if(e.getSlot() == 15){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.commands-on-open.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Command"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.commands-on-open.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Command to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 17){
            //adds abilities to add and remove types
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.panelType.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Panel type"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.panelType.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Panel Type to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 25){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.disabled-worlds.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New World Name"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.disabled-worlds.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter World line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 11){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.name"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Name"));
            p.closeInventory();
        }
        if(e.getSlot() == 18){
            plugin.editorGuis.openEditorGui(p,0);
            p.updateInventory();
        }
        if(e.getSlot() == 40){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.material"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Material"));
            p.closeInventory();
        }
        if(e.getSlot() == 38 && hotbarItems){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.name"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Name"));
            p.closeInventory();
        }
        if(e.getSlot() == 36 && hotbarItems){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.lore.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Item Lore"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.lore.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter lore line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 42 && hotbarItems){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.stationary"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Location (1 to 9)"));
            p.closeInventory();
        }
    }

    @EventHandler
    public void onItemSettings(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        String tag = plugin.config.getString("config.format.tag") + " ";
        try {
            if (Objects.requireNonNull(e.getClickedInventory()).getType() != InventoryType.CHEST) {
                return;
            }
        }catch(Exception outOf){
            //skip as player clicked outside the inventory
            return;
        }
        if(!p.getOpenInventory().getTitle().contains("Item Settings:") || plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        e.setCancelled(true);
        String panelName = ""; //all panels from ALL files (panel names)
        YamlConfiguration panelYaml = null; //all panels from ALL files (panel names)
        boolean found = false;
        try {
            //loop through files to get file names
            for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String key : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)){
                    if(e.getView().getTitle().equals("Item Settings: " + key)){
                        panelName = key;
                        panelYaml = temp;
                        found = true;
                        break;
                    }
                }
                if(found){
                    break;
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(!found){
            return;
        }
        String itemSlot;
        try {
            itemSlot = ChatColor.stripColor(e.getView().getTopInventory().getItem(35).getItemMeta().getDisplayName().split("\\s")[2]);
        }catch(Exception ex){
            plugin.getServer().getConsoleSender().sendMessage("[CommandPanels] Could not get item slot");
            plugin.debug(ex);
            return;
        }
        if(e.getSlot() == 1){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item:" + itemSlot + ":name"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Item Name"));
            p.closeInventory();
        }
        if(e.getSlot() == 3){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item:" + itemSlot + ":commands:add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Item Command"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item:" + itemSlot + ":commands:remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter command line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 5){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item:" + itemSlot + ":enchanted"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Item Enchantment"));
            p.closeInventory();
        }
        if(e.getSlot() == 7){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item:" + itemSlot + ":potion"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Item Potion Effect"));
            p.closeInventory();
        }
        if(e.getSlot() == 13){
            //adds abilities to add and remove items
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item:" + itemSlot + ":duplicate:add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Duplicate Item Location/s"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item:" + itemSlot + ":duplicate:remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Duplicate Item/s to Remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 19){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item:" + itemSlot + ":lore:add"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Item Lore"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item:" + itemSlot + ":lore:remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter lore line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 21){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item:" + itemSlot + ":stack"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Item Stack (must be an integer)"));
            p.closeInventory();
        }
        if(e.getSlot() == 23){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item:" + itemSlot + ":customdata"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Custom Model Data"));
            p.closeInventory();
        }
        if(e.getSlot() == 25){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item:" + itemSlot + ":leatherarmor"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Leather Armor Colour"));
            p.closeInventory();
        }
        if(e.getSlot() == 31){
            //section includes the slot number at the front
            plugin.editorGuis.openItemSections(p,panelName,panelYaml.getConfigurationSection("panels." + panelName + ".item." + itemSlot), itemSlot);
            p.updateInventory();
        }
        if(e.getSlot() == 35){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item:" + itemSlot + ":head"});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter New Custom Material (eg. cps= self)"));
            p.closeInventory();
        }
        if(e.getSlot() == 27){
            if(itemSlot.contains(".")){
                String newSection = itemSlot.substring(0, itemSlot.lastIndexOf("."));
                plugin.editorGuis.openItemSections(p,panelName,panelYaml.getConfigurationSection("panels." + panelName + ".item." + newSection), newSection);
            }else {
                plugin.createGUI.openGui(panelName, p, panelYaml.getConfigurationSection("panels." + panelName), 3, 0);
            }
            p.updateInventory();
        }
    }

    //item section viewer click event
    @EventHandler
    public void onItemSection(InventoryClickEvent e) {
        Player p = (Player)e.getWhoClicked();
        String tag = plugin.config.getString("config.format.tag") + " ";
        try {
            if (Objects.requireNonNull(e.getClickedInventory()).getType() != InventoryType.CHEST) {
                return;
            }
        }catch(Exception outOf){
            //skip as player clicked outside the inventory
            return;
        }
        if(!p.getOpenInventory().getTitle().contains("Item Sections:") || plugin.openPanels.hasPanelOpen(p.getName())){
            return;
        }
        e.setCancelled(true);
        String panelName = ""; //all panels from ALL files (panel names)
        YamlConfiguration panelYaml = null;
        ConfigurationSection itemConfSection; //all panels from ALL files (panel names)
        boolean found = false;
        try {
            //loop through files to get file names
            YamlConfiguration temp;
            for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String key : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)){
                    if(e.getView().getTitle().equals("Item Sections: " + key)){
                        panelName = key;
                        panelYaml = temp;
                        found = true;
                        break;
                    }
                }
                if(found){
                    break;
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(!found){
            return;
        }

        //this section includes slot at front
        String section;
        try {
            section = ChatColor.stripColor(Objects.requireNonNull(Objects.requireNonNull(e.getView().getTopInventory().getItem(44)).getItemMeta()).getDisplayName().split("\\s")[2]);
        }catch(Exception ex){
            plugin.getServer().getConsoleSender().sendMessage("[CommandPanels] Could not get item slot");
            plugin.debug(ex);
            return;
        }
        itemConfSection = panelYaml.getConfigurationSection("panels." + panelName + ".item." + section);

        if(e.getSlot() <= 35){
            if(e.getInventory().getItem(e.getSlot()) != null){
                if(e.getClick().isLeftClick()) {
                    String newSection = section + "." + ChatColor.stripColor(e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName());
                    plugin.editorGuis.openItemSettings(p, panelName, itemConfSection.getConfigurationSection(ChatColor.stripColor(e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName())), newSection);
                    p.updateInventory();
                }else{
                    String itemNameSection = "." + ChatColor.stripColor(e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName());
                    plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"section.change." + section + itemNameSection});
                    p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Setting to change, eg, value:500"));
                    p.closeInventory();
                }
            }
        }

        if(e.getSlot() == 38){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"section.remove." + section});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Section name to remove, eg, hasperm or hasperm0"));
            p.closeInventory();
        }

        if(e.getSlot() == 42){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"section.add." + section});
            p.sendMessage(plugin.papi(tag + ChatColor.WHITE + "Enter Section name to add, eg, hasperm or hasperm0"));
            p.closeInventory();
        }

        if(e.getSlot() == 36){
            plugin.editorGuis.openItemSettings(p,panelName,itemConfSection, section);
            p.updateInventory();
        }
    }

    public void saveTempItem(InventoryClickEvent e, Player p, YamlConfiguration file, String panelName){
        //saves item to temp, using getslot
        tempEdit.set("panels." + panelName + ".temp." + p.getName(),file.get("panels." + panelName + ".item." + e.getSlot()));
        saveFile("temp.yml", tempEdit, false);
    }
    public void loadTempItem(InventoryClickEvent e, Player p, YamlConfiguration file,String fileName, String panelName){
        //loads temp item to the current item
        if(tempEdit.contains("panels." + panelName + ".temp." + p.getName())){
            file.set("panels." + panelName + ".item." + e.getSlot(),tempEdit.get("panels." + panelName + ".temp." + p.getName()));
            saveFile(fileName, file, true);
        }
    }
    public void removeOldItem(InventoryClickEvent e, Player p, YamlConfiguration file,String fileName, String panelName){
        //removes the old item from config, if it has been picked up (use this only after saving)
        file.set("panels." + panelName + ".item." + e.getSlot(),null);
        saveFile(fileName, file, true);
    }
    public void clearTemp(Player p, String panelName){
        //empty temp item
        tempEdit.set("panels." + panelName + ".temp." + p.getName(),null);
        saveFile("temp.yml", tempEdit, false);
    }
    public void saveFile(String fileName, YamlConfiguration file, boolean inPanelsFolder){
        try {
            if(inPanelsFolder){
                file.save(new File(plugin.panelsf + File.separator + fileName));
            }else{
                file.save(new File(plugin.getDataFolder() + File.separator + fileName));
            }
        } catch (IOException s) {
            plugin.debug(s);
        }
    }

    @SuppressWarnings("deprecation")
    public void onEditPanelClose(Player p, Inventory inv, InventoryView invView) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if(inv.getType() != InventoryType.CHEST){
            return;
        }
        if(!p.getOpenInventory().getTitle().contains("Editing Panel:")){
            return;
        }
        String panelName = ""; //all panels from ALL files (panel names)
        String fileName = ""; //all panels from ALL files (panel names)
        YamlConfiguration file = new YamlConfiguration(); //all panels from ALL files (panel yaml files)
        boolean found = false;
        try {
            //neew to loop through files to get file names
            for(String tempFile : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + tempFile));
                String key;
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    key = s;
                    if (invView.getTitle().equals("Editing Panel: " + key)) {
                        panelName = key;
                        fileName = tempFile;
                        file = temp;
                        found = true;
                        break;
                    }
                }
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(!found){
            return;
        }
        //save items as they appear
        file = plugin.itemCreate.generatePanelFile(panelName,inv,file);
        try {
            file.save(new File(plugin.panelsf + File.separator + fileName));
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Saved Changes!"));
        } catch (IOException s) {
            p.sendMessage(plugin.papi(tag + ChatColor.RED + "Could Not Save Changes!"));
            plugin.debug(s);
        }
        plugin.reloadPanelFiles();
    }
}