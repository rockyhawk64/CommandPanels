package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BannerPatternLayers;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BannerComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.banner().isEmpty()) return itemStack;

        List<Pattern> patterns = new ArrayList<>();
        for (String temp : item.banner()) {
            temp = ctx.text.parseTextToString(player,temp);
            String[] dyePattern = temp.split(",");

            patterns.add(
                    new Pattern(DyeColor.valueOf(
                            dyePattern[0].toUpperCase()),
                            Registry.BANNER_PATTERN.get(NamespacedKey.fromString("minecraft:" + dyePattern[1].toLowerCase()))
                    )
            ); //load patterns in config: RED,STRIPE_TOP
        }

        itemStack.setData(
                DataComponentTypes.BANNER_PATTERNS,
                BannerPatternLayers.bannerPatternLayers().addAll(patterns)
        );
        
        return itemStack;
    }
}