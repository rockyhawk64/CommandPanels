package me.rockyhawk.commandpanels.manager.open;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SoundHandler {
    private final Context ctx;

    public SoundHandler(Context ctx) {
        this.ctx = ctx;
    }

    public void playOpenSound(Panel panel, Player p) {
        if (panel.getConfig().contains("sound-on-open")) {
            String soundStr = panel.getConfig().getString("sound-on-open");
            if (!"off".equalsIgnoreCase(soundStr)) {
                try {
                    String[] args = Objects.requireNonNull(soundStr).split(" ");
                    if (args.length >= 3) {
                        p.playSound(p.getLocation(), Sound.valueOf(args[0].toUpperCase()), Float.parseFloat(args[1]), Float.parseFloat(args[2]));
                    } else {
                        p.playSound(p.getLocation(), Sound.valueOf(soundStr.toUpperCase()), 1F, 1F);
                    }
                } catch (Exception e) {
                    p.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.error") + " sound-on-open: " + soundStr));
                }
            }
        }
    }
}
