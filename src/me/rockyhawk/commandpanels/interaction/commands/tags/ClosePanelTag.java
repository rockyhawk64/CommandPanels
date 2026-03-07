package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.inventory.backend.InventoryCloseReason;
import org.bukkit.entity.Player;

public class ClosePanelTag implements CommandTagResolver {

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.startsWith("[close]");
    }

    @Override
    public void handle(Context ctx, Panel panel, Player player, String raw, String command) {
        ctx.inventoryPanels.closeActiveView(player, InventoryCloseReason.CLIENT_CLOSE, true);
    }
}

