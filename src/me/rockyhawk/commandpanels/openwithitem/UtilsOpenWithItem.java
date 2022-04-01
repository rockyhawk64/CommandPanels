package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.GetItemInHand;
import me.rockyhawk.commandpanels.ioclasses.GetItemInHand_Legacy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class UtilsOpenWithItem implements Listener {
    CommandPanels plugin;
    public UtilsOpenWithItem(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {
        //on a click when in any inventory
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        Player p = (Player)e.getWhoClicked();
        //get the item clicked, then loop through panel names after action isn't nothing
        if(e.getAction() == InventoryAction.NOTHING){return;}
        if(e.getSlot() == -999){return;}
        if(e.getClickedInventory() == null) {
            //skip if null to stop errors
            return;
        }
        if(e.getClickedInventory().getType() == InventoryType.PLAYER && !e.isCancelled()) {
            if (plugin.hotbar.stationaryExecute(e.getSlot(), p,e.getClick(), true)) {
                e.setCancelled(true);
                p.updateInventory();
            }
        }
    }
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e){
        //item right clicked only (not left because that causes issues when things are interacted with)
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        try {
            if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK || Objects.requireNonNull(e.getItem()).getType() == Material.AIR){
                return;
            }
        }catch(Exception b){
            return;
        }
        Player p = e.getPlayer();
        if(plugin.hotbar.itemCheckExecute(e.getItem(),p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        plugin.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        plugin.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //a new list instance has to be created with the dropped items to avoid ConcurrentModificationException
        for(ItemStack s : new ArrayList<>(e.getDrops())){
            try {
                if (plugin.nbt.getNBT(s, "CommandPanelsHotbar") != null) {
                    //do not remove items that are not stationary
                    if(!plugin.nbt.getNBT(s, "CommandPanelsHotbar").endsWith("-1")) {
                        e.getDrops().remove(s);
                    }
                }
            }catch(NullPointerException | IllegalArgumentException ignore){}
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        plugin.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        plugin.hotbar.stationaryItems.remove(e.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //if item dropped
        Player p = e.getPlayer();
        if(plugin.hotbar.itemCheckExecute(e.getItemDrop().getItemStack(),p,false,true)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e){
        if(!plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //cancel everything if holding item (item frames eg)
        Player p = e.getPlayer();
        ItemStack clicked;
        if(Bukkit.getVersion().contains("1.8")){
            clicked =  new GetItemInHand_Legacy(plugin).itemInHand(p);
        }else{
            clicked = new GetItemInHand(plugin).itemInHand(p);
        }
        if(plugin.hotbar.itemCheckExecute(clicked,p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
}
