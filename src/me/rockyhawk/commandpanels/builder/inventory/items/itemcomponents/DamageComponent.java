package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DamageComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.damage().equals("0")) return itemStack;

        //change the damage amount (placeholders accepted)
        //if the damage is not unbreakable and should be a value
        if(item.damage().equals("-1")){
            //if the player wants the item to be unbreakable
            itemStack.setData(
                    DataComponentTypes.UNBREAKABLE
            );
        }else {
            itemStack.setData(
                    DataComponentTypes.DAMAGE,
                    Integer.parseInt(ctx.text.parseTextToString(player, item.damage()))
            );
        }

        return itemStack;
    }
}