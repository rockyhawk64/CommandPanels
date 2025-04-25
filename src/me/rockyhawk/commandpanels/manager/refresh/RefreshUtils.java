package me.rockyhawk.commandpanels.manager.refresh;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.Bukkit;

public class RefreshUtils {

    protected boolean isStaticPanel(Panel panel) {
        return panel.getConfig().getStringList("panelType").contains("static");
    }

    protected void removeUnsupportedSounds(Panel panel) {
        if (panel.getConfig().contains("sound-on-open") && Bukkit.getVersion().contains("1.8")) {
            panel.getConfig().set("sound-on-open", null);
        }
    }

    protected int getRefreshDelay(Panel panel, Context ctx) {
        int defaultDelay = ctx.configHandler.config.getInt("config.refresh-delay");
        return panel.getConfig().contains("refresh-delay")
                ? panel.getConfig().getInt("refresh-delay")
                : defaultDelay;
    }

    protected int getAnimateValue(Panel panel) {
        if (!panel.getConfig().contains("animatevalue")) return -1;
        try {
            return Integer.parseInt(panel.getConfig().getString("animatevalue"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}