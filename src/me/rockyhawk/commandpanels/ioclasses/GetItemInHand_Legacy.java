package me.rockyhawk.commandpanels.ioclasses;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemInHand_Legacy {
    CommandPanels plugin;
    public GetItemInHand_Legacy(CommandPanels pl) {
        this.plugin = pl;
    }

    @SuppressWarnings("deprecation")
    public ItemStack itemInHand(Player p){
        return p.getInventory().getItemInHand();
    }
}
