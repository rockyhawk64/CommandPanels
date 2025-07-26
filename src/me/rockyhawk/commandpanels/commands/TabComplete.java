package me.rockyhawk.commandpanels.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabComplete implements TabCompleter {
    private final Context ctx;

    public TabComplete(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("commandpanels.command")) return null;

        ArrayList<String> output = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("commandpanels.command.open")) {
                output.add("open");
            }
            if (sender.hasPermission("commandpanels.command.reload")) {
                output.add("reload");
            }
            if (sender.hasPermission("commandpanels.command.generate")) {
                output.add("generate");
            }
            if (sender.hasPermission("commandpanels.command.help")) {
                output.add("help");
            }
            if (sender.hasPermission("commandpanels.command.version")) {
                output.add("version");
            }
            if (sender.hasPermission("commandpanels.command.data")) {
                output.add("data");
            }
            // REMOVE AFTER CONVERTER IS REMOVED
            if (sender.hasPermission("commandpanels.command.convert")) {
                output.add("convert");
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("open") && sender.hasPermission("commandpanels.command.open")) {
                if (sender instanceof Player player) {
                    for (String panelName : ctx.plugin.panels.keySet()) {
                        Panel panel = ctx.plugin.panels.get(panelName);
                        if (panel.canOpen(player, ctx)) {
                            output.add(panelName);
                        }
                    }
                } else {
                    output.addAll(ctx.plugin.panels.keySet());
                }
            } else if (args[0].equalsIgnoreCase("data") && sender.hasPermission("commandpanels.command.data")) {
                output.addAll(Arrays.asList("get", "set", "overwrite", "math", "del", "clear"));
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("open") && sender.hasPermission("commandpanels.command.open.other")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    output.add(player.getName());
                }
            } else if (args[0].equalsIgnoreCase("data") && sender.hasPermission("commandpanels.command.data")) {
                output.addAll(ctx.dataLoader.dataPlayers.getPlayerNames());
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("data") && sender.hasPermission("commandpanels.command.data")) {
                String action = args[1].toLowerCase();
                if (Arrays.asList("get", "set", "overwrite", "math", "del").contains(action)) {
                    // Placeholder keys (you could fetch actual keys per player if needed)
                    output.add("<key>");
                }
            }
        }

        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("data") && sender.hasPermission("commandpanels.command.data")) {
                String action = args[1].toLowerCase();
                if (Arrays.asList("set", "overwrite", "math").contains(action)) {
                    output.add("<value_or_expression>");
                }
            }
        }

        return output;
    }
}