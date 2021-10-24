package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class HotbarItemLoader {
    CommandPanels plugin;
    public HotbarItemLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    //stationary slots 0-8 are the hotbar, using 9-35 for inside the inventory
    HashMap<UUID,HotbarPlayerManager> stationaryItems = new HashMap<>();

    //will compile the ArrayList {slot 0-4, index of panelNames}
    public void reloadHotbarSlots() {
        stationaryItems.clear();
        //update hotbar items for all players when reloaded
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            plugin.hotbar.updateHotbarItems(p);
        }
    }

    //return true if found
    public boolean stationaryExecute(int slot, Player p, ClickType click, boolean openPanel){
        if(stationaryItems.get(p.getUniqueId()).list.containsKey(slot)){
            if(openPanel) {
                try {
                    if (!plugin.nbt.getNBT(p.getInventory().getItem(slot), "CommandPanelsHotbar").split(":")[1].equals(String.valueOf(slot))) {
                        return false;
                    }
                }catch(Exception ex){
                    return false;
                }
                Panel panel = stationaryItems.get(p.getUniqueId()).getPanel(slot);
                //only open panel automatically if there are no commands and player world is not disabled
                if(!p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))){
                    return false;
                }
                if(!itemCheckExecute(p.getInventory().getItem(slot),p,false,false)){
                    return false;
                }
                if(panel.getHotbarSection(p).contains("commands")){
                    plugin.commandTags.runCommands(panel,PanelPosition.Top,p,panel.getHotbarSection(p).getStringList("commands"),click);
                    return true;
                }
                panel.open(p, PanelPosition.Top);
            }
            return true;
        }
        return false;
    }

    //return true if found
    public boolean itemCheckExecute(ItemStack invItem, Player p, boolean openPanel, boolean stationaryOnly){
        try {
            if (plugin.nbt.getNBT(invItem, "CommandPanelsHotbar") == null) {
                return false;
            }
        }catch(IllegalArgumentException | NullPointerException nu){
            return false;
        }
        for(Panel panel : plugin.panelList) {
            if(stationaryOnly){
                try {
                    if (plugin.nbt.getNBT(invItem, "CommandPanelsHotbar").split(":")[1].equals("-1")) {
                        continue;
                    }
                }catch(NullPointerException | IllegalArgumentException ignore){}
            }
            if(panel.hasHotbarItem()){
                if(plugin.nbt.getNBT(invItem,"CommandPanelsHotbar").split(":")[0].equals(panel.getName())){
                    if(openPanel) {
                        //only open panel automatically if there are no commands and if world is not disabled
                        if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                            return false;
                        }
                        if(panel.getHotbarSection(p).contains("commands")){
                            for(String command : panel.getHotbarSection(p).getStringList("commands")){
                                plugin.commandTags.runCommand(panel,PanelPosition.Top,p, command);
                            }
                            return true;
                        }
                        panel.open(p,PanelPosition.Top);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void updateHotbarItems(Player p){
        /*
        If the player is using disabled-worlds/enabled-worlds
        and they change worlds, it will check if the player can have the item
        and if they can, it gives the item. This is because onRespawn doesn't
        give the item to the player in all the worlds that it could automatically.

        The player will of course need a plugin to split inventories between worlds
        for this to take effect. I don't want to delete the item on the wrong world
        because then it might overwrite one of their actual slots upon rejoining the enabled world.
         */
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }

        //remove any old hotbar items
        stationaryItems.put(p.getUniqueId(),new HotbarPlayerManager());
        for(int i = 0; i <= 35; i++){
            try {
                if (plugin.nbt.getNBT(p.getInventory().getItem(i), "CommandPanelsHotbar") != null) {
                    //do not remove items that are not stationary
                    if(!plugin.nbt.getNBT(p.getInventory().getItem(i), "CommandPanelsHotbar").endsWith("-1")) {
                        p.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                }
            }catch(NullPointerException | IllegalArgumentException ignore){}
        }

        //add current hotbar items
        for(Panel panel : plugin.panelList) { //will loop through all the files in folder
            if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                continue;
            }
            if (p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm")) && panel.hasHotbarItem()) {
                ItemStack s = panel.getHotbarItem(p);
                if(panel.getHotbarSection(p).contains("stationary")) {
                    p.getInventory().setItem(panel.getHotbarSection(p).getInt("stationary"),s);
                    stationaryItems.get(p.getUniqueId()).addSlot(panel.getHotbarSection(p).getInt("stationary"),panel);
                }
            }
        }
        p.updateInventory();
    }
}