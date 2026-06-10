package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class SessionTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[session]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        String[] args = command.split("\\s");
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

            case "eval": {
                // Syntax: [session] eval <key> :: <condition> :: <true_val> :: <false_val>
                if (args.length < 2) return;
                String content = joinArgs(args, 1);

                // Split from right to left using ' :: ' up to 4 parts
                String[] parts = content.split(" :: ", 4);
                if (parts.length != 4) return; // Malformed tag

                String key = parts[0];
                String conditions = parts[1];
                String valueIfTrue = parts[2];
                String valueIfFalse = parts[3];

                // Execute condition checks and return final result
                ConditionNode conditionNode = new ConditionParser().parse(conditions);
                boolean result = conditionNode.evaluate(player, panel, ctx);

                String finalValue = result ? valueIfTrue : valueIfFalse;

                player.getPersistentDataContainer()
                        .set(new NamespacedKey(ctx.plugin, key),
                                PersistentDataType.STRING, finalValue);
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