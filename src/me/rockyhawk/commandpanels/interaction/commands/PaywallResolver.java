package me.rockyhawk.commandpanels.interaction.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public interface PaywallResolver {
    PaywallOutput handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command, boolean performOperation);
}