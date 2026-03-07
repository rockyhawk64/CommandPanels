package me.rockyhawk.commandpanels.session.inventory.backend;

import java.util.Locale;

public enum InventoryBackendPreference {
    AUTO,
    PACKET,
    LEGACY;

    public static InventoryBackendPreference from(String value) {
        if (value == null || value.isBlank()) {
            return AUTO;
        }

        try {
            return InventoryBackendPreference.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return AUTO;
        }
    }
}
