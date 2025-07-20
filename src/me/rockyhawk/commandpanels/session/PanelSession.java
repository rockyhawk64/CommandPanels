package me.rockyhawk.commandpanels.session;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PanelSession {
    private Panel panel;
    private Panel previous;
    private final Player player;
    private final Map<String, String> data;

    private PanelUpdater updater;

    public PanelSession(Panel panel, Player player) {
        this.panel = panel;
        this.previous = null;
        this.player = player;
        this.data = new HashMap<>();
    }

    public Panel getPanel() {
        return panel;
    }

    public void setPanel(Panel panel) {
        // First panel since session start
        if(this.panel == null) return;
        // Update previous panel if new panel is different
        if(!panel.getName().equals(this.panel.getName()))
            this.previous = this.panel;
        this.panel = panel;
    }

    public Player getPlayer(){
        return player;
    }

    // Updater controls
    public void startUpdateTask(Context ctx, PanelUpdater updater) {
        removeUpdateTask();
        this.updater = updater;
        updater.start(ctx, this);
    }

    public void removeUpdateTask() {
        if (updater != null){
            this.updater.stop();
            this.updater = null;
        }
    }

    // Session data
    public void setData(String key, String value) {
        data.put(key, value);
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void removeData(String key) {
        data.remove(key);
    }

    public void clearData() {
        data.clear();
    }

    public Panel getPrevious() {
        return previous;
    }
}