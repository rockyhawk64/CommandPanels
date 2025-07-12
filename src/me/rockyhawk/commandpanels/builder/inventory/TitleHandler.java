package me.rockyhawk.commandpanels.builder.inventory;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class TitleHandler {
    public Component getTitle(Context ctx, InventoryPanel panel, Player p){
        Component title;
        title = ctx.text.parseTextToComponent(p, panel.getTitle());
        return title;
    }
}