package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class GrantTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[grant]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        // Split arguments
        String[] parts = command.split("\\s+", 2);

        if (parts.length < 2) {
            ctx.text.sendError(player, Message.ITEM_GRANT_SYNTAX_INVALID);
            return;
        }

        String permission = parts[0];
        String commandToExecute = parts[1];

        boolean hadPermission = player.hasPermission(permission) || player.isOp();
        PermissionAttachment attachment = null;

        if (!hadPermission) {
            attachment = player.addAttachment(ctx.plugin);
            attachment.setPermission(permission, true);
        }

        // Perform chat operation with the permission attached
        player.chat(commandToExecute);

        // Cleanup: remove permission only if it was not already granted
        if (attachment != null) {
            player.removeAttachment(attachment);
        }
    }
}
