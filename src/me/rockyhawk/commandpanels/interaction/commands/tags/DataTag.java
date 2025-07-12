package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public class DataTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[data]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String command) {
        String playerName = player.getName();
        String[] args = command.split("\\s");

        if (args.length < 1) return;

        String action = args[0].toLowerCase();

        switch (action) {

            case "set": {
                // [data] set key value with spaces
                if (args.length < 3) return;
                String key = args[1];
                String value = joinArgs(args, 2);
                ctx.dataLoader.setUserData(playerName, key, value, false); // do not overwrite
                break;
            }

            case "overwrite": {
                // [data] overwrite key value with spaces
                if (args.length < 3) return;
                String key = args[1];
                String value = joinArgs(args, 2);
                ctx.dataLoader.setUserData(playerName, key, value, true); // always overwrite
                break;
            }

            case "math": {
                // [data] math key +5
                if (args.length < 3) return;
                String key = args[1];
                String expression = joinArgs(args, 2);
                ctx.dataLoader.doDataMath(playerName, key, expression);
                break;
            }

            case "del": {
                // [data] del key
                if (args.length < 2) return;
                String key = args[1];
                ctx.dataLoader.delUserData(playerName, key);
                break;
            }

            case "clear": {
                // [data] clear
                ctx.dataLoader.clearData(playerName);
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