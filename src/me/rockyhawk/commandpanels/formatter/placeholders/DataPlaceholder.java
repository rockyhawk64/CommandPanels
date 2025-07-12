package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.PlaceholderResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class DataPlaceholder implements PlaceholderResolver {

    @Override
    public String resolve(OfflinePlayer player, String identifier, Context ctx) {
        // Only handle placeholders starting with "data_"
        if (!identifier.startsWith("data_")) return null;
        if (!player.isOnline()) return null;

        Player p = (Player) player;
        String playerName = p.getName();

        // Remove "data_" prefix
        String key = identifier.substring("data_".length());

        // %commandpanels_data_key%
        // Gets the value stored under "key"
        String value = ctx.dataLoader.getUserData(playerName, key);
        return value != null ? value : "null";
    }
}