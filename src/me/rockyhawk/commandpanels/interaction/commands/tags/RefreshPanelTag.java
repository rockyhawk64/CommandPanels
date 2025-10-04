package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import org.bukkit.entity.Player;

public class RefreshPanelTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[refresh]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        panel.open(ctx, player, SessionManager.PanelOpenType.REFRESH);
    }
}