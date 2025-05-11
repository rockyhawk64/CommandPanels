package me.rockyhawk.commandpanels.interaction.input;

import com.loohp.platformscheduler.Scheduler;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PlayerInputUtils implements Listener {
    Context ctx;
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
    public HashMap<Player, PlayerInput> playerInput = new HashMap<>();

    public PlayerInputUtils(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent e) {
        Player player = e.getPlayer();
        if (playerInput.containsKey(player)) {
            e.setCancelled(true);
            String message = plainSerializer.serialize(e.message());

            // Handle cancel keyword
            String cancelKeyword = ctx.configHandler.config.getString("input.input-cancel");
            if (message.equalsIgnoreCase(cancelKeyword)) {
                PlayerInput input = playerInput.remove(player);
                if (input.cancelCommands != null) {
                    Scheduler.runTaskLater(ctx.plugin, () -> {
                        ctx.commands.runCommands(input.panel, PanelPosition.Top, player, input.cancelCommands, input.click);
                    }, 1);
                }
                return;
            }

            // Add the full input
            PlayerInput input = playerInput.get(player);
            input.panel.placeholders.addPlaceholder("player-input", message);

            // Check per-panel max length
            String panelMax = input.panel.getConfig().getString("max-input-length");
            if (panelMax != null) {
                int max = Integer.parseInt(panelMax);
                if (max != -1 && message.length() > max) {
                    player.sendMessage(ctx.text.colour(ctx.tag + input.panel.getConfig().getString("custom-messages.input")));
                    return;
                }
            }
            // Check global max length
            int globalMax = Integer.parseInt(ctx.configHandler.config.getString("input.max-input-length"));
            if (globalMax != -1 && message.length() > globalMax) {
                player.sendMessage(ctx.text.colour(ctx.tag + ctx.configHandler.config.getString("config.format.input")));
                return;
            }

            // Split into words
            String[] parts = message.split("\\s");
            for (int i = 0; i < parts.length; i++) {
                input.panel.placeholders.addPlaceholder("player-input-" + (i + 1), parts[i]);
            }

            // Execute commands
            PlayerInput taskInput = playerInput.remove(player);
            Scheduler.runTaskLater(ctx.plugin, () ->
                    ctx.commands.runCommands(taskInput.panel, PanelPosition.Top, player, taskInput.commands, taskInput.click), 1);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerInput.remove(e.getPlayer());
    }

    public void sendInputMessage(Panel panel, PanelPosition pos, Player p) {
        List<String> inputMessages;
        if (panel.getConfig().contains("custom-messages.input-message")) {
            inputMessages = new ArrayList<>(panel.getConfig().getStringList("custom-messages.input-message"));
        } else {
            inputMessages = new ArrayList<>(ctx.configHandler.config.getStringList("input.input-message"));
        }
        String cancelKeyword = ctx.configHandler.config.getString("input.input-cancel");
        for (String line : inputMessages) {
            String msg = line.replaceAll("%cp-args%", Objects.requireNonNull(cancelKeyword));
            p.sendMessage(ctx.text.placeholders(panel, pos, p, msg));
        }
    }
}
