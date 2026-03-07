package me.rockyhawk.commandpanels.session.inventory.listeners;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.backend.InventoryCloseReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
}
