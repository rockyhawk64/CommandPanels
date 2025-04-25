package me.rockyhawk.commandpanels.items.builder;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface MaterialComponent {
    boolean matches(String materialTag);
    ItemStack createItem(String materialTag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos);
}