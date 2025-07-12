package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import org.bukkit.configuration.ConfigurationSection;

public class DialogButton extends DialogComponent {

    private final String tooltip;
    private final String width;
    private final String url;
    private final String clipboard;

    public DialogButton(String id, ConfigurationSection section) {
        super(id, section);
        this.tooltip = section.getString("tooltip", "");
        this.width = section.getString("width", "150");

        this.url = section.getString("url", "");
        this.clipboard = section.getString("clipboard", "");

    }

    public String getTooltip() {
        return tooltip;
    }

    public String getWidth() {
        return width;
    }

    public String getUrl() {
        return url;
    }

    public String getClipboard() {
        return clipboard;
    }
}