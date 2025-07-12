package me.rockyhawk.commandpanels.session.floodgate.components;

import me.rockyhawk.commandpanels.session.floodgate.FloodgateComponent;
import org.bukkit.configuration.ConfigurationSection;

public class FloodgateButton extends FloodgateComponent {

    private final String iconType;
    private final String iconTexture;

    public FloodgateButton(String id, ConfigurationSection section) {
        super(id, section);
        this.iconType = section.getString("icon-type", "PATH");
        this.iconTexture = section.getString("icon-texture", "");
    }

    public String getIconType() {
        return iconType;
    }

    public String getIconTexture() {
        return iconTexture;
    }
}