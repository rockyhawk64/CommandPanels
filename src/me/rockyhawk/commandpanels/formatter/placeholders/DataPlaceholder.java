package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.PlaceholderResolver;
import org.bukkit.OfflinePlayer;

public class DataPlaceholder implements PlaceholderResolver {

    @Override
    public String resolve(OfflinePlayer player, String identifier, Context ctx) {
        // Only handle placeholders starting with "data_"
        if (!identifier.startsWith("data_")) return null;

        // Remove "data_" prefix
        String dataPart = identifier.substring("data_".length());

        // Find the last comma
        int lastCommaIndex = dataPart.lastIndexOf(',');

        String key;
        String targetName;

        if (lastCommaIndex == -1) {
            // No comma found, use entire string as key and current player
            key = dataPart;
            targetName = player.getName();
        } else {
            // Split into key and targetName by last comma
            key = dataPart.substring(0, lastCommaIndex);
            targetName = dataPart.substring(lastCommaIndex + 1);
        }

        // Load the data
        String value = ctx.dataLoader.getUserData(targetName, key);
        return value != null ? value : "null";
    }

}
