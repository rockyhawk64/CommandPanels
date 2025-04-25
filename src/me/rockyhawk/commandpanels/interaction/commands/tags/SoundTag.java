package me.rockyhawk.commandpanels.interaction.commands.tags;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.commands.TagResolver;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundTag implements TagResolver {
    @Override
    public boolean handle(Context ctx, Panel panel, PanelPosition pos, Player player, String command) {
        String[] args = command.split("\\s+");  // Arguments are space-separated
        if (command.startsWith("sound=")) {
            try {
                if (args.length == 4) {
                    player.playSound(player.getLocation(), Sound.valueOf(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]));
                } else {
                    player.playSound(player.getLocation(), Sound.valueOf(args[1]), 1F, 1F);
                }
            } catch (Exception s) {
                ctx.debug.send(s, player, ctx);
                ctx.text.sendMessage(player, ctx.configHandler.config.getString("config.format.error") + " " + "commands: " + command);
            }
            return true;
        } else if (command.startsWith("stopsound=")) {
            try {
                player.stopSound(Sound.valueOf(args[1]));
            } catch (Exception ss) {
                ctx.debug.send(ss, player, ctx);
                ctx.text.sendMessage(player, ctx.configHandler.config.getString("config.format.error") + " " + "commands: " + command);
            }
            return true;
        }
        return false;
    }
}
