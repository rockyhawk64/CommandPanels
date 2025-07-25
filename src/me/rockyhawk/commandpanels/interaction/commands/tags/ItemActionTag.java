package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class ItemActionTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[itemaction]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        try {
            String[] args = ctx.text.parseTextToString(player, command).split("\\s+");
            if (args.length < 2) {
                ctx.text.sendError(player, "Invalid item action syntax. Usage: slot action [params]");
                return;
            }

            int slot = Integer.parseInt(args[1]);
            String action = args[0].toLowerCase();

            Inventory gui = player.getOpenInventory().getTopInventory();
            ItemStack item = gui.getItem(slot);

            if (item == null || item.getType() == Material.AIR || !(panel instanceof InventoryPanel)) {
                // No item in slot
                return;
            }

            switch (action) {
                case "enchant" -> handleEnchant(item, args, ctx, player);
                case "repair" -> repairItem(item);
                case "amount" -> item.setAmount(Integer.parseInt(args[2]));
                case "remove" -> gui.setItem(slot, null);
                default -> ctx.text.sendError(player, "Unknown item action");
            }

            // Update the item with changes unless item was removed
            if (!action.equals("remove")) gui.setItem(slot, item);

        } catch (Exception e) {
            ctx.text.sendError(player, "An error occurred while executing item action.");
        }
    }

    private void handleEnchant(ItemStack item, String[] args, Context ctx, Player player) {
        if (args.length < 3) {
            ctx.text.sendError(player, "Improper usage.");
            return;
        }

        String sub = args[2].toLowerCase();

        switch (sub) {
            case "add" -> {
                if (args.length < 5) {
                    ctx.text.sendError(player, "Missing enchantment name or level.");
                    return;
                }
                NamespacedKey key = args[3].contains(":") ?
                        NamespacedKey.fromString(args[3].toLowerCase()) :
                        NamespacedKey.minecraft(args[3].toLowerCase());
                Enchantment enchant = Registry.ENCHANTMENT.get(key);
                int level = Integer.parseInt(args[4]);
                if (enchant != null) item.addUnsafeEnchantment(enchant, level);
                else ctx.text.sendError(player, "Invalid enchantment");
            }
            case "remove" -> {
                if (args.length < 4) {
                    ctx.text.sendError(player, "Missing enchantment to remove.");
                    return;
                }
                NamespacedKey key = args[3].contains(":") ?
                        NamespacedKey.fromString(args[3].toLowerCase()) :
                        NamespacedKey.minecraft(args[3].toLowerCase());
                Enchantment enchant = Registry.ENCHANTMENT.get(key);
                if (enchant != null) item.removeEnchantment(enchant);
                else ctx.text.sendError(player, "Invalid enchantment.");
            }
            case "clear" -> item.getEnchantments().keySet().forEach(item::removeEnchantment);
            default -> ctx.text.sendError(player, "Unknown enchant action.");
        }
    }

    private void repairItem(ItemStack item) {
        if (item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(damageable);
        }
    }
}