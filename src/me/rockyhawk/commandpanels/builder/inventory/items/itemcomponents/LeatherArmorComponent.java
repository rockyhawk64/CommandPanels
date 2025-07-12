package me.rockyhawk.commandpanels.builder.inventory.items.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;

public class LeatherArmorComponent implements ItemComponent {

    @Override
    public ItemStack apply(Context ctx, ItemStack itemStack, Player player, PanelItem item) {
        if(item.leatherArmor() == null) return itemStack;

        if (itemStack.getType() == Material.LEATHER_BOOTS ||
                itemStack.getType() == Material.LEATHER_LEGGINGS ||
                itemStack.getType() == Material.LEATHER_CHESTPLATE ||
                itemStack.getType() == Material.LEATHER_HELMET ||
                itemStack.getType() == Material.LEATHER_HORSE_ARMOR) {

            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            String colourCode = ctx.text.parseTextToString(player, item.leatherArmor());

            if (!colourCode.contains(",")) {
                //use a color name
                leatherMeta.setColor(colourCodes.get(colourCode.toUpperCase()));
            } else {
                //use RGB sequence
                int[] colorRGB = {255, 255, 255};
                int count = 0;
                for (String colourNum : colourCode.split(",")) {
                    colorRGB[count] = Integer.parseInt(colourNum);
                    count += 1;
                }
                leatherMeta.setColor(Color.fromRGB(colorRGB[0], colorRGB[1], colorRGB[2]));
            }
            itemStack.setItemMeta(leatherMeta);
        }

        return itemStack;
    }

    private final Map<String, Color> colourCodes = new HashMap<>() {{
        put("AQUA", Color.AQUA);
        put("BLUE", Color.BLUE);
        put("GRAY", Color.GRAY);
        put("GREEN", Color.GREEN);
        put("RED", Color.RED);
        put("WHITE", Color.WHITE);
        put("BLACK", Color.BLACK);
        put("FUCHSIA", Color.FUCHSIA);
        put("LIME", Color.LIME);
        put("MAROON", Color.MAROON);
        put("NAVY", Color.NAVY);
        put("OLIVE", Color.OLIVE);
        put("ORANGE", Color.ORANGE);
        put("PURPLE", Color.PURPLE);
        put("SILVER", Color.SILVER);
        put("TEAL", Color.TEAL);
        put("YELLOW", Color.YELLOW);
    }};
}