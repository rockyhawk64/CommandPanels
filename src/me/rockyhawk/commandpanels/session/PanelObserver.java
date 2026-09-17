package me.rockyhawk.commandpanels.session;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PanelObserver {

    private final HashSet<String> permissions;
    private final Map<String, HashSet<String>> visualPlaceholders;

    public PanelObserver(YamlConfiguration config){
        permissions = new HashSet<>();
        visualPlaceholders = new HashMap<>();

        for (String key : config.getKeys(true)) {
            boolean isItemField = key.startsWith("items.") && key.split("\\.").length == 3;
            if (!isItemField) continue;

            String itemId = key.split("\\.")[1];

            if (config.isString(key)) {
                placeholderSearch(key, itemId, config.getString(key));
            } else if (config.isList(key)) {
                for (String line : config.getStringList(key)) {
                    placeholderSearch(key, itemId, line);
                }
            }
        }
    }
    private void placeholderSearch(String key, String itemId, String value) {
        if (value == null || value.isEmpty()) return;
        if (!key.endsWith("conditions"))
            extractInto(value, visualPlaceholders.computeIfAbsent(itemId, k -> new HashSet<>()));
    }

    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%[^%]+%");
    private void extractInto(String text, Set<String> target) {
        if (text == null || text.isEmpty()) return;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            target.add(matcher.group());
        }
    }

    public HashSet<String> getPerms() {
        return permissions;
    }
    public void addPerm(String node) {
        permissions.add(node);
    }

    public HashSet<String> getVisualPlaceholders(String itemId) {
        return visualPlaceholders.getOrDefault(itemId, new HashSet<>());
    }
}