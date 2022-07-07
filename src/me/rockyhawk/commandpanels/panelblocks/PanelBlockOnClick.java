package me.rockyhawk.commandpanels.panelblocks;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class PanelBlockOnClick implements Listener {
    CommandPanels plugin;
    public PanelBlockOnClick(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            boolean isPanelBlock = blockClickEventTrigger(e.getClickedBlock().getLocation(), e.getPlayer(), true);
            if (isPanelBlock) {
                blockClickEventTrigger(e.getClickedBlock().getLocation(), e.getPlayer(), false);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e){
        boolean isPanelBlock = blockClickEventTrigger(e.getRightClicked().getLocation().getBlock().getLocation(), e.getPlayer(),true);
        if(isPanelBlock) {
            if (!e.getPlayer().isSneaking()) {
                blockClickEventTrigger(e.getRightClicked().getLocation().getBlock().getLocation(), e.getPlayer(), false);
                e.setCancelled(true);
            }
        }
    }

    //if isVoid is used, it will not trigger anything to happen
    public boolean blockClickEventTrigger(Location location, Player p, boolean isVoid){
        //if panel blocks are disabled return
        if(Objects.requireNonNull(plugin.config.getString("config.panel-blocks")).equalsIgnoreCase("false")){
            return false;
        }
        if(!plugin.blockConfig.contains("blocks")){
            return false;
        }
        if(plugin.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top)) {
            //some blocks run this event twice, skip if panel already open
            //as blocks cannot be clicked obviously if a panel is open
            return false;
        }
        for (String configLocation : Objects.requireNonNull(plugin.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
            String[] loc = configLocation.split("_");
            Location tempLocation = new Location(plugin.getServer().getWorld(loc[0].replaceAll("%dash%","_")),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
            if(tempLocation.equals(location)){
                if(plugin.blockConfig.contains("blocks." + configLocation + ".commands")){
                    if(!isVoid) {
                        for (String command : plugin.blockConfig.getStringList("blocks." + configLocation + ".commands")) {
                            plugin.commandTags.runCommand(null, PanelPosition.Top, p, command);
                        }
                    }
                    return true;
                }
                //uses the open= tag because it will open a panel with panel names, but also works with open= features like placeholders
                if(!isVoid) {
                    String command = "open= " + plugin.blockConfig.getString("blocks." + configLocation + ".panel");
                    plugin.commandTags.runCommand(null, PanelPosition.Top, p, command);
                }
                return true;
            }
        }
        return false;
    }
}