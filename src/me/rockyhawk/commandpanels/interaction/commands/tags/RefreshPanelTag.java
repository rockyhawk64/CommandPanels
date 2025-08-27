package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RefreshPanelTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[refresh]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        int delayTicks = 0; // Default to 0 (immediate)

        if (!command.isEmpty()) {
            try {
                delayTicks = Integer.parseInt(command);
                if (delayTicks < 0) delayTicks = 0; // Clamp negative to 0
                if (delayTicks > 5) delayTicks = 5; // Maximum delay 0.25 seconds or 5 ticks
            } catch (NumberFormatException ignore) {
                // leave delayTicks at 0
            }
        }

        Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
            panel.open(ctx, player, SessionManager.PanelOpenType.REFRESH);
        }, delayTicks);
    }
}