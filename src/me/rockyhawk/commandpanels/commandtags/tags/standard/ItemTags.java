package me.rockyhawk.commandpanels.commandtags.tags.standard;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import org.bukkit.configuration.ConfigurationSection;
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
            ItemStack itm = plugin.itemCreate.makeCustomItemFromConfig(null,plugin.openPanels.getOpenPanel(e.p.getName()).getConfig().getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, false);
            e.p.getInventory().addItem(itm);
            return;
        }
        if(e.name.equalsIgnoreCase("setitem=")){
            e.commandTagUsed();
            //if player uses setitem= [custom item] [slot] it will change the item slot to something, used for placeable items
            //make a section in the panel called "custom-item" then whatever the title of the item is, put that here
            ConfigurationSection panelCF = plugin.openPanels.getOpenPanel(e.p.getName()).getConfig();
            ItemStack s = plugin.itemCreate.makeItemFromConfig(null,panelCF.getConfigurationSection("custom-item." + e.args[0]), e.p, true, true, true);
            e.p.getOpenInventory().getTopInventory().setItem(Integer.parseInt(e.args[1]), s);
        }
    }
}
