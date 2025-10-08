package me.rockyhawk.commandpanels.openwithitem.events;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

public class UtilsChestSortEvent implements Listener {
    Context ctx;
    public UtilsChestSortEvent(Context pl) {
        this.ctx = pl;
    }

    // Called reflectively from Context via a generic EventExecutor to avoid compile-time dependency on ChestSort API
    public void handleChestSortEvent(Event event){
        Player debugPlayer = null;
        try {
            Object playerObj = event.getClass().getMethod("getPlayer").invoke(event);
            if (playerObj == null) return;
            if (playerObj instanceof Player) {
                debugPlayer = (Player) playerObj;
            }
            String playerName = (playerObj instanceof HumanEntity) ? ((HumanEntity) playerObj).getName() : null;
            if (playerName != null && ctx.openPanels.hasPanelOpen(playerName, PanelPosition.Top)) {
                // e.setCancelled(true)
                event.getClass().getMethod("setCancelled", boolean.class).invoke(event, true);
                return;
            }
            if (!ctx.plugin.openWithItem) return;

            Object inv = event.getClass().getMethod("getInventory").invoke(event);
            if (inv != null) {
                InventoryType type = (InventoryType) inv.getClass().getMethod("getType").invoke(inv);
                if (type == InventoryType.PLAYER) {
                    for (String slot : ctx.hotbar.stationaryItems.get(((HumanEntity) playerObj).getUniqueId()).list.keySet()) {
                        // e.setUnmovable(int)
                        event.getClass().getMethod("setUnmovable", int.class).invoke(event, Integer.parseInt(slot));
                    }
                }
            }
        } catch (Exception ex) {
            ctx.debug.send(ex, debugPlayer, ctx);
        }
    }
}
