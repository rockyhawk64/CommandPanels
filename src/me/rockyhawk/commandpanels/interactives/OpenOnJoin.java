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
        //only opens when the player first logins
        openOnJoin(e.getPlayer(),"open-on-login.");
    }

    @EventHandler
    public void onWorldJoin(PlayerChangedWorldEvent e){
        //only opens when the player changes the world internally
        openOnJoin(e.getPlayer(),"open-on-join.");
    }

    private void openOnJoin(Player p, String joinType){
        if(plugin.config.contains(joinType + p.getWorld().getName())){
            String command = "open= " + plugin.config.getString(joinType + p.getWorld().getName());
            plugin.commandTags.runCommand(null, PanelPosition.Top,p, command);
        }
    }
}
