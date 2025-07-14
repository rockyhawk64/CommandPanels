package me.rockyhawk.commandpanels.session.inventory.generator.resolvers;

import me.rockyhawk.commandpanels.session.inventory.generator.ItemResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class DisplayNameResolver implements ItemResolver {
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.builder().character('&').build();

    @Override
    public void resolve(ItemStack item, Map<String, Object> itemData) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            itemData.put("name", legacy.serialize(meta.displayName()));
        }
    }
}
