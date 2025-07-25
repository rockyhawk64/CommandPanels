package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class PotionComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.potion() == null) return itemStack;

        //if the item is a potion, give it an effect
        String[] effectType = ctx.text.parseTextToString(player,item.potion()).split("\\s");
        PotionType newData = PotionType.valueOf(effectType[0].toUpperCase());

        //set meta
        itemStack.setData(DataComponentTypes.POTION_CONTENTS,
                PotionContents.potionContents().addCustomEffect(newData.getPotionEffects().get(0)));

        return itemStack;
    }
}