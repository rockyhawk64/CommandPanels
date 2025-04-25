package me.rockyhawk.commandpanels.items.builder.itemcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.ItemComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.List;

public class BannerComponent implements ItemComponent {

    @Override
    public ItemStack apply(ItemStack item, ConfigurationSection section, Context ctx, Player player, Panel panel, PanelPosition pos, boolean addNBT) {
        if(!section.contains("banner")) return item;

        BannerMeta bannerMeta = (BannerMeta) item.getItemMeta();
        List<Pattern> patterns = new ArrayList<>(); //Load patterns in order top to bottom
        for (String temp : section.getStringList("banner")) {
            temp = ctx.text.placeholdersNoColour(panel,pos,player,temp);
            String[] dyePattern = temp.split(",");
            patterns.add(new Pattern(DyeColor.valueOf(dyePattern[0]), PatternType.valueOf(dyePattern[1]))); //load patterns in config: RED,STRIPE_TOP
        }
        bannerMeta.setPatterns(patterns);
        item.setItemMeta(bannerMeta);
        
        return item;
    }
}