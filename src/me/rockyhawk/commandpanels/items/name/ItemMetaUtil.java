package me.rockyhawk.commandpanels.items.name;

import com.google.common.collect.ImmutableMultimap;
import me.rockyhawk.commandpanels.Context;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemMetaUtil {

    private final Context ctx;

    public ItemMetaUtil(Context ctx) {
        this.ctx = ctx;
    }

    public void hideAttributes(ItemMeta meta) {
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        if (ctx.version.isAtLeast("1.20.5")) {
            meta.addItemFlags(ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP"));
        }

        if (ctx.version.isBelow("1.20.5")) {
            meta.addItemFlags(ItemFlag.valueOf("HIDE_POTION_EFFECTS"));
        }

        if (ctx.version.isAtLeast("1.20")) {
            meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        }

        if (ctx.version.isAtLeast("1.17")) {
            meta.addItemFlags(ItemFlag.HIDE_DYE);
        }

        if (ctx.version.isAtLeast("1.14")) {
            meta.setAttributeModifiers(ImmutableMultimap.of());
        }
    }
}