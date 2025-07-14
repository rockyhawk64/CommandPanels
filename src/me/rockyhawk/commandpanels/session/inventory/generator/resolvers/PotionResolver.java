package me.rockyhawk.commandpanels.session.inventory.generator.resolvers;

import me.rockyhawk.commandpanels.session.inventory.generator.ItemResolver;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PotionResolver implements ItemResolver {

    private static final Set<Material> POTION_MATERIALS = EnumSet.of(
            Material.POTION,
            Material.SPLASH_POTION,
            Material.LINGERING_POTION,
            Material.TIPPED_ARROW
    );

    @Override
    public void resolve(ItemStack item, Map<String, Object> itemData) {
        if (!POTION_MATERIALS.contains(item.getType())) return;

        if (!(item.getItemMeta() instanceof PotionMeta meta)) return;

        if (meta.getBasePotionType() != null) {
            itemData.put("potion", meta.getBasePotionType().name());
        }

        if (meta.hasColor()) {
            Color color = meta.getColor();
            itemData.put("potion-color", Map.of(
                    "r", color.getRed(),
                    "g", color.getGreen(),
                    "b", color.getBlue()
            ));
        }
    }
}
