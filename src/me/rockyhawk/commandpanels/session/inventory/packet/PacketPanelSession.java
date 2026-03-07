package me.rockyhawk.commandpanels.session.inventory.packet;

import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.render.InventoryRenderSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketPanelSession {
    private final UUID playerId;
    private final int windowId;
    private final long openedAt;
    private InventoryPanel panel;
    private InventoryRenderSnapshot snapshot;
    private List<org.bukkit.inventory.ItemStack> committedItems;
    private int stateId;
    private int invalidClickCount;

    public PacketPanelSession(UUID playerId, InventoryPanel panel, int windowId, InventoryRenderSnapshot snapshot) {
        this.playerId = playerId;
        this.panel = panel;
        this.windowId = windowId;
        this.snapshot = snapshot;
        this.committedItems = copyItems(snapshot.topItems());
        this.stateId = 1;
        this.openedAt = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public InventoryPanel getPanel() {
        return panel;
    }

    public void setPanel(InventoryPanel panel) {
        this.panel = panel;
    }

    public String getPanelName() {
        return panel.getName();
    }

    public int getWindowId() {
        return windowId;
    }

    public long getOpenedAt() {
        return openedAt;
    }

    public InventoryRenderSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(InventoryRenderSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public org.bukkit.inventory.ItemStack getPreviousItem(int slot) {
        return slot >= 0 && slot < committedItems.size() ? committedItems.get(slot) : null;
    }

    public void commitSnapshot() {
        this.committedItems = copyItems(snapshot.topItems());
    }

    public int getStateId() {
        return stateId;
    }

    public void incrementStateId() {
        stateId++;
    }

    public int incrementInvalidClickCount() {
        return ++invalidClickCount;
    }

    public void resetInvalidClickCount() {
        invalidClickCount = 0;
    }

    private List<org.bukkit.inventory.ItemStack> copyItems(List<org.bukkit.inventory.ItemStack> items) {
        List<org.bukkit.inventory.ItemStack> copy = new ArrayList<>(items.size());
        for (org.bukkit.inventory.ItemStack item : items) {
            copy.add(item == null ? null : item.clone());
        }
        return copy;
    }
}
