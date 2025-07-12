package me.rockyhawk.commandpanels.session.floodgate.components;

import me.rockyhawk.commandpanels.session.floodgate.FloodgateComponent;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class FloodgateStepSlider extends FloodgateComponent {
    private final List<String> steps;
    private final String defaultValue;

    public FloodgateStepSlider(String id, ConfigurationSection section) {
        super(id, section);
        this.steps = section.getStringList("steps");
        this.defaultValue = section.getString("default", "");
    }

    public List<String> getSteps() {
        return steps;
    }

    public String getDefault() {
        return defaultValue;
    }
}