package me.rockyhawk.commandpanels.manager.open;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class PreLoadCommands {
    private final Context ctx;

    public PreLoadCommands(Context ctx) {
        this.ctx = ctx;
    }

    public void executePreLoad(Panel panel, PanelPosition position, Player p) {
        if (panel.getConfig().contains("pre-load-commands")) {
            try {
                ctx.commandRunner.runCommands(panel, position, p, panel.getConfig().getStringList("pre-load-commands"), null);
            } catch (Exception e) {
                ctx.debug.send(e, p, ctx);
            }
        }
    }
}