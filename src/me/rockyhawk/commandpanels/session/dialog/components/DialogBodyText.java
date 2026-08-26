package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import org.bukkit.configuration.ConfigurationSection;

public class DialogBodyText extends DialogComponent {

    private final String width;

    public DialogBodyText(String id, ConfigurationSection section) {
        super(id, section);
        this.width = section.getString("width", "200");
    }

    public String getWidth() {
        return width;
    }

}