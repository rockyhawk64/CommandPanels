package me.rockyhawk.commandpanels.interaction.logic;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.entity.Player;

public interface ConditionNode {
    boolean evaluate(Player player, Context ctx, Panel panel);
}
