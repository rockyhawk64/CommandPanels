package me.rockyhawk.commandpanels.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PanelOpenedEvent extends Event implements Cancellable {

    private boolean isCancelled;
    private Player p;
    private ConfigurationSection cf;
    private String name;

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public PanelOpenedEvent(Player player, ConfigurationSection panelConfig, String panelName) {
        this.p = player;
        this.cf = panelConfig;
        this.name = panelName;
    }

    public Player getPlayer(){
        return this.p;
    }

    public ConfigurationSection getPanelConfig(){
        return this.cf;
    }

    public String getPanelName(){
        return this.name;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
