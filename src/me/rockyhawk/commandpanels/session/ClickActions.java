package me.rockyhawk.commandpanels.session;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public record ClickActions(
        List<String> requirements,
        List<String> commands,
        List<String> fail
) {
    public static ClickActions fromSection(ConfigurationSection section) {
        if (section == null) return new ClickActions(List.of(), List.of(), List.of());
        return new ClickActions(
                section.getStringList("requirements"),
                section.getStringList("commands"),
                section.getStringList("fail")
        );
    }
}