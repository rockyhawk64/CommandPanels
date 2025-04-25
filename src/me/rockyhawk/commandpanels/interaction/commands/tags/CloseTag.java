package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;

public class CloseTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("close=")) return false;

        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        PanelPosition position = PanelPosition.valueOf(args[1]);

        if (position == PanelPosition.Middle && ctx.openPanels.hasPanelOpen(player.getName(), position)) {
            ctx.openPanels.closePanelForLoader(player.getName(), PanelPosition.Middle);
        } else if (position == PanelPosition.Bottom && ctx.openPanels.hasPanelOpen(player.getName(), position)) {
            ctx.openPanels.closePanelForLoader(player.getName(), PanelPosition.Bottom);
        } else if (position == PanelPosition.Top && ctx.openPanels.hasPanelOpen(player.getName(), position)) {
            ctx.commands.runCommand(panel, pos, player, "cpc");
        }
        return true;
    }
}