package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class MaterialPlaceholder implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("material-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String matNumber = identifier.replace("material-", "");
        String material;
        try {
            material = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getType().toString();
            if(ctx.version.isBelow("1.13")){
                //add the ID to the end if it is legacy (eg, material:id)
                material = material + ":" + p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getData().getData();
            }
        } catch (NullPointerException er) {
            material = "AIR";
        }
        return material;
    }
}