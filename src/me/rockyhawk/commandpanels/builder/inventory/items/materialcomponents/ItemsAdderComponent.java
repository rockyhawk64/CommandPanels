package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import dev.lone.itemsadder.api.CustomStack;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderComponent implements MaterialComponent {
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[itemsadder]");
    }

    @Override
    public ItemStack createItem(Context ctx, String itemID, Player player, PanelItem item) {
        CustomStack stack = CustomStack.getInstance(itemID);
        return stack.getItemStack().clone();
    }
}