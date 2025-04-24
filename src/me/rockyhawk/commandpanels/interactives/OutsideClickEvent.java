package me.rockyhawk.commandpanels.interactives;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class OutsideClickEvent implements Listener {
    Context ctx;
    public OutsideClickEvent(Context pl) {
        this.ctx = pl;
    }
    @EventHandler
    public void onOutsideClick(InventoryClickEvent e){
        Player p = (Player)e.getWhoClicked();

        //cancel while a panel is open
        if(ctx.openPanels.hasPanelOpen(p.getName(), PanelPosition.Top) || e.getClick() == ClickType.DOUBLE_CLICK){
            return;
        }

        //if there is no panel open, use outside-commands from config.yml
        if(e.getSlotType() == InventoryType.SlotType.OUTSIDE){
            //if the panel is clicked on the outside area of the GUI
            if (ctx.configHandler.config.contains("outside-commands")) {
                try {
                    ctx.commandRunner.runCommands(null,PanelPosition.Top,p, ctx.configHandler.config.getStringList("outside-commands"),e.getClick());
                }catch(Exception s){
                    ctx.debug.send(s,p, ctx);
                }
            }
        }
    }
}
