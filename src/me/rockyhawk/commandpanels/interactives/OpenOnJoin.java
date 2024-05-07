package me.rockyhawk.commandpanels.interactives;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class OpenOnJoin implements Listener {
    CommandPanels plugin;
    public OpenOnJoin(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onWorldLogin(PlayerJoinEvent e){
        if (!e.getPlayer().hasPlayedBefore() && plugin.config.contains("open-on-first-login")) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
                    openOnJoin(e.getPlayer(), "open-on-first-login"), 40L);  // 2 seconds delay
            return;
        }
        //only opens when the player logs into the server
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
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
        if(plugin.config.contains(joinString)){
            String command = "open= " + plugin.config.getString(joinString);
            plugin.commandRunner.runCommand(null, PanelPosition.Top,p, command);
        }
    }
}
