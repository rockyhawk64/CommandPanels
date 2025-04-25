package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class HotbarItemLoader {
    private final Context ctx;
    public GiveHotbarItem give;

    public HotbarItemLoader(Context pl) {
        ctx = pl;
        give = new GiveHotbarItem(ctx);
        reloadHotbarSlots(); //Run on initialisation
    }

    //stationary slots 0-8 are the hotbar, using 9-35 for inside the inventory
    public HashMap<UUID, PlayerManager> stationaryItems = new HashMap<>();

    //will compile the ArrayList {slot 0-4, index of panelNames}
    public void reloadHotbarSlots() {
        stationaryItems.clear();
        //Update hotbar items for all players when reloaded
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            ctx.hotbar.updateHotbarItems(p);
        }
    }

    //return true if found
    public boolean stationaryExecute(int slot, Player p, ClickType click, boolean openPanel){
        if(stationaryItems.get(p.getUniqueId()).list.containsKey(String.valueOf(slot))){
            if(openPanel) {
                try {
                    if (ctx.nbt.getNBTValue(p.getInventory().getItem(slot), "CommandPanelsHotbar") != null &&
                            !String.valueOf(ctx.nbt.getNBTValue(p.getInventory().getItem(slot), "CommandPanelsHotbar")).split(":")[1].equals(String.valueOf(slot))) {
                        return false;
                    }
                }catch(Exception ex){
                    return false;
                }
                Panel panel = stationaryItems.get(p.getUniqueId()).getPanel(String.valueOf(slot));
                //only open panel automatically if there are no commands and player world is not disabled
                if(!p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm"))){
                    return false;
                }
                if(!itemCheckExecute(p.getInventory().getItem(slot),p,false,false)){
                    return false;
                }
                if(panel.getHotbarSection(p).contains("commands")){
                    ctx.commands.runCommands(panel,PanelPosition.Top,p,panel.getHotbarSection(p).getStringList("commands"),click);
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
            if (Objects.equals(String.valueOf(ctx.nbt.getNBTValue(invItem, "CommandPanelsHotbar")), "")) {
                return false;
            }
        }catch(IllegalArgumentException | NullPointerException nu){
            return false;
        }
        for(Panel panel : ctx.plugin.panelList) {
            if(stationaryOnly){
                try {
                    if (ctx.nbt.getNBTValue(invItem, "CommandPanelsHotbar") != null &&
                            String.valueOf(ctx.nbt.getNBTValue(invItem, "CommandPanelsHotbar"))
                                    .split(":")[1].equals("-1")) {
                        continue;
                    }
                }catch(NullPointerException | IllegalArgumentException ignore){}
            }
            if(panel.hasHotbarItem()){
                if(ctx.nbt.getNBTValue(invItem, "CommandPanelsHotbar") != null &&
                        String.valueOf(ctx.nbt.getNBTValue(invItem, "CommandPanelsHotbar"))
                                .split(":")[0].equals(panel.getName())){
                    if(openPanel) {
                        //only open panel automatically if there are no commands and if world is not disabled
                        if(!ctx.openPanel.permission.isPanelWorldEnabled(p,panel.getConfig())){
                            return false;
                        }
                        if(panel.getHotbarSection(p).contains("commands")){
                            for(String command : panel.getHotbarSection(p).getStringList("commands")){
                                ctx.commands.runCommand(panel,PanelPosition.Top,p, command);
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
        if(!ctx.plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }

        //remove any old hotbar items
        stationaryItems.put(p.getUniqueId(),new PlayerManager());
        for(int i = 0; i <= 35; i++){
            try {
                if (ctx.nbt.getNBTValue(p.getInventory().getItem(i), "CommandPanelsHotbar") != null &&
                        !Objects.equals(String.valueOf(ctx.nbt.getNBTValue(p.getInventory().getItem(i), "CommandPanelsHotbar")), "")) {
                    //do not remove items that are not stationary
                    if(!String.valueOf(ctx.nbt.getNBTValue(p.getInventory().getItem(i), "CommandPanelsHotbar")).endsWith("-1")) {
                        p.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                }
            }catch(NullPointerException | IllegalArgumentException ignore){}
        }

        //add current hotbar items
        for(Panel panel : ctx.plugin.panelList) { //will loop through all the files in folder
            if(!ctx.openPanel.permission.isPanelWorldEnabled(p,panel.getConfig())){
                continue;
            }
            if (p.hasPermission("commandpanel.panel." + panel.getConfig().getString("perm")) && panel.hasHotbarItem()) {
                ItemStack s = panel.getHotbarItem(p);
                if(panel.getHotbarSection(p).contains("stationary")) {
                    p.getInventory().setItem(Integer.parseInt(panel.getHotbarSection(p).getString("stationary")),s);
                    stationaryItems.get(p.getUniqueId()).addSlot(panel.getHotbarSection(p).getString("stationary"),panel);
                }
            }
        }
        p.updateInventory();
    }
}