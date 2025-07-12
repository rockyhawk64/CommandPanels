package me.rockyhawk.commandpanels.session.floodgate.components;

import me.rockyhawk.commandpanels.session.floodgate.FloodgateComponent;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class FloodgateDropdown extends FloodgateComponent {

    private final List<String> options;

    public FloodgateDropdown(String id, ConfigurationSection section) {
        super(id, section);
        this.options = section.getStringList("options");
    }

    public List<String> getOptions() {
        return options;
    }
}