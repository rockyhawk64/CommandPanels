package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class CustomPlaceholders implements PlaceholderResolver {
    @Override
    public boolean canResolve(String identifier) {
        return true;
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        if(panel != null) {
            for (String placeholder : panel.placeholders.keys.keySet()) {
                if(identifier.equals(placeholder)) {
                    try {
                        return panel.placeholders.keys.get(placeholder);
                    } catch (Exception ex) {
                        ctx.debug.send(ex, p, ctx);
                        break;
                    }
                }
            }
        }
        return identifier;
    }
}