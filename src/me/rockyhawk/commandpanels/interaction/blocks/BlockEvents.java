package me.rockyhawk.commandpanels.interaction.blocks;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class BlockEvents implements Listener {
    Context ctx;
    public BlockEvents(Context pl) {
        this.ctx = pl;
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
        if(!ctx.configHandler.isTrue("config.panel-blocks")){
            return false;
        }
        if(!ctx.configHandler.blockConfig.contains("blocks")){
            return false;
        }
        if(ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top)) {
            //some blocks run this event twice, skip if panel already open
            //as blocks cannot be clicked obviously if a panel is open
            return false;
        }
        for (String configLocation : Objects.requireNonNull(ctx.configHandler.blockConfig.getConfigurationSection("blocks")).getKeys(false)) {
            String[] loc = configLocation.split("_");
            Location tempLocation = new Location(ctx.plugin.getServer().getWorld(loc[0].replaceAll("%dash%","_")),Double.parseDouble(loc[1]),Double.parseDouble(loc[2]),Double.parseDouble(loc[3]));
            if(tempLocation.equals(location)){
                if(ctx.configHandler.blockConfig.contains("blocks." + configLocation + ".commands")){
                    if(!isVoid) {
                        for (String command : ctx.configHandler.blockConfig.getStringList("blocks." + configLocation + ".commands")) {
                            ctx.commands.runCommand(null, PanelPosition.Top, p, command);
                        }
                    }
                    return true;
                }
                //uses the open= tag because it will open a panel with panel names, but also works with open= features like placeholders
                if(!isVoid) {
                    String command = "open= " + ctx.configHandler.blockConfig.getString("blocks." + configLocation + ".panel");
                    ctx.commands.runCommand(null, PanelPosition.Top, p, command);
                }
                return true;
            }
        }
        return false;
    }
}