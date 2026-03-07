package me.rockyhawk.commandpanels.session.inventory.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanelService;
import org.bukkit.entity.Player;

public class PacketInventoryDebugListener extends PacketListenerAbstract {
    private final Context ctx;
    private final InventoryPanelService inventoryPanels;

    public PacketInventoryDebugListener(Context ctx, InventoryPanelService inventoryPanels) {
        this.ctx = ctx;
        this.inventoryPanels = inventoryPanels;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !isDebugEnabled()) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow wrapper = new WrapperPlayServerOpenWindow(event);
            ctx.plugin.getLogger().info("[PacketDebug] SEND OPEN_WINDOW to " + player.getName()
                    + " containerId=" + wrapper.getContainerId()
                    + " type=" + wrapper.getType()
                    + " title=" + wrapper.getTitle());
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event);
            ctx.plugin.getLogger().info("[PacketDebug] SEND WINDOW_ITEMS to " + player.getName()
                    + " windowId=" + wrapper.getWindowId()
                    + " stateId=" + wrapper.getStateId()
                    + " slots=" + wrapper.getItems().size());
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
            WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event);
            if (inventoryPanels.hasPacketSession(player)) {
                ctx.plugin.getLogger().info("[PacketDebug] SEND SET_SLOT to " + player.getName()
                        + " windowId=" + wrapper.getWindowId()
                        + " stateId=" + wrapper.getStateId()
                        + " slot=" + wrapper.getSlot());
            }
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.CLOSE_WINDOW) {
            WrapperPlayServerCloseWindow wrapper = new WrapperPlayServerCloseWindow(event);
            ctx.plugin.getLogger().info("[PacketDebug] SEND CLOSE_WINDOW to " + player.getName()
                    + " windowId=" + wrapper.getWindowId());
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !isDebugEnabled()) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            WrapperPlayClientCloseWindow wrapper = new WrapperPlayClientCloseWindow(event);
            ctx.plugin.getLogger().info("[PacketDebug] RECV CLOSE_WINDOW from " + player.getName()
                    + " windowId=" + wrapper.getWindowId());
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            if (inventoryPanels.hasPacketSession(player)) {
                ctx.plugin.getLogger().info("[PacketDebug] RECV CLICK_WINDOW from " + player.getName()
                        + " windowId=" + wrapper.getWindowId()
                        + " stateId=" + wrapper.getStateId().orElse(null)
                        + " slot=" + wrapper.getSlot()
                        + " button=" + wrapper.getButton()
                        + " type=" + wrapper.getWindowClickType());
            }
        }
    }

    private boolean isDebugEnabled() {
        return ctx.fileHandler != null && ctx.fileHandler.config.getBoolean("packet-inventory-debug", false);
    }
}
