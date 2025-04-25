package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class OraxenComponent implements MaterialComponent {
    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("oraxen=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        String itemID = tag.split("\\s")[1];
        try {
            // Load the OraxenItems class
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");

            // Retrieve the 'getItemById' method from the OraxenItems class
            Method getItemByIdMethod = oraxenItemsClass.getMethod("getItemById", String.class);
            getItemByIdMethod.setAccessible(true);

            // Invoke the 'getItemById' method with the itemID
            Object oraxenItem = getItemByIdMethod.invoke(null, itemID); // static method, so pass 'null'

            // Ensure that the method returned a valid Oraxen item
            if (oraxenItem != null) {
                // Now we need to invoke 'getReferenceClone' on the OraxenItem object
                Method getReferenceCloneMethod = oraxenItem.getClass().getMethod("getReferenceClone");
                getReferenceCloneMethod.setAccessible(true);
                ItemStack stack = (ItemStack) getReferenceCloneMethod.invoke(oraxenItem);
                return stack;
            }
        } catch (Exception e) {
            ctx.debug.send(e, null, ctx);
            // Handle the error or inform the player
        }
        return null;
    }
}