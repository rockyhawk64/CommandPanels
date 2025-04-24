package me.rockyhawk.commandpanels.openwithitem.iteminhand;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemInHand {
    public ItemStack itemInHand(Player p){
        return p.getInventory().getItemInMainHand();
    }
}
