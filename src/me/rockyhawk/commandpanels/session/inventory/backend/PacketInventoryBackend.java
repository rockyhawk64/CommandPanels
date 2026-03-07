package me.rockyhawk.commandpanels.session.inventory.backend;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.packet.PacketPanelSession;
import me.rockyhawk.commandpanels.session.inventory.render.InventoryRenderSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PacketInventoryBackend implements InventoryBackend {
    private final Context ctx;
    private final ConcurrentMap<UUID, PacketPanelSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Integer> chestTypeIds = new ConcurrentHashMap<>();
    private final AtomicInteger nextWindowId = new AtomicInteger(1);

    public PacketInventoryBackend(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public InventoryBackendType type() {
        return InventoryBackendType.PACKET;
    }

    public void open(Player player, InventoryPanel panel, InventoryRenderSnapshot snapshot, boolean isRefresh) {
        PacketPanelSession current = sessions.get(player.getUniqueId());
        PacketPanelSession session;

        if (isRefresh && current != null && current.getPanelName().equals(panel.getName())) {
            current.setPanel(panel);
            current.setSnapshot(snapshot);
            current.incrementStateId();
            session = current;
        } else {
            int windowId = nextWindowId.getAndUpdate(currentId -> currentId >= 100 ? 1 : currentId + 1);
            session = new PacketPanelSession(player.getUniqueId(), panel, windowId, snapshot);
            sessions.put(player.getUniqueId(), session);
        }

        if (ctx.fileHandler.config.getBoolean("packet-inventory-debug", false)) {
            ctx.plugin.getLogger().info("Opening packet-backed panel '" + panel.getName()
                    + "' for " + player.getName()
                    + " with windowId=" + session.getWindowId()
                    + ", size=" + session.getSnapshot().size()
                    + ", stateId=" + session.getStateId()
                    + ", typeId=" + chestTypeId(session.getSnapshot().size())
                    + ", thread=" + Thread.currentThread().getName());
        }
        if (isRefresh) {
            sendOpenWindow(player, session);
            sendFullRender(player, session);
            return;
        }

        int scheduledWindowId = session.getWindowId();
        player.getScheduler().run(ctx.plugin, task -> {
            PacketPanelSession activeSession = sessions.get(player.getUniqueId());
            if (activeSession == null || activeSession.getWindowId() != scheduledWindowId) {
                return;
            }

            if (ctx.fileHandler.config.getBoolean("packet-inventory-debug", false)) {
                ctx.plugin.getLogger().info("Sending initial packet-backed window '" + activeSession.getPanelName()
                        + "' to " + player.getName()
                        + " with windowId=" + activeSession.getWindowId()
                        + ", stateId=" + activeSession.getStateId()
                        + ", thread=" + Thread.currentThread().getName());
            }

            sendOpenWindow(player, activeSession);
            sendFullRender(player, activeSession);
        }, null);
    }

    @Override
    public void applySnapshot(Player player, InventoryPanel panel, InventoryRenderSnapshot snapshot) {
        PacketPanelSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.getPanelName().equals(panel.getName())) {
            return;
        }

        if (!Objects.equals(session.getSnapshot().title(), snapshot.title())
                || session.getSnapshot().size() != snapshot.size()) {
            open(player, panel, snapshot, true);
            return;
        }

        boolean changed = false;
        for (int slot = 0; slot < snapshot.size(); slot++) {
            org.bukkit.inventory.ItemStack currentItem = session.getSnapshot().itemAt(slot);
            org.bukkit.inventory.ItemStack newItem = snapshot.itemAt(slot);
            if (!Objects.equals(currentItem, newItem)) {
                changed = true;
                break;
            }
        }

        if (!changed) {
            return;
        }

        session.setSnapshot(snapshot);
        session.incrementStateId();
        for (int slot = 0; slot < snapshot.size(); slot++) {
            org.bukkit.inventory.ItemStack item = snapshot.itemAt(slot);
            org.bukkit.inventory.ItemStack previous = session.getPreviousItem(slot);
            if (Objects.equals(previous, item)) {
                continue;
            }

            sendPacket(player, new WrapperPlayServerSetSlot(
                    session.getWindowId(),
                    session.getStateId(),
                    slot,
                    toPacketItem(item)));
        }
        session.commitSnapshot();
    }

    @Override
    public boolean isViewing(Player player, InventoryPanel panel) {
        PacketPanelSession session = sessions.get(player.getUniqueId());
        return session != null && session.getPanelName().equals(panel.getName());
    }

    public PacketPanelSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public void resync(Player player) {
        PacketPanelSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (ctx.fileHandler.config.getBoolean("packet-inventory-debug", false)) {
            ctx.plugin.getLogger().info("Resyncing packet-backed panel '" + session.getPanelName()
                    + "' for " + player.getName()
                    + " with windowId=" + session.getWindowId()
                    + ", stateId=" + (session.getStateId() + 1));
        }
        session.incrementStateId();
        sendFullRender(player, session);
    }

    public PacketPanelSession close(Player player, InventoryCloseReason reason, boolean sendClosePacket) {
        PacketPanelSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return null;
        }

        if (sendClosePacket) {
            sendPacket(player, new WrapperPlayServerCloseWindow(session.getWindowId()));
        }
        return session;
    }

    public void shutdown() {
        sessions.clear();
    }

    private void sendOpenWindow(Player player, PacketPanelSession session) {
        if (ctx.fileHandler.config.getBoolean("packet-inventory-debug", false)) {
            ctx.plugin.getLogger().info("[PacketDebug] QUEUE OPEN_WINDOW to " + player.getName()
                    + " containerId=" + session.getWindowId()
                    + " type=" + chestTypeId(session.getSnapshot().size())
                    + " title=" + session.getSnapshot().title());
        }
        sendPacket(player, new WrapperPlayServerOpenWindow(
                session.getWindowId(),
                chestTypeId(session.getSnapshot().size()),
                session.getSnapshot().title()));
    }

    private void sendFullRender(Player player, PacketPanelSession session) {
        if (ctx.fileHandler.config.getBoolean("packet-inventory-debug", false)) {
            ctx.plugin.getLogger().info("[PacketDebug] QUEUE WINDOW_ITEMS to " + player.getName()
                    + " windowId=" + session.getWindowId()
                    + " stateId=" + session.getStateId()
                    + " slots=" + (session.getSnapshot().size() + 36));
        }
        sendPacket(player, new WrapperPlayServerWindowItems(
                session.getWindowId(),
                session.getStateId(),
                buildWindowItems(player, session),
                ItemStack.EMPTY));
        sendPacket(player, new WrapperPlayServerSetCursorItem(ItemStack.EMPTY));
        for (int slot = 0; slot < session.getSnapshot().size(); slot++) {
            sendPacket(player, new WrapperPlayServerSetSlot(
                    session.getWindowId(),
                    session.getStateId(),
                    slot,
                    toPacketItem(session.getSnapshot().itemAt(slot))));
        }
        session.commitSnapshot();
    }

    private int chestTypeId(int size) {
        int rows = size / 9;
        return chestTypeIds.computeIfAbsent(rows, this::resolveChestTypeId);
    }

    private List<ItemStack> buildWindowItems(Player player, PacketPanelSession session) {
        List<ItemStack> items = new ArrayList<>(session.getSnapshot().size() + 36);

        for (int slot = 0; slot < session.getSnapshot().size(); slot++) {
            items.add(toPacketItem(session.getSnapshot().itemAt(slot)));
        }

        org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
        for (int slot = 9; slot < 36; slot++) {
            items.add(toPacketItem(inventory.getItem(slot)));
        }
        for (int slot = 0; slot < 9; slot++) {
            items.add(toPacketItem(inventory.getItem(slot)));
        }

        return items;
    }

    private int resolveChestTypeId(int rows) {
        try {
            MenuType.Typed<?, ?> menuType = switch (rows) {
                case 1 -> MenuType.GENERIC_9X1;
                case 2 -> MenuType.GENERIC_9X2;
                case 3 -> MenuType.GENERIC_9X3;
                case 4 -> MenuType.GENERIC_9X4;
                case 5 -> MenuType.GENERIC_9X5;
                case 6 -> MenuType.GENERIC_9X6;
                default -> null;
            };
            if (menuType == null) {
                return Math.max(0, Math.min(5, rows - 1));
            }

            Object handle = resolveHandle(menuType);
            if (handle == null) {
                return Math.max(0, Math.min(5, rows - 1));
            }

            Class<?> builtInRegistries = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            Field menuField = builtInRegistries.getField("MENU");
            Object registry = menuField.get(null);
            Method getId = registry.getClass().getMethod("getId", Object.class);
            Object result = getId.invoke(registry, handle);
            if (result instanceof Integer id && id >= 0) {
                return id;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return Math.max(0, Math.min(5, rows - 1));
    }

    private Object resolveHandle(Object menuType) throws ReflectiveOperationException {
        for (String methodName : new String[]{"getHandle", "handle"}) {
            try {
                Method method = menuType.getClass().getMethod(methodName);
                method.setAccessible(true);
                return method.invoke(menuType);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private ItemStack toPacketItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return ItemStack.EMPTY;
        }
        return SpigotConversionUtil.fromBukkitItemStack(item);
    }

    private void sendPacket(Player player, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
