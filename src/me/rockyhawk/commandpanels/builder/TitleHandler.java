package me.rockyhawk.commandpanels.builder;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class TitleHandler {
    public String getTitle(Context ctx, Panel panel, Player p, PanelPosition position, Integer animateValue){
        String title;
        ConfigurationSection pconfig = panel.getConfig();
        if(pconfig.contains("custom-title")) {
            //used for titles in the custom-title section, for has sections
            String section = ctx.has.hasSection(panel,position,pconfig.getConfigurationSection("custom-title"), p);

            //check for if there is animations inside the custom-title section
            if (pconfig.contains("custom-title" + section + ".animate" + animateValue)) {
                section = section + ".animate" + animateValue;
            }

            title = ctx.text.placeholders(panel, position, p, pconfig.getString("custom-title" + section + ".title"));
        }else {
            //regular inventory title
            title = ctx.text.placeholders(panel, position, p, pconfig.getString("title"));
        }
        return title;
    }
}