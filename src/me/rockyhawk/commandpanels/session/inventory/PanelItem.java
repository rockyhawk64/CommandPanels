package me.rockyhawk.commandpanels.session.inventory;

import me.rockyhawk.commandpanels.session.CommandActions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        CommandActions mouseActions,
        CommandActions keyActions,
        CommandActions leftClick,
        CommandActions rightClick,
        CommandActions shiftLeftClick,
        CommandActions shiftRightClick,
        CommandActions drop,
        CommandActions ctrlDrop,
        CommandActions swapOffhand,
        Map<Integer, CommandActions> numberKeys,
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
            CommandActions mouseActions,
            CommandActions keyActions,
            CommandActions leftClick,
            CommandActions rightClick,
            CommandActions shiftLeftClick,
            CommandActions shiftRightClick,
            CommandActions drop,
            CommandActions ctrlDrop,
            CommandActions swapOffhand,
            Map<Integer, CommandActions> numberKeys,
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
        this.mouseActions = mouseActions;
        this.keyActions = keyActions;
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.shiftLeftClick = shiftLeftClick;
        this.shiftRightClick = shiftRightClick;
        this.drop = drop;
        this.ctrlDrop = ctrlDrop;
        this.swapOffhand = swapOffhand;
        this.numberKeys = numberKeys;
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
        CommandActions mouseActions = CommandActions.fromSection(section.getConfigurationSection("mouse-actions"));
        CommandActions keyActions = CommandActions.fromSection(section.getConfigurationSection("key-actions"));
        CommandActions leftClick = CommandActions.fromSection(section.getConfigurationSection("left-click"));
        CommandActions rightClick = CommandActions.fromSection(section.getConfigurationSection("right-click"));
        CommandActions shiftLeftClick = CommandActions.fromSection(section.getConfigurationSection("shift-left-click"));
        CommandActions shiftRightClick = CommandActions.fromSection(section.getConfigurationSection("shift-right-click"));
        CommandActions drop = CommandActions.fromSection(section.getConfigurationSection("drop"));
        CommandActions ctrlDrop = CommandActions.fromSection(section.getConfigurationSection("ctrl-drop"));
        CommandActions swapOffhand = CommandActions.fromSection(section.getConfigurationSection("swap-offhand"));

        Map<Integer, CommandActions> numberKeys = new HashMap<>();
        for (int i = 1; i <= 9; i++) {
            ConfigurationSection keySection = section.getConfigurationSection("key-" + i);
            if (keySection != null) {
                numberKeys.put(i, CommandActions.fromSection(keySection));
            }
        }

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
                mouseActions,
                keyActions,
                leftClick,
                rightClick,
                shiftLeftClick,
                shiftRightClick,
                drop,
                ctrlDrop,
                swapOffhand,
                numberKeys,
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
        if (!actions.requirements().isEmpty() || !actions.commands().isEmpty()) return actions;

        boolean isMouseClick =
                clickType == ClickType.LEFT ||
                clickType == ClickType.RIGHT ||
                clickType == ClickType.SHIFT_LEFT ||
                clickType == ClickType.SHIFT_RIGHT;
        if (isMouseClick && (!mouseActions.requirements().isEmpty() || !mouseActions.commands().isEmpty())) return mouseActions;

        // Check keyboard actions
        // Excluding number keys, which are handled separately
        boolean isKeyClick =
                clickType == ClickType.DROP ||
                clickType == ClickType.CONTROL_DROP ||
                clickType == ClickType.SWAP_OFFHAND;
        if (isKeyClick && (!keyActions.requirements().isEmpty() || !keyActions.commands().isEmpty())) return keyActions;

        // LEFT case defaults
        return switch (clickType) {
            case RIGHT -> rightClick;
            case SHIFT_LEFT -> shiftLeftClick;
            case SHIFT_RIGHT -> shiftRightClick;
            case DROP -> drop;
            case CONTROL_DROP -> ctrlDrop;
            case SWAP_OFFHAND -> swapOffhand;
            default -> leftClick;
        };
    }

    public CommandActions getNumberKeyAction(int number) {
        if (!actions.requirements().isEmpty() || !actions.commands().isEmpty()) return actions;
        if (!keyActions.requirements().isEmpty() || !keyActions.commands().isEmpty()) return keyActions;
        return numberKeys.getOrDefault(number, new CommandActions(List.of(), List.of(), List.of()));
    }
}