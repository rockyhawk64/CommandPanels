package me.rockyhawk.commandPanels.ingameEditor;

import me.rockyhawk.commandPanels.commandpanels;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class editorUtils implements Listener {
    public YamlConfiguration tempEdit;
    public ArrayList<String> inventoryItemSettingsOpening = new ArrayList<>();
    commandpanels plugin;
    public editorUtils(commandpanels pl) {
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
        if(!p.getOpenInventory().getTitle().equals(ChatColor.stripColor(plugin.papi("Command Panels Editor")))){
            return;
        }
        e.setCancelled(true);
        if(e.getClickedInventory() != e.getView().getTopInventory()){
            return;
        }
        ArrayList<String> panelNames = new ArrayList<String>(); //all panels from ALL files (panel names)
        ArrayList<String> panelTitles = new ArrayList<String>(); //all panels from ALL files (panel titles)
        ArrayList<YamlConfiguration> panelYaml = new ArrayList<YamlConfiguration>(); //all panels from ALL files (panel yaml files)
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
                    panelYaml.add(temp);
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
            if(Objects.requireNonNull(e.getCurrentItem()).getType() == Material.SUNFLOWER){
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
                                plugin.openGui(panelName, p, panelYaml.get(count),3,0);
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
        if(!p.getOpenInventory().getTitle().contains(ChatColor.GRAY + "Editing Panel:")){
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
                    if (e.getView().getTitle().equals(ChatColor.GRAY + "Editing Panel: " + plugin.papi( Objects.requireNonNull(temp.getString("panels." + key + ".title"))))) {
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
        if(!p.getOpenInventory().getTitle().contains(ChatColor.GRAY + "Editing Panel:")){
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
                String key;
                if(!plugin.checkPanels(temp)){
                    continue;
                }
                for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    key = s;
                    if (e.getView().getTitle().equals(ChatColor.GRAY + "Editing Panel: " + plugin.papi( Objects.requireNonNull(temp.getString("panels." + key + ".title"))))) {
                        panelName = key;
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
            if(e.getCurrentItem() == null) {
                return;
            }
            onEditPanelClose(p,e.getInventory(),e.getView());
            inventoryItemSettingsOpening.add(p.getName());
            //refresh the yaml config
            file = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
            plugin.editorGuis.openItemSettings(p,panelName,file,e.getSlot());
            return;
        }
        if(tempEdit.contains("panels." + panelName + ".temp." + p.getName() + ".material")) {
            if(Objects.requireNonNull(e.getCursor()).getType() != Material.PLAYER_HEAD) {
                //if the material doesn't match and also isn't a PLAYER_HEAD
                if (e.getCursor().getType() != Material.matchMaterial(Objects.requireNonNull(tempEdit.getString("panels." + panelName + ".temp." + p.getName() + ".material")))) {
                    clearTemp(p, panelName);
                }
            }
        }
        if(e.getAction() == InventoryAction.CLONE_STACK){
            saveTempItem(e, p, file, panelName);
        }else if(e.getAction() == InventoryAction.PLACE_ALL){
            loadTempItem(e, p, file, fileName, panelName);
            clearTemp(p, panelName);
        }else if(e.getAction() == InventoryAction.COLLECT_TO_CURSOR){
            //e.setCancelled(true);
            saveTempItem(e, p, file, panelName);
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
        }else if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR){
            e.setCancelled(true);
        }else if(e.getAction() == InventoryAction.PICKUP_ALL){
            saveTempItem(e, p, file, panelName);
        }else if(e.getAction() == InventoryAction.PICKUP_HALF){
            saveTempItem(e, p, file, panelName);
        }else if(e.getAction() == InventoryAction.PICKUP_ONE){
            saveTempItem(e, p, file, panelName);
        }else if(e.getAction() == InventoryAction.PICKUP_SOME){
            saveTempItem(e, p, file, panelName);
        }else if(e.getAction() == InventoryAction.PLACE_ONE){
            loadTempItem(e, p, file, fileName, panelName);
        }
    }
    @EventHandler
    public void onEditInventoryClose(InventoryCloseEvent e) {
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
        if(!p.getOpenInventory().getTitle().contains("Panel Settings:")){
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
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Permission"));
            p.closeInventory();
        }
        if(e.getSlot() == 3){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.title"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Title"));
            p.closeInventory();
        }
        if(e.getSlot() == 5){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.sound-on-open"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Sound ID"));
            p.closeInventory();
        }
        if(e.getSlot() == 7){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.command"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Command"));
            p.closeInventory();
        }
        if(e.getSlot() == 21){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.delete"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Are you sure? (yes/no)"));
            p.closeInventory();
        }
        if(e.getSlot() == 23){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.rows"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter Row Amount (1 to 6)"));
            p.closeInventory();
        }
        if(e.getSlot() == 13){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.empty"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Material ID"));
            p.closeInventory();
        }
        if(e.getSlot() == 15){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.commands-on-open.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Command"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "panel.commands-on-open.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter command line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 11){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.name"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Name"));
            p.closeInventory();
        }
        if(e.getSlot() == 18){
            plugin.editorGuis.openEditorGui(p,0);
            p.updateInventory();
        }
        if(e.getSlot() == 40){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.material"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Material"));
            p.closeInventory();
        }
        if(e.getSlot() == 38 && hotbarItems){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.name"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Name"));
            p.closeInventory();
        }
        if(e.getSlot() == 36 && hotbarItems){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.lore.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Item Lore"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.lore.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter lore line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 42 && hotbarItems){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"panel.hotbar.stationary"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter Location (1 to 9)"));
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
        if(!p.getOpenInventory().getTitle().contains("Item Settings:")){
            return;
        }
        e.setCancelled(true);
        String panelName = ""; //all panels from ALL files (panel names)
        YamlConfiguration panelYaml = null; //all panels from ALL files (panel names)
        boolean found = false;
        try {
            //neew to loop through files to get file names
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
            }
        }catch(Exception fail){
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }
        if(!found){
            return;
        }
        int itemSlot;
        try {
            itemSlot = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(e.getView().getTopInventory().getItem(35)).getItemMeta()).getDisplayName().split("\\s")[2]);
        }catch(Exception ex){
            plugin.getServer().getConsoleSender().sendMessage("[CommandPanels] Could not get item slot");
            plugin.debug(ex);
            return;
        }
        if(e.getSlot() == 1){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item." + itemSlot + ".name"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Item Name"));
            p.closeInventory();
        }
        if(e.getSlot() == 3){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item." + itemSlot + ".commands.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Item Command"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item." + itemSlot + ".commands.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter command line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 5){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item." + itemSlot + ".enchanted"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Item Enchantment"));
            p.closeInventory();
        }
        if(e.getSlot() == 7){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item." + itemSlot + ".potion"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Item Potion Effect"));
            p.closeInventory();
        }
        if(e.getSlot() == 19){
            //adds abilities to add and remove lines
            if(e.getClick().isLeftClick()) {
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item." + itemSlot + ".lore.add"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Item Lore"));
            }else{
                plugin.editorInputStrings.add(new String[]{p.getName(), panelName, "item." + itemSlot + ".lore.remove"});
                p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter lore line to remove (must be an integer)"));
            }
            p.closeInventory();
        }
        if(e.getSlot() == 21){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item." + itemSlot + ".stack"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Item Stack (must be an integer)"));
            p.closeInventory();
        }
        if(e.getSlot() == 23){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item." + itemSlot + ".customdata"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Custom Model Data"));
            p.closeInventory();
        }
        if(e.getSlot() == 25){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item." + itemSlot + ".leatherarmor"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Leather Armor Colour"));
            p.closeInventory();
        }
        if(e.getSlot() == 35){
            plugin.editorInputStrings.add(new String[]{p.getName(),panelName,"item." + itemSlot + ".head"});
            p.sendMessage(plugin.papi(tag + ChatColor.GREEN + "Enter New Custom Material (eg. cps= self)"));
            p.closeInventory();
        }
        if(e.getSlot() == 27){
            plugin.openGui(panelName, p, panelYaml,3,0);
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
        if(!p.getOpenInventory().getTitle().contains(ChatColor.GRAY + "Editing Panel:")){
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
                    if (invView.getTitle().equals(ChatColor.GRAY + "Editing Panel: " + plugin.papi( Objects.requireNonNull(temp.getString("panels." + key + ".title"))))) {
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
        ItemStack cont;
        for(int i = 0; inv.getSize() > i; i++){
            cont = inv.getItem(i);
            //repeat through all the items in the editor
            try{
                //make the item here
                if(cont == null){
                    //remove if items have been removed
                    if(file.contains("panels." + panelName + ".item." + i)){
                        file.set("panels." + panelName + ".item." + i, null);
                        continue;
                    }
                }
                if(file.contains("panels." + panelName + ".item." + i + ".material")){
                    if(Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("%") || Objects.requireNonNull(file.getString("panels." + panelName + ".item." + i + ".material")).contains("=")){
                        if(cont.getType() != Material.PLAYER_HEAD){
                            file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                        }
                    }else{
                        file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                    }
                }else{
                    file.set("panels." + panelName + ".item." + i + ".material", cont.getType().toString());
                }
                if(cont.getType() == Material.PLAYER_HEAD){
                    //inject base64 here
                    if(plugin.getHeadBase64(cont) != null){
                        file.set("panels." + panelName + ".item." + i + ".material", "cps= " + plugin.getHeadBase64(cont));
                    }
                    //check for skull owner
                    SkullMeta meta = (SkullMeta) cont.getItemMeta();
                    if(meta.hasOwner()){
                        file.set("panels." + panelName + ".item." + i + ".material", "cps= " + meta.getOwner());
                    }
                }
                if(cont.getAmount() != 1){
                    file.set("panels." + panelName + ".item." + i + ".stack", cont.getAmount());
                }
                if(!cont.getEnchantments().isEmpty()){
                    file.set("panels." + panelName + ".item." + i + ".enchanted", "true");
                }
                file.set("panels." + panelName + ".item." + i + ".name", Objects.requireNonNull(cont.getItemMeta()).getDisplayName());
                file.set("panels." + panelName + ".item." + i + ".lore", Objects.requireNonNull(cont.getItemMeta()).getLore());
            }catch(Exception n){
                //skip over an item that spits an error
            }
        }
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