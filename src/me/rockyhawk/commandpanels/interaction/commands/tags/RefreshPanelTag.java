package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RefreshPanelTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[refresh]");
    }

    /**
     * Schedules refreshes as they need to happen after
     * other actions such as permission changes
     */
    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        Bukkit.getGlobalRegionScheduler().run(ctx.plugin, task -> {
            panel.open(ctx, player, false);
        });
    }
}