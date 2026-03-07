package me.rockyhawk.commandpanels.session.inventory;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.rockyhawk.commandpanels.Context;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class InventoryPanelUpdater {

    private ScheduledTask heartbeatTask;
    private ScheduledTask updateTask;

    private final Map<String, Boolean> lastObservedPermStates = new HashMap<>();

    public void start(Context ctx, Player p, InventoryPanel panel) {
        stop(); // always clean slate

        Object viewToken = ctx.inventoryPanels.captureViewToken(p, panel);
        if (viewToken == null) {
            return;
        }

        startHeartbeat(ctx, p, panel, viewToken);

        int updateDelay = parseUpdateDelay(panel.getUpdateDelay());
        if (updateDelay > 0) {
            startUpdater(ctx, p, panel, viewToken, updateDelay);
        }
    }

    private void startHeartbeat(Context ctx, Player p, InventoryPanel panel, Object viewToken) {
        final boolean isUsingPermObserver = ctx.fileHandler.config.getBoolean("permission-observer");

        heartbeatTask = p.getScheduler().runAtFixedRate(
                ctx.plugin,
                (task) -> {
                    if (!ctx.inventoryPanels.matchesViewToken(p, panel, viewToken)) {
                        stop();
                        return;
                    }

                    // Handle permission observer
                    if (!isUsingPermObserver) return;
                    for (String node : panel.getObservedPerms()) {
                        boolean current = p.hasPermission(node);
                        Boolean previous = lastObservedPermStates.put(node, current);
                        if (previous != null && previous != current) {
                            ctx.panelOpenService.openPanel(panel, p, false, false);
                            return;
                        }
                    }
                },
                null,
                2,
                2
        );
    }

    private void startUpdater(Context ctx, Player p, InventoryPanel panel, Object viewToken, int updateDelay) {
        updateTask = p.getScheduler().runAtFixedRate(
                ctx.plugin,
                (task) -> {
                    if (!ctx.inventoryPanels.matchesViewToken(p, panel, viewToken)) {
                        stopUpdater(); // only stop this task, heartbeat may continue
                        return;
                    }

                    ctx.inventoryPanels.applySnapshot(p, panel);
                },
                null,
                updateDelay,
                updateDelay
        );
    }

    private int parseUpdateDelay(String delayStr) {
        if (delayStr != null && delayStr.matches("\\d+")) {
            return Integer.parseInt(delayStr);
        }
        return 20; // default
    }

    public void stop() {
        stopHeartbeat();
        stopUpdater();
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    private void stopUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
}
