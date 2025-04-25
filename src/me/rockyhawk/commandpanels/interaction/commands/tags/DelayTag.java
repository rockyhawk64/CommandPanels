package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DelayTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("delay=")) return false;

        String[] args = command.split("\\s");
        args = Arrays.copyOfRange(args, 1, args.length); // Remove the tag name

        final int delayTicks = Integer.parseInt(args[0]);
        String finalCommand = String.join(" ", args).replaceFirst(args[0], "").trim();

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    ctx.commands.runCommand(panel, pos, player, finalCommand);
                } catch (Exception ex) {
                    ctx.debug.send(ex, player, ctx);
                    this.cancel();
                }
                this.cancel();
            }
        }.runTaskTimer(ctx.plugin, delayTicks, 1); //20 ticks == 1 second
        return true;
    }
}