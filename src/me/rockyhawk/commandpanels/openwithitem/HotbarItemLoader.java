package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;

public class HotbarItemLoader {
    CommandPanels plugin;
    public HotbarItemLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    //stationary slots 0-8 are the hotbar, using 9-35 for inside the inventory
    HashMap<Integer,Panel> stationaryItems = new HashMap<>();

    //will compile the ArrayList {slot 0-4, index of panelNames}
    public void reloadHotbarSlots() {
        stationaryItems.clear();
        for (Panel panel : plugin.panelList) {
            if(panel.getConfig().contains("open-with-item.stationary")){
                stationaryItems.put(panel.getConfig().getInt("open-with-item.stationary"), panel.copy());
            }
        }
        //update hotbar items for all players when reloaded
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            plugin.hotbar.updateHotbarItems(p);
        }
    }

    public Set<Integer> getStationaryItemSlots(){
        return stationaryItems.keySet();
    }

    //return true if found
    public boolean stationaryExecute(int slot, Player p, boolean openPanel){
        for(int temp : stationaryItems.keySet()){
            if(slot == temp){
                if(openPanel) {
                    Panel panel = stationaryItems.get(temp);
                    //only open panel automatically if there are no commands and player world is not disabled
                    if(!p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))){
                        return false;
                    }
                    if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                        return false;
                    }
                    if(!itemCheckExecute(p.getInventory().getItem(slot),p,false,false)){
                        return false;
                    }
                    if(panel.getConfig().contains("open-with-item.commands")){
                        for(String command : panel.getConfig().getStringList("open-with-item.commands")){
                            plugin.commandTags.runCommand(panel,p, command);
                        }
                        return true;
                    }
                    panel.open(p);
                }
                return true;
            }
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
                if(!panel.getConfig().contains("open-with-item.stationary")){
                    continue;
                }
            }
            if(panel.hasHotbarItem()){
                if(plugin.nbt.getNBT(invItem,"CommandPanelsHotbar").equals(panel.getName())){
                    if(openPanel) {
                        //only open panel automatically if there are no commands and if world is not disabled
                        if(!plugin.panelPerms.isPanelWorldEnabled(p,panel.getConfig())){
                            return false;
                        }
                        if(panel.getConfig().contains("open-with-item.commands")){
                            for(String command : panel.getConfig().getStringList("open-with-item.commands")){
                                plugin.commandTags.runCommand(panel,p, command);
                            }
                            return true;
                        }
                        panel.open(p);
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
        for(int i = 0; i <= 35; i++){
            try {
                if (plugin.nbt.getNBT(p.getInventory().getItem(i), "CommandPanelsHotbar") != null) {
                    p.getInventory().setItem(i,new ItemStack(Material.AIR));
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
                if(panel.getConfig().contains("open-with-item.stationary")) {
                    p.getInventory().setItem(panel.getConfig().getInt("open-with-item.stationary"),s);
                }
            }
        }
    }
}