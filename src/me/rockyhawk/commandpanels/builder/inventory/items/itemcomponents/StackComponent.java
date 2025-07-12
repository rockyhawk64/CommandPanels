package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StackComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.stack().equals("1")) return itemStack;

        //change the stack amount (placeholders accepted)
        int amount = (int)Double.parseDouble(ctx.text.parseTextToString(player,item.stack()));
        itemStack.setAmount(amount);

        return itemStack;
    }
}