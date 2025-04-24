package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Lore implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("lore-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String loreNumber = identifier.replace("lore-", "");
        StringBuilder lore = new StringBuilder();
        ItemStack item = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(loreNumber));
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasLore()) {
                List<String> ListLore = item.getItemMeta().getLore();
                for (String list : ListLore) {
                    lore.append(list).append("\n");
                }
            }
        }
        return lore.toString();
    }
}