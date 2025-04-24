package me.rockyhawk.commandpanels.manager.open;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class PanelCommandExecutor {
    private final Context ctx;

    public PanelCommandExecutor(Context ctx) {
        this.ctx = ctx;
    }

    public void executeOpenCommands(Panel panel, PanelPosition position, Player p) {
        if (panel.getConfig().contains("commands-on-open")) {
            try {
                ctx.commandRunner.runCommands(panel, position, p, panel.getConfig().getStringList("commands-on-open"), null);
            } catch (Exception e) {
                p.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.error") + " commands-on-open: " + panel.getConfig().getString("commands-on-open")));
            }
        }
    }
}
