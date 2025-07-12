package me.rockyhawk.commandpanels.session;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanelUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager implements Listener {
    private final Context ctx;
    private final Map<UUID, PanelSession> panelSessions = new HashMap<>();

    public SessionManager(Context ctx) {
        this.ctx = ctx;
    }

    public PanelSession getPlayerSession(Player player) {
        return panelSessions.get(player.getUniqueId());
    }

    public void updateSession(Player player, Panel panel, PanelOpenType openType) {
        UUID uuid = player.getUniqueId();
        boolean panelSnooper = ctx.fileHandler.config.getBoolean("panel-snooper");

        panelSessions.compute(uuid, (key, session) -> {
            // Start a new session no panel open, or switch panel
            if (session == null || openType == PanelOpenType.EXTERNAL) {
                session = new PanelSession(panel, player);
                if(panelSnooper && panel != null) ctx.text.sendInfo(Bukkit.getConsoleSender(), String.format("%s opened %s, new session has been started.", player.getName(), panel.getName()));
            } else if(openType == PanelOpenType.CUSTOM) {
                session.setPanel(panel);
                if(panelSnooper) ctx.text.sendInfo(Bukkit.getConsoleSender(), String.format("%s opened %s, new session has been started.", player.getName(), panel.getName()));
            } else if(openType == PanelOpenType.INTERNAL){
                session.setPanel(panel);
                if(panelSnooper) ctx.text.sendInfo(Bukkit.getConsoleSender(), String.format("%s opened %s, continuing existing session.", player.getName(), panel.getName()));
            }

            // Update data file when sessions are started
            ctx.dataLoader.saveDataFileAsync();

            // Assign refresher for new panel
            if (panel instanceof InventoryPanel) {
                InventoryPanelUpdater updater = new InventoryPanelUpdater();
                session.startUpdateTask(ctx, updater);
            }
            return session;
        });
    }

    public void removeSession(Player player) {
        panelSessions.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        // Start session with no panel to allow data to be added
        ctx.session.updateSession(e.getPlayer(), null, PanelOpenType.EXTERNAL);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        // Always remove session once player leaves
        ctx.session.removeSession(e.getPlayer());
    }

    public enum PanelOpenType {
        EXTERNAL,  // Opened via external action
        CUSTOM, // External but don't clear data
        INTERNAL,  // Opened via in-panel navigation
        REFRESH // Internal and refresh only
    }

}