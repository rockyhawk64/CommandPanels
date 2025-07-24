package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomModelDataComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.customModelData() == null) return itemStack;

        try{
            itemStack.setData(
                    DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(
                            Float.parseFloat(ctx.text.parseTextToString(player, item.customModelData()))
                    )
            );
        }catch (NumberFormatException e){
            itemStack.setData(
                    DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString(
                            ctx.text.parseTextToString(player, item.customModelData())
                    )
            );
        }

        return itemStack;
    }
}