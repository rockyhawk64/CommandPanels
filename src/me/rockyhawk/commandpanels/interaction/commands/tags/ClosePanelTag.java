package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.entity.Player;

public class ClosePanelTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.equalsIgnoreCase("cpc") && !command.equalsIgnoreCase("commandpanelclose")) {
            return false;
        }

        // Return if no panel is open
        if (!ctx.openPanels.hasPanelOpen(player.getName(), PanelPosition.Top)) {
            return true;
        }

        // Unclosable panels are at the Top only
        if (ctx.openPanels.getOpenPanel(player.getName(), PanelPosition.Top).getConfig().contains("panelType")) {
            if (ctx.openPanels.getOpenPanel(player.getName(), PanelPosition.Top).getConfig().getStringList("panelType").contains("unclosable")) {
                ctx.openPanels.closePanelForLoader(player.getName(), PanelPosition.Top);
                ctx.openPanels.skipPanelClose.add(player.getName());
            }
        }

        // Close the inventory and remove from skip list
        player.closeInventory();
        ctx.openPanels.skipPanelClose.remove(player.getName());
        return true;
    }
}