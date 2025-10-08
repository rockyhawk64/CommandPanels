package me.rockyhawk.commandpanels.interaction.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.formatter.MiniMessage;
import me.rockyhawk.commandpanels.interaction.commands.paywalls.*;
import me.rockyhawk.commandpanels.interaction.commands.paywalls.itempaywall.ItemPaywall;
import me.rockyhawk.commandpanels.interaction.commands.tags.*;
import me.rockyhawk.commandpanels.interaction.commands.tags.BasicTags;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class CommandRunner {

    private final Context ctx;

    private final List<PaywallResolver> paywalls = new ArrayList<>();
    private final List<TagResolver> tags = new ArrayList<>();

    public CommandRunner(Context ctx) {
        this.ctx = ctx;
        registerBuiltInTags();
    }

    public void runCommands(Panel panel, PanelPosition position, Player p, List<String> commands, ClickType click) {
        for (String command : commands) {
            if (click != null) {
                command = hasCorrectClick(command, click);
                if (command.equals("")) continue;
            }

            PaywallOutput output = runPaywalls(panel, position, p, command, true);
            if (output == PaywallOutput.Blocked) {
                break;
            }else if(output == PaywallOutput.NotApplicable){
                //Run the command as this is not a paywall
                runCommand(panel, position, p, command);
            }
        }
    }

    public void runCommand(Panel panel, PanelPosition position, Player p, String commandRaw) {
        for (TagResolver tag : tags) {
            if (tag.handle(ctx, panel, position, p, commandRaw)) {
                return;
            }
        }

        Bukkit.dispatchCommand(p, ctx.text.attachPlaceholders(panel, position, p, commandRaw.trim()));
    }

    public boolean runMultiPaywall(Panel panel, PanelPosition position, Player p, List<String> paywalls, List<String> commands, ClickType click) {
        List<String> allCommands = new ArrayList<>(paywalls);
        allCommands.addAll(commands);

        for (String command : allCommands) {
            if (runPaywalls(panel, position, p, command, false) == PaywallOutput.Blocked) {
                return false;
            }
        }

        runCommands(panel, position, p, allCommands, click);
        return true;
    }

    // Other plugins can hook and add their own tags and paywalls
    public void addTag(TagResolver tag){
        tags.add(tag);
    }
    public void addPaywall(PaywallResolver paywall){
        paywalls.add(paywall);
    }

    private void registerBuiltInTags() {
        // Paywalls
        paywalls.add(new Paywall());
        paywalls.add(new TokenPaywall());
        paywalls.add(new ItemPaywall());
        paywalls.add(new HasPerm());
        paywalls.add(new XpPaywall());
        paywalls.add(new DataPaywall());

        // Tags
        tags.add(new AddPlaceholderTag());
        tags.add(new BasicTags());
        tags.add(new BungeeTag());
        tags.add(new ClosePanelTag());
        tags.add(new CloseTag());
        tags.add(new DataAddTag());
        tags.add(new DataClearTag());
        tags.add(new DataDelTag());
        tags.add(new DataMathTag());
        tags.add(new DataSetTag());
        tags.add(new DelayTag());
        tags.add(new EnchantTag());
        tags.add(new EvalDelayTag());
        tags.add(new GiveItemTag());
        try {
            // Check all the minimessage classes exist before loading
            Class.forName("net.kyori.adventure.text.Component");
            Class.forName("net.kyori.adventure.text.format.TextDecoration");
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            tags.add(new MiniMessageTag());
        } catch (ClassNotFoundException ignore) {
            // Do not load minimessage on Spigot
        }
        tags.add(new OpenTag());
        tags.add(new PlaceholderTag());
        tags.add(new RefreshTag());
        tags.add(new SetCustomDataTag());
        tags.add(new SetItemTag());
        tags.add(new SkriptTag());
        tags.add(new SoundTag());
        tags.add(new TeleportTag());
        tags.add(new TitleTag());
    }

    private PaywallOutput runPaywalls(Panel panel, PanelPosition position, Player p, String command, boolean performOperation) {
        for (PaywallResolver paywall : paywalls) {
            PaywallOutput result = paywall.handle(ctx, panel, position, p, command, performOperation);
            if (result != PaywallOutput.NotApplicable) {
                return result;
            }
        }
        return PaywallOutput.NotApplicable;
    }

    public String hasCorrectClick(String command, ClickType click) {
        try {
            String[] parts = command.split("\\s", 2);
            String prefix = parts[0];
            String rest = parts.length > 1 ? parts[1] : "";

            switch (prefix) {
                case "right=":
                    return click == ClickType.RIGHT ? rest : "";
                case "rightshift=":
                    return click == ClickType.SHIFT_RIGHT ? rest : "";
                case "left=":
                    return click == ClickType.LEFT ? rest : "";
                case "leftshift=":
                    return click == ClickType.SHIFT_LEFT ? rest : "";
                case "middle=":
                    return click == ClickType.MIDDLE ? rest : "";
                default:
                    return command;
            }
        } catch (Exception e) {
            return "";
        }
    }
}