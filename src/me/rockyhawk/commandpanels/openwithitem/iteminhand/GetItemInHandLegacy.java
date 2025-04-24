package me.rockyhawk.commandpanels.openwithitem.iteminhand;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GetItemInHandLegacy {

    @SuppressWarnings("deprecation")
    public ItemStack itemInHand(Player p){
        return p.getInventory().getItemInHand();
    }
}
