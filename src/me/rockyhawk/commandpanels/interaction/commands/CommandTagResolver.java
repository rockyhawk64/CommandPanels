package me.rockyhawk.commandpanels.interaction.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public interface CommandTagResolver {
    /**
     * @return true if this tag handled the command and it shouldn't be dispatched further.
     */
    boolean isCorrectTag(String tag);
    void handle(Context ctx, Panel panel, Player player, String raw, String command);
}