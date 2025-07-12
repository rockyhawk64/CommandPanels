package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class DialogInputOption extends DialogComponent {
    private final List<String> options;
    private final String initial;
    private final String width;

    public DialogInputOption(String id, ConfigurationSection section) {
        super(id, section);
        this.options = section.getStringList("options");
        this.initial = section.getString("initial", options.isEmpty() ? "" : options.get(0));
        this.width = section.getString("width", "200");
    }

    public List<String> getOptions() {
        return options;
    }

    public String getWidth() {
        return width;
    }

    public String getInitial() {
        return initial;
    }
}