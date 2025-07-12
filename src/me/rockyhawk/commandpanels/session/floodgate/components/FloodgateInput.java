package me.rockyhawk.commandpanels.session.floodgate.components;

import me.rockyhawk.commandpanels.session.floodgate.FloodgateComponent;
import org.bukkit.configuration.ConfigurationSection;

public class FloodgateInput extends FloodgateComponent {

    private final String placeholder;
    private final String defaultValue;

    public FloodgateInput(String id, ConfigurationSection section) {
        super(id, section);
        this.placeholder = section.getString("placeholder", "");
        this.defaultValue = section.getString("default", "");
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getDefault() {
        return defaultValue;
    }
}