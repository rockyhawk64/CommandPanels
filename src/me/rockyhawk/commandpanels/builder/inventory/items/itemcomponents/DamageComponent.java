package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class DamageComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.damage().equals("0")) return itemStack;

        //change the damage amount (placeholders accepted)
        //if the damage is not unbreakable and should be a value
        if(item.damage().equals("-1")){
            //if the player wants the item to be unbreakable. Only works in non legacy versions
            ItemMeta unbreak = itemStack.getItemMeta();
            unbreak.setUnbreakable(true);
            itemStack.setItemMeta(unbreak);
        }else {
            try {
                Damageable itemDamage = (Damageable) itemStack.getItemMeta();
                itemDamage.setDamage(Integer.parseInt(ctx.text.parseTextToString(player, item.damage())));
                itemStack.setItemMeta(itemDamage);
            } catch (Exception e) {
                ctx.text.sendError(player, "Error with Item Damage for: " + item.id());
            }
        }

        return itemStack;
    }
}