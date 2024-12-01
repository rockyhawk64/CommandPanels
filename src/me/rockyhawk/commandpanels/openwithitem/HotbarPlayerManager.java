package me.rockyhawk.commandpanels.openwithitem;

import me.rockyhawk.commandpanels.api.Panel;

import java.util.HashMap;

public class HotbarPlayerManager {
    public HashMap<String, Panel> list = new HashMap<>();
    private boolean hotbarItemsEnabled;  // Variable zum Verwalten des Status der Hotbar-Items

    public HotbarPlayerManager() {
        this.hotbarItemsEnabled = false;  // Standardmäßig sind die Hotbar-Items deaktiviert
    }

    // Fügt ein Panel für einen bestimmten Slot hinzu
    public void addSlot(String slot, Panel panel) {
        list.put(slot, panel);
    }

    // Holt das Panel für einen bestimmten Slot
    public Panel getPanel(String slot) {
        return list.get(slot) != null ? list.get(slot).copy() : null;
    }

    // Setzt den Status der Hotbar-Items (aktiviert oder deaktiviert)
    public void setHotbarItemsEnabled(boolean enabled) {
        this.hotbarItemsEnabled = enabled;
    }

    // Gibt zurück, ob die Hotbar-Items für den Spieler aktiviert sind
    public boolean isHotbarItemsEnabled() {
        return hotbarItemsEnabled;
    }

    // Entfernt alle Panels (z. B. beim Deaktivieren der Hotbar-Items)
    public void clear() {
        list.clear();
        this.hotbarItemsEnabled = false;  // Setzt den Status auf deaktiviert, wenn alle Panels entfernt werden
    }
}
