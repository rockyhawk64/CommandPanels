package me.rockyhawk.commandpanels.session.inventory.listeners;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.CommandActions;
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

        // Run in scheduler which delays by one tick
        // This ensures inventory was actually closed and not refreshed
        player.getScheduler().run(ctx.plugin, task -> {

            // If inventory event was for a panel
            if (event.getInventory().getHolder() instanceof InventoryPanel panel) {

                // Run on close OR on refresh
                itemSanitiser(player.getInventory());
                itemDropper(player, event.getInventory());

                // Panel was just refreshed, not closed
                if(player.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel currentPanel
                        && currentPanel == panel){
                    return;
                }

                // Run close commands
                RequirementRunner requirements = new RequirementRunner(ctx);
                CommandRunner commands = new CommandRunner(ctx);
                CommandActions actions = panel.getCloseCommands();
                if (!requirements.processRequirements(panel, player, actions.requirements())) {
                    commands.runCommands(panel, player, actions.fail());
                    return;
                }
                commands.runCommands(panel, player, actions.commands());
            }

        },null);
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

            String itemName = item.getPersistentDataContainer().get(key, PersistentDataType.STRING);
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
        Inventory newTop = player.getOpenInventory().getTopInventory();

        // If it's a refresh, items are returned to the panel not the player
        boolean isRefreshOfSamePanel = inventory.getHolder() instanceof InventoryPanel oldPanel
                && newTop.getHolder() instanceof InventoryPanel newPanel
                && oldPanel == newPanel;

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().isAir()) continue;

            boolean isPanelItem = item.getPersistentDataContainer().has(key, PersistentDataType.STRING);
            if (isPanelItem) continue;

            if (isRefreshOfSamePanel) {
                ItemStack newSlotItem = newTop.getItem(i);
                boolean slotIsClaimedByPanel = newSlotItem != null && newSlotItem.getPersistentDataContainer().has(key, PersistentDataType.STRING);

                if (!slotIsClaimedByPanel) {
                    newTop.setItem(i, item);
                    continue;
                }
            }

            Map<Integer, ItemStack> leftovers = playerInv.addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }

        inventory.clear();
    }

}