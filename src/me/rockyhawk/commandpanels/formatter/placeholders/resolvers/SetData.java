package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class SetData implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("setdata-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        try {
            String point_value = identifier.replace("setdata-", "");
            String command = "set-data= " + point_value.split(",")[0] + " " + point_value.split(",")[1];
            ctx.commandRunner.runCommand(panel,position,p, command);
            return "";
        }catch (Exception ex){
            ctx.debug.send(ex,p, ctx);
            return "Error";
        }
    }
}