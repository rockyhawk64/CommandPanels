package me.rockyhawk.commandpanels.builder.inventory;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemBuilder;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PanelFactory {
    protected final Context ctx;
    protected final InventoryPanelBuilder panelBuilder;

    public PanelFactory(Context ctx, InventoryPanelBuilder builder) {
        this.ctx = ctx;
        this.panelBuilder = builder;
    }

    public Inventory createInventory(InventoryPanel panel, Player p) {
        TitleHandler titleHandler = new TitleHandler();
        Component title = titleHandler.getTitle(ctx, panel, p);

        // Create the inventory
        String rows = ctx.text.parseTextToString(p, panel.getRows());
        Inventory inv;
        if (rows.matches("\\d+")) {
            int rowsNum = Integer.parseInt(rows);
            if(rowsNum > 6) rowsNum = 6;
            inv = Bukkit.createInventory(p, rowsNum * 9, title);
        } else {
            try {
                inv = Bukkit.createInventory(p, InventoryType.valueOf(rows.toUpperCase()), title);
            } catch (IllegalArgumentException e) {
                ctx.text.sendError(p, Message.PANEL_INVALID_TYPE);
                inv = Bukkit.createInventory(p, 9, title); // fallback to 1 row
            }
        }

        // Place all the items in the inventory
        ItemStack fill = null;
        ItemBuilder itemBuilder = new ItemBuilder(ctx, panelBuilder);
        for(String slot : panel.getSlots().keySet()){
            String parsedSlot = ctx.text.parseTextToString(p, slot);
            ItemStack itemStack = new ItemStack(Material.AIR);

            // Loop through items in slot
            for(String name : panel.getSlots().get(slot)) {
                PanelItem item = panel.getItems().get(name);
                if(item == null) continue;

                // Check conditions for which item to use in the slot
                if (!item.conditions().trim().isEmpty()) {
                    ConditionNode conditionNode = new ConditionParser().parse(item.conditions());
                    boolean result = conditionNode.evaluate(p, ctx);
                    if (!result) continue;
                }

                itemStack = itemBuilder.buildItem(panel, item);
                break;
            }

            // Fill empty slot item
            if(parsedSlot.equalsIgnoreCase("fill")){
                fill = itemStack;
                continue;
            }

            if(!slot.matches("\\d+")) continue; // Not a number
            int slotIndex = Integer.parseInt(parsedSlot);
            if (slotIndex >= 0 && slotIndex < inv.getSize()) {
                inv.setItem(slotIndex, itemStack);
            }
        }

        // Fill empty slots if necessary
        if(fill != null) {
            // Hide the tooltip for filler items
            TooltipDisplay tooltipHidden = TooltipDisplay.tooltipDisplay().hideTooltip(true).build();
            fill.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipHidden);
            // Assign data so that the updater will skip over filler items
            NamespacedKey filler = new NamespacedKey(ctx.plugin, "fill_item");
            fill.editPersistentDataContainer(c -> c.set(filler, PersistentDataType.STRING, "true"));
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack current = inv.getItem(i);
                if (current == null || current.getType() == Material.AIR) {
                    inv.setItem(i, fill);
                }
            }
        }
        return inv;
    }
}