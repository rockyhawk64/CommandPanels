package me.rockyhawk.commandpanels.interaction.commands.requirements;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.interaction.commands.RequirementTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemTag implements RequirementTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[item]");
    }

    @Override
    public boolean check(Context ctx, Panel panel, Player player, String raw, String args) {
        ParsedItemRequirement req = parseArgs(ctx, player, args);
        if (req == null) return false;
        return hasMatchingItems(ctx, player, req);
    }

    @Override
    public void execute(Context ctx, Panel panel, Player player, String raw, String args) {
        ParsedItemRequirement req = parseArgs(ctx, player, args);
        if (req == null) return;
        if(req.remove) removeMatchingItems(ctx, player, req);
    }

    private ParsedItemRequirement parseArgs(Context ctx, Player player, String args) {
        Map<String, String> map = parseArgumentMap(args);

        Material material;
        int amount = 1;
        boolean remove = true;
        String custom = null;
        NamespacedKey model = null;
        String source = "player"; // default to player

        if (map.containsKey("material")) {
            try {
                material = Material.valueOf(map.get("material").toUpperCase());
            } catch (IllegalArgumentException e) {
                ctx.text.sendError(player, Message.REQUIREMENT_MATERIAL_INVALID);
                return null;
            }
        } else {
            ctx.text.sendError(player, Message.REQUIREMENT_MATERIAL_REQUIRED);
            return null;
        }

        if (map.containsKey("remove")) {
            remove = Boolean.parseBoolean(map.get("remove"));
        }

        if (map.containsKey("amount")) {
            try {
                amount = Integer.parseInt(map.get("amount"));
            } catch (NumberFormatException e) {
                ctx.text.sendError(player, Message.REQUIREMENT_AMOUNT_INVALID);
                return null;
            }
        }

        if (map.containsKey("model")) {
            String raw = map.get("model");
            model = NamespacedKey.fromString(raw);
            if (model == null) {
                ctx.text.sendError(player, Message.ITEM_MODEL_INVALID);
                return null;
            }
        }

        if (map.containsKey("custom")) {
            custom = map.get("custom");
            if (custom.isEmpty()) {
                ctx.text.sendError(player, Message.ITEM_CUSTOM_MODEL_INVALID);
                return null;
            }
        }

        if (map.containsKey("source")) {
            String s = map.get("source").toLowerCase();
            if (!s.equals("player") && !s.equals("panel")) {
                ctx.text.sendError(player, Message.REQUIREMENT_SOURCE_INVALID);
                return null;
            }
            source = s;
        }

        return new ParsedItemRequirement(material, amount, model, custom, source, remove);
    }

    private boolean hasMatchingItems(Context ctx, Player player, ParsedItemRequirement req) {
        // If zero items are required, always pass
        if (req.amount <= 0) return true;

        Inventory inv = req.source.equals("panel")
                ? player.getOpenInventory().getTopInventory()
                : player.getInventory();

        NamespacedKey itemId = new NamespacedKey(ctx.plugin, "item_id");
        int count = 0;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || !item.getType().equals(req.material)) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            // Skip items that have item_id (panel-defined items)
            if (meta.getPersistentDataContainer().has(itemId, PersistentDataType.STRING)) continue;

            // If a specific model is required, only match items with that exact model.
            // If no model is specified, skip items that have any item model data.
            if (req.model != null) {
                if (!meta.hasItemModel() || !req.model.equals(meta.getItemModel())) continue;
            } else {
                if (meta.hasItemModel()) continue;
            }

            // If a specific Custom Model Data is required, only match items with that exact data.
            // If no custom model data is specified, skip items that have any data.
            if (req.custom != null) {
                if (item.getData(DataComponentTypes.CUSTOM_MODEL_DATA) == null) continue;
                try{
                    float num = Float.parseFloat(req.custom);
                    if(!item.getData(DataComponentTypes.CUSTOM_MODEL_DATA).floats().contains(num)) continue;
                }catch (NumberFormatException e){
                    if(!item.getData(DataComponentTypes.CUSTOM_MODEL_DATA).strings().contains(req.custom)) continue;
                }
            } else {
                if (item.getData(DataComponentTypes.CUSTOM_MODEL_DATA) != null) continue;
            }

            count += item.getAmount();
            if (count >= req.amount) return true;
        }

        return false;
    }

    private void removeMatchingItems(Context ctx, Player player, ParsedItemRequirement req) {
        Inventory inv = req.source.equals("panel")
                ? player.getOpenInventory().getTopInventory()
                : player.getInventory();

        NamespacedKey itemId = new NamespacedKey(ctx.plugin, "item_id");
        int toRemove = req.amount;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || !item.getType().equals(req.material)) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            // Skip items that have item_id (panel-defined items)
            if (meta.getPersistentDataContainer().has(itemId, PersistentDataType.STRING)) continue;

            // If a specific Item Model is required, only match items with that exact model.
            // If no model is specified, skip items that have any item model data.
            if (req.model != null) {
                if (!meta.hasItemModel() || !req.model.equals(meta.getItemModel())) continue;
            } else {
                if (meta.hasItemModel()) continue;
            }

            // If a specific Custom Model Data is required, only match items with that exact data.
            // If no custom model data is specified, skip items that have any data.
            if (req.custom != null) {
                if (item.getData(DataComponentTypes.CUSTOM_MODEL_DATA) == null) continue;
                try{
                    float num = Float.parseFloat(req.custom);
                    if(!item.getData(DataComponentTypes.CUSTOM_MODEL_DATA).floats().contains(num)) continue;
                }catch (NumberFormatException e){
                    if(!item.getData(DataComponentTypes.CUSTOM_MODEL_DATA).strings().contains(req.custom)) continue;
                }
            } else {
                if (item.getData(DataComponentTypes.CUSTOM_MODEL_DATA) != null) continue;
            }

            int stackAmount = item.getAmount();
            if (stackAmount <= toRemove) {
                inv.setItem(i, null);
                toRemove -= stackAmount;
            } else {
                item.setAmount(stackAmount - toRemove);
                inv.setItem(i, item);
                break;
            }

            if (toRemove <= 0) break;
        }
    }

    private Map<String, String> parseArgumentMap(String args) {
        Map<String, String> map = new HashMap<>();
        Matcher matcher = Pattern.compile("(\\w+)=((\"[^\"]*\")|\\S+)").matcher(args);
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String value = matcher.group(2).replaceAll("^\"|\"$", ""); // Strip quotes
            map.put(key, value);
        }
        return map;
    }

    private static class ParsedItemRequirement {
        Material material;
        int amount;
        boolean remove;
        NamespacedKey model;
        String custom;
        String source;

        ParsedItemRequirement(Material material, int amount, NamespacedKey model, String custom, String source, boolean remove) {
            this.material = material;
            this.amount = amount;
            this.remove = remove;
            this.model = model;
            this.custom = custom;
            this.source = source;
        }
    }
}