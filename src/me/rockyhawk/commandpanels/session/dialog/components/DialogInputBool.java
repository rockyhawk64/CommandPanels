package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import org.bukkit.configuration.ConfigurationSection;

public class DialogInputBool extends DialogComponent {
    private final String initial;

    public DialogInputBool(String id, ConfigurationSection section) {
        super(id, section);
        this.initial = section.getString("initial", "false");
    }

    public String getInitial() {
        return initial;
    }
}