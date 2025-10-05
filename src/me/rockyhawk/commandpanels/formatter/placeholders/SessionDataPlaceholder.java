package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.PlaceholderResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.persistence.PersistentDataType;

public class SessionDataPlaceholder implements PlaceholderResolver {
    @Override
    public String resolve(OfflinePlayer player, String identifier, Context ctx) {
        // Pre placeholder check for valid placeholder
        if (!identifier.startsWith("session_")) return null;
        if(!player.isOnline()) return "not_online";

        // Get data key
        String key = identifier.substring("session_".length());

        // Get session data if it exists
        String data = player.getPersistentDataContainer()
                .get(new NamespacedKey(ctx.plugin, key),
                        PersistentDataType.STRING);
        if(data == null) return "null";

        // Get the data from session

        return data;
    }
}
