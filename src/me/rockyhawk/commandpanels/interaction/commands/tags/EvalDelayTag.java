package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class EvalDelayTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("eval-delay=")) return false;

        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        args = Arrays.copyOfRange(args, 1, args.length); // Remove the tag name

        final int delayTicks = Integer.parseInt(args[0]);
        final String staticValue = args[1];
        final String parsedValue = ctx.text.placeholders(panel, pos, player, args[1].trim());
        String finalCommand = String.join(" ", args).replaceFirst(args[0], "").replaceFirst(args[1], "").trim();

        ctx.scheduler.runTaskLaterForEntity(player, () -> {
            try {
                if (ctx.text.placeholders(panel, pos, player, staticValue.trim()).equals(parsedValue)) {
                    ctx.commands.runCommand(panel, pos, player, finalCommand);
                }
            } catch (Exception ex) {
                ctx.debug.send(ex, player, ctx);
            }
        }, delayTicks);
        return true;
    }
}
