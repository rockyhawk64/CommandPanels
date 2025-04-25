package me.rockyhawk.commandpanels.interaction.click;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;

import java.util.Objects;

public class ClickSlotResolver {
    private final InteractionHandler handler;

    protected ClickSlotResolver(InteractionHandler handler) {
        this.handler = handler;
    }

    protected PanelPosition resolveSlotPosition(InventoryClickEvent e, Panel panel, Player p, int clickedSlot) {
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                e.setCancelled(true);
                return null;
            }

            if (e.getSlotType() == InventoryType.SlotType.CONTAINER) {
                if (handler.ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Middle)) {
                    e.setCancelled(false);
                    return PanelPosition.Middle;
                }
            } else {
                if (handler.ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Bottom)) {
                    e.setCancelled(true);
                    return PanelPosition.Bottom;
                }
            }

            e.setCancelled(itemsUnmovable(panel));
            return null;
        }

        return PanelPosition.Top;
    }

    protected String resolveClickedItem(Panel panel, Player p, int clickedSlot, PanelPosition position) {
        for (String item : Objects.requireNonNull(panel.getConfig().getConfigurationSection("item")).getKeys(false)) {
            String slot = handler.ctx.text.placeholdersNoColour(panel, position, p, item);
            if (slot.equals(String.valueOf(clickedSlot))) {
                return item;
            }
        }

        for (String item : panel.getConfig().getConfigurationSection("item").getKeys(false)) {
            String section = handler.ctx.has.hasSection(panel, position, panel.getConfig().getConfigurationSection("item." + item), p);
            String dupe = panel.getConfig().getString("item." + item + section + ".duplicate");
            if (isSlotInDuplicate(clickedSlot, dupe)) return item;
        }

        return null;
    }

    private boolean isSlotInDuplicate(int slot, String duplicateConfig) {
        if (duplicateConfig == null) return false;
        for (String dupe : duplicateConfig.split(",")) {
            dupe = dupe.trim();
            if (dupe.contains("-")) {
                String[] range = dupe.split("-");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                if (slot >= min && slot <= max) return true;
            } else {
                try {
                    if (Integer.parseInt(dupe) == slot) return true;
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }

    private boolean itemsUnmovable(Panel panel) {
        return panel.getConfig().isSet("panelType") &&
                panel.getConfig().getStringList("panelType").contains("unmovable");
    }
}