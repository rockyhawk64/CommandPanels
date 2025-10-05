package me.rockyhawk.commandpanels.builder.dialog;

import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.ClickActions;
import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import me.rockyhawk.commandpanels.session.dialog.DialogPanel;
import me.rockyhawk.commandpanels.session.dialog.components.DialogButton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

public class ActionBuilder implements Listener {
    private final Context ctx;
    private final DialogPanelBuilder builder;

    public ActionBuilder(Context ctx, DialogPanelBuilder builder) {
        this.ctx = ctx;
        this.builder = builder;
    }

    public ActionButton buildButton(DialogButton button, DialogPanel panel) {
        Player player = builder.getPlayer();

        if (!button.getConditions().trim().isEmpty()) {
            ConditionNode conditionNode = new ConditionParser().parse(button.getConditions());
            if (!conditionNode.evaluate(player, ctx)) return null;
        }

        Component name = ctx.text.parseTextToComponent(player, button.getName());
        Component tooltip = ctx.text.parseTextToComponent(player, button.getTooltip());

        DialogAction action = DialogAction.customClick((options, audience) -> {
                    ClickActions actions = button.getClickActions();
                    CommandRunner commands = new CommandRunner(ctx);
                    RequirementRunner requirements = new RequirementRunner(ctx);

                    //Loop through inputs and assign them to session data
                    for(DialogComponent comp : panel.getComponents().values()){
                        String id = comp.getId();

                        // Get the possible input value of the component
                        String text = options.getText(id);
                        Boolean bool = options.getBoolean(id);
                        Float number = options.getFloat(id);

                        // Use the first non-null value
                        if (text != null) {
                            player.getPersistentDataContainer()
                                    .set(new NamespacedKey(ctx.plugin, id),
                                            PersistentDataType.STRING, text);
                        } else if (number != null) {
                            player.getPersistentDataContainer()
                                    .set(new NamespacedKey(ctx.plugin, id),
                                            PersistentDataType.STRING, String.valueOf(number));
                        } else if (bool != null) {
                            player.getPersistentDataContainer()
                                    .set(new NamespacedKey(ctx.plugin, id),
                                            PersistentDataType.STRING, String.valueOf(bool));
                        }
                    }

                    // run commands
                    if (!requirements.processRequirements(panel, player, actions.requirements())) {
                        commands.runCommands(panel, player, actions.fail());
                    } else {
                        commands.runCommands(panel, player, actions.commands());
                    }
                }, ClickCallback.Options.builder().build()
        );

        // Override with a custom Dialog action if button contains one
        // Bottom of the list is prioritised over the top
        if (!button.getClipboard().isEmpty()) {
            action = DialogAction.staticAction(
                    ClickEvent.copyToClipboard(
                            ctx.text.parseTextToString(player, button.getClipboard())));
        }
        if (!button.getUrl().isEmpty()) {
            action = DialogAction.staticAction(
                    ClickEvent.openUrl(
                            ctx.text.parseTextToString(player, button.getUrl())));
        }

        return ActionButton.builder(name)
                .tooltip(tooltip)
                .action(action)
                .width(builder.parseInt(button.getWidth()))
                .build();
    }
}
