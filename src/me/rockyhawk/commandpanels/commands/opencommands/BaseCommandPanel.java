package me.rockyhawk.commandpanels.commands.opencommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseCommandPanel extends Command {

    private final Context ctx;
    private final Panel panel;
    private final List<String> subcommandPatterns;

    public BaseCommandPanel(Context ctx, Panel panel, String name, List<String> subcommandPatterns) {
        super(name);
        this.ctx = ctx;
        this.panel = panel;
        this.subcommandPatterns = subcommandPatterns;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;

        // If no args and empty pattern allowed
        if (args.length == 0 && subcommandPatterns.contains("")) {
            panel.copy().open(player, PanelPosition.Top);
            return true;
        }

        String[] phEnds = ctx.placeholders.getPlaceholderEnds(panel, true);

        // Try to match each subcommand pattern
        for (String pattern : subcommandPatterns) {
            if (pattern.isEmpty() && args.length == 0) {
                panel.copy().open(player, PanelPosition.Top);
                return true;
            }

            String[] patternParts = pattern.split(" ");
            if (patternParts.length != args.length) {
                continue; // length mismatch
            }

            Panel openPanel = panel.copy();
            boolean matched = true;

            for (int i = 0; i < patternParts.length; i++) {
                String expected = patternParts[i];
                String actual = args[i];

                if (expected.startsWith(phEnds[0]) && expected.endsWith(phEnds[1])) {
                    // Placeholder found: extract key without phEnds
                    String key = expected.substring(phEnds[0].length(), expected.length() - phEnds[1].length());
                    openPanel.placeholders.addPlaceholder(key, actual);
                } else if (!expected.equalsIgnoreCase(actual)) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                openPanel.open(player, PanelPosition.Top);
                return true;
            }
        }

        // No pattern matched
        return false;
    }
}
