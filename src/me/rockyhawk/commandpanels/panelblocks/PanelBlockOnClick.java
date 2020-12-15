package me.rockyhawk.commandpanels.panelblocks;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class PanelBlockOnClick implements Listener {
    CommandPanels plugin;
    public PanelBlockOnClick(CommandPanels pl) {
        this.plugin = pl;
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        //if panel blocks are disabled return
        if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
            return;
        }
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();
        assert block != null;
        if(!plugin.blockConfig.contains("blocks")){
            return;
        }
        if(plugin.openPanels.hasPanelOpen(p.getName())) {
            //some blocks run this event twice, skip if panel already open
            //as blocks cannot be clicked obviously if a panel is open
            return;
        }
        for (String configLocation : Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
            String[] loc = configLocation.split("_");
            Location tempLocation = new Location(plugin.getServer().getWorld(loc[0].replaceAll("%dash%","_")),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
            if(tempLocation.equals(block.getLocation())){
                e.setCancelled(true);
                if(plugin.blockConfig.contains("blocks." + configLocation + ".commands")){
                    for(String command : plugin.blockConfig.getStringList("blocks." + configLocation + ".commands")){
                        plugin.commandTags.commandTags(p,plugin.papi(p,command),command);
                    }
                    return;
                }
                //uses the open= tag because it will open a panel with panel names, but also works with open= features like placeholders
                String command = "open= " + plugin.blockConfig.getString("blocks." + configLocation + ".panel");
                plugin.commandTags.commandTags(p, plugin.papi(p, command), command);
            }
        }
    }
}