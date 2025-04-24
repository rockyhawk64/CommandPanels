package me.rockyhawk.commandpanels.formatter.placeholders.resolvers;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.placeholders.PlaceholderResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CheckInventory implements PlaceholderResolver {

    @Override
    public boolean canResolve(String identifier) {
        return identifier.startsWith("checkinv-");
    }

    @Override
    public String resolve(Panel panel, PanelPosition position, Player p, String identifier, Context ctx) {
        String[] materialList = identifier.replace("checkinv-", "").split(":");

        String material = materialList[0].toUpperCase();
        int amount = Integer.parseInt(materialList[1]);
        int totalAmount = 0;

        // Loop through each item in the inventory array
        for (ItemStack item : ctx.inventorySaver.getNormalInventory(p)) {
            // Check if the item is not null and matches the specified material
            if (item != null && item.getType() == Material.valueOf(material)) {
                // Add the amount of this item to the total
                totalAmount += item.getAmount();
            }
        }

        // If the total amount of the material is greater than or equal to the required amount
        if (totalAmount >= amount) {
            return "true";
        } else {
            return "false";
        }
    }
}