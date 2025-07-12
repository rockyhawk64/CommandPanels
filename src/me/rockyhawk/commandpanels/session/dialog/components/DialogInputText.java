package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import org.bukkit.configuration.ConfigurationSection;

public class DialogInputText extends DialogComponent {
    private final String rows;
    private final String width;
    private final String height;
    private final String maxLength;
    private final String initial;

    public DialogInputText(String id, ConfigurationSection section) {
        super(id, section);
        this.rows = section.getString("multiline-rows", null);
        this.height = section.getString("multiline-height", null);
        this.width = section.getString("width", "200");
        this.maxLength = section.getString("max-length", "256");
        this.initial = section.getString("initial", "");
    }

    public String getMultilineRows() {
        return rows;
    }

    public String getHeight() {
        return height;
    }

    public String getWidth() {
        return width;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public String getInitial() {
        return initial;
    }
}