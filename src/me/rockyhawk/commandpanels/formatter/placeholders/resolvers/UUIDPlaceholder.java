package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class UUIDPlaceholder implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("uuid-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String dataPoint = identifier.replace("uuid-", "");
        //get data from other user
        if(ctx.panelDataPlayers.getOffline(dataPoint) == null){
            return "unknown";
        }
        return ctx.panelDataPlayers.getOffline(dataPoint).toString();
    }
}