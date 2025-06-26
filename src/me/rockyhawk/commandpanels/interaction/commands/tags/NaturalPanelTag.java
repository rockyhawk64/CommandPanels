package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class NaturalPanelTag implements TagResolver {
    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (command.startsWith("natural=")) {
            if (ctx.version.isBelow("1.21.6")) return true;
            Plugin plugin = Bukkit.getPluginManager().getPlugin("NaturalPanels");
            if (plugin != null && plugin.isEnabled()) {
                try {
                    player.closeInventory();
                    Class<?> clazz = Class.forName("me.rockyhawk.naturalpanels.NaturalPanels");
                    Method method = clazz.getMethod("openPanel", Player.class, String.class);
                    boolean result = (boolean) method.invoke(null, player, command.substring(9)); //substring "natural= "

                    if (!result) {
                        ctx.text.sendMessage(player, ChatColor.RED + "Panel does not exist.");
                    }
                } catch (Exception e) {
                    ctx.debug.send(e, player, ctx);
                }
                return true;
            }
        }
        return false;
    }
}
