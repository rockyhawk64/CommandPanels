package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.openwithitem.iteminhand.GetItemInHand;
import me.rockyhawk.commandpanels.openwithitem.iteminhand.GetItemInHandLegacy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class UtilsOpenWithItem implements Listener {
    Context ctx;
    public UtilsOpenWithItem(Context pl) {
        this.ctx = pl;
    }
    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {
        //on a click when in any inventory
        if(!ctx.plugin.openWithItem){
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
            if (ctx.hotbar.stationaryExecute(e.getSlot(), p,e.getClick(), true)) {
                e.setCancelled(true);
                p.updateInventory();
            }
        }
    }
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent e){
        //item right-clicked only (not left because that causes issues when things are interacted with)
        if(!ctx.plugin.openWithItem){
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
        if(ctx.hotbar.itemCheckExecute(e.getItem(),p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e)
    {
        //item right-clicked only (not left because that causes issues when things are interacted with)
        if(!ctx.plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }

        Player p = e.getPlayer();
        if(Bukkit.getVersion().contains("1.8")){
            if(ctx.hotbar.itemCheckExecute(e.getPlayer().getItemInHand(),p,false,false)){
                e.setCancelled(true);
                p.updateInventory();
            }
        }else{
            if(ctx.hotbar.itemCheckExecute(e.getPlayer().getInventory().getItemInMainHand(),p,false,false)){
                e.setCancelled(true);
                p.updateInventory();
            }
        }

    }
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e){
        ctx.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        ctx.hotbar.updateHotbarItems(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        if(!ctx.plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }

        //a new list instance has to be created with the dropped items to avoid ConcurrentModificationException
        try {
            for (ItemStack s : new ArrayList<>(e.getDrops())) {
                try {
                    if (ctx.nbt.getNBTValue(s, "CommandPanelsHotbar") != null &&
                            !String.valueOf(ctx.nbt.getNBTValue(s, "CommandPanelsHotbar")).isEmpty()) {
                        //do not remove items that are not stationary
                        if (!String.valueOf(ctx.nbt.getNBTValue(s, "CommandPanelsHotbar")).endsWith("-1")) {
                            e.getDrops().remove(s);
                        }
                    }
                } catch (NullPointerException | IllegalArgumentException ignore) {}
            }
        }catch (NullPointerException ignore){}
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        ctx.hotbar.updateHotbarItems(e.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        ctx.hotbar.stationaryItems.remove(e.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if(!ctx.plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //if item dropped
        Player p = e.getPlayer();
        if(ctx.hotbar.itemCheckExecute(e.getItemDrop().getItemStack(),p,false,true)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e){
        if(!ctx.plugin.openWithItem){
            //if none of the panels have open-with-item
            return;
        }
        //cancel everything if holding item (item frames eg)
        Player p = e.getPlayer();
        ItemStack clicked;
        if(Bukkit.getVersion().contains("1.8")){
            clicked =  new GetItemInHandLegacy().itemInHand(p);
        }else{
            clicked = new GetItemInHand().itemInHand(p);
        }
        if(ctx.hotbar.itemCheckExecute(clicked,p,true,false)){
            e.setCancelled(true);
            p.updateInventory();
        }
    }
}
