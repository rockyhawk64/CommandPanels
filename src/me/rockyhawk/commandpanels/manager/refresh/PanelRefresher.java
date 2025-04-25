package me.rockyhawk.commandpanels.manager.refresh;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.events.PanelOpenedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PanelRefresher implements Listener {
    protected final Context ctx;
    protected RefreshUtils refreshUtils = new RefreshUtils();

    public PanelRefresher(Context ctx) {
        this.ctx = ctx;
    }

    @EventHandler
    public void onPanelOpen(PanelOpenedEvent e) {
        if (e.isCancelled() || !ctx.configHandler.isTrue("config.refresh-panels")) {
            return;
        }

        Panel panel = e.getPanel();
        Player player = e.getPlayer();

        if (refreshUtils.isStaticPanel(panel)) return;
        refreshUtils.removeUnsupportedSounds(panel);

        int refreshDelay = refreshUtils.getRefreshDelay(panel, ctx);
        int animateValue = refreshUtils.getAnimateValue(panel);

        new RefreshTask(ctx, e, panel, player, refreshDelay, animateValue).runTaskTimer(ctx.plugin, 1, 1);
    }
}