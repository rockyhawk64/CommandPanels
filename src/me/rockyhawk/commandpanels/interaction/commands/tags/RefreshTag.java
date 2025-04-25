package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.entity.Player;

public class RefreshTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.equalsIgnoreCase("refresh")) {
            return false;
        }

        if (ctx.openPanels.hasPanelOpen(player.getName(), pos)) {
            new PanelBuilder(ctx).refreshInv(panel, player, pos, 0);
        }
        if (ctx.inventorySaver.hasNormalInventory(player)) {
            ctx.hotbar.updateHotbarItems(player);
        }
        return true;
    }
}
