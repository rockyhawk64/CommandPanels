package me.rockyhawk.commandpanels.interaction.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Registrable;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

public interface RequirementTagResolver extends Registrable {
    boolean isCorrectTag(String tag);
    boolean check(Context ctx, Panel panel, Player player, String raw, String args);
    void execute(Context ctx, Panel panel, Player player, String raw, String args);
}
