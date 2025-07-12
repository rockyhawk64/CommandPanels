package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[msg]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String command) {
        Bukkit.getScheduler().runTask(ctx.plugin, () -> {
            player.sendMessage(command);
        });
    }
}

