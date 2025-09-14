package me.rockyhawk.commandpanels.builder.inventory.items.materialcomponents;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.MaterialComponent;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HeadDatabaseComponent implements MaterialComponent {
    @Override
    public boolean isCorrectTag(String tag) {
        return tag.toLowerCase().startsWith("[hdb]");
    }

    @Override
    public ItemStack createItem(Context ctx, String head, Player player, PanelItem item) {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("HeadDatabase")) {
            HeadDatabaseAPI api;
            api = new HeadDatabaseAPI();
            return api.getItemHead(head);
        } else {
            ctx.text.sendWarn(player, Message.REQUIRE_HEADDATABASE);
        }
        return null;
    }
}
