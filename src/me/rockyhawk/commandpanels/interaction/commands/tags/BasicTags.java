package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class BasicTags implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        String[] args = command.split("\\s+");  // Arguments are space-separated
        args = Arrays.copyOfRange(args, 1, args.length); // Remove first element from args
        if (command.startsWith("console=")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.join(" ", args));
            return true;
        } else if (command.startsWith("send=")) {
            player.chat(String.join(" ", args));
            return true;
        } else if (command.startsWith("sudo=")) {
            player.chat("/" + String.join(" ", args));
            return true;
        } else if (command.startsWith("msg=")) {
            ctx.text.sendString(panel, pos, player, String.join(" ", args));
            return true;
        } else if (command.startsWith("broadcast=")) {
            Bukkit.broadcastMessage(ctx.text.placeholders(null, null, null, String.join(" ", args).trim()));
            return true;
        } else if (command.startsWith("broadcast-perm=")) {
            StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }
            Bukkit.broadcast(ctx.text.placeholders(null, null, null, message.toString().trim()), String.valueOf(args[0]));
            return true;
        } else if (command.startsWith("op=")) {
            boolean isOp = player.isOp();
            try {
                player.setOp(true);
                Bukkit.dispatchCommand(player, String.join(" ", args));
                player.setOp(isOp);
            } catch (Exception exc) {
                player.setOp(isOp);
                ctx.debug.send(exc, player, ctx);
                player.sendMessage(ctx.tag + ctx.text.colour(ctx.configHandler.config.getString("config.format.error") + " op=: Error in op command!"));
            }
            return true;
        }
        return false;
    }
}