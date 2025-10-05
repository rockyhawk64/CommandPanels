package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SessionTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[session]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        // Use raw placeholder parsing
        String[] args = ctx.text.applyPlaceholders(player, raw).split("\\s");

        if (args.length < 1) return;

        String action = args[0].toLowerCase();

        switch (action) {

            case "set": {
                // [data] set key value with spaces (overwrites)
                if (args.length < 3) return;
                String key = args[1];
                String value = joinArgs(args, 2);
                player.getPersistentDataContainer()
                        .set(new NamespacedKey(ctx.plugin, key),
                                PersistentDataType.STRING, value);
                break;
            }

            case "del": {
                // [session] del key
                if (args.length < 2) return;
                String key = args[1];
                player.getPersistentDataContainer()
                        .remove(new NamespacedKey(ctx.plugin, key));
                break;
            }

            default:
                // Unknown action
                break;
        }
    }

    // Helper to join remaining args into a single string (e.g., value with spaces)
    private String joinArgs(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) sb.append(" ");
        }
        return sb.toString();
    }
}