package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.PlaceholderResolver;
import me.rockyhawk.commandpanels.session.PanelSession;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class SessionDataPlaceholder implements PlaceholderResolver {
    @Override
    public String resolve(OfflinePlayer player, String identifier, Context ctx) {
        // Pre placeholder check for valid placeholder
        if (!identifier.startsWith("session_")) return null;
        if(!player.isOnline()) return "not_online";

        // Get data key
        String key = identifier.substring("session_".length());

        // Get session if it exists
        PanelSession session = ctx.session.getPlayerSession((Player) player);
        if(session == null) return "no_session_found";

        // Get the data from session

        return session.getData(key);
    }
}
