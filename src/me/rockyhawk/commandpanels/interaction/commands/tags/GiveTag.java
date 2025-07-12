package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GiveTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[give]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String command) {
        Bukkit.getScheduler().runTask(ctx.plugin, () -> {
            // Example: [give] DIAMOND 3
            // Basic give tag that gives normal items and drops them if inventory is full
            String[] parts = command.split("\\s+");

            if (parts.length < 1) return;

            try {
                Material material = Material.matchMaterial(parts[0].toUpperCase());
                if (material == null || !material.isItem()) {
                    ctx.text.sendError(player, "Invalid item.");
                    return;
                }

                int amount = 1;
                if (parts.length >= 2) {
                    amount = Math.max(1, Integer.parseInt(parts[1]));
                }

                ItemStack item = new ItemStack(material, amount);

                // Add item and handle overflow
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack left : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }

            } catch (Exception e) {
                ctx.text.sendError(player, "Error giving item.");
            }
        });
    }
}