package me.rockyhawk.commandpanels.interaction.commands.tags;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public class ServerTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[server]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        // Remove the tag prefix and parse placeholders
        String parsedCmd = ctx.text.parseTextToString(player, command);
        String[] args = parsedCmd.split("\\s+");

        if (args.length == 0 || args[0].isEmpty()) {
            ctx.text.sendError(player, "No server was specified.");
            return;
        }

        String serverName = args[0];
        String proxyType = "bungeecord";  // default proxy type

        // Check for optional args (perm ignored since always forced, type supported)
        for (String arg : args) {
            if (arg.startsWith("type=")) {
                String type = arg.substring(5).toLowerCase();
                if (type.equals("velocity") || type.equals("bungeecord")) {
                    proxyType = type;
                }
            }
        }

        // Prepare and send plugin message to proxy without permission checks
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        String channel = proxyType.equals("velocity") ? "velocity:main" : "BungeeCord";
        player.sendPluginMessage(ctx.plugin, channel, out.toByteArray());
    }
}
