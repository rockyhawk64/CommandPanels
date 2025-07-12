package me.rockyhawk.commandpanels.formatter;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.OfflinePlayer;

public interface PlaceholderResolver {
    String resolve(OfflinePlayer p, String identifier, Context ctx);
}
