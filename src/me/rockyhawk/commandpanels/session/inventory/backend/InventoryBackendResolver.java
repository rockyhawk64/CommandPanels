package me.rockyhawk.commandpanels.session.inventory.backend;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.entity.Player;

import java.util.Collection;

public class InventoryBackendResolver {
    private final Context ctx;

    public InventoryBackendResolver(Context ctx) {
        this.ctx = ctx;
    }

    public ResolvedInventoryBackend resolve(Player player, InventoryPanel panel) {
        InventoryBackendPreference panelPreference = InventoryBackendPreference.from(panel.getInventoryBackend());
        InventoryBackendPreference defaultPreference = InventoryBackendPreference.from(
                ctx.fileHandler.config.getString("inventory-backend-default", "packet"));

        String parsedRows = ctx.text.parseTextToString(player, panel.getRows()).trim();
        Integer packetRows = parsePacketRows(parsedRows);

        if (panelPreference == InventoryBackendPreference.LEGACY) {
            return new ResolvedInventoryBackend(InventoryBackendType.LEGACY, packetRows);
        }

        if (panelPreference == InventoryBackendPreference.PACKET) {
            if (packetRows == null) {
                ctx.plugin.getLogger().severe("Panel '" + panel.getName()
                        + "' requested the packet inventory backend, but rows '" + parsedRows
                        + "' are not supported by the chest-only packet backend.");
                return null;
            }
            return new ResolvedInventoryBackend(InventoryBackendType.PACKET, packetRows);
        }

        if (packetRows == null) {
            return new ResolvedInventoryBackend(InventoryBackendType.LEGACY, null);
        }

        InventoryBackendType type = defaultPreference == InventoryBackendPreference.LEGACY
                ? InventoryBackendType.LEGACY
                : InventoryBackendType.PACKET;
        return new ResolvedInventoryBackend(type, packetRows);
    }

    public void logMigrationWarnings(Collection<Panel> panels) {
        InventoryBackendPreference defaultPreference = InventoryBackendPreference.from(
                ctx.fileHandler.config.getString("inventory-backend-default", "packet"));

        for (Panel panel : panels) {
            if (!(panel instanceof InventoryPanel inventoryPanel)) {
                continue;
            }

            InventoryBackendPreference panelPreference = InventoryBackendPreference.from(inventoryPanel.getInventoryBackend());
            String configuredRows = inventoryPanel.getRows().trim();
            boolean packetCapable = parsePacketRows(configuredRows) != null;
            boolean looksDynamic = configuredRows.contains("%") || configuredRows.contains("{");

            if (panelPreference == InventoryBackendPreference.PACKET && !packetCapable && !looksDynamic) {
                ctx.plugin.getLogger().warning("Panel '" + inventoryPanel.getName()
                        + "' is configured for the packet inventory backend, but rows '"
                        + configuredRows + "' are not supported. The panel will fail closed.");
                continue;
            }

            if (panelPreference == InventoryBackendPreference.AUTO
                    && defaultPreference != InventoryBackendPreference.LEGACY
                    && !packetCapable
                    && !looksDynamic) {
                ctx.plugin.getLogger().warning("Panel '" + inventoryPanel.getName()
                        + "' uses unsupported rows '" + configuredRows
                        + "' for the chest-only packet backend and will fall back to the legacy backend.");
            }
        }
    }

    private Integer parsePacketRows(String rowsValue) {
        if (rowsValue == null || !rowsValue.matches("\\d+")) {
            return null;
        }

        int rows = Integer.parseInt(rowsValue);
        return rows >= 1 && rows <= 6 ? rows : null;
    }
}
