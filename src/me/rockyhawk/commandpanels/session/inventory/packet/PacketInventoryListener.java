package me.rockyhawk.commandpanels.session.inventory.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanelService;
import org.bukkit.entity.Player;

public class PacketInventoryListener extends PacketListenerAbstract {
    private final InventoryPanelService inventoryPanels;

    public PacketInventoryListener(InventoryPanelService inventoryPanels) {
        this.inventoryPanels = inventoryPanels;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!inventoryPanels.hasPacketSession(player)) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            event.setCancelled(true);
            inventoryPanels.handlePacketClick(
                    player,
                    wrapper.getWindowId(),
                    wrapper.getStateId().orElse(null),
                    wrapper.getSlot(),
                    wrapper.getButton(),
                    wrapper.getWindowClickType());
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            WrapperPlayClientCloseWindow wrapper = new WrapperPlayClientCloseWindow(event);
            event.setCancelled(true);
            inventoryPanels.handlePacketClose(player, wrapper.getWindowId());
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            WrapperPlayClientCreativeInventoryAction wrapper = new WrapperPlayClientCreativeInventoryAction(event);
            event.setCancelled(true);
            inventoryPanels.handleCreativeInventoryAction(player, wrapper.getSlot());
        }
    }
}
