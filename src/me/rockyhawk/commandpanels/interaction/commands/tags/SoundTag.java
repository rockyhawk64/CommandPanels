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
        String[] args = ctx.text.attachPlaceholders(panel, pos, player, command).split("\\s+"); // Arguments are space-separated
        if (command.startsWith("sound=")) {
            try {
                if (args.length == 4) {
                    String soundName = args[1];
                    float volume, pitch;

                    try {
                        volume = Float.parseFloat(args[2]);
                        pitch = Float.parseFloat(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid number format for volume or pitch.");
                        return false;
                    }

                    try {
                        player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), volume, pitch);
                    } catch (IllegalArgumentException e) {
                        player.playSound(player.getLocation(), soundName, volume, pitch);
                    }
                } else {
                    try {
                        Sound sound = Sound.valueOf(args[1].toUpperCase());
                        player.playSound(player.getLocation(), sound, 1F, 1F);
                    } catch (IllegalArgumentException e) {
                        player.playSound(player.getLocation(), args[1], 1F, 1F);
                    }
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
