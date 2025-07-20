package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NameHandler {

    private final Context ctx;
    private final LoreFormatter loreFormatter;

    public NameHandler(Context ctx) {
        this.ctx = ctx;
        this.loreFormatter = new LoreFormatter(ctx);
    }

    public ItemStack setName(ItemStack item, PanelItem panelItem, Player player) {
        boolean attributes = Boolean.parseBoolean(
                ctx.text.parseTextToString(player,panelItem.attributes()));
        if (!attributes) {
            TooltipDisplay hideAttributes = TooltipDisplay.tooltipDisplay()
                    .addHiddenComponents(
                            DataComponentTypes.POTION_CONTENTS,
                            DataComponentTypes.ENCHANTMENTS,
                            DataComponentTypes.ATTRIBUTE_MODIFIERS,
                            DataComponentTypes.DYED_COLOR,
                            DataComponentTypes.TRIM,
                            DataComponentTypes.BANNER_PATTERNS
                    ).build();
            item.setData(DataComponentTypes.TOOLTIP_DISPLAY, hideAttributes);
        }

        boolean tooltip = Boolean.parseBoolean(
                ctx.text.parseTextToString(player,panelItem.tooltip()));
        if (!tooltip) {
            TooltipDisplay tooltipHidden = TooltipDisplay.tooltipDisplay().hideTooltip(true).build();
            item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipHidden);
        }

        String name = panelItem.displayName();
        if (!name.isEmpty()) {
            item.setData(DataComponentTypes.CUSTOM_NAME,
                    ctx.text.parseTextToComponent(player, name));
        }

        List<String> lore = panelItem.lore();
        if (!lore.isEmpty()) {
            List<Component> formattedLore = loreFormatter.format(lore, player);
            item.setData(DataComponentTypes.LORE, ItemLore.lore(
                    formattedLore));
        }
        return item;
    }
}