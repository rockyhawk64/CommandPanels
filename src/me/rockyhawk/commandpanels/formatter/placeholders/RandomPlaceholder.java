package me.rockyhawk.commandpanels.formatter.placeholders;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.PlaceholderResolver;
import org.bukkit.OfflinePlayer;

import java.util.Random;

public class RandomPlaceholder implements PlaceholderResolver {

    @Override
    public String resolve(OfflinePlayer player, String identifier, Context ctx) {
        if (!identifier.startsWith("random_")) return null;

        // Extract the min and max values from the identifier "random-min,max"
        String rangePart = identifier.substring("random_".length());
        String[] parts = rangePart.split(",");

        if (parts.length != 2) {
            return "Invalid format";
        }

        try {
            int min = (int) Double.parseDouble(parts[0]);
            int max = (int) Double.parseDouble(parts[1]);

            if (min >= max) {
                return "Invalid range";
            }

            return String.valueOf(getRandomNumberInRange(min, max));
        } catch (NumberFormatException e) {
            return "Invalid numbers";
        }
    }

    private int getRandomNumberInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }
}
