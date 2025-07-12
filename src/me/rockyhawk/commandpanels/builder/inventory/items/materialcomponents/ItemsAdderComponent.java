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
    public ItemStack createItem(Context ctx, String tag, Player player, PanelItem item) {
        String namespaceID = tag.split("\\s")[1];
        CustomStack stack = CustomStack.getInstance(namespaceID);
        return stack.getItemStack().clone();
    }
}
