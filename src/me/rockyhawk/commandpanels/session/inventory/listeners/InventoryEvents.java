package me.rockyhawk.commandpanels.session.inventory.listeners;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.backend.InventoryCloseReason;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class InventoryEvents implements Listener {
    private final Context ctx;

    public InventoryEvents(Context ctx) {
        this.ctx = ctx;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ctx.inventoryPanels.closePacketSession(event.getPlayer(), InventoryCloseReason.QUIT, false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        ctx.inventoryPanels.closePacketSession(event.getPlayer(), InventoryCloseReason.DEATH, true);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        ctx.inventoryPanels.closePacketSession(event.getPlayer(), InventoryCloseReason.TELEPORT, true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !ctx.inventoryPanels.hasPacketSession(player)) {
            return;
        }

        ctx.inventoryPanels.closePacketSession(player, InventoryCloseReason.REPLACED, false);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !ctx.inventoryPanels.hasPacketSession(player)) {
            return;
        }

        ctx.inventoryPanels.closePacketSession(player, InventoryCloseReason.REPLACED, false);
    }
}
