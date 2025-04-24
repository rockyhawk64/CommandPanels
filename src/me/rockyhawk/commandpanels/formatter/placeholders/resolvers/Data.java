package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class Data implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("data-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String dataPoint = identifier.replace("data-", "");
        //get data from other user
        if(dataPoint.contains(",")){
            String dataName = dataPoint.split(",")[0];
            String playerName = dataPoint.split(",")[1];
            return ctx.panelData.getUserData(ctx.panelDataPlayers.getOffline(playerName),dataName);
        }else{
            return ctx.panelData.getUserData(p.getUniqueId(),dataPoint);
        }
    }
}