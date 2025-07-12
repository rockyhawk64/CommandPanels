package me.rockyhawk.commandpanels.builder.floodgate;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.ClickActions;
import me.rockyhawk.commandpanels.session.floodgate.FloodgatePanel;
import me.rockyhawk.commandpanels.session.floodgate.components.FloodgateButton;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;

public class SimpleForm {
    protected final Context ctx;
    protected final FloodgatePanelBuilder builder;

    public SimpleForm(Context ctx, FloodgatePanelBuilder builder) {
        this.ctx = ctx;
        this.builder = builder;
    }

    public void sendForm(FloodgatePanel panel) {
        Player p = builder.getPlayer();
        org.geysermc.cumulus.form.SimpleForm.Builder form = org.geysermc.cumulus.form.SimpleForm.builder()
                .title(ctx.text.parseTextToString(p, panel.getTitle()))
                .content(ctx.text.parseTextToString(p, panel.getSubtitle().replaceAll("\\\\n", "\n")));

        // Go through all the order sections
        List<FloodgateButton> buttonList = new ArrayList<>();
        for (int i = 0; i < panel.getOrder().size(); i++) {
            if(!panel.getOrder().containsKey(String.valueOf(i))){
                ctx.text.sendError(p,"Panel order is missing a number");
                return;
            }

            // Go through the buttons in the one order section
            List<String> componentIds = panel.getOrder().get(String.valueOf(i));
            for(String key : componentIds){
                if(!(panel.getComponents().get(key) instanceof FloodgateButton button)) continue;

                // Check conditions for which button to use in the slot
                String conditions = ctx.text.parseTextToString(p, button.getConditions());
                if (!conditions.trim().isEmpty()) {
                    ConditionNode conditionNode = new ConditionParser().parse(conditions);
                    boolean result = conditionNode.evaluate(p, ctx);
                    if (!result) continue;
                }

                // Place the button in the form
                String buttonContent = ctx.text.parseTextToString(p, button.getName().replaceAll("\\\\n", "\n"));
                if (button.getIconTexture().isEmpty()) {
                    form.button(buttonContent);
                } else {
                    FormImage.Type type = FormImage.Type.valueOf(ctx.text.parseTextToString(p, button.getIconType()).toUpperCase());
                    String texture = ctx.text.parseTextToString(p, button.getIconTexture());
                    form.button(buttonContent, type, texture);
                }
                buttonList.add(button);
                break;
            }
        }

        form.validResultHandler((SimpleFormResponse response) -> {
            int clickedButtonId = response.clickedButtonId();
            // Index out of bounds, button does not exist
            if (clickedButtonId >= buttonList.size()) return;

            // Run commands
            ClickActions actions = buttonList.get(clickedButtonId).getClickActions();
            CommandRunner commands = new CommandRunner(ctx);
            RequirementRunner requirements = new RequirementRunner(ctx);

            // Check requirements, run fail or commands
            if(!requirements.processRequirements(panel, p, actions.requirements())){
                commands.runCommands(panel, p, actions.fail());
                return;
            }
            commands.runCommands(panel, p, actions.commands());
        });

        FloodgateApi.getInstance().sendForm(p.getUniqueId(), form);
    }
}
