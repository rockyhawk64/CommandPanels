package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class Damaged implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("damaged-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String matNumber = identifier.replace("damaged-", "");
        boolean damaged = false;
        ItemStack itm = p.getOpenInventory().getTopInventory().getItem((int)Double.parseDouble(matNumber));
        try {
            if(ctx.version.isBelow("1.13")){
                if(itm.getType().getMaxDurability() != 0) {
                    damaged = (itm.getType().getMaxDurability() - itm.getDurability()) < itm.getType().getMaxDurability();
                }
            }else {
                Damageable itemDamage = (Damageable) itm.getItemMeta();
                damaged = itemDamage.hasDamage();
            }
        } catch (NullPointerException er) {
            damaged = false;
        }
        return String.valueOf(damaged);
    }
}