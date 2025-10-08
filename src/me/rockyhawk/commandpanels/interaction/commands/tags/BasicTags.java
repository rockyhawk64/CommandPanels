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
        String[] tokens = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");  // Arguments are space-separated
        final String[] args = Arrays.copyOfRange(tokens, 1, tokens.length); // Remove first element from args
        if (command.startsWith("console=")) {
            ctx.scheduler.runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.join(" ", args)));
            return true;
        } else if (command.startsWith("send=")) {
            // Player chat should run on the player's entity thread
            ctx.scheduler.runTaskForEntity(player, () -> player.chat(String.join(" ", args)));
            return true;
        } else if (command.startsWith("sudo=")) {
            // Execute as player via chat on the player's entity thread
            ctx.scheduler.runTaskForEntity(player, () -> player.chat("/" + String.join(" ", args)));
            return true;
        } else if (command.startsWith("msg=")) {
            ctx.text.sendString(panel, pos, player, String.join(" ", args));
            return true;
        } else if (command.startsWith("broadcast=")) {
            ctx.scheduler.runTask(() -> Bukkit.broadcastMessage(ctx.text.placeholders(null, null, null, String.join(" ", args).trim())));
            return true;
        } else if (command.startsWith("broadcast-perm=")) {
            StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                message.append(args[i]).append(" ");
            }
            String finalMessage = ctx.text.placeholders(null, null, null, message.toString().trim());
            String permission = String.valueOf(args[0]);
            ctx.scheduler.runTask(() -> Bukkit.broadcast(finalMessage, permission));
            return true;
        } else if (command.startsWith("op=")) {
            // Ensure player state changes happen on the player's entity thread
            String joined = String.join(" ", args);
            ctx.scheduler.runTaskForEntity(player, () -> {
                boolean isOp = player.isOp();
                try {
                    player.setOp(true);
                    // Dispatch the command on the global thread while the player is op
                    ctx.scheduler.runTask(() -> {
                        try {
                            Bukkit.dispatchCommand(player, joined);
                        } catch (Exception exc) {
                            ctx.debug.send(exc, player, ctx);
                            ctx.scheduler.runTaskForEntity(player, () -> player.sendMessage(ctx.tag + ctx.text.colour(ctx.configHandler.config.getString("config.format.error") + " op=: Error in op command!")));
                        } finally {
                            // Restore OP status back on the entity thread
                            ctx.scheduler.runTaskForEntity(player, () -> player.setOp(isOp));
                        }
                    });
                } catch (Exception exc) {
                    // If setting OP failed, restore and report
                    player.setOp(isOp);
                    ctx.debug.send(exc, player, ctx);
                    player.sendMessage(ctx.tag + ctx.text.colour(ctx.configHandler.config.getString("config.format.error") + " op=: Error in op command!"));
                }
            });
            return true;
        }
        return false;
    }
}