package me.rockyhawk.commandpanels.builder.inventory.items;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents.*;
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

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private final Context ctx;
    private final List<MaterialComponent> materialComponents = new ArrayList<>();
    private final List<ItemComponent> itemComponents = new ArrayList<>();
    private final PanelBuilder panelBuilder;
    private final NameHandler name;

    public ItemBuilder(Context ctx, PanelBuilder panelBuilder) {
        this.ctx = ctx;
        this.name = new NameHandler(ctx);
        this.panelBuilder = panelBuilder;
        initialiseComponents();
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
        for (MaterialComponent mc : materialComponents) {
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
        for (ItemComponent ic : itemComponents) {
            try {
                baseItem = ic.apply(ctx, baseItem, player, item);
            } catch (Exception e) {
                ctx.text.sendError(player, Message.ITEM_DECORATION_FAIL, item.id(), ic.getClass().getSimpleName());
            }
        }

        baseItem = name.setName(baseItem, item, player);
        return baseItem;
    }

    private void initialiseComponents() {
        // Add Material Components
        this.materialComponents.add(new MinecraftComponent());
        this.materialComponents.add(new HeadComponent());
        this.materialComponents.add(new NexoComponent());
        this.materialComponents.add(new ItemsAdderComponent());
        this.materialComponents.add(new MMOItemsComponent());
        this.materialComponents.add(new HeadDatabaseComponent());

        // Add Item Components
        this.itemComponents.add(new EnchantedComponent());
        this.itemComponents.add(new ItemModelComponent());
        this.itemComponents.add(new CustomModelDataComponent());
        this.itemComponents.add(new TooltipComponent());
        this.itemComponents.add(new BannerComponent());
        this.itemComponents.add(new LeatherColorComponent());
        this.itemComponents.add(new PotionComponent());
        this.itemComponents.add(new PotionColorComponent());
        this.itemComponents.add(new DamageComponent());
        this.itemComponents.add(new TrimComponent());
        this.itemComponents.add(new StackComponent());
    }
}