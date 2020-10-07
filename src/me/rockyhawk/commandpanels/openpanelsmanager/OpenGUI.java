package me.rockyhawk.commandpanels.openpanelsmanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.Objects;

public class OpenGUI {
    CommandPanels plugin;
    public OpenGUI(CommandPanels pl) {
        this.plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public Inventory openGui(String panels, Player p, ConfigurationSection pconfig, int onOpen, int animateValue) {
        String tag = plugin.config.getString("config.format.tag") + " ";
        if (Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) < 7 && Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) > 0) {
            Inventory i;
            if (onOpen != 3) {
                //use the regular inventory
                i = Bukkit.createInventory(null, Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) * 9, plugin.papi(p, Objects.requireNonNull(pconfig.getString("title"))));
            } else {
                //this means it is the Editor window
                i = Bukkit.createInventory(null, Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) * 9, "Editing Panel: " + panels);
            }
            String item = "";

            String key;
            for (Iterator var6 = Objects.requireNonNull(pconfig.getConfigurationSection("item")).getKeys(false).iterator(); var6.hasNext(); item = item + key + " ") {
                key = (String) var6.next();
            }

            item = item.trim();
            int c;
            for (c = 0; item.split("\\s").length - 1 >= c; ++c) {
                if(item.equals("")){
                    //skip putting any items in the inventory if it is empty
                    break;
                }
                String section = "";
                //onOpen needs to not be 3 so the editor won't include hasperm and hasvalue, etc items
                if (onOpen != 3) {
                    section = plugin.itemCreate.hasSection(pconfig.getConfigurationSection("item." + Integer.parseInt(item.split("\\s")[c])), p);
                    //This section is for animations below here: VISUAL ONLY

                    //check for if there is animations inside the items section
                    if (pconfig.contains("item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                        //check for if it contains the animate that has the animvatevalue
                        if (pconfig.contains("item." + item.split("\\s")[c] + section + ".animate" + animateValue)) {
                            section = section + ".animate" + animateValue;
                        }
                    }
                }
                ItemStack s = plugin.itemCreate.makeItemFromConfig(Objects.requireNonNull(pconfig.getConfigurationSection("item." + item.split("\\s")[c] + section)), p, onOpen != 3, onOpen != 3);
                try {
                    i.setItem(Integer.parseInt(item.split("\\s")[c]), s);
                } catch (ArrayIndexOutOfBoundsException var24) {
                    plugin.debug(var24);
                    if (plugin.debug) {
                        p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " item: One of the items does not fit in the Panel!"));
                    }
                }
            }
            if (pconfig.contains("empty") && !Objects.equals(pconfig.getString("empty"), "AIR")) {
                for (c = 0; Integer.parseInt(Objects.requireNonNull(pconfig.getString("rows"))) * 9 - 1 >= c; ++c) {
                    boolean found = false;
                    if(!item.equals("")) {
                        for (int f = 0; item.split("\\s").length - 1 >= f; ++f) {
                            if (Integer.parseInt(item.split("\\s")[f]) == c) {
                                found = true;
                            }
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
                            if (empty.getType() == Material.AIR) {
                                continue;
                            }
                        } catch (IllegalArgumentException | NullPointerException var26) {
                            plugin.debug(var26);
                            p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " empty: " + pconfig.getString("empty")));
                            return null;
                        }

                        ItemMeta renamedMeta = empty.getItemMeta();
                        assert renamedMeta != null;
                        renamedMeta.setDisplayName(" ");
                        empty.setItemMeta(renamedMeta);
                        if (onOpen != 3) {
                            //only place empty items if not editing
                            i.setItem(c, empty);
                        }
                    }
                }
            }
            if (onOpen == 1 || onOpen == 3) {
                //onOpen 1 is default and 3 is for the editor
                p.openInventory(i);
            } else if (onOpen == 0) {
                //onOpen 0 will just refresh the panel
                plugin.legacy.setStorageContents(p,plugin.legacy.getStorageContents(i));
            } else if (onOpen == 2) {
                //will return the inventory, not opening it at all
                return i;
            }
            return i;
        } else {
            p.sendMessage(plugin.papi(tag + plugin.config.getString("config.format.error") + " rows: " + pconfig.getString("rows")));
            return null;
        }
    }
}
