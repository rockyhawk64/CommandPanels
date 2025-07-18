package me.rockyhawk.commandpanels.formatter;

import me.rockyhawk.commandpanels.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final Map<String, String> translations = new HashMap<>();
    private Context ctx;

    public LanguageManager(Context ctx) {
        this.ctx = ctx;
        reloadTranslations();
    }

    // Use translations from the languages file and translate matches.
    // Keys or the English side of translations should have periods omitted as it breaks YAML
    // The plugin will ensure they are still detected without it
    public void reloadTranslations(){
        translations.clear();
        try {
            YamlConfiguration langFile = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder(), "lang.yml"));
            for (String key : langFile.getKeys(false)) {
                translations.put(key.toLowerCase(), langFile.getString(key));
            }
        }catch (IllegalArgumentException e){
            // Send raw message as text class will not be initialised here
            Bukkit.getConsoleSender().sendMessage(Component.text(
                    "[CommandPanels] Language file could not be loaded, periods should not be in language keys.",
                    NamedTextColor.RED));
        }
    }

    public String translate(String message) {
        if (message == null) return "";
        // Normalize: lowercase, remove non-alphabetic characters, trim
        String key = message.toLowerCase().replaceAll("[^a-z]", "").trim();
        return translations.getOrDefault(key, message);
    }
}
