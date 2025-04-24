package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public interface PlaceholderResolver {
    boolean canResolve(String identifier);
    String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx);
}