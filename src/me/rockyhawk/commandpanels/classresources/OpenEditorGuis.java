package me.rockyhawk.commandpanels.classresources;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenEditorGuis {
    CommandPanels plugin;
    public OpenEditorGuis(CommandPanels pl) {
        plugin = pl;
    }

    public void openEditorGui(Player p, int pageChange) {
        Inventory i = Bukkit.createInventory(null, 54, "Command Panels Editor");
        ArrayList<String> panelNames = new ArrayList<String>(); //all panels from ALL files (panel names)
        ArrayList<String> panelTitles = new ArrayList<String>(); //all panels from ALL files (panel titles)
        ArrayList<ItemStack> panelItems = new ArrayList<ItemStack>(); //all panels from ALL files (panel materials)
        try {
            for(String fileName : plugin.panelFiles) { //will loop through all the files in folder
                YamlConfiguration temp = YamlConfiguration.loadConfiguration(new File(plugin.panelsf + File.separator + fileName));
                String key;
                if (!plugin.checkPanels(temp)) {
                    continue;
                }
                for (String s : Objects.requireNonNull(temp.getConfigurationSection("panels")).getKeys(false)) {
                    key = s;
                    panelNames.add(plugin.papi( key));
                    panelTitles.add(plugin.papi( Objects.requireNonNull(temp.getString("panels." + key + ".title"))));
                    if (temp.contains("panels." + key + ".open-with-item.material")) {
                        panelItems.add(plugin.itemCreate.makeItemFromConfig(temp.getConfigurationSection("panels." + key + ".open-with-item"), p, false, true));
                    } else {
                        panelItems.add(new ItemStack(Material.FILLED_MAP));
                    }
                }
            }
        } catch (Exception fail) {
            //could not fetch all panel names (probably no panels exist)
            plugin.debug(fail);
            return;
        }

        int pageNumber = 1;
        if (p.getOpenInventory().getTitle().equals("Command Panels Editor")) {
            pageNumber = Integer.parseInt(ChatColor.stripColor(Objects.requireNonNull(Objects.requireNonNull(p.getOpenInventory().getItem(49)).getItemMeta()).getDisplayName()).replace("Page ", ""));
        }
        //will add the difference
        pageNumber = pageNumber + pageChange;
        if (pageNumber <= 0) {
            //double check page number IS NOT under 1
            pageNumber = 1;
        }
        //get amount of pages total
        int pagesAmount = (int) Math.ceil(panelNames.size() / 45.0);
        //make all the bottom bar items
        ItemStack temp;
        temp = new ItemStack(Material.SUNFLOWER, 1);
        plugin.setName(temp, ChatColor.WHITE + "Page " + pageNumber, null, p, true, true);
        i.setItem(49, temp);
        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(temp, ChatColor.RED + "Exit Menu", null, p, true, true);
        i.setItem(45, temp);
        temp = new ItemStack(Material.BOOK, 1);
        List<String> lore = new ArrayList();
        lore.add(ChatColor.GRAY + "- Click on a panel to edit items.");
        lore.add(ChatColor.GRAY + "- Right click on a panel to edit settings.");
        lore.add(ChatColor.GRAY + "- To edit an item in a panel, shift click");
        lore.add(ChatColor.GRAY + "  on the item of choice.");
        lore.add(ChatColor.GRAY + "- When entering a value,");
        lore.add(ChatColor.GRAY + "  type 'remove' to set a");
        lore.add(ChatColor.GRAY + "  value to default, and use");
        lore.add(ChatColor.GRAY + "  '" + plugin.config.getString("config.input-cancel") + "' to cancel.");
        plugin.setName(temp, ChatColor.WHITE + "Panel Editor Tips", lore, p, true, true);
        i.setItem(53, temp);
        if (pageNumber != 1) {
            //only show previous page button if number is not one
            temp = new ItemStack(Material.PAPER, 1);
            plugin.setName(temp, ChatColor.WHITE + "Previous Page", null, p, true, true);
            i.setItem(48, temp);
        }
        if (pageNumber < pagesAmount) {
            //if page number is under pages amount
            temp = new ItemStack(Material.PAPER, 1);
            plugin.setName(temp, ChatColor.WHITE + "Next Page", null, p, true, true);
            i.setItem(50, temp);
        }
        int count = 0;
        int slot = 0;
        lore.clear();
        for (String panelName : panelNames) {
            //count is +1 because count starts at 0 not 1
            if ((pageNumber * 45 - 45) < (count + 1) && (pageNumber * 45) > (count)) {
                temp = panelItems.get(count);
                plugin.setName(temp, ChatColor.WHITE + panelName, lore, p, false, true);
                i.setItem(slot, temp);
                slot += 1;
            }
            count += 1;
        }
        p.openInventory(i);
    }

    public void openPanelSettings(Player p, String panelName, YamlConfiguration cf) {
        Inventory i = Bukkit.createInventory(null, 45, "Panel Settings: " + panelName);
        List<String> lore = new ArrayList();
        ItemStack temp;
        //remove if the player already had a string from previously
        for (int o = 0; plugin.editorInputStrings.size() > o; o++) {
            if (plugin.editorInputStrings.get(o)[0].equals(p.getName())) {
                plugin.editorInputStrings.remove(o);
                o = o - 1;
            }
        }
        //make all the items
        temp = new ItemStack(Material.WRITABLE_BOOK, 1);
        lore.add(ChatColor.GRAY + "Permission required to open panel");
        lore.add(ChatColor.GRAY + "commandpanel.panel.[insert]");
        if (cf.contains("panels." + panelName + ".perm")) {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "commandpanel.panel." + cf.getString("panels." + panelName + ".perm"));
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Permission", lore, p,true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Title of the Panel");
        if (cf.contains("panels." + panelName + ".title")) {
            lore.add(ChatColor.WHITE + "------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".title"));
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Title", lore, p,true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.JUKEBOX, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Sound when opening panel");
        if (cf.contains("panels." + panelName + ".sound-on-open")) {
            lore.add(ChatColor.WHITE + "------------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".sound-on-open")).toUpperCase());
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Sound", lore, p,true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.IRON_DOOR, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Custom command to open panel");
        if (cf.contains("panels." + panelName + ".command")) {
            lore.add(ChatColor.WHITE + "----------------------------");
            lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".command"));
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Command", lore, p,true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.LAVA_BUCKET, 1);
        lore.clear();
        lore.add(ChatColor.DARK_RED + "Permanently delete Panel");
        plugin.setName(temp, ChatColor.RED + "Delete Panel", lore, p,true, true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.PISTON, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many rows the panel will be");
        lore.add(ChatColor.GRAY + "choose an integer from 1 to 6");
        plugin.setName(temp, ChatColor.WHITE + "Panel Rows", lore, p,true, true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Fill empty slots with an item");
        if (cf.contains("panels." + panelName + ".empty")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".empty")).toUpperCase());
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Empty Item", lore, p,true, true);
        i.setItem(13, temp);

        temp = new ItemStack(Material.COMMAND_BLOCK, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Execute commands when opening");
        lore.add(ChatColor.GRAY + "- Left click to add command");
        lore.add(ChatColor.GRAY + "- Right click to remove command");
        if (cf.contains("panels." + panelName + ".commands-on-open")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panels." + panelName + ".commands-on-open")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Commands", lore, p,true, true);
        i.setItem(15, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Code name to open panel");
        lore.add(ChatColor.GRAY + "/cp [name]");
        lore.add(ChatColor.WHITE + "-----------------------");
        lore.add(ChatColor.WHITE + panelName);
        plugin.setName(temp, ChatColor.WHITE + "Panel Name", lore, p,true, true);
        i.setItem(11, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(temp, ChatColor.RED + "Back", null, p,true, true);
        i.setItem(18, temp);

        //This will create a wall of glass panes, separating panel settings with hotbar settings
        temp = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        plugin.setName(temp, ChatColor.WHITE + "", null, p,false, true);
        for(int d = 27; d < 36; d++){
            i.setItem(d, temp);
        }
        //This is the items for hotbar items (open-with-item)
        boolean hotbarItems = false;

        if(cf.contains("panels." + panelName + ".open-with-item.material")){
            hotbarItems = true;
            temp = plugin.itemCreate.makeItemFromConfig(cf.getConfigurationSection("panels." + panelName + ".open-with-item"), p, false, true);
        }else{
            temp = new ItemStack(Material.REDSTONE_BLOCK, 1);
        }
        lore.clear();
        lore.add(ChatColor.GRAY + "Current Item");
        if (cf.contains("panels." + panelName + ".open-with-item.material")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".open-with-item.material")).toUpperCase());
        }else{
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.RED + "DISABLED");
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Hotbar Item", lore, p,true, true);
        i.setItem(40, temp);

        if(hotbarItems) {
            temp = new ItemStack(Material.NAME_TAG, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Name for Hotbar item");
            if (cf.contains("panels." + panelName + ".open-with-item.name")) {
                lore.add(ChatColor.WHITE + "----------");
                lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("panels." + panelName + ".open-with-item.name")));
            }
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Item Name", lore, p, true, true);
            i.setItem(38, temp);

            temp = new ItemStack(Material.SPRUCE_SIGN, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Display a lore under the Hotbar item");
            lore.add(ChatColor.GRAY + "- Left click to add lore");
            lore.add(ChatColor.GRAY + "- Right click to remove lore");
            if (cf.contains("panels." + panelName + ".open-with-item.lore")) {
                lore.add(ChatColor.WHITE + "-------------------------------");
                int count = 1;
                for (String tempLore : cf.getStringList("panels." + panelName + ".open-with-item.lore")) {
                    lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                    count += 1;
                }
            }
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Lore", lore, p,true, true);
            i.setItem(36, temp);

            temp = new ItemStack(Material.BEDROCK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Hotbar location for the item");
            lore.add(ChatColor.GRAY + "choose a number from 1 to 9");
            if (cf.contains("panels." + panelName + ".open-with-item.stationary")) {
                lore.add(ChatColor.WHITE + "-------------------------");
                //in the editor, change the value of 0-8 to 1-9 for simplicity
                int location = cf.getInt("panels." + panelName + ".open-with-item.stationary") + 1;
                lore.add(ChatColor.WHITE + String.valueOf(location));
            }
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Item Location", lore, p, true, true);
            i.setItem(42, temp);

            temp = new ItemStack(Material.BOOK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "- To refresh changes use");
            lore.add(ChatColor.GRAY + "  /cp " + panelName + " item");
            lore.add(ChatColor.GRAY + "- Hotbar items will need a");
            lore.add(ChatColor.GRAY + "  name to work properly.");
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Item Tips", lore, p, true, true);
            i.setItem(44, temp);
        }

        p.openInventory(i);
    }

    public void openItemSettings(Player p, String panelName, YamlConfiguration cf, int itemNumber) {
        Inventory i = Bukkit.createInventory(null, 36, "Item Settings: " + panelName);
        List<String> lore = new ArrayList();
        ItemStack temp;
        //remove if the player already had a string from previously
        for (int o = 0; plugin.editorInputStrings.size() > o; o++) {
            if (plugin.editorInputStrings.get(o)[0].equals(p.getName())) {
                plugin.editorInputStrings.remove(o);
                o = o - 1;
            }
        }
        //make all the items
        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.add(ChatColor.GRAY + "Display name of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".name")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".name"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Name", lore, p,true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.COMMAND_BLOCK, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Execute commands when item is clicked");
        lore.add(ChatColor.GRAY + "- Left click to add command");
        lore.add(ChatColor.GRAY + "- Right click to remove command");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".commands")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panels." + panelName + ".item." + itemNumber + ".commands")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Commands", lore, p,true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display enchantment of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".enchanted")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".enchanted"));
            }
        } else {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "false");
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Enchantment", lore, p,true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.POTION, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display potion effect of the item in the Panel");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".potion")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".potion"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".potion"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Potion Effect", lore, p,true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.SPRUCE_SIGN, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display a lore under the item name");
        lore.add(ChatColor.GRAY + "- Left click to add lore line");
        lore.add(ChatColor.GRAY + "- Right click to remove lore line");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".lore")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panels." + panelName + ".item." + itemNumber + ".lore")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Lores", lore, p, true, true);
        i.setItem(19, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 2);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many of the item will be stacked");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".stack")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"), "")) {
                try {
                    temp.setAmount(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"))));
                } catch (Exception ignored) {
                }
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Stack Size", lore, p, true, true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.ANVIL, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Add Custom Model Data here");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".customdata")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".customdata"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".customdata"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Custom Model Data", lore, p, true, true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.LEATHER_HELMET, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Choose a colour for the armor");
        lore.add(ChatColor.GRAY + "use r,g,b or a spigot API color");
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".leatherarmor")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".leatherarmor"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("panels." + panelName + ".item." + itemNumber + ".leatherarmor"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Leather Armor Colour", lore, p, true, true);
        i.setItem(25, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(temp, ChatColor.RED + "Back", null, p, true, true);
        i.setItem(27, temp);

        if(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).startsWith("cps=")){
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
            if(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).equalsIgnoreCase("cps= self")){
                //if self
                SkullMeta meta = (SkullMeta) temp.getItemMeta();
                try {
                    assert meta != null;
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
                } catch (Exception var23) {
                    plugin.debug(var23);
                }
                temp.setItemMeta(meta);
            }else{
                //custom head
                temp = plugin.customHeads.getCustomHead(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).replace("cps=", "").trim());
            }
        }else if (Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).startsWith("%cp-player-online-")){
            //leave default for the find material tag
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
        }else if (Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).startsWith("hdb=")){
            //head database head
            temp = new ItemStack(Material.PLAYER_HEAD, 1);
            if (plugin.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
                HeadDatabaseAPI api;
                api = new HeadDatabaseAPI();
                try {
                    temp = api.getItemHead(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")).replace("hdb=", "").trim());
                } catch (Exception var22) {
                    plugin.debug(var22);
                }
            }
        }else{
            temp = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".material")))), 1);
        }
        try {
            temp.setAmount(Integer.parseInt(Objects.requireNonNull(cf.getString("panels." + panelName + ".item." + itemNumber + ".stack"))));
        } catch (Exception ex) {
            //skip
        }
        if (cf.contains("panels." + panelName + ".item." + itemNumber + ".enchanted")) {
            if (!Objects.equals(cf.getString("panels." + panelName + ".item." + itemNumber + ".enchanted"), "false")) {
                ItemMeta EnchantMeta;
                EnchantMeta = temp.getItemMeta();
                assert EnchantMeta != null;
                EnchantMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                EnchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                temp.setItemMeta(EnchantMeta);
            }
        }
        lore.clear();
        lore.add(ChatColor.GRAY + "Click to set custom material");
        lore.add(ChatColor.GRAY + "typically for custom heads");
        plugin.setName(temp, ChatColor.WHITE + "Item Slot " + itemNumber + " Preview", lore, p, true, true);
        i.setItem(35, temp);

        p.openInventory(i);
    }
}
