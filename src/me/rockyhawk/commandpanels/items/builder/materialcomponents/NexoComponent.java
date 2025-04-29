package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NexoComponent implements MaterialComponent {
    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("nexo=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        try {
            String itemID = tag.split("\\s")[1];
            ItemBuilder builder = NexoItems.itemFromId(itemID);
            if (builder != null) {
                return builder.build();
            }
        } catch (Exception e) {
            ctx.debug.send(e, player, ctx);
        }
        return null;
    }
}