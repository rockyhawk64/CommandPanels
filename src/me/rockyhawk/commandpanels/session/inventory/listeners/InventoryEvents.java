package me.rockyhawk.commandpanels.session.inventory.listeners;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryEvents implements Listener {

    private final Context ctx;

    public InventoryEvents(Context ctx) {
        this.ctx = ctx;
    }

    @EventHandler
    public void onCloseEvent(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // Only remove the session if the player has one
        if (event.getInventory().getHolder() instanceof InventoryPanel) {
            itemSanitiser(player.getInventory());
            itemDropper(player, event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        closePanels(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        closePanels(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        closePanels(event.getPlayer());
    }

    private void closePanels(Player p){
        if (p.getOpenInventory() != null) { // Player has an open inventory
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel) {
                p.closeInventory();
            }
        }
    }

    private void itemSanitiser(PlayerInventory inv) {
        if (inv == null) return;

        List<ItemStack> toRemove = new ArrayList<>();
        NamespacedKey key = new NamespacedKey(ctx.plugin, "item_id");

        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            String itemName = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (itemName != null) {
                toRemove.add(item);
            }
        }

        // Remove after iteration to avoid concurrent modification issues
        for (ItemStack item : toRemove) {
            inv.remove(item);
        }
    }

    private void itemDropper(Player player, Inventory inventory) {
        NamespacedKey key = new NamespacedKey(ctx.plugin, "item_id");
        PlayerInventory playerInv = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            ItemMeta meta = item.getItemMeta();
            boolean isPanelItem = meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.STRING);

            if (!isPanelItem) {
                // Try to add item to player's inventory
                Map<Integer, ItemStack> leftovers = playerInv.addItem(item);

                // If some items didn't fit, drop those on the ground
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        }

        // Clear the original inventory to avoid duplication
        inventory.clear();
    }

}