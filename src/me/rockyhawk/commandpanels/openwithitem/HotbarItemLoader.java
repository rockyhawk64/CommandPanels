package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;

public class HotbarItemLoader {
    CommandPanels plugin;
    public HotbarItemLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    //stationary slots 0-8 are the hotbar, using 9-33 for inside the inventory
    HashMap<Integer,Panel> stationaryItems = new HashMap<>();

    //will compile the ArrayList {slot 0-4, index of panelNames}
    public void reloadHotbarSlots() {
        stationaryItems.clear();
        for (Panel panel : plugin.panelList) {
            if(panel.getConfig().contains("open-with-item.stationary")){
                stationaryItems.put(panel.getConfig().getInt("open-with-item.stationary"), panel.copy());
            }
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
        for(Panel panel : plugin.panelList) {
            if(stationaryOnly){
                if(!panel.getConfig().contains("open-with-item.stationary")){
                    continue;
                }
            }
            if(panel.hasHotbarItem()){
                ItemStack panelItem = panel.getHotbarItem(p);
                if(invItem != null && panelItem != null) {
                    panelItem.setAmount(invItem.getAmount());
                }else{
                    return false;
                }
                if(panelItem.isSimilar(invItem)){
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
}