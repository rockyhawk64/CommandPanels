package me.rockyhawk.commandpanels.builder;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public abstract class PanelBuilder {
    protected final Context ctx;
    private final Player player;

    public PanelBuilder(Context ctx, Player player) {
        this.ctx = ctx;
        this.player = player;
    }

    public abstract void open(Panel panel);

    public Player getPlayer() {
        return player;
    }
}