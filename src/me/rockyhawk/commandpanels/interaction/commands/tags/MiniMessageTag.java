package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MiniMessageTag implements TagResolver {
    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        if (command.startsWith("minimessage=")) {
            if (ctx.version.isAtLeast("1.18")) {
                Audience audience = (Audience) player;
                Component parsedText = ctx.miniMessage.doMiniMessage(command.substring(13)); //substring "minimessage= "
                audience.sendMessage(parsedText);
            } else {
                ctx.text.sendString(player, ctx.tag + ChatColor.RED + "MiniMessage-Feature needs Paper 1.18 or newer to work!");
            }
            return true;
        }
        return false;
    }
}
