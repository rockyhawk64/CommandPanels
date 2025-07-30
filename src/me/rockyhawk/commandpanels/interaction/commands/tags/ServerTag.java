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

        // Prepare and send plugin message to proxy without permission checks
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(parsedCmd);

        // Velocity and BungeeCord both can use this message
        String channel = "BungeeCord";
        player.sendPluginMessage(ctx.plugin, channel, out.toByteArray());
    }
}
