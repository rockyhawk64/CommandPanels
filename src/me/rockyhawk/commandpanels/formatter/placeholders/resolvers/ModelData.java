package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class ModelData implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("modeldata-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String matNumber = identifier.replace("modeldata-", "");
        int modelData;
        try {
            modelData = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getItemMeta().getCustomModelData();
        } catch (NullPointerException er) {
            modelData = 0;
        }
        return String.valueOf(modelData);
    }
}