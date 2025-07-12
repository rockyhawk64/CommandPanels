package me.rockyhawk.commandpanels.session.inventory.generator;

import me.rockyhawk.commandpanels.Context;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GenerateManager implements Listener {

    private final Context ctx;
    private final Set<UUID> generatingPlayers = new HashSet<>();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.builder()
            .character('&')
            .build();

    public GenerateManager(Context ctx) {
        this.ctx = ctx;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        if (isInGenerateMode(player) && e.getInventory().getHolder() instanceof BlockState) {
            handleInventoryOpen(player, e.getInventory());
        }
    }

    /**
     * Put player into generate mode for 10 seconds
     */
    public void startGenerateMode(Player player) {
        generatingPlayers.add(player.getUniqueId());
        ctx.text.sendInfo(player, "Generate mode enabled.");

        // Schedule a timeout to remove generate mode after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (generatingPlayers.remove(player.getUniqueId())) {
                    ctx.text.sendInfo(player, "Generate mode expired.");
                }
            }
        }.runTaskLater(ctx.plugin, 20L * 10); // 20 ticks * 10 = 10 seconds
    }

    /**
     * Check if a player is in generate mode.
     */
    public boolean isInGenerateMode(Player player) {
        return generatingPlayers.contains(player.getUniqueId());
    }

    /**
     * Called when player opens an inventory.
     * Generates the YAML and removes player from generate mode.
     */
    @SuppressWarnings("ConstantConditions")

    public void handleInventoryOpen(Player player, Inventory inv) {
        if (!isInGenerateMode(player)) return;

        generatingPlayers.remove(player.getUniqueId());

        String panelName = findAvailablePanelName();

        // Prepare YAML configuration
        var yamlData = new org.bukkit.configuration.file.YamlConfiguration();
        yamlData.set("title", "Generated " + panelName);
        yamlData.set("type", "inventory");
        yamlData.set("rows", inv.getSize() / 9);

        Map<String, List<String>> layout = new LinkedHashMap<>();
        Map<String, Object> items = new LinkedHashMap<>();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            String key = ("item_" + item.getType() + "_" + i).toLowerCase();
            layout.put(String.valueOf(i), Collections.singletonList(key));

            Map<String, Object> itemData = new LinkedHashMap<>();
            itemData.put("material", item.getType().name());
            itemData.put("stack", item.getAmount());

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) itemData.put("name", legacy.serialize(meta.displayName()));
                if (meta.hasItemModel()) itemData.put("item-model", meta.getItemModel().toString());
                if (meta.hasLore()) {
                    List<String> serializedLore = meta.lore().stream()
                            .map(legacy::serialize)
                            .toList();
                    itemData.put("lore", serializedLore);
                }
                if (meta.hasEnchants()) {
                    List<String> enchantList = new ArrayList<>();
                    for (var entry : meta.getEnchants().entrySet()) {
                        String name = entry.getKey().getKey().getKey();
                        int level = entry.getValue();
                        enchantList.add(name + " " + level);
                    }
                    itemData.put("enchantments", enchantList);
                }
            }

            items.put(key, itemData);
        }

        yamlData.set("layout", layout);
        yamlData.set("items", items);

        // Save YAML to file
        try {
            yamlData.save(new File(ctx.plugin.folder, panelName + ".yml"));
            ctx.fileHandler.reloadPanels();
            ctx.text.sendInfo(player, "Generated a new panel file.");
        } catch (IOException e) {
            ctx.text.sendError(player, "Could not save new panel file.");
        }
    }

    /**
     * Find the next available panel file name.
     */
    private String findAvailablePanelName() {
        for (int index = 1; index < Integer.MAX_VALUE; index++) {
            String panelName = "panel_" + index;
            File panelFile = new File(ctx.plugin.folder, panelName + ".yml");
            if (!panelFile.exists()) {
                return panelName;
            }
        }
        return "panel_1"; // fallback, theoretically unreachable
    }
}