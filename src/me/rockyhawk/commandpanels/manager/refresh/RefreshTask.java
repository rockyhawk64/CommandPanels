package me.rockyhawk.commandpanels.manager.refresh;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.events.PanelOpenedEvent;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class RefreshTask extends BukkitRunnable {
    private final Context ctx;
    private final PanelOpenedEvent event;
    private final Panel panel;
    private final Player player;
    private final int refreshDelay;
    private final int animateValue;
    private int tickCounter = 0;
    private int animateCounter = 0;
    private final PanelBuilder builder;

    public RefreshTask(Context ctx, PanelOpenedEvent event, Panel panel, Player player, int refreshDelay, int animateValue) {
        this.ctx = ctx;
        this.event = event;
        this.panel = panel;
        this.player = player;
        this.refreshDelay = refreshDelay;
        this.animateValue = animateValue;
        this.builder = new PanelBuilder(ctx);
    }

    @Override
    public void run() {
        if (!panel.isOpen || player.getOpenInventory().getTopInventory().getHolder() != player) {
            stopPanel();
            return;
        }

        if (tickCounter++ >= refreshDelay) {
            tickCounter = 0;

            if (animateValue != -1) {
                animateCounter = (animateCounter + 1) % (animateValue + 1);
            }

            try {
                if (ctx.debug.isEnabled(player) && panel.getFile() != null) {
                    panel.setConfig(YamlConfiguration.loadConfiguration(panel.getFile()));
                }
                builder.refreshInv(panel, player, event.getPosition(), animateCounter);
            } catch (Exception e) {
                player.closeInventory();
                ctx.openPanels.closePanelForLoader(player.getName(), event.getPosition());
                this.cancel();
            }
        }
    }

    private void stopPanel() {
        if (ctx.configHandler.isTrue("config.stop-sound")) {
            try {
                String soundName = panel.getConfig().getString("sound-on-open");
                if (soundName != null) {
                    player.stopSound(Sound.valueOf(soundName.toUpperCase()));
                }
            } catch (Exception ignored) {}
        }

        player.updateInventory();
        removeCommandPanelsItems();
        this.cancel();
    }

    private void removeCommandPanelsItems() {
        if (ctx.inventorySaver.hasNormalInventory(player)) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && ctx.nbt.hasNBT(item, "CommandPanelsItem")) {
                    player.getInventory().remove(item);
                }
            }
        }
    }
}