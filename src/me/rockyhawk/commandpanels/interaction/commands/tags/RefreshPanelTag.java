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
        if (!command.isEmpty()) {
            try {
                int delayTicks = Integer.parseInt(command); // Parse delay in ticks
                if (delayTicks < 0) delayTicks = 0; // Prevent negative delay

                Bukkit.getScheduler().runTaskLater(ctx.plugin, () -> {
                    panel.open(ctx, player, SessionManager.PanelOpenType.REFRESH);
                }, delayTicks);

                return;

            } catch (NumberFormatException ignore) {
                // If parsing fails, fall back to immediate refresh
            }
        }
        panel.open(ctx, player, SessionManager.PanelOpenType.REFRESH);
    }
}