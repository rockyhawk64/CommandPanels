package me.rockyhawk.commandpanels.session.floodgate.components;

import me.rockyhawk.commandpanels.session.floodgate.FloodgateComponent;
import org.bukkit.configuration.ConfigurationSection;

public class FloodgateSlider extends FloodgateComponent {

    private final String minimum;
    private final String maximum;
    private final String step;
    private final String defaultValue;

    public FloodgateSlider(String id, ConfigurationSection section) {
        super(id, section);
        this.minimum = section.getString("min", "1");
        this.maximum = section.getString("max", "10");
        this.step = section.getString("step", "1");
        this.defaultValue = section.getString("default", minimum);
    }

    public String getMinimum() {
        return minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public String getStep() {
        return step;
    }

    public String getDefault() {
        return defaultValue;
    }
}