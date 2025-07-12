package me.rockyhawk.commandpanels.session.dialog.components;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.dialog.DialogPanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.items.ItemBuilder;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import me.rockyhawk.commandpanels.session.inventory.PanelItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DialogItem extends DialogComponent {
    private final PanelItem item;
    private final String text;

    public DialogItem(String id, ConfigurationSection section) {
        super(id, section);
        this.item = PanelItem.fromSection(id, section);
        this.text = section.getString("text", "");
    }

    public ItemStack getItemStack(Context ctx, Panel panel, Player player) {
        DialogPanelBuilder panelBuilder = new DialogPanelBuilder(ctx, player);
        ItemBuilder itemBuilder = new ItemBuilder(ctx, panelBuilder);
        return itemBuilder.buildItem(panel, item);
    }

    public String getText() {
        return text;
    }
}