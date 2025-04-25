package me.rockyhawk.commandpanels.manager;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerJoinEvent implements Listener {
    Context ctx;
    public PlayerJoinEvent(Context pl) {
        this.ctx = pl;
    }
    @EventHandler
    public void onWorldLogin(org.bukkit.event.player.PlayerJoinEvent e){
        if (!e.getPlayer().hasPlayedBefore() && ctx.configHandler.config.contains("open-on-first-login")) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ctx.plugin, () ->
                    openOnJoin(e.getPlayer(), "open-on-first-login"), 40L);  // 2 seconds delay
            return;
        }
        //only opens when the player logs into the server
        Bukkit.getScheduler().scheduleSyncDelayedTask(ctx.plugin, () ->
                openOnJoin(e.getPlayer(),"open-on-login"), 20L);  // 1 seconds delay
    }

    @EventHandler
    public void onWorldJoin(PlayerChangedWorldEvent e){
        //only opens when the player changes the world internally
        openOnJoin(e.getPlayer(),"open-on-join");
    }

    private void openOnJoin(Player p, String joinType){
        String world = p.getWorld().getName();
        // Limited to '1' panel as you can only have '1' inventory open
        // pass the world as "" to tell the code to not use world
        if (joinType.equalsIgnoreCase("open-on-first-login")) world="";

        String joinString = joinType + (world.isEmpty() ? "" : "."+ world);
        if(ctx.configHandler.config.contains(joinString)){
            String command = "open= " + ctx.configHandler.config.getString(joinString);
            ctx.commands.runCommand(null, PanelPosition.Top,p, command);
        }
    }
}