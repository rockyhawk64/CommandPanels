package me.rockyhawk.commandpanels.interaction.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Registry;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandRunner {
    private final Context ctx;

    public CommandRunner(Context pl) {
        this.ctx = pl;
    }

    public void runCommands(Panel panel, Player player, List<String> commands) {
        runCommands(panel, player, commands, 0);
    }

    private void runCommands(Panel panel, Player player, List<String> commands, int index) {
        if (index >= commands.size()) return;

        String command = commands.get(index).trim();

        if (command.startsWith("[delay]")) {
            String delayStr = ctx.text.applyPlaceholders(
                    player,
                    command.replace("[delay]", "").trim());

            int ticks;
            try {
                ticks = Integer.parseInt(delayStr);
            } catch (NumberFormatException e) {
                ticks = 0;
            }

            int nextIndex = index + 1;
            Bukkit.getGlobalRegionScheduler().runDelayed(
                    ctx.plugin,
                    task -> runCommands(panel, player, commands, nextIndex),
                    ticks
            );
            return;
        }

        // Run the command
        runCommand(panel, player, command);

        // Move to the next command
        runCommands(panel, player, commands, index + 1);
    }


    public void runCommand(Panel panel, Player player, String command) {
        for (CommandTagResolver resolver : Registry.COMMAND_TAG_RESOLVERS) {
            if (command.isEmpty()) return;

            String[] parts = command.split("\\s+", 2); // Split into 2 parts: tag and rest

            String tag = parts[0];

            String args = (parts.length > 1) ? parts[1].trim() : "";
            String argsParsed = ctx.text.applyPlaceholders(player, args);

            if (resolver.isCorrectTag(tag)) {
                resolver.handle(ctx, panel, player, args, argsParsed);
                return;
            }
        }
    }
}