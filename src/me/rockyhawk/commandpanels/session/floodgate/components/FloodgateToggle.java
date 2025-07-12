package me.rockyhawk.commandpanels.session.floodgate.components;

import me.rockyhawk.commandpanels.session.floodgate.FloodgateComponent;
import org.bukkit.configuration.ConfigurationSection;

public class FloodgateToggle extends FloodgateComponent {

    private final String defaultValue;

    public FloodgateToggle(String id, ConfigurationSection section) {
        super(id, section);
        this.defaultValue = section.getString("default", "false");
    }

    public String getDefault() {
        return defaultValue;
    }
}