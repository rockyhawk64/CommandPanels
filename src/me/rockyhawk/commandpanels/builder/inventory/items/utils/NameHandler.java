package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class NameHandler {

    private final Context ctx;
    private final LoreFormatter loreFormatter;

    public NameHandler(Context ctx) {
        this.ctx = ctx;
        this.loreFormatter = new LoreFormatter(ctx);
    }

    public ItemStack setName(ItemStack item, PanelItem panelItem, Player player) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        boolean attributes = Boolean.parseBoolean(
                ctx.text.parseTextToString(player,panelItem.attributes()));
        if (!attributes) {
            hideAttributes(meta);
        }

        boolean tooltip = Boolean.parseBoolean(
                ctx.text.parseTextToString(player,panelItem.tooltip()));
        if (!tooltip) {
            meta.setHideTooltip(true);
        }

        String name = panelItem.displayName();
        if (!name.isEmpty()) {
            meta.displayName(ctx.text.parseTextToComponent(player, name));
        }

        List<String> lore = panelItem.lore();
        if (!lore.isEmpty()) {
            List<Component> formattedLore = loreFormatter.format(lore, player);
            meta.lore(formattedLore);
        }

        // Add CommandPanels PersistentData
        NamespacedKey namespacedKey = new NamespacedKey(ctx.plugin, "panel_item_id");
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, name);
        item.setItemMeta(meta);

        return item;
    }

    private void hideAttributes(ItemMeta meta) {
        meta.addItemFlags(
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ARMOR_TRIM,
                ItemFlag.HIDE_DYE,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_PLACED_ON
        );
    }
}