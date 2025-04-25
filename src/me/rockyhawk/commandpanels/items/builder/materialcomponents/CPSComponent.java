package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CPSComponent implements MaterialComponent {
    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("cps=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        try {
            ItemStack s;
            if (tag.split("\\s")[1].equalsIgnoreCase("self")) {
                //if cps= self
                s = new ItemStack(ctx.customHeads.getPlayerHead(player.getName()));
            }else if (ctx.text.placeholdersNoColour(panel,pos,player,tag.split("\\s")[1]).length() <= 16) {
                //if cps= username
                s = ctx.customHeads.getPlayerHead(ctx.text.placeholdersNoColour(panel,pos,player,tag.split("\\s")[1]));
            } else {
                //custom data cps= base64
                s = ctx.customHeads.getCustomHead(ctx.text.placeholdersNoColour(panel,pos,player,tag.split("\\s")[1]));
            }
            return s;
        } catch (Exception var32) {
            Bukkit.getConsoleSender().sendMessage(ctx.text.colour( ctx.tag + ctx.configHandler.config.getString("config.format.error") + " head material: Could not load skull"));
        }
        return null;
    }
}
