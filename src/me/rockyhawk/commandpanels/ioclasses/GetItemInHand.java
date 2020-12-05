package me.rockyhawk.commandpanels.ioclasses;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemInHand {
    CommandPanels plugin;
    public GetItemInHand(CommandPanels pl) {
        this.plugin = pl;
    }

    public ItemStack itemInHand(Player p){
        return p.getInventory().getItemInMainHand();
    }
}
