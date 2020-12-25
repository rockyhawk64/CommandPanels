package me.rockyhawk.commandpanels.classresources;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        ArrayList<String> panelNames = new ArrayList<>(); //all panels from ALL files (panel names)
        ArrayList<String> panelTitles = new ArrayList<>(); //all panels from ALL files (panel titles)
        ArrayList<ItemStack> panelItems = new ArrayList<>(); //all panels from ALL files (panel materials)
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
                        panelItems.add(plugin.itemCreate.makeItemFromConfig(temp.getConfigurationSection("panels." + key + ".open-with-item"), p, false, true, true));
                    } else {
                        panelItems.add(new ItemStack(Material.PAPER));
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
        temp = new ItemStack(Material.SLIME_BALL, 1);
        plugin.setName(temp, ChatColor.WHITE + "Page " + pageNumber, null, p, true, true, true);
        i.setItem(49, temp);
        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(temp, ChatColor.RED + "Exit Menu", null, p, true, true, true);
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
        plugin.setName(temp, ChatColor.WHITE + "Panel Editor Tips", lore, p, true, true, true);
        i.setItem(53, temp);
        if (pageNumber != 1) {
            //only show previous page button if number is not one
            temp = new ItemStack(Material.PAPER, 1);
            plugin.setName(temp, ChatColor.WHITE + "Previous Page", null, p, true, true, true);
            i.setItem(48, temp);
        }
        if (pageNumber < pagesAmount) {
            //if page number is under pages amount
            temp = new ItemStack(Material.PAPER, 1);
            plugin.setName(temp, ChatColor.WHITE + "Next Page", null, p, true, true, true);
            i.setItem(50, temp);
        }
        int count = 0;
        int slot = 0;
        lore.clear();
        for (String panelName : panelNames) {
            //count is +1 because count starts at 0 not 1
            if ((pageNumber * 45 - 45) < (count + 1) && (pageNumber * 45) > (count)) {
                temp = panelItems.get(count);
                plugin.setName(temp, ChatColor.WHITE + panelName, lore, p, false, true, true);
                i.setItem(slot, temp);
                slot += 1;
            }
            count += 1;
        }
        p.openInventory(i);
    }

    @SuppressWarnings("deprecation")
    public void openPanelSettings(Player p, String panelName, ConfigurationSection cf) {
        Inventory i = Bukkit.createInventory(null, 45, ChatColor.stripColor("Panel Settings: " + panelName));
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
        temp = new ItemStack(Material.IRON_INGOT, 1);
        lore.add(ChatColor.GRAY + "Permission required to open panel");
        lore.add(ChatColor.GRAY + "commandpanel.panel.[insert]");
        if (cf.contains("perm")) {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "commandpanel.panel." + cf.getString("perm"));
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Permission", lore, p, true, true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.NAME_TAG, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Title of the Panel");
        if (cf.contains("title")) {
            lore.add(ChatColor.WHITE + "------------------");
            lore.add(ChatColor.WHITE + cf.getString("title"));
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Title", lore, p, true, true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.JUKEBOX, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Sound when opening panel");
        if (cf.contains("sound-on-open")) {
            lore.add(ChatColor.WHITE + "------------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("sound-on-open")).toUpperCase());
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Sound", lore, p, true, true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.IRON_DOOR, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Custom commands to open panel");
        lore.add(ChatColor.GRAY + "- Left click to add command");
        lore.add(ChatColor.GRAY + "- Right click to remove command");
        if (cf.contains("commands")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("commands")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Command", lore, p, true, true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.LAVA_BUCKET, 1);
        lore.clear();
        lore.add(ChatColor.DARK_RED + "Permanently delete Panel");
        plugin.setName(temp, ChatColor.RED + "Delete Panel", lore, p, true, true, true);
        i.setItem(21, temp);

        temp = new ItemStack(Material.LADDER, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many rows the panel will be");
        lore.add(ChatColor.GRAY + "choose an integer from 1 to 6");
        plugin.setName(temp, ChatColor.WHITE + "Panel Rows", lore, p, true, true, true);
        i.setItem(23, temp);

        temp = new ItemStack(Material.STONE, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Worlds that cannot access the panel");
        lore.add(ChatColor.GRAY + "- Left click to add world");
        lore.add(ChatColor.GRAY + "- Right click to remove world");
        if (cf.contains("disabled-worlds")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("disabled-worlds")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Disabled Worlds", lore, p, true, true, true);
        i.setItem(25, temp);

        temp = new ItemStack(Material.GLASS, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Fill empty slots with an item");
        if (cf.contains("empty")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("empty")).toUpperCase());
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Empty Item", lore, p, true, true, true);
        i.setItem(13, temp);

        temp = new ItemStack(Material.ANVIL, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Execute commands when opening");
        lore.add(ChatColor.GRAY + "- Left click to add command");
        lore.add(ChatColor.GRAY + "- Right click to remove command");
        if (cf.contains("commands-on-open")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("commands-on-open")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Commands", lore, p, true, true, true);
        i.setItem(15, temp);

        temp = new ItemStack(Material.STRING, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Special panel types");
        lore.add(ChatColor.GRAY + "- Left click to add panel type");
        lore.add(ChatColor.GRAY + "- Right click to remove panel type");
        if (cf.contains("panelType")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("panelType")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Types", lore, p, true, true, true);
        i.setItem(17, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Code name to open panel");
        lore.add(ChatColor.GRAY + "/cp [name]");
        lore.add(ChatColor.WHITE + "-----------------------");
        lore.add(ChatColor.WHITE + panelName);
        plugin.setName(temp, ChatColor.WHITE + "Panel Name", lore, p, true, true, true);
        i.setItem(11, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(temp, ChatColor.RED + "Back", null, p, true, true, true);
        i.setItem(18, temp);

        //This will create a wall of glass panes, separating panel settings with hotbar settings
        if(plugin.legacy.isLegacy()) {
            temp = new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1,(short)15);
        }else{
            temp = new ItemStack(Material.matchMaterial("BLACK_STAINED_GLASS_PANE"), 1);
        }
        plugin.setName(temp, ChatColor.WHITE + "", null, p,false, true, true);
        for(int d = 27; d < 36; d++){
            i.setItem(d, temp);
        }
        //This is the items for hotbar items (open-with-item)
        boolean hotbarItems = false;

        if(cf.contains("open-with-item.material")){
            hotbarItems = true;
            temp = plugin.itemCreate.makeItemFromConfig(cf.getConfigurationSection("open-with-item"), p, false, true, true);
        }else{
            temp = new ItemStack(Material.REDSTONE_BLOCK, 1);
        }
        lore.clear();
        lore.add(ChatColor.GRAY + "Current Item");
        if (cf.contains("open-with-item.material")) {
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("open-with-item.material")).toUpperCase());
        }else{
            lore.add(ChatColor.WHITE + "-----------------------");
            lore.add(ChatColor.RED + "DISABLED");
        }
        plugin.setName(temp, ChatColor.WHITE + "Panel Hotbar Item", lore, p, true, true, true);
        i.setItem(40, temp);

        if(hotbarItems) {
            temp = new ItemStack(Material.NAME_TAG, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Name for Hotbar item");
            if (cf.contains("open-with-item.name")) {
                lore.add(ChatColor.WHITE + "----------");
                lore.add(ChatColor.WHITE + Objects.requireNonNull(cf.getString("open-with-item.name")));
            }
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Item Name", lore, p, true, true, true);
            i.setItem(38, temp);

            temp = new ItemStack(Material.FEATHER, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Display a lore under the Hotbar item");
            lore.add(ChatColor.GRAY + "- Left click to add lore");
            lore.add(ChatColor.GRAY + "- Right click to remove lore");
            if (cf.contains("open-with-item.lore")) {
                lore.add(ChatColor.WHITE + "-------------------------------");
                int count = 1;
                for (String tempLore : cf.getStringList("open-with-item.lore")) {
                    lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                    count += 1;
                }
            }
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Lore", lore, p, true, true, true);
            i.setItem(36, temp);

            temp = new ItemStack(Material.BEDROCK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Hotbar location for the item");
            lore.add(ChatColor.GRAY + "choose a number from 0 to 33");
            if (cf.contains("open-with-item.stationary")) {
                lore.add(ChatColor.WHITE + "-------------------------");
                int location = cf.getInt("open-with-item.stationary") + 1;
                lore.add(ChatColor.WHITE + String.valueOf(location));
            }
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Item Location", lore, p, true, true, true);
            i.setItem(42, temp);

            temp = new ItemStack(Material.BOOK, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Execute commands from Hotbar Item");
            lore.add(ChatColor.GRAY + "- Left click to add command");
            lore.add(ChatColor.GRAY + "- Right click to remove command");
            lore.add(ChatColor.GRAY + "If commands are added, the item will");
            lore.add(ChatColor.GRAY + "no longer automatically open the panel");
            if (cf.contains("open-with-item.commands")) {
                lore.add(ChatColor.WHITE + "-------------------------------");
                int count = 1;
                for (String tempLore : cf.getStringList("open-with-item.commands")) {
                    lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                    count += 1;
                }
            }
            plugin.setName(temp, ChatColor.WHITE + "Hotbar Item Commands", lore, p, true, true,true);
            i.setItem(44, temp);
        }

        p.openInventory(i);
    }

    //section is similar to hassection, but with the slot eg, 1.hasperm.hasvalue
    public void openItemSettings(Player p, String panelName, ConfigurationSection cf, String section) {
        Inventory i = Bukkit.createInventory(null, 36, ChatColor.stripColor("Item Settings: " + panelName));
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
        if (cf.contains("name")) {
            if (!Objects.equals(cf.getString("name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("name"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Name", lore, p, true, true, true);
        i.setItem(1, temp);

        temp = new ItemStack(Material.ANVIL, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Execute commands when item is clicked");
        lore.add(ChatColor.GRAY + "- Left click to add command");
        lore.add(ChatColor.GRAY + "- Right click to remove command");
        if (cf.contains("commands")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("commands")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Commands", lore, p, true, true, true);
        i.setItem(3, temp);

        temp = new ItemStack(Material.ENCHANTED_BOOK, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display enchantment of the item in the Panel");
        if (cf.contains("enchanted")) {
            if (!Objects.equals(cf.getString("name"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("enchanted"));
            }
        } else {
            lore.add(ChatColor.WHITE + "--------------------------------");
            lore.add(ChatColor.WHITE + "false");
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Enchantment", lore, p, true, true, true);
        i.setItem(5, temp);

        temp = new ItemStack(Material.POTION, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display potion effect of the item in the Panel");
        if (cf.contains("potion")) {
            if (!Objects.equals(cf.getString("potion"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("potion"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Potion Effect", lore, p, true, true, true);
        i.setItem(7, temp);

        temp = new ItemStack(Material.PAPER, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Duplicate item visuals in other slots");
        lore.add(ChatColor.GRAY + "- Left click to add duplicate item/s");
        lore.add(ChatColor.GRAY + "- Right click to remove duplicate item/s");
        if (cf.contains("duplicate")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getString("duplicate").split(",")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Duplicates", lore, p, true, true, true);
        i.setItem(13, temp);

        temp = new ItemStack(Material.FEATHER, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Display a lore under the item name");
        lore.add(ChatColor.GRAY + "- Left click to add lore line");
        lore.add(ChatColor.GRAY + "- Right click to remove lore line");
        if (cf.contains("lore")) {
            lore.add(ChatColor.WHITE + "-----------------------------");
            int count = 1;
            for (String tempLore : cf.getStringList("lore")) {
                lore.add(ChatColor.WHITE + Integer.toString(count) + ") " + tempLore);
                count += 1;
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Lores", lore, p, true, true, true);
        i.setItem(19, temp);

        temp = new ItemStack(Material.ITEM_FRAME, 2);
        lore.clear();
        lore.add(ChatColor.GRAY + "How many of the item will be stacked");
        if (cf.contains("stack")) {
            if (!Objects.equals(cf.getString("stack"), "")) {
                try {
                    temp.setAmount(Integer.parseInt(Objects.requireNonNull(cf.getString("stack"))));
                } catch (Exception ignored) {
                }
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("stack"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Item Stack Size", lore, p, true, true, true);
        i.setItem(21, temp);

        if(!plugin.legacy.isLegacy()) {
            temp = new ItemStack(Material.PAINTING, 1);
            lore.clear();
            lore.add(ChatColor.GRAY + "Add Custom Model Data here");
            if (cf.contains("customdata")) {
                if (!Objects.equals(cf.getString("customdata"), "")) {
                    lore.add(ChatColor.WHITE + "--------------------------------");
                    lore.add(ChatColor.WHITE + cf.getString("customdata"));
                }
            }
            plugin.setName(temp, ChatColor.WHITE + "Custom Model Data", lore, p, true, true, true);
            i.setItem(23, temp);
        }

        /*show the compass that opens the viewer for the sections..
        it will only show the sections immidiatly under the other, so
        if there is a hasperm inside another hasperm, it will not appear
        until the other hasperm is opened.
         */
        temp = new ItemStack(Material.COMPASS, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "View the items different");
        lore.add(ChatColor.GRAY + "Sections and add complex values.");
        lore.add(ChatColor.WHITE + "--------------------------------");
        lore.add(ChatColor.WHITE + section);
        plugin.setName(temp, ChatColor.WHITE + "Item Sections", lore, p, true, true, true);
        i.setItem(31, temp);

        temp = new ItemStack(Material.LEATHER_HELMET, 1);
        lore.clear();
        lore.add(ChatColor.GRAY + "Choose a colour for the armor");
        lore.add(ChatColor.GRAY + "use r,g,b or a spigot API color");
        if (cf.contains("leatherarmor")) {
            if (!Objects.equals(cf.getString("leatherarmor"), "")) {
                lore.add(ChatColor.WHITE + "--------------------------------");
                lore.add(ChatColor.WHITE + cf.getString("leatherarmor"));
            }
        }
        plugin.setName(temp, ChatColor.WHITE + "Leather Armor Colour", lore, p, true, true, true);
        i.setItem(25, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(temp, ChatColor.RED + "Back", null, p, true, true, true);
        i.setItem(27, temp);

        temp = plugin.itemCreate.makeItemFromConfig(cf,p,false,false, true);
        lore.clear();
        lore.add(ChatColor.GRAY + "Click to set custom material");
        lore.add(ChatColor.GRAY + "typically for custom heads");
        plugin.setName(temp, ChatColor.WHITE + "Item Section " + section + " Preview", lore, p, true, true, true);
        i.setItem(35, temp);

        p.openInventory(i);
    }

    //section is similar to hassection, but with the slot eg, 1.hasperm.hasvalue
    public void openItemSections(Player p, String panelName, ConfigurationSection cf, String itemSection) {
        Inventory i = Bukkit.createInventory(null, 45, ChatColor.stripColor("Item Sections: " + panelName));
        ItemStack temp;
        int slot = 0;
        for(String section : cf.getKeys(false)){
            //get list of item sections
            if(slot > 35){
                break;
            }
            if(section.contains("hasperm") || section.contains("hasvalue") || section.contains("hasgreater")){
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Left click to open item");
                lore.add(ChatColor.GRAY + "Right click to change below settings");
                if(cf.contains(section + ".output")) {
                    lore.add(ChatColor.WHITE + "Output: " + ChatColor.GRAY + cf.getString(section + ".output"));
                }else{
                    lore.add(ChatColor.WHITE + "Output: " + ChatColor.GRAY + "true");
                }
                if(cf.contains(section + ".perm")) {
                    lore.add(ChatColor.WHITE + "Perm: " + ChatColor.GRAY + cf.getString(section + ".perm"));
                }
                if(cf.contains(section + ".value")) {
                    lore.add(ChatColor.WHITE + "Value: " + ChatColor.GRAY + cf.getString(section + ".value"));
                }
                if(cf.contains(section + ".compare")) {
                    lore.add(ChatColor.WHITE + "Compare: " + ChatColor.GRAY + cf.getString(section + ".compare"));
                }

                temp = plugin.itemCreate.makeItemFromConfig(cf.getConfigurationSection(section),p,false,false, true);
                plugin.setName(temp, ChatColor.AQUA + section, lore, p,false, true, true);
                i.setItem(slot, temp);
                slot++;
            }
        }

        temp = new ItemStack(Material.REDSTONE, 1);
        plugin.setName(temp, ChatColor.WHITE + "Remove Section", null, p, true, true, true);
        i.setItem(38, temp);

        temp = new ItemStack(Material.SLIME_BALL, 1);
        plugin.setName(temp, ChatColor.WHITE + "Add Section", null, p, true, true, true);
        i.setItem(42, temp);

        temp = new ItemStack(Material.BARRIER, 1);
        plugin.setName(temp, ChatColor.RED + "Back", null, p, true, true, true);
        i.setItem(36, temp);

        temp = new ItemStack(Material.BOOK, 1);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Section Types:");
        lore.add(ChatColor.GRAY + "- hasperm");
        lore.add(ChatColor.GRAY + "- hasvalue");
        lore.add(ChatColor.GRAY + "- hasgreater");
        plugin.setName(temp, ChatColor.WHITE + "Item Section " + itemSection, lore, p, true, true, true);
        i.setItem(44, temp);

        p.openInventory(i);
    }
}
