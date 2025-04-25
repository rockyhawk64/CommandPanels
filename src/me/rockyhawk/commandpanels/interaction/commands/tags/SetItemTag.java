package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetItemTag implements TagResolver {

    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (!command.startsWith("setitem=")) return false;

        String[] args = command.split("\\s+");
        ItemStack item = ctx.itemBuilder.buildItem(null, pos, panel.getConfig().getConfigurationSection("custom-item." + args[1]), player, false);
        PanelPosition position = PanelPosition.valueOf(args[3]);

        if (position == PanelPosition.Top) {
            player.getOpenInventory().getTopInventory().setItem(Integer.parseInt(args[2]), item);
        } else if (position == PanelPosition.Middle) {
            player.getInventory().setItem(Integer.parseInt(args[2]) + 9, item);
        } else {
            player.getInventory().setItem(Integer.parseInt(args[2]), item);
        }
        return true;
    }
}
