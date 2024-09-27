package me.rockyhawk.commandpanels.commandtags;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.commandtags.paywalls.*;
import me.rockyhawk.commandpanels.commandtags.paywalls.itempaywall.ItemPaywall;
import me.rockyhawk.commandpanels.commandtags.tags.other.DataTags;
import me.rockyhawk.commandpanels.commandtags.tags.other.PlaceholderTags;
import me.rockyhawk.commandpanels.commandtags.tags.other.SpecialTags;
import me.rockyhawk.commandpanels.commandtags.tags.standard.BasicTags;
import me.rockyhawk.commandpanels.commandtags.tags.standard.BungeeTags;
import me.rockyhawk.commandpanels.commandtags.tags.standard.ItemTags;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class CommandRunner {
    CommandPanels plugin;

    public CommandRunner(CommandPanels pl) {
        this.plugin = pl;
    }

    //with the click type included, use null for no click type check
    public void runCommands(Panel panel, PanelPosition position, Player p, List<String> commands, ClickType click) {
        for (String command : commands) {
            if(click != null) {
                command = plugin.commandRunner.hasCorrectClick(command, click);
                if (command.equals("")) {
                    //click type is wrong
                    continue;
                }
            }

            //do paywall check
            PaywallEvent paywallEvent = new PaywallEvent(plugin, panel, position, p, command);
            Bukkit.getPluginManager().callEvent(paywallEvent);
            if (paywallEvent.PAYWALL_OUTPUT == PaywallOutput.Blocked) {
                break;
            }
            //not a paywall, run as command
            if (paywallEvent.PAYWALL_OUTPUT == PaywallOutput.NotApplicable) {
                plugin.commandRunner.runCommand(panel, position, p, command);
            }
        }
    }

    public void runCommand(Panel panel, PanelPosition position, Player p, String commandRAW) {
        CommandTagEvent tags = new CommandTagEvent(plugin, panel, position, p, commandRAW);
        Bukkit.getPluginManager().callEvent(tags);
        if (!tags.commandTagUsed) {
            Bukkit.dispatchCommand(p, plugin.tex.attachPlaceholders(panel, position, p, commandRAW.trim()));
        }
    }

    public boolean runMultiPaywall(Panel panel, PanelPosition position, Player p, List<String> paywalls, List<String> commands, ClickType click) {
        boolean allPaywallsValid = true;

        // New list combining paywalls and commands
        List<String> allCommands = new ArrayList<>(paywalls);
        allCommands.addAll(commands);

        for (String command : allCommands) {
            // Trigger the event but do not remove the payment from the player
            PaywallEvent paywallEvent = new PaywallEvent(plugin, panel, position, p, command);
            paywallEvent.doDelete = false;
            Bukkit.getPluginManager().callEvent(paywallEvent);

            if (paywallEvent.PAYWALL_OUTPUT == PaywallOutput.Blocked) {
                allPaywallsValid = false; // Set flag to false if any paywall is blocked
                break; // Exit the loop if any command is blocked by the paywall
            }
        }

        // Execute all commands if all paywalls are valid
        if (allPaywallsValid) {
            plugin.commandRunner.runCommands(panel, position, p, allCommands, click);
        }

        // Return output as boolean for usage if applicable
        return allPaywallsValid;
    }

    //do this on startup to load listeners
    public void registerBuiltInTags() {
        plugin.getServer().getPluginManager().registerEvents(new Paywall(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TokenPaywall(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemPaywall(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new Hasperm(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new XpPaywall(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DataPaywall(plugin), plugin);

        plugin.getServer().getPluginManager().registerEvents(new DataTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlaceholderTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SpecialTags(plugin), plugin);

        plugin.getServer().getPluginManager().registerEvents(new BasicTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BungeeTags(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemTags(plugin), plugin);
    }

    public String hasCorrectClick(String command, ClickType click) {
        try {
            switch (command.split("\\s")[0]) {
                case "right=": {
                    //if commands is for right-clicking, remove the 'right=' and continue
                    command = command.replace("right= ", "");
                    if (click != ClickType.RIGHT) {
                        return "";
                    }
                    break;
                }
                case "rightshift=": {
                    command = command.replace("rightshift= ", "");
                    if (click != ClickType.SHIFT_RIGHT) {
                        return "";
                    }
                    break;
                }
                case "left=": {
                    command = command.replace("left= ", "");
                    if (click != ClickType.LEFT) {
                        return "";
                    }
                    break;
                }
                case "leftshift=": {
                    command = command.replace("leftshift= ", "");
                    if (click != ClickType.SHIFT_LEFT) {
                        return "";
                    }
                    break;
                }
                case "middle=": {
                    command = command.replace("middle= ", "");
                    if (click != ClickType.MIDDLE) {
                        return "";
                    }
                    break;
                }
            }
            return command;
        } catch (Exception ex) {
            return "";
            //skip if you can't do this
        }
    }
}
