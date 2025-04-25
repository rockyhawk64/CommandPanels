package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeadDatabaseComponent implements MaterialComponent {
    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("hdb=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
            HeadDatabaseAPI api;
            api = new HeadDatabaseAPI();

            return api.getItemHead(tag.split("\\s")[1].trim());
        } else {
            Bukkit.getConsoleSender().sendMessage(ctx.text.colour(ctx.tag + "Download HeadDatabaseHook from Spigot to use this feature!"));
        }
        return null;
    }
}
