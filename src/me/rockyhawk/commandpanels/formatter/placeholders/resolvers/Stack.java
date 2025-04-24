package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class Stack implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("stack-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String matNumber = identifier.replace("stack-", "");
        int amount;
        try {
            amount = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber)).getAmount();
        } catch (NullPointerException er) {
            amount = 0;
        }
        return String.valueOf(amount);

    }
}