package me.rockyhawk.commandpanels.session.dialog;

import me.rockyhawk.commandpanels.session.CommandActions;
import me.rockyhawk.commandpanels.session.dialog.components.*;
import org.bukkit.configuration.ConfigurationSection;

public abstract class DialogComponent {
    private final String id;
    private final String name;
    private final String conditions;
    private final String type;
    private final CommandActions actions;

    public DialogComponent(String id, ConfigurationSection config) {
        this.id = id;
        this.conditions = config.getString("conditions", "");
        this.name = config.getString("name", "");
        this.type = config.getString("type", "button");
        this.actions = CommandActions.fromSection(config.getConfigurationSection("actions"));
    }

    public static DialogComponent fromSection(String id, ConfigurationSection section) {
        String type = section.getString("type", "button"); // default type

        // Default to a normal button
        return switch (type.toLowerCase()) {
            case "item" -> new DialogItem(id, section);
            case "text" -> new DialogBodyText(id, section);
            case "boolean" -> new DialogInputBool(id, section);
            case "range" -> new DialogInputRange(id, section);
            case "option" -> new DialogInputOption(id, section);
            case "input" -> new DialogInputText(id, section);
            default -> new DialogButton(id, section);
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

    public CommandActions getClickActions() {
        return actions;
    }
}