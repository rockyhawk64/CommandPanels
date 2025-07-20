package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import org.bukkit.entity.Player;

public class OpenPanelTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[open]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        if (ctx.plugin.panels.get(command) == null) {
            return;
        }

        // Open panel tag will gracefully move from one panel to the next within the same session
        Panel openPanel = ctx.plugin.panels.get(command);
        openPanel.open(ctx, player, SessionManager.PanelOpenType.INTERNAL);
    }
}

