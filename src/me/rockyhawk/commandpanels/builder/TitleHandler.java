package me.rockyhawk.commandpanels.builder;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class TitleHandler {
    public static String getTitle(Context ctx, Panel panel, Player p, PanelPosition position, int animateValue) {
        ConfigurationSection config = panel.getConfig();
        if (config.contains("title.animate" + animateValue)) {
            return ctx.text.placeholders(panel, position, p, config.getString("title.animate" + animateValue));
        } else {
            return ctx.text.placeholders(panel, position, p, config.getString("title"));
        }
    }
}