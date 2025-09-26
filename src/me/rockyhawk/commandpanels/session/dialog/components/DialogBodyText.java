package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import org.bukkit.configuration.ConfigurationSection;

public class DialogBodyText extends DialogComponent {

    private final int width;

    public DialogBodyText(String id, ConfigurationSection section) {
        super(id, section);
        this.width = section.getInt("width", 200);
    }

    public int getWidth() {
        return width;
    }

}