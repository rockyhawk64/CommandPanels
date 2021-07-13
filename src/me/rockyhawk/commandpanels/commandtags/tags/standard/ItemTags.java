package me.rockyhawk.commandpanels.commandtags.tags.standard;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ItemTags implements Listener {
    CommandPanels plugin;
    public ItemTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("give-item=")){
            e.commandTagUsed();
            ItemStack itm = plugin.itemCreate.makeCustomItemFromConfig(null,e.pos,e.panel.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, false);
            plugin.inventorySaver.addItem(e.p,itm);
            return;
        }
        if(e.name.equalsIgnoreCase("setitem=")){
            e.commandTagUsed();
            //if player uses setitem= [custom item] [slot] [position] it will change the item slot to something, used for placeable items
            //make a section in the panel called "custom-item" then whatever the title of the item is, put that here
            ItemStack s = plugin.itemCreate.makeItemFromConfig(null, e.pos,e.panel.getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, true);
            PanelPosition position = PanelPosition.valueOf(e.args[2]);
            if(position == PanelPosition.Top) {
                e.p.getOpenInventory().getTopInventory().setItem(Integer.parseInt(e.args[1]), s);
            }else if(position == PanelPosition.Middle) {
                e.p.getInventory().setItem(Integer.parseInt(e.args[1])+9, s);
            }else{
                e.p.getInventory().setItem(Integer.parseInt(e.args[1]), s);
            }
        }
    }
}
