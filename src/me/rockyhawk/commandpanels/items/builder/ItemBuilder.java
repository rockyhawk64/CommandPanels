package me.rockyhawk.commandpanels.items.builder;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.itemcomponents.*;
import me.rockyhawk.commandpanels.items.builder.materialcomponents.*;
import me.rockyhawk.commandpanels.items.name.NameHandler;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemBuilder {
    private final Context ctx;
    private final List<MaterialComponent> materialComponents = new ArrayList<>();
    private final List<ItemComponent> itemComponents = new ArrayList<>();
    private NameHandler name;

    public ItemBuilder(Context ctx) {
        this.ctx = ctx;
        name = new NameHandler(ctx);
        initialiseComponents();
    }

    public ItemStack buildItem(Panel panel, PanelPosition pos, ConfigurationSection section, Player player, boolean addNBT) {
        String materialTag = ctx.text.placeholdersNoColour(panel, pos, player, section.getString("material"));
        if (materialTag == null) return null;

        ItemStack baseItem = null;

        for (MaterialComponent mc : materialComponents) {
            if (mc.matches(materialTag)) {
                try {
                    baseItem = mc.createItem(materialTag, player, ctx, section, panel, pos);
                    break;
                }catch (Exception e){
                    player.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.error") + mc));
                }
            }
        }

        if (baseItem == null) {
            Material mat = Material.matchMaterial(materialTag.toUpperCase());
            if (mat == null) return null;
            baseItem = new ItemStack(mat);
        }

        for (ItemComponent ic : itemComponents) {
            try {
                baseItem = ic.apply(baseItem, section, ctx, player, panel, pos, addNBT);
            }catch (Exception e){
                player.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.error") + ic));
            }
        }

        baseItem = name.setName(panel,baseItem, section, player);

        return baseItem;
    }

    private void initialiseComponents(){
        // Add Material Components
        this.materialComponents.add(new CPSComponent());
        this.materialComponents.add(new NexoComponent());
        this.materialComponents.add(new OraxenComponent());
        this.materialComponents.add(new ItemsAdderComponent());
        this.materialComponents.add(new MMOItemsComponent());
        this.materialComponents.add(new HeadDatabaseComponent());
        this.materialComponents.add(new CPIComponent());
        this.materialComponents.add(new BookComponent());

        // Add Item Components
        this.itemComponents.add(new NBTComponent());
        this.itemComponents.add(new EnchantedComponent());
        this.itemComponents.add(new ItemModelComponent());
        this.itemComponents.add(new TooltipComponent());
        this.itemComponents.add(new CustomDataComponent());
        this.itemComponents.add(new BannerComponent());
        this.itemComponents.add(new LeatherArmorComponent());
        this.itemComponents.add(new PotionComponent());
        this.itemComponents.add(new PotionColorComponent());
        this.itemComponents.add(new DamageComponent());
        this.itemComponents.add(new TrimComponent());
        this.itemComponents.add(new StackComponent());
        this.itemComponents.add(new RefreshCommandsComponent());
    }
}