package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import org.bukkit.configuration.ConfigurationSection;

public class DialogInputRange extends DialogComponent {
    private final String start;
    private final String end;
    private final String step;
    private final String initial;
    private final String width;

    public DialogInputRange(String id, ConfigurationSection section) {
        super(id, section);
        this.start = section.getString("start", "1");
        this.end = section.getString("end", "10");
        this.step = section.getString("step", "1");
        this.initial = section.getString("initial", start);
        this.width = section.getString("width", "200");
    }

    public String getInitial() {
        return initial;
    }

    public String getEnd() {
        return end;
    }

    public String getStart() {
        return start;
    }

    public String getStep() {
        return step;
    }

    public String getWidth() {
        return width;
    }
}