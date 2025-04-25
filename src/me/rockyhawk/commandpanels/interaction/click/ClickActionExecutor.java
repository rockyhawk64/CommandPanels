package me.rockyhawk.commandpanels.interaction.click;

import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.input.PlayerInput;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class ClickActionExecutor {
    private final InteractionHandler handler;

    protected ClickActionExecutor(InteractionHandler handler) {
        this.handler = handler;
    }

    protected void execute(Panel panel, Player p, InventoryClickEvent e, String foundSlot, PanelPosition position) {
        String section = handler.ctx.has.hasSection(panel, position, panel.getConfig().getConfigurationSection("item." + foundSlot), p);

        if (panel.getConfig().contains("item." + foundSlot + section + ".itemType") &&
                panel.getConfig().getStringList("item." + foundSlot + section + ".itemType").contains("placeable")) {
            e.setCancelled(false);
            return;
        }

        e.setCancelled(true);
        p.updateInventory();

        if (panel.getConfig().contains("item." + foundSlot + section + ".player-input")) {
            List<String> playerInputs = panel.getConfig().getStringList("item." + foundSlot + section + ".player-input");
            List<String> filtered = new ArrayList<>();
            ClickType click = e.getClick();
            boolean validClick = false;

            for (String input : playerInputs) {
                String validInput = handler.ctx.commands.hasCorrectClick(input, click);
                if (!validInput.isEmpty()) {
                    filtered.add(validInput);
                    validClick = true;
                }
            }

            if (validClick) {
                List<String> cancelCommands = panel.getConfig().contains("item." + foundSlot + section + ".player-input-cancel")
                        ? panel.getConfig().getStringList("item." + foundSlot + section + ".player-input-cancel")
                        : null;
                handler.ctx.inputUtils.playerInput.put(p, new PlayerInput(panel, filtered, cancelCommands, click));
                handler.ctx.inputUtils.sendInputMessage(panel, position, p);
            }
        }

        if (panel.getConfig().contains("item." + foundSlot + section + ".commands")) {
            List<String> commands = new ArrayList<>(panel.getConfig().getStringList("item." + foundSlot + section + ".commands"));
            for (int i = 0; i < commands.size(); i++) {
                try {
                    commands.set(i, commands.get(i).replaceAll("%cp-clicked%", e.getCurrentItem().getType().toString()));
                } catch (Exception ex) {
                    commands.set(i, commands.get(i).replaceAll("%cp-clicked%", "AIR"));
                }
            }

            if (panel.getConfig().contains("item." + foundSlot + section + ".multi-paywall")) {
                handler.ctx.commands.runMultiPaywall(panel, position, p,
                        panel.getConfig().getStringList("item." + foundSlot + section + ".multi-paywall"),
                        commands, e.getClick());
            } else {
                handler.ctx.commands.runCommands(panel, position, p, commands, e.getClick());
            }
        }
    }
}