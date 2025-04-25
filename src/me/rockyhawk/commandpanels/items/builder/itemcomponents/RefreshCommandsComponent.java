package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RefreshCommandsComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("refresh-commands")) return item;

        if (ctx.openPanels.hasPanelOpen(player.getName(), panel.getName(), pos)) {
            try {
                ctx.commands.runCommands(panel,pos,player,section.getStringList("refresh-commands"), null);
            }catch(Exception ex){
                ctx.debug.send(ex,player, ctx);
            }
        }

        return item;
    }
}