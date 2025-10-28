package me.rockyhawk.commandpanels.session.inventory;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.inventory.InventoryPanelBuilder;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.CommandActions;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.floodgate.FloodgatePanel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryPanel extends Panel implements InventoryHolder {
    private final Map<String, PanelItem> items = new HashMap<>();
    private final Map<String, List<String>> slots = new HashMap<>();
    private final CommandActions outside;
    private final CommandActions close;
    private final String rows;
    private final String floodgate;
    private final String updateDelay;
    private final String inventoryLock;
    private final String itemCooldown;

    public InventoryPanel(String name, YamlConfiguration config) {
        super(name, config);

        this.rows = config.getString("rows", "1");
        this.floodgate = config.getString("floodgate", "");
        this.updateDelay = config.getString("update-delay", "20");
        this.inventoryLock = config.getString("inventory-lock", "false");
        this.itemCooldown = config.getString("item-cooldown", "0");

        outside = new CommandActions(
                config.getStringList("outside.requirements"),
                config.getStringList("outside.commands"),
                config.getStringList("outside.fail")
        );
        close = new CommandActions(
                config.getStringList("close.requirements"),
                config.getStringList("close.commands"),
                config.getStringList("close.fail")
        );

        ConfigurationSection slotSection = config.getConfigurationSection("layout");
        if (slotSection != null) {
            for (String key : slotSection.getKeys(false)) {
                slots.put(key, slotSection.getStringList(key));
            }
        }

        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    // Store item id as string key in hashmap AND store id in PanelItem
                    PanelItem item = PanelItem.fromSection(key, itemSection);
                    items.put(itemSection.getName(), item);
                }
            }
        }
    }

    @Override
    public void open(Context ctx, Player player, boolean isNewPanelSession) {
        // Check for floodgate panel if bedrock player
        Panel panel = ctx.plugin.panels.get(floodgate);
        if (Bukkit.getPluginManager().getPlugin("floodgate") != null) {
            if (panel instanceof FloodgatePanel floodgatePanel &&
                    FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                floodgatePanel.open(ctx, player, true);
                return;
            }
        }

        if (isNewPanelSession) {
            // Don't open same panel if its already open
            if (!canOpen(player, ctx)) {
                return;
            }
            updatePanelData(ctx, player);

            // Run panel commands
            RequirementRunner requirements = new RequirementRunner(ctx);
            CommandRunner commands = new CommandRunner(ctx);
            CommandActions actions = this.getOpenCommands();
            if (!requirements.processRequirements(this, player, actions.requirements())) {
                commands.runCommands(this, player, actions.fail());
                return;
            }
            commands.runCommands(this, player, actions.commands());
        }

        // Build and open the panel
        PanelBuilder builder = new InventoryPanelBuilder(ctx, player);
        builder.open(this);

        if (isNewPanelSession) {
            // Start a panel updater
            InventoryPanelUpdater updater = new InventoryPanelUpdater();
            updater.start(ctx, player, this);
        }
    }

    public Map<String, PanelItem> getItems() {
        return items;
    }

    public Map<String, List<String>> getSlots() {
        return slots;
    }

    public CommandActions getOutsideCommands() {
        return outside;
    }

    public CommandActions getCloseCommands() {
        return close;
    }

    public String getRows() {
        return rows;
    }

    public String getUpdateDelay() {
        return updateDelay;
    }

    public String getInventoryLock() {
        return inventoryLock;
    }

    public String getItemCooldown() {
        return itemCooldown;
    }

    // For InventoryHolder implementation
    @Override
    public Inventory getInventory() {
        return null;
    }
}