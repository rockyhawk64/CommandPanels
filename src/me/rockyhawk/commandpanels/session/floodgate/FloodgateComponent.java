package me.rockyhawk.commandpanels.session.floodgate;

import me.rockyhawk.commandpanels.session.ClickActions;
import me.rockyhawk.commandpanels.session.floodgate.components.*;
import org.bukkit.configuration.ConfigurationSection;

public abstract class FloodgateComponent {
    private final String id;
    private final String name;
    private final String conditions;
    private final String type;
    private final ClickActions actions;

    public FloodgateComponent(String id, ConfigurationSection config) {
        this.id = id;
        this.conditions = config.getString("conditions", "");
        this.name = config.getString("name", "");
        this.type = config.getString("type", "button");
        this.actions = ClickActions.fromSection(config.getConfigurationSection("actions"));
    }

    public static FloodgateComponent fromSection(String id, ConfigurationSection section) {
        String type = section.getString("type", "button"); // default type

        return switch (type.toLowerCase()) {
            case "button" -> new FloodgateButton(id, section);
            case "label" -> new FloodgateLabel(id, section);
            case "dropdown" -> new FloodgateDropdown(id, section);
            case "input" -> new FloodgateInput(id, section);
            case "slider" -> new FloodgateSlider(id, section);
            case "step-slider" -> new FloodgateStepSlider(id, section);
            case "toggle" -> new FloodgateToggle(id, section);
            default -> null;
        };
    }

    public String getName() { return name; }

    public String getConditions() {
        return conditions;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public ClickActions getClickActions() {
        return actions;
    }
}