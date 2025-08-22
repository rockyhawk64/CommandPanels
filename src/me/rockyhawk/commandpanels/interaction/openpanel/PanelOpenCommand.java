package me.rockyhawk.commandpanels.interaction.openpanel;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.*;

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
        String[] parts = raw.split("\\s+");

        String label = parts[0].toLowerCase(Locale.ROOT); // Just the command (e.g. "shop")
        String[] args = Arrays.copyOfRange(parts, 1, parts.length); // just the arguments

        // Match base command to a panels list
        List<Panel> panels = commands.get(label);
        if (panels == null || panels.isEmpty()) return;
        boolean hadMatch = false;

        // Iterate over panels that share the same base command until one can be opened
        for(Panel panel : panels) {
            if (panel == null) continue;

            // Get panel command args
            String[] pnlParts = panel.getCommand().split("\\s+");
            String[] pnlCmdArgs = Arrays.copyOfRange(pnlParts, 1, pnlParts.length); // arguments for the panel command
            if (args.length != pnlCmdArgs.length) continue;
            hadMatch = true;

            // If there are any args add the data
            // Check and add before checking conditions
            for (int i = 0; i < args.length; i++) {
                String key = pnlCmdArgs[i];
                String value = args[i];
                ctx.session.getPlayerSession(e.getPlayer()).setData(key, value);
            }

            // Stop and do not open panel if conditions are false
            if (!panel.canOpen(e.getPlayer(), ctx)) {
                continue;
            }

            e.setCancelled(true);
            Bukkit.getScheduler().runTask(ctx.plugin, () -> {
                panel.open(ctx, e.getPlayer(), SessionManager.PanelOpenType.EXTERNAL);
            });
            return;
        }

        // No panel was found that could be opened
        if(hadMatch) ctx.text.sendError(e.getPlayer(), "No permission.");
    }

    public void populateCommands() {
        // Populate the base commands list with new commands e.g. punish player -> punish
        commands.clear();
        for (Panel panel : ctx.plugin.panels.values()){
            // Add the panel command
            String command = panel.getCommand().split("\\s+")[0].toLowerCase(Locale.ROOT);
            if(command.isEmpty()) continue;
            commands.computeIfAbsent(command, k -> new ArrayList<>()).add(panel);

            // Do not register if registration is disabled in config
            if(ctx.fileHandler.config.getBoolean("custom-commands"))
                commandRegister.registerPanelCommand(command);

            // Add aliases, the aliases use the same args as main command (strip any extra words)
            if(!panel.getAliases().isEmpty()){
                for(String alias : panel.getAliases()){
                    alias = alias.split("\\s+")[0].toLowerCase(Locale.ROOT);
                    commands.computeIfAbsent(alias, k -> new ArrayList<>()).add(panel);
                    // Do not register if registration is disabled in config
                    if(ctx.fileHandler.config.getBoolean("custom-commands"))
                        commandRegister.registerPanelCommand(alias);
                }
            }
        }
    }

}