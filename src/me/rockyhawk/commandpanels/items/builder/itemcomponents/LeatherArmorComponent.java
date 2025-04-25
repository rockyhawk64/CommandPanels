package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;
import java.util.Map;

public class LeatherArmorComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("leatherarmor")) return item;

        if (item.getType() == Material.LEATHER_BOOTS ||
                item.getType() == Material.LEATHER_LEGGINGS ||
                item.getType() == Material.LEATHER_CHESTPLATE ||
                item.getType() == Material.LEATHER_HELMET ||
                item.getType() == Material.matchMaterial("LEATHER_HORSE_ARMOR")) { //avoid exceptions on older versions which don't have leather armour
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
            String colourCode = ctx.text.placeholdersNoColour(panel,pos,player,section.getString("leatherarmor"));
            assert colourCode != null;
            if (!colourCode.contains(",")) {
                //use a color name
                assert leatherMeta != null;
                leatherMeta.setColor(colourCodes.get(colourCode.toUpperCase()));
            } else {
                //use RGB sequence
                int[] colorRGB = {255, 255, 255};
                int count = 0;
                for (String colourNum : colourCode.split(",")) {
                    colorRGB[count] = Integer.parseInt(colourNum);
                    count += 1;
                }
                assert leatherMeta != null;
                leatherMeta.setColor(Color.fromRGB(colorRGB[0], colorRGB[1], colorRGB[2]));
            }
            item.setItemMeta(leatherMeta);
        }

        return item;
    }

    private final Map<String, Color> colourCodes = new HashMap<String, Color>() {{
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