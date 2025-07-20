package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.PanelSession;
import me.rockyhawk.commandpanels.session.SessionManager;
import org.bukkit.entity.Player;

public class PreviousPanelTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[previous]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        // Open the previous panel
        PanelSession session = ctx.session.getPlayerSession(player);
        if(ctx.session.getPlayerSession(player) != null){
            if(session.getPrevious() != null)
                session.getPrevious().open(ctx, player, SessionManager.PanelOpenType.INTERNAL);
        }
    }
}

