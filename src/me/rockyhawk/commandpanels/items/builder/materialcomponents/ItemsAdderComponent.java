package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import dev.lone.itemsadder.api.CustomStack;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderComponent implements MaterialComponent {
    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("itemsadder=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        String namespaceID = tag.split("\\s")[1];
        CustomStack stack = CustomStack.getInstance(namespaceID);
        return stack.getItemStack().clone();
    }
}
