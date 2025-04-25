package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DataAddTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("add-data=")) return false;

        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        args = Arrays.copyOfRange(args, 1, args.length); // Remove the tag name

        if (args.length == 3) {
            ctx.panelData.setUserData(
                    ctx.panelDataPlayers.getOffline(args[2]),
                    ctx.text.placeholdersNoColour(panel, pos, player, args[0]),
                    ctx.text.placeholdersNoColour(panel, pos, player, args[1]), false
            );
            return true;
        }

        ctx.panelData.setUserData(
                player.getUniqueId(),
                ctx.text.placeholdersNoColour(panel, pos, player, args[0]),
                ctx.text.placeholdersNoColour(panel, pos, player, args[1]), false
        );
        return true;
    }
}