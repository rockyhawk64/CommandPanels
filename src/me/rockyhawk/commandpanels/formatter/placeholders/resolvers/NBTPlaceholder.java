package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class NBTPlaceholder implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("nbt-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String slot_key = identifier.replace("nbt-", "");
        Object value;
        value = ctx.nbt.getNBTValue(p.getOpenInventory().getTopInventory().getItem((int) Double.parseDouble(slot_key.split(":")[0])), slot_key.split(":")[1]);
        // Convert any object type to a string, handle null explicitly if desired
        return value == null ? "empty" : String.valueOf(value);
    }
}