package me.rockyhawk.commandpanels.builder.inventory.items;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Registry;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents.*;
import me.rockyhawk.commandpanels.builder.inventory.items.utils.NameHandler;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ItemBuilder {
    private final Context ctx;
    private final PanelBuilder panelBuilder;
    private final NameHandler name;

    public ItemBuilder(Context ctx, PanelBuilder panelBuilder) {
        this.ctx = ctx;
        this.name = new NameHandler(ctx);
        this.panelBuilder = panelBuilder;
    }

    @NotNull
    public ItemStack buildItem(Panel panel, PanelItem item) {
        Player p = panelBuilder.getPlayer();
        if(item == null) return new ItemStack(Material.AIR);

        // Create the item
        ItemStack itemStack = builder(item, p.getPlayer());

        // Return if AIR
        Material type = itemStack.getType();
        if (type == Material.AIR || type == Material.VOID_AIR || type == Material.CAVE_AIR) {
            return itemStack;
        }

        // Use PersistentDataContainer for item recognition
        NamespacedKey itemId = new NamespacedKey(ctx.plugin, "item_id");
        NamespacedKey baseItemId = new NamespacedKey(ctx.plugin, "base_item_id");
        itemStack.editPersistentDataContainer(c -> c.set(itemId, PersistentDataType.STRING, item.id()));
        itemStack.editPersistentDataContainer(c -> c.set(baseItemId, PersistentDataType.STRING, item.id()));

        // Set item to the slot
        return itemStack;
    }

    @NotNull
    private ItemStack builder(PanelItem item, Player player) {
        String material = ctx.text.parseTextToString(player, item.material());
        ItemStack baseItem = null;

        // Check for a material tag
        String[] parts = material.split("\\s+", 2); // Split into 2 parts: tag and rest
        String tag = parts[0];
        String args = (parts.length > 1) ? parts[1].trim() : "";
        for (MaterialComponent mc : Registry.MATERIAL_COMPONENTS) {
            if (mc.isCorrectTag(tag)) {
                try {
                    baseItem = mc.createItem(ctx, args, player, item);
                    break;
                } catch (Exception e) {
                    ctx.text.sendError(player, Message.ITEM_CREATE_FAIL, item.id(), mc.getClass().getSimpleName());
                }
            }
        }

        // Fallback check for a vanilla material if a material tag could not be found
        if (baseItem == null) {
            baseItem = new MinecraftComponent().createItem(ctx, material, player, item);
            if (baseItem == null) return new ItemStack(Material.AIR);
        }

        // Complete item with its details and data
        for (ItemComponent ic : Registry.ITEM_COMPONENTS) {
            try {
                baseItem = ic.apply(ctx, baseItem, player, item);
            } catch (Exception e) {
                ctx.text.sendError(player, Message.ITEM_DECORATION_FAIL, item.id(), ic.getClass().getSimpleName());
            }
        }

        baseItem = name.setName(baseItem, item, player);
        return baseItem;
    }
}