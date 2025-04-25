package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveItemTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("give-item=")) return false;

        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+");
        ItemStack itm = ctx.itemBuilder.buildItem(null, pos, panel.getConfig().getConfigurationSection("custom-item." + args[1]), player, false);

        if (args.length == 3) {
            try {
                itm.setAmount(Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                ctx.debug.send(e, player, ctx);
            }
        }

        ctx.inventorySaver.addItem(player, itm);
        return true;
    }
}