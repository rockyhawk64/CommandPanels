package me.rockyhawk.commandpanels.playerinventoryhandler;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.PanelOpenedEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventorySaver implements Listener {
    public YamlConfiguration inventoryConfig;

    CommandPanels plugin;
    public InventorySaver(CommandPanels pl) {
        this.plugin = pl;
    }

    public void saveInventoryFile(){
        try {
            inventoryConfig.save(plugin.getDataFolder() + File.separator + "inventories.yml");
        } catch (IOException s) {
            s.printStackTrace();
            plugin.debug(s,null);
        }
    }

    @EventHandler
    public void onOpen(PanelOpenedEvent e){
        if(e.getPosition() != PanelPosition.Top) {
            addInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        //drop the players inventory if a panel is open in the inventory
        if (plugin.openPanels.hasPanelOpen(e.getEntity().getName(), PanelPosition.Middle) || plugin.openPanels.hasPanelOpen(e.getEntity().getName(), PanelPosition.Bottom)) {
            e.getDrops().clear();
            e.getDrops().addAll(Arrays.asList(plugin.inventorySaver.getNormalInventory(e.getEntity())));
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e){
        if(e.getEntity() instanceof HumanEntity) {
            Player p = (Player)e.getEntity();
            //move the item into the players inventory instead of the panel
            if (plugin.openPanels.hasPanelOpen(p.getName(), PanelPosition.Middle) || plugin.openPanels.hasPanelOpen(p.getName(), PanelPosition.Bottom)) {
                plugin.inventorySaver.addItem(p,e.getItem().getItemStack());
                e.getItem().remove();
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerJoined(PlayerJoinEvent e){
        restoreInventory(e.getPlayer(), PanelPosition.Top);
    }

    public void restoreInventory(Player p, PanelPosition position){
        if(p == null){
            return;
        }
        if(plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Middle) || plugin.openPanels.hasPanelOpen(p.getName(),PanelPosition.Bottom)){
            if(position == PanelPosition.Bottom){
                for(int s = 0; s < 9; s++){
                    p.getInventory().setItem(s,null);
                }
            }else if(position == PanelPosition.Middle){
                for(int s = 9; s < 36; s++){
                    p.getInventory().setItem(s,null);
                }
            }
            return;
        }
        if(inventoryConfig.isSet(p.getUniqueId().toString())){
            p.getInventory().setContents(plugin.itemSerializer.itemStackArrayFromBase64(inventoryConfig.getString(p.getUniqueId().toString())));
            inventoryConfig.set(p.getUniqueId().toString(),null);
        }
    }

    public void addInventory(Player p){
        if(!inventoryConfig.contains(p.getUniqueId().toString())){
            inventoryConfig.set(p.getUniqueId().toString(),plugin.itemSerializer.itemStackArrayToBase64(p.getInventory().getContents()));
            //will clear items except leave armour on the player while panels are open
            ItemStack[] armorContents = p.getInventory().getArmorContents().clone(); //Clone armour slots
            p.getInventory().clear(); //Clear inventory
            p.getInventory().setArmorContents(armorContents); //Place armour back in slots
        }
    }

    public ItemStack[] getNormalInventory(Player p){
        if(hasNormalInventory(p)){
            return p.getInventory().getContents();
        }else{
            return plugin.itemSerializer.itemStackArrayFromBase64(inventoryConfig.getString(p.getUniqueId().toString()));
        }
    }

    public boolean hasNormalInventory(Player p){
        return !inventoryConfig.isSet(p.getUniqueId().toString());
    }

    public void addItem(Player p, ItemStack item){
        if(hasNormalInventory(p)){
            if (p.getInventory().firstEmpty() >= 0) {
                p.getInventory().addItem(item);
                return;
            }
        }else {
            List<ItemStack> cont = new ArrayList<>(Arrays.asList(getNormalInventory(p)));
            boolean found = false;
            for (int i = 0; 36 > i; i++){
                if(cont.get(i) == null){
                    cont.set(i,item);
                    found = true;
                    break;
                }
                if(cont.get(i).isSimilar(item)){
                    cont.get(i).setAmount(cont.get(i).getAmount() + item.getAmount());
                    found = true;
                    break;
                }
            }
            if(found){
                inventoryConfig.set(p.getUniqueId().toString(), plugin.itemSerializer.itemStackArrayToBase64(cont.toArray(new ItemStack[0])));
                return;
            }
        }
        p.getLocation().getWorld().dropItemNaturally(p.getLocation(), item);
    }
}
