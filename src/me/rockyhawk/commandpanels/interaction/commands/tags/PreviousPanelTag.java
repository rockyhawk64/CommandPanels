package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class PreviousPanelTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[previous]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        // Open the previous panel
        String previousPanel = player.getPersistentDataContainer()
                .get(new NamespacedKey(ctx.plugin, "previous"),
                        PersistentDataType.STRING);

        if(previousPanel != null){
            ctx.plugin.panels.get(previousPanel)
                    .open(ctx, player, true);
        }
    }
}

