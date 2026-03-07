package me.rockyhawk.commandpanels.session.inventory;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemBuilder;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.CommandActions;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.inventory.backend.*;
import me.rockyhawk.commandpanels.session.inventory.packet.PacketInventoryDebugListener;
import me.rockyhawk.commandpanels.session.inventory.packet.PacketInventoryListener;
import me.rockyhawk.commandpanels.session.inventory.packet.PacketPanelSession;
import me.rockyhawk.commandpanels.session.inventory.render.InventoryPanelRenderer;
import me.rockyhawk.commandpanels.session.inventory.render.InventoryRenderSnapshot;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InventoryPanelService {
    private static final long CLICK_COOLDOWN_MILLIS = 100L;

    private final Context ctx;
    private final InventoryBackendResolver resolver;
    private final InventoryPanelRenderer renderer;
    private final LegacyInventoryBackend legacyBackend;
    private final PacketInventoryBackend packetBackend;
    private final AtomicLong nextViewToken = new AtomicLong(1L);
    private final Map<UUID, ActiveInventoryView> activeViews = new ConcurrentHashMap<>();

    public InventoryPanelService(Context ctx) {
        this.ctx = ctx;
        this.resolver = new InventoryBackendResolver(ctx);
        this.renderer = new InventoryPanelRenderer(ctx);
        this.legacyBackend = new LegacyInventoryBackend(ctx);
        this.packetBackend = new PacketInventoryBackend(ctx);
    }

    public void registerPacketListener() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketInventoryListener(this));
        PacketEvents.getAPI().getEventManager().registerListener(new PacketInventoryDebugListener(ctx, this));
    }

    public boolean open(InventoryPanel panel, Player player, boolean isNewPanelSession) {
        ResolvedInventoryBackend resolved = resolver.resolve(player, panel);
        if (resolved == null) {
            return false;
        }

        ActiveInventoryView currentView = activeViews.get(player.getUniqueId());
        boolean preserveToken = !isNewPanelSession
                && currentView != null
                && currentView.panelName().equals(panel.getName());
        boolean sameBackend = preserveToken && currentView != null && currentView.backendType() == resolved.type();

        if (resolved.type() == InventoryBackendType.PACKET) {
            if (packetBackend.hasSession(player) && !sameBackend) {
                closePacketSession(player, InventoryCloseReason.REPLACED, false);
            }

            if (!sameBackend && player.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel) {
                player.closeInventory();
            }

            InventoryRenderSnapshot snapshot = renderer.render(player, panel);
            packetBackend.open(player, panel, snapshot, sameBackend);
        } else {
            if (packetBackend.hasSession(player)) {
                closePacketSession(player, preserveToken ? InventoryCloseReason.REFRESH : InventoryCloseReason.REPLACED, false);
            }
            legacyBackend.open(player, panel);
        }

        registerActiveView(player, panel, resolved.type(), preserveToken);
        return true;
    }

    public void applySnapshot(Player player, InventoryPanel panel) {
        ActiveInventoryView currentView = activeViews.get(player.getUniqueId());
        if (currentView == null || !currentView.panelName().equals(panel.getName())) {
            return;
        }

        InventoryRenderSnapshot snapshot = renderer.render(player, panel);
        snapshot = applyAnimationState(player, panel, currentView, snapshot);
        if (currentView.backendType() == InventoryBackendType.PACKET) {
            packetBackend.applySnapshot(player, panel, snapshot);
        } else {
            legacyBackend.applySnapshot(player, panel, snapshot);
        }
    }

    public boolean isViewingPanel(Player player, InventoryPanel panel) {
        ActiveInventoryView currentView = activeViews.get(player.getUniqueId());
        if (currentView == null || !currentView.panelName().equals(panel.getName())) {
            return false;
        }

        return currentView.backendType() == InventoryBackendType.PACKET
                ? packetBackend.isViewing(player, panel)
                : legacyBackend.isViewing(player, panel);
    }

    public boolean matchesViewToken(Player player, InventoryPanel panel, Object token) {
        if (!(token instanceof Long tokenValue)) {
            return false;
        }

        ActiveInventoryView currentView = activeViews.get(player.getUniqueId());
        return currentView != null
                && currentView.token() == tokenValue
                && currentView.panelName().equals(panel.getName())
                && isViewingPanel(player, panel);
    }

    public Object captureViewToken(Player player, InventoryPanel panel) {
        ActiveInventoryView currentView = activeViews.get(player.getUniqueId());
        if (currentView == null || !currentView.panelName().equals(panel.getName())) {
            return null;
        }

        return isViewingPanel(player, panel) ? currentView.token() : null;
    }

    public void closeActiveView(Player player, InventoryCloseReason reason, boolean sendClientClosePacket) {
        if (packetBackend.hasSession(player)) {
            closePacketSession(player, reason, sendClientClosePacket);
            return;
        }

        if (player.getOpenInventory().getTopInventory().getHolder() instanceof InventoryPanel) {
            player.closeInventory();
        }
    }

    public void closePacketSession(Player player, InventoryCloseReason reason, boolean sendClientClosePacket) {
        PacketPanelSession session = packetBackend.close(player, reason, sendClientClosePacket);
        if (session == null) {
            return;
        }

        clearActiveView(player.getUniqueId(), session.getPanelName());
        if (reason.shouldRunCloseCommands()) {
            runActions(session.getPanel(), player, session.getPanel().getCloseCommands());
        }
    }

    public void onLegacyInventoryClosed(Player player, InventoryPanel panel) {
        clearActiveView(player.getUniqueId(), panel.getName());
    }

    public void shutdown() {
        for (Player player : ctx.plugin.getServer().getOnlinePlayers()) {
            closePacketSession(player, InventoryCloseReason.DISABLE, false);
        }
        activeViews.clear();
        packetBackend.shutdown();
    }

    public boolean hasPacketSession(Player player) {
        return packetBackend.hasSession(player);
    }

    public void handlePacketClick(Player player,
                                  int windowId,
                                  Integer stateId,
                                  int slot,
                                  int button,
                                  WrapperPlayClientClickWindow.WindowClickType clickType) {
        player.getScheduler().run(ctx.plugin, task -> {
            PacketPanelSession session = packetBackend.getSession(player);
            if (session == null) {
                return;
            }

            if (windowId != session.getWindowId()) {
                handleInvalidPacket(player, session, "stale window id " + windowId);
                return;
            }

            if (stateId != null && stateId != session.getStateId()) {
                handleInvalidPacket(player, session, "stale state id " + stateId);
                return;
            }

            if (slot == -999) {
                session.resetInvalidClickCount();
                if (!tryConsumeClick(player)) {
                    return;
                }
                runActions(session.getPanel(), player, session.getPanel().getOutsideCommands());
                return;
            }

            if (slot < 0 || slot >= session.getSnapshot().size()) {
                handleInvalidPacket(player, session, "slot " + slot + " outside packet-backed menu bounds");
                return;
            }

            ClickType resolvedClick = resolveClick(clickType, button);
            if (resolvedClick == null) {
                handleInvalidPacket(player, session, "unsupported click type " + clickType + " button " + button);
                return;
            }

            String itemId = session.getSnapshot().actionSlots().get(slot);
            if (itemId == null) {
                handleInvalidPacket(player, session, "slot " + slot + " is not bound to a panel item");
                return;
            }

            PanelItem panelItem = session.getPanel().getItems().get(itemId);
            if (panelItem == null) {
                handleInvalidPacket(player, session, "missing panel item '" + itemId + "'");
                return;
            }

            session.resetInvalidClickCount();
            if (!tryConsumeClick(player)) {
                resyncIfSessionUnchanged(player, session);
                return;
            }

            runActions(session.getPanel(), player, panelItem.getClickActions(resolvedClick));
            resyncIfSessionUnchanged(player, session);
        }, null);
    }

    public void handlePacketClose(Player player, int windowId) {
        player.getScheduler().run(ctx.plugin, task -> {
            PacketPanelSession session = packetBackend.getSession(player);
            if (session == null) {
                return;
            }

            if (windowId != session.getWindowId()) {
                handleInvalidPacket(player, session, "close packet for stale window id " + windowId);
                return;
            }

            closePacketSession(player, InventoryCloseReason.CLIENT_CLOSE, false);
        }, null);
    }

    public void handleCreativeInventoryAction(Player player, int slot) {
        player.getScheduler().run(ctx.plugin, task -> {
            PacketPanelSession session = packetBackend.getSession(player);
            if (session == null) {
                return;
            }

            handleInvalidPacket(player, session, "creative inventory action on slot " + slot);
        }, null);
    }

    public boolean runActions(Panel panel, Player player, CommandActions actions) {
        RequirementRunner requirements = new RequirementRunner(ctx);
        CommandRunner commands = new CommandRunner(ctx);

        if (!requirements.processRequirements(panel, player, actions.requirements())) {
            commands.runCommands(panel, player, actions.fail());
            return false;
        }

        commands.runCommands(panel, player, actions.commands());
        return true;
    }

    public boolean tryConsumeClick(Player player) {
        NamespacedKey key = new NamespacedKey(ctx.plugin, "last_click_time");
        Long lastClick = player.getPersistentDataContainer().get(key, PersistentDataType.LONG);
        long now = System.currentTimeMillis();
        if (lastClick != null && now - lastClick < CLICK_COOLDOWN_MILLIS) {
            return false;
        }

        player.getPersistentDataContainer().set(key, PersistentDataType.LONG, now);
        return true;
    }

    public void logMigrationWarnings(Collection<Panel> panels) {
        resolver.logMigrationWarnings(panels);
    }

    private void registerActiveView(Player player,
                                    InventoryPanel panel,
                                    InventoryBackendType backendType,
                                    boolean preserveToken) {
        UUID playerId = player.getUniqueId();
        ActiveInventoryView currentView = activeViews.get(playerId);
        long token = preserveToken && currentView != null
                ? currentView.token()
                : nextViewToken.getAndIncrement();
        activeViews.put(playerId, new ActiveInventoryView(panel.getName(), backendType, token));
    }

    private void clearActiveView(UUID playerId, String panelName) {
        activeViews.computeIfPresent(playerId, (ignored, currentView) ->
                currentView.panelName().equals(panelName) ? null : currentView);
    }

    private ClickType resolveClick(WrapperPlayClientClickWindow.WindowClickType clickType, int button) {
        return switch (clickType) {
            case PICKUP -> switch (button) {
                case 0 -> ClickType.LEFT;
                case 1 -> ClickType.RIGHT;
                default -> null;
            };
            case QUICK_MOVE -> switch (button) {
                case 0 -> ClickType.SHIFT_LEFT;
                case 1 -> ClickType.SHIFT_RIGHT;
                default -> null;
            };
            default -> null;
        };
    }

    private void handleInvalidPacket(Player player, PacketPanelSession session, String detail) {
        int invalidCount = session.incrementInvalidClickCount();
        if (ctx.fileHandler.config.getBoolean("packet-inventory-debug", false)) {
            ctx.plugin.getLogger().warning("Invalid packet-backed inventory interaction for player '"
                    + player.getName() + "' on panel '" + session.getPanelName() + "': " + detail);
        }

        packetBackend.resync(player);

        int threshold = ctx.fileHandler.config.getInt("packet-close-on-invalid-click-threshold", 5);
        if (threshold > 0 && invalidCount >= threshold) {
            closePacketSession(player, InventoryCloseReason.INVALID_CLICK, true);
        }
    }

    private void resyncIfSessionUnchanged(Player player, PacketPanelSession expectedSession) {
        PacketPanelSession currentSession = packetBackend.getSession(player);
        if (currentSession == null) {
            return;
        }

        if (currentSession.getWindowId() != expectedSession.getWindowId()) {
            return;
        }

        packetBackend.resync(player);
    }

    private InventoryRenderSnapshot applyAnimationState(Player player,
                                                        InventoryPanel panel,
                                                        ActiveInventoryView currentView,
                                                        InventoryRenderSnapshot snapshot) {
        List<ItemStack> currentItems = getCurrentRenderedItems(player, panel, currentView);
        if (currentItems.isEmpty()) {
            return snapshot;
        }

        NamespacedKey itemIdKey = new NamespacedKey(ctx.plugin, "item_id");
        NamespacedKey baseIdKey = new NamespacedKey(ctx.plugin, "base_item_id");
        NamespacedKey fillItemKey = new NamespacedKey(ctx.plugin, "fill_item");
        ItemBuilder itemBuilder = new ItemBuilder(ctx, new InventoryPanelBuilder(ctx, player));

        List<ItemStack> updatedItems = new ArrayList<>(snapshot.topItems());
        Map<Integer, String> actionSlots = new HashMap<>(snapshot.actionSlots());
        boolean changed = false;

        for (int slot = 0; slot < Math.min(snapshot.size(), currentItems.size()); slot++) {
            ItemStack currentItem = currentItems.get(slot);
            if (currentItem == null || currentItem.getType().isAir() || !currentItem.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = currentItem.getItemMeta();
            if (meta == null) {
                continue;
            }

            if (meta.getPersistentDataContainer().has(fillItemKey, PersistentDataType.STRING)
                    || !meta.getPersistentDataContainer().has(itemIdKey, PersistentDataType.STRING)) {
                continue;
            }

            String itemId = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
            String baseItemId = meta.getPersistentDataContainer().get(baseIdKey, PersistentDataType.STRING);
            if (itemId == null || baseItemId == null) {
                continue;
            }

            PanelItem displayedItem = panel.getItems().get(itemId);
            PanelItem nextItem = null;
            if (displayedItem != null && !displayedItem.animate().isEmpty()) {
                nextItem = panel.getItems().get(displayedItem.animate());
            } else if (!baseItemId.equals(itemId)) {
                nextItem = panel.getItems().get(baseItemId);
            }

            if (nextItem == null) {
                continue;
            }

            ItemStack animatedItem = itemBuilder.buildItem(panel, nextItem);
            animatedItem.editPersistentDataContainer(container ->
                    container.set(baseIdKey, PersistentDataType.STRING, baseItemId));
            updatedItems.set(slot, animatedItem);
            actionSlots.put(slot, baseItemId);
            changed = true;
        }

        if (!changed) {
            return snapshot;
        }

        return new InventoryRenderSnapshot(snapshot.title(), snapshot.size(), updatedItems, actionSlots);
    }

    private List<ItemStack> getCurrentRenderedItems(Player player, InventoryPanel panel, ActiveInventoryView currentView) {
        if (currentView.backendType() == InventoryBackendType.PACKET) {
            PacketPanelSession session = packetBackend.getSession(player);
            return session == null ? List.of() : session.getSnapshot().topItems();
        }

        if (!legacyBackend.isViewing(player, panel)) {
            return List.of();
        }

        List<ItemStack> items = new ArrayList<>(player.getOpenInventory().getTopInventory().getSize());
        for (ItemStack item : player.getOpenInventory().getTopInventory().getContents()) {
            items.add(item == null ? null : item.clone());
        }
        return items;
    }

    private record ActiveInventoryView(String panelName, InventoryBackendType backendType, long token) {
    }
}
