package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("enchant=")) return false;

        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        PanelPosition position = PanelPosition.valueOf(args[2]);
        ItemStack editItem;

        if (position == PanelPosition.Top) {
            editItem = player.getOpenInventory().getTopInventory().getItem(Integer.parseInt(args[1]));
        } else if (position == PanelPosition.Middle) {
            editItem = player.getInventory().getItem(Integer.parseInt(args[1]) + 9);
        } else {
            editItem = player.getInventory().getItem(Integer.parseInt(args[1]));
        }

        if (args[3].equalsIgnoreCase("add")) {
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(args[4].toLowerCase()));
            if (enchant != null) {
                editItem.addEnchantment(enchant, Integer.parseInt(args[5]));
            }
        } else if (args[3].equalsIgnoreCase("remove")) {
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(args[4].toLowerCase()));
            if (enchant != null) {
                editItem.removeEnchantment(enchant);
            }
        } else if (args[3].equalsIgnoreCase("clear")) {
            editItem.getEnchantments().keySet().forEach(editItem::removeEnchantment);
        }

        return true;
    }
}
