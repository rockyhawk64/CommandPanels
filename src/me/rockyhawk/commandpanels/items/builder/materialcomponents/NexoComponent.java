package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class NexoComponent implements MaterialComponent {
    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("nexo=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        try {
            String itemID = tag.split("\\s")[1];
            Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
            Method itemFromId = nexoItemsClass.getMethod("itemFromId", String.class);
            Object item = itemFromId.invoke(null, itemID);
            if (item != null) {
                Method buildMethod = item.getClass().getMethod("build");
                return (ItemStack) buildMethod.invoke(item);
            }
        } catch (Exception e) {
            ctx.debug.send(e, player, ctx);
        }
        return null;
    }
}