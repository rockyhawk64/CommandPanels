package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NamePlaceholder implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("name-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String nameNumber = identifier.replace("name-", "");
        String name;
        try {
            ItemStack item = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(nameNumber));
            name = item.getType().toString().replace("_"," ");
            if(item.hasItemMeta()){
                if(item.getItemMeta().hasDisplayName()){
                    name = item.getItemMeta().getDisplayName();
                }
            }
        } catch (NullPointerException er) {
            name = "";
        }
        return name;
    }
}