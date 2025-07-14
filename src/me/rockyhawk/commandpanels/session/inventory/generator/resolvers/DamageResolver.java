package me.rockyhawk.commandpanels.session.inventory.generator.resolvers;

import me.rockyhawk.commandpanels.session.inventory.generator.ItemResolver;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Map;

public class DamageResolver implements ItemResolver {

    @Override
    public void resolve(ItemStack item, Map<String, Object> itemData) {
        if (!(item.getItemMeta() instanceof Damageable damageMeta)) return;

        int damage = damageMeta.getDamage();
        if (damage > 0) {
            itemData.put("damage", damage);
        }
    }
}