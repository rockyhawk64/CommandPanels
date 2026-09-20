package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NameHandler {

    private final Context ctx;
    private final LoreFormatter loreFormatter;

    private static final Set<DataComponentType> KEEP = Set.of(
            DataComponentTypes.CUSTOM_NAME,
            DataComponentTypes.ITEM_NAME,
            DataComponentTypes.LORE,
            DataComponentTypes.TOOLTIP_DISPLAY,
            DataComponentTypes.RARITY
    );

    public NameHandler(Context ctx) {
        this.ctx = ctx;
        this.loreFormatter = new LoreFormatter(ctx);
    }

    public ItemStack setName(ItemStack item, PanelItem panelItem, Player player) {
        TooltipDisplay.Builder tooltipBuilder = TooltipDisplay.tooltipDisplay();
        boolean modified = false;

        if (!Boolean.parseBoolean(ctx.text.parseTextToString(player, panelItem.attributes()))) {
            Set<DataComponentType> hidden = new HashSet<>(item.getDataTypes());
            hidden.removeAll(KEEP);
            tooltipBuilder.hiddenComponents(hidden);
            modified = true;
        }

        if (!Boolean.parseBoolean(ctx.text.parseTextToString(player, panelItem.tooltip()))) {
            tooltipBuilder.hideTooltip(true);
            modified = true;
        }

        if (modified) {
            item.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipBuilder.build());
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