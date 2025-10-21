package me.rockyhawk.commandpanels.session.inventory;

import me.rockyhawk.commandpanels.session.CommandActions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public record PanelItem(
        String id,
        String material,
        String stack,
        String displayName,
        List<String> lore,
        String attributes,
        String tooltip,
        String animate,
        String conditions,
        CommandActions actions,
        CommandActions leftClick,
        CommandActions rightClick,
        CommandActions shiftLeftClick,
        CommandActions shiftRightClick,
        String damage,
        String itemModel,
        String customModelData,
        String leatherColor,
        String armorTrim,
        String potionColor,
        String potion,
        String tooltipStyle,
        List<String> banner,
        List<String> enchantments
) {
    public PanelItem(
            String id,
            String material,
            String stack,
            String displayName,
            List<String> lore,
            String attributes,
            String tooltip,
            String animate,
            String conditions,
            CommandActions actions,
            CommandActions leftClick,
            CommandActions rightClick,
            CommandActions shiftLeftClick,
            CommandActions shiftRightClick,
            String damage,
            String itemModel,
            String customModelData,
            String leatherColor,
            String armorTrim,
            String potionColor,
            String potion,
            String tooltipStyle,
            List<String> banner,
            List<String> enchantments
    ) {
        this.id = id;
        this.material = material;
        this.stack = stack;
        this.displayName = displayName;
        this.lore = List.copyOf(lore);
        this.attributes = attributes;
        this.tooltip = tooltip;
        this.animate = animate;
        this.conditions = conditions;
        this.actions = actions;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftLeftClick = shiftLeftClick;
        this.shiftRightClick = shiftRightClick;
        this.damage = damage;
        this.itemModel = itemModel;
        this.customModelData = customModelData;
        this.leatherColor = leatherColor;
        this.armorTrim = armorTrim;
        this.potionColor = potionColor;
        this.potion = potion;
        this.tooltipStyle = tooltipStyle;
        this.banner = List.copyOf(banner);
        this.enchantments = List.copyOf(enchantments);
    }

    public static PanelItem fromSection(String id, ConfigurationSection section) {
        String material = section.getString("material", "STONE");
        String stack = section.getString("stack", "1");
        String name = section.getString("name", "");
        List<String> lore = section.getStringList("lore");
        String attributes = section.getString("attributes", "false");
        String tooltip = section.getString("tooltip", "true");
        String animate = section.getString("animate", "");
        String conditions = section.getString("conditions", "");

        CommandActions actions = CommandActions.fromSection(section.getConfigurationSection("actions"));
        CommandActions leftClick = CommandActions.fromSection(section.getConfigurationSection("left-click"));
        CommandActions rightClick = CommandActions.fromSection(section.getConfigurationSection("right-click"));
        CommandActions shiftLeftClick = CommandActions.fromSection(section.getConfigurationSection("shift-left-click"));
        CommandActions shiftRightClick = CommandActions.fromSection(section.getConfigurationSection("shift-right-click"));

        String damage = section.getString("damage", "0");
        String itemModel = section.getString("item-model", null);
        String customModelData = section.getString("custom-model-data", null);
        String leatherColor = section.getString("leather-color", null);
        String armorTrim = section.getString("armor-trim", null);
        String potionColor = section.getString("potion-color", null);
        String potion = section.getString("potion", null);
        String tooltipStyle = section.getString("tooltip-style", null);

        List<String> banner = section.getStringList("banner");
        List<String> enchanted = section.getStringList("enchantments");

        return new PanelItem(
                id,
                material,
                stack,
                name,
                lore,
                attributes,
                tooltip,
                animate,
                conditions,
                actions,
                leftClick,
                rightClick,
                shiftLeftClick,
                shiftRightClick,
                damage,
                itemModel,
                customModelData,
                leatherColor,
                armorTrim,
                potionColor,
                potion,
                tooltipStyle,
                banner,
                enchanted
        );
    }

    public CommandActions getClickActions(ClickType clickType) {
        if(!actions.requirements().isEmpty() || !actions.commands().isEmpty()) return actions;
        // LEFT case defaults
        return switch (clickType) {
            case RIGHT -> rightClick;
            case SHIFT_LEFT -> shiftLeftClick;
            case SHIFT_RIGHT -> shiftRightClick;
            default -> leftClick;
        };
    }
}