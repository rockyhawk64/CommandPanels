package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.ioclasses.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.Set;

public class OpenGUI {
    CommandPanels plugin;
    public OpenGUI(CommandPanels pl) {
        this.plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public Inventory openGui(Panel panel, Player p, int onOpen, int animateValue) {
        ConfigurationSection pconfig = panel.getConfig();

        String title;
        if (onOpen != 3) {
            //regular inventory
            title = plugin.tex.papi(panel,p,pconfig.getString("title"));
        } else {
            //editor inventory
            title = "Editing Panel: " + panel.getName();
        }

        Inventory i;
        if(isNumeric(pconfig.getString("rows"))){
            i = Bukkit.createInventory(p, pconfig.getInt("rows") * 9, title);
        }else{
            i = Bukkit.createInventory(p, InventoryType.valueOf(pconfig.getString("rows")), title);
        }
            
        Set<String> itemList = pconfig.getConfigurationSection("item").getKeys(false);
        for (String item : itemList) {
            String section = "";
            //onOpen needs to not be 3 so the editor won't include hasperm and hasvalue, etc items
            if (onOpen != 3) {
                section = plugin.itemCreate.hasSection(panel,pconfig.getConfigurationSection("item." + Integer.parseInt(item)), p);
                //This section is for animations below here: VISUAL ONLY

                //check for if there is animations inside the items section
                if (pconfig.contains("item." + item + section + ".animate" + animateValue)) {
                    //check for if it contains the animate that has the animvatevalue
                    if (pconfig.contains("item." + item + section + ".animate" + animateValue)) {
                        section = section + ".animate" + animateValue;
                    }
                }
            }

            //will only add NBT if not an editor GUI
            ItemStack s = plugin.itemCreate.makeItemFromConfig(panel,Objects.requireNonNull(pconfig.getConfigurationSection("item." + item + section)), p, onOpen != 3, onOpen != 3, onOpen != 3);

            //This is for CUSTOM ITEMS
            if(pconfig.contains("item." + item + section + ".itemType")) {
                //this is for contents in the itemType section
                if (pconfig.getStringList("item." + item + section + ".itemType").contains("placeable") && onOpen == 0) {
                    //keep item the same, onOpen == 0 meaning panel is refreshing
                    i.setItem(Integer.parseInt(item), p.getOpenInventory().getItem(Integer.parseInt(item)));
                    continue;
                }
            }

            try {
                //place item into the GUI
                i.setItem(Integer.parseInt(item), s);
                //only place duplicate items in without the editor mode. These are merely visual and will not carry over commands
                if(pconfig.contains("item." + item + section + ".duplicate") && onOpen != 3) {
                    try {
                        String[] duplicateItems = pconfig.getString("item." + item + section + ".duplicate").split(",");
                        for (String tempDupe : duplicateItems) {
                            if (tempDupe.contains("-")) {
                                //if there is multiple dupe items, convert numbers to ints
                                int[] bothNumbers = new int[]{Integer.parseInt(tempDupe.split("-")[0]), Integer.parseInt(tempDupe.split("-")[1])};
                                for(int n = bothNumbers[0]; n <= bothNumbers[1]; n++){
                                    try{
                                        if(!pconfig.contains("item." + n)){
                                            i.setItem(n, s);
                                        }
                                    }catch(NullPointerException ignore){
                                        i.setItem(n, s);
                                    }
                                }
                            } else {
                                //if there is only one dupe item
                                try{
                                    if(!pconfig.contains("item." + Integer.parseInt(tempDupe))){
                                        i.setItem(Integer.parseInt(tempDupe), s);
                                    }
                                }catch(NullPointerException ignore){
                                    i.setItem(Integer.parseInt(tempDupe), s);
                                }
                            }
                        }
                    }catch(NullPointerException nullp){
                        plugin.debug(nullp,p);
                        p.closeInventory();
                        plugin.openPanels.closePanelForLoader(p.getName());
                    }
                }
            } catch (ArrayIndexOutOfBoundsException var24) {
                plugin.debug(var24,p);
                if (plugin.debug.isEnabled(p)) {
                    p.sendMessage(plugin.tex.papi(plugin.tag + plugin.config.getString("config.format.error") + " item: One of the items does not fit in the Panel!"));
                    p.closeInventory();
                    plugin.openPanels.closePanelForLoader(p.getName());
                }
            }
        }
        if (pconfig.contains("empty") && !Objects.equals(pconfig.getString("empty"), "AIR")) {
            for (int c = 0; i.getSize() > c; ++c) {
                boolean found = false;
                if(itemList.contains(String.valueOf(c))){
                    if(i.getItem(c) == null){
                        found = true;
                    }
                }
                if (!found) {
                    ItemStack empty;
                    try {
                        short id = 0;
                        if(pconfig.contains("emptyID")){
                            id = Short.parseShort(pconfig.getString("emptyID"));
                        }
                        empty = new ItemStack(Objects.requireNonNull(Material.matchMaterial(Objects.requireNonNull(pconfig.getString("empty")).toUpperCase())), 1,id);
                        empty = NBTEditor.set(empty,"CommandPanels","CommandPanels");
                        if (empty.getType() == Material.AIR) {
                            continue;
                        }
                    } catch (IllegalArgumentException | NullPointerException var26) {
                        plugin.debug(var26,p);
                        p.sendMessage(plugin.tex.papi(plugin.tag + plugin.config.getString("config.format.error") + " empty: " + pconfig.getString("empty")));
                        p.closeInventory();
                        plugin.openPanels.closePanelForLoader(p.getName());
                        return null;
                    }

                    ItemMeta renamedMeta = empty.getItemMeta();
                    assert renamedMeta != null;
                    renamedMeta.setDisplayName(" ");
                    empty.setItemMeta(renamedMeta);
                    if (onOpen != 3) {
                        //only place empty items if not editing
                        if(i.getItem(c) == null && !pconfig.contains("item." + c)) {
                            i.setItem(c, empty);
                        }
                    }
                }
            }
        }
        if (onOpen == 1 || onOpen == 3) {
            //onOpen 1 is default and 3 is for the editor
            plugin.openPanels.skipPanelClose.add(p.getName());
            plugin.openPanels.openPanelForLoader(p.getName(),panel);
            p.openInventory(i);
            plugin.openPanels.skipPanelClose.remove(p.getName());
        } else if (onOpen == 0) {
            //onOpen 0 will just refresh the panel
            plugin.legacy.setStorageContents(p,plugin.legacy.getStorageContents(i));
        } else if (onOpen == 2) {
            //will return the inventory, not opening it at all
            return i;
        }
        return i;
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
