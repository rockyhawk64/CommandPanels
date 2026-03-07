package me.rockyhawk.commandpanels.interaction.openpanel;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PanelOpenCommand implements Listener {
    Context ctx;
    CommandRegister commandRegister;

    // List of all the base custom commands and the panel they open
    private final HashMap<String, List<Panel>> commands = new HashMap<>();

    public PanelOpenCommand(Context pl) {
        this.ctx = pl;
        this.commandRegister = new CommandRegister(ctx);
    }

    // For custom commands that are used to open panels
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String raw = e.getMessage().substring(1); // remove leading slash
        String[] parts = splitCommand(raw);
        if (parts.length == 0) return;

        String label = parts[0].toLowerCase(Locale.ROOT); // Just the command (e.g. "shop")
        String[] args = Arrays.copyOfRange(parts, 1, parts.length); // just the arguments

        // Match base command to a panels list
        List<Panel> panels = commands.get(label);
        if (panels == null || panels.isEmpty()) return;
        boolean hadMatch = false;

        // Iterate over panels that share the same base command until one can be opened
        for(Panel panel : panels) {
            if (panel == null) continue;

            CommandMatch match = resolveMatch(panel, e.getPlayer(), label, args);
            if (match == null) continue;

            // Get panel command args
            String[] pnlParts = splitCommand(panel.getCommand());
            String[] pnlCmdArgs = Arrays.copyOfRange(pnlParts, 1, pnlParts.length); // arguments for the panel command
            hadMatch = true;

            if (panel.requiresOnlinePlayer()) {
                if (match.args().length < 1 || Bukkit.getPlayerExact(match.args()[0]) == null) {
                    ctx.text.sendError(e.getPlayer(), Message.PANEL_OPEN_PLAYER_OFFLINE);
                    e.setCancelled(true);
                    return;
                }
            }

            // If there are any args add the data
            // Check and add before checking conditions
            for (int i = 0; i < match.args().length; i++) {
                String key = pnlCmdArgs[i];
                String value = match.args()[i];
                e.getPlayer().getPersistentDataContainer()
                        .set(new NamespacedKey(ctx.plugin, key),
                                PersistentDataType.STRING, value);
            }

            if (!panel.passesConditions(e.getPlayer(), ctx)) {
                continue;
            }

            e.setCancelled(true);
            ctx.panelOpenService.scheduleOpenPanel(panel, e.getPlayer(), true, false);
            return;
        }

        // Had panel match but no panel had permission, cancel and send no perm message
        if(hadMatch){
            ctx.text.sendError(e.getPlayer(), Message.COMMAND_NO_PERMISSION);
            e.setCancelled(true);
        }
    }

    private CommandMatch resolveMatch(Panel panel, org.bukkit.entity.Player player, String label, String[] args) {
        String effectiveRaw = buildEffectiveRawCommand(panel, player, label, args);
        if (effectiveRaw == null) return null;

        String[] effectiveParts = splitCommand(effectiveRaw);
        if (effectiveParts.length == 0) return null;

        String effectiveLabel = effectiveParts[0].toLowerCase(Locale.ROOT);
        if (!getCommandBases(panel).contains(effectiveLabel)) return null;

        String[] panelParts = splitCommand(panel.getCommand());
        if (panelParts.length == 0) return null;

        String[] effectiveArgs = Arrays.copyOfRange(effectiveParts, 1, effectiveParts.length);
        String[] panelArgs = Arrays.copyOfRange(panelParts, 1, panelParts.length);
        if (effectiveArgs.length != panelArgs.length) return null;

        return new CommandMatch(effectiveArgs);
    }

    private String buildEffectiveRawCommand(Panel panel, org.bukkit.entity.Player player, String label, String[] args) {
        if (args.length != 0) {
            return joinCommand(label, args);
        }

        String noArgDefault = panel.getNoArgDefault().trim();
        if (noArgDefault.isEmpty()) {
            return joinCommand(label, args);
        }

        return ctx.text.applyPlaceholders(player, noArgDefault).trim();
    }

    private Set<String> getCommandBases(Panel panel) {
        Set<String> bases = new HashSet<>();

        String[] mainParts = splitCommand(panel.getCommand());
        if (mainParts.length > 0) {
            bases.add(mainParts[0].toLowerCase(Locale.ROOT));
        }

        for (String alias : panel.getAliases()) {
            String[] aliasParts = splitCommand(alias);
            if (aliasParts.length > 0) {
                bases.add(aliasParts[0].toLowerCase(Locale.ROOT));
            }
        }

        return bases;
    }

    private String joinCommand(String label, String[] args) {
        if (args.length == 0) return label;
        return label + " " + String.join(" ", args);
    }

    private String[] splitCommand(String command) {
        String trimmed = command == null ? "" : command.trim();
        if (trimmed.isEmpty()) return new String[0];
        return trimmed.split("\\s+");
    }

    private record CommandMatch(String[] args) {}

    public void populateCommands() {
        // Populate the base commands list with new commands e.g. punish player -> punish
        commands.clear();
        for (Panel panel : ctx.plugin.panels.values()){
            // Add the panel command
            String[] commandParts = splitCommand(panel.getCommand());
            if(commandParts.length == 0) continue;
            String command = commandParts[0].toLowerCase(Locale.ROOT);
            if(command.isEmpty()) continue;
            commands.computeIfAbsent(command, k -> new ArrayList<>()).add(panel);

            // Do not register if registration is disabled in config
            if(ctx.fileHandler.config.getBoolean("custom-commands"))
                commandRegister.registerPanelCommand(command);

            // Add aliases, the aliases use the same args as main command (strip any extra words)
            if(!panel.getAliases().isEmpty()){
                for(String alias : panel.getAliases()){
                    String[] aliasParts = splitCommand(alias);
                    if (aliasParts.length == 0) continue;
                    alias = aliasParts[0].toLowerCase(Locale.ROOT);
                    commands.computeIfAbsent(alias, k -> new ArrayList<>()).add(panel);
                    // Do not register if registration is disabled in config
                    if(ctx.fileHandler.config.getBoolean("custom-commands"))
                        commandRegister.registerPanelCommand(alias);
                }
            }
        }
    }

}
