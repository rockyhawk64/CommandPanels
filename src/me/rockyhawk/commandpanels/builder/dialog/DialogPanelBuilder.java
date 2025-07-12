package me.rockyhawk.commandpanels.builder.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.SessionManager;
import me.rockyhawk.commandpanels.session.dialog.DialogComponent;
import me.rockyhawk.commandpanels.session.dialog.DialogPanel;
import me.rockyhawk.commandpanels.session.dialog.components.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogPanelBuilder extends PanelBuilder {
    ActionBuilder buttonBuilder;
    InputBuilder inputBuilder;
    BodyBuilder bodyBuilder;

    public DialogPanelBuilder(Context ctx, Player player) {
        super(ctx, player);
        buttonBuilder = new ActionBuilder(ctx, this);
        inputBuilder = new InputBuilder(ctx, this);
        bodyBuilder = new BodyBuilder(ctx, this);
    }

    @Override
    public void open(Panel openPanel, SessionManager.PanelOpenType openType) {
        if (!(openPanel instanceof DialogPanel panel)) {
            throw new IllegalArgumentException("Expected DialogPanel, got " + openPanel.getClass());
        }

        Player player = this.getPlayer();

        // List of elements to go in dialog
        List<ActionButton> buttons = new ArrayList<>();
        List<DialogInput> inputs = new ArrayList<>();
        List<DialogBody> bodies = new ArrayList<>();

        // Loop through components and build them with helper classes
        List<String> sortedOrder = new ArrayList<>(panel.getOrder().keySet());
        sortedOrder.sort(Comparator.comparingInt(this::parseInt));

        for (String key : sortedOrder) {
            for (String id : panel.getOrder().get(key)) {

                if(!panel.getComponents().containsKey(id)) continue;
                DialogComponent comp = panel.getComponents().get(id);

                // Check conditions for which component to use
                String conditions = ctx.text.parseTextToString(player, comp.getConditions());
                if (!conditions.trim().isEmpty()) {
                    ConditionNode conditionNode = new ConditionParser().parse(conditions);
                    boolean result = conditionNode.evaluate(player, ctx);
                    if (!result) continue;
                }

                // Add the component to the dialog
                if (panel.getComponents().get(id) instanceof DialogButton button){
                    buttons.add(buttonBuilder.buildButton(button, panel));
                    continue;
                }
                if (panel.getComponents().get(id) instanceof DialogItem item){
                    bodies.add(bodyBuilder.createItem(item, panel));
                    continue;
                }
                if (panel.getComponents().get(id) instanceof DialogBodyText text){
                    bodies.add(bodyBuilder.createText(text, panel));
                    continue;
                }
                if (panel.getComponents().get(id) instanceof DialogInputBool bool){
                    inputs.add(inputBuilder.createBool(bool));
                    continue;
                }
                if (panel.getComponents().get(id) instanceof DialogInputRange range){
                    inputs.add(inputBuilder.createRange(range));
                    continue;
                }
                if (panel.getComponents().get(id) instanceof DialogInputOption option){
                    inputs.add(inputBuilder.createOption(option));
                    continue;
                }
                if (panel.getComponents().get(id) instanceof DialogInputText inputText){
                    inputs.add(inputBuilder.createText(inputText));
                }
            }
        }

        // Check for exit action button
        final ActionButton exitAction;
        boolean hasExitButton = Boolean.parseBoolean(ctx.text.parseTextToString(player, panel.getExitButton()));
        if(!buttons.isEmpty() && hasExitButton){
            exitAction = buttons.removeLast();
        } else {
            exitAction = null;
        }

        // Make sure there is at least one button
        if(buttons.isEmpty()){
            ctx.text.sendError(player, "Dialog needs at least one button");
            return;
        }

        // Create the dialog
        Dialog dialog = Dialog.create(dialogBuilder -> dialogBuilder
                .empty()
                .base(DialogBase.builder(
                                ctx.text.parseTextToComponent(player, panel.getTitle()))
                        .canCloseWithEscape(Boolean.parseBoolean(
                                ctx.text.parseTextToString(player, panel.getEscapable())))
                        .body(bodies)
                        .inputs(inputs)
                        .build())
                .type(DialogType.multiAction(
                        buttons,
                        exitAction,
                        parseInt(panel.getColumns())
                ))
        );

        // Show the dialog to player and create session
        player.showDialog(dialog);
        ctx.session.updateSession(this.getPlayer(), panel, openType);
    }

    public float parseFloat(String raw) {
        String parsed = ctx.text.parseTextToString(this.getPlayer(), raw);
        Matcher matcher = SIMPLE_FLOAT_PATTERN.matcher(parsed);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group());
            } catch (NumberFormatException ignored) {
                // fallback below
            }
        }
        return 0.0f;
    }
    public int parseInt(String raw) {
        String parsed = ctx.text.parseTextToString(this.getPlayer(), raw);
        Matcher matcher = SIMPLE_INT_PATTERN.matcher(parsed);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException ignored) {
                // fallback below
            }
        }
        return 0;
    }

    private static final Pattern SIMPLE_INT_PATTERN = Pattern.compile("-?\\d+");
    private static final Pattern SIMPLE_FLOAT_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
}
