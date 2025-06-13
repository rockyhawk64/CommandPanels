package me.rockyhawk.commandpanels.interaction.commands.tags;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class BungeeTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");

        if (args.length <= 1) {
            player.sendMessage(ctx.text.colour(ctx.tag + "No server was given."));
            return true;
        }

        // Defaults
        String serverName = args[1];
        String proxyType = "bungeecord";
        String permission = "bungeecord.command.server." + serverName.toLowerCase();

        // Parse optional args
        for (String arg : args) {
            if (arg.startsWith("perm:")) {
                permission = arg.substring("perm:".length()).toLowerCase();
            } else if (arg.startsWith("type:")) {
                String type = arg.substring("type:".length()).toLowerCase();
                if (type.equals("velocity") || type.equals("bungeecord")) {
                    proxyType = type;
                } else {
                    proxyType = "bungeecord"; // fallback
                }
            }
        }

        boolean force = command.startsWith("force-server=");
        boolean hasPermission = player.hasPermission(permission);

        if (!force && !hasPermission) {
            player.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.perms")));
            return true;
        }

        // Send plugin message
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        String channel = proxyType.equals("velocity") ? "velocity:main" : "BungeeCord";
        player.sendPluginMessage(ctx.plugin, channel, out.toByteArray());

        return true;
    }
}
