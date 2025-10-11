package me.rockyhawk.commandpanels.builder.floodgate;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.logic.ConditionNode;
import me.rockyhawk.commandpanels.builder.logic.ConditionParser;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.interaction.commands.CommandRunner;
import me.rockyhawk.commandpanels.interaction.commands.RequirementRunner;
import me.rockyhawk.commandpanels.session.floodgate.FloodgateComponent;
import me.rockyhawk.commandpanels.session.floodgate.FloodgatePanel;
import me.rockyhawk.commandpanels.session.floodgate.components.*;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CustomForm {
    protected final Context ctx;
    protected final FloodgatePanelBuilder builder;

    public CustomForm(Context ctx, FloodgatePanelBuilder builder) {
        this.ctx = ctx;
        this.builder = builder;
    }

    public void sendForm(FloodgatePanel panel) {
        Player player = builder.getPlayer();
        org.geysermc.cumulus.form.CustomForm.Builder form = org.geysermc.cumulus.form.CustomForm.builder()
                .title(ctx.text.parseTextToString(player, panel.getTitle()));

        List<FloodgateComponent> inputOrder = new ArrayList<>();

        // Build the form inputs in order
        for (int i = 0; i < panel.getOrder().size(); i++) {
            if (!panel.getOrder().containsKey(String.valueOf(i))) {
                ctx.text.sendError(player, Message.PANEL_LAYOUT_NUMBER_MISSING);
                return;
            }

            List<String> componentIds = panel.getOrder().get(String.valueOf(i));
            for (String key : componentIds) {
                FloodgateComponent comp = panel.getComponents().get(key);
                if (comp == null) continue;

                // Evaluate conditions
                if (!comp.getConditions().trim().isEmpty()) {
                    ConditionNode conditionNode = new ConditionParser().parse(comp.getConditions());
                    if (!conditionNode.evaluate(player, panel, ctx)) continue;
                }

                // Create the component
                switch (comp) {
                    case FloodgateLabel input -> {
                        form.label(
                                parseText(input.getName().replaceAll("\\\\n", "\n"))
                        );
                        inputOrder.add(input);
                    }
                    case FloodgateInput input -> {
                        form.input(
                                parseText(input.getName()),
                                parseText(input.getPlaceholder()),
                                parseText(input.getDefault())
                        );
                        inputOrder.add(input);
                    }
                    case FloodgateSlider slider -> {
                        form.slider(
                                parseText(slider.getName()),
                                parseFloat(slider.getMinimum()),
                                parseFloat(slider.getMaximum()),
                                parseInt(slider.getStep()),
                                parseFloat(slider.getDefault())
                        );
                        inputOrder.add(slider);
                    }
                    case FloodgateDropdown dropdown -> {
                        List<String> parsedOptions = new ArrayList<>();
                        for (String opt : dropdown.getOptions()) {
                            parsedOptions.add(parseText(opt));
                        }
                        form.dropdown(
                                parseText(dropdown.getName()),
                                parsedOptions,
                                parseInt(dropdown.getDefault())
                        );
                        inputOrder.add(dropdown);
                    }
                    case FloodgateToggle toggle -> {
                        form.toggle(
                                parseText(toggle.getName()),
                                parseBoolean(toggle.getDefault())
                        );
                        inputOrder.add(toggle);
                    }
                    case FloodgateStepSlider stepSlider -> {
                        List<String> parsedSteps = new ArrayList<>();
                        for (String step : stepSlider.getSteps()) {
                            parsedSteps.add(parseText(step));
                        }
                        form.stepSlider(
                                parseText(stepSlider.getName()),
                                parsedSteps,
                                parseInt(stepSlider.getDefault())
                        );
                        inputOrder.add(stepSlider);
                    }
                    // unknown component, skip
                    default -> {}
                }

                // Only one component per order slot
                break;
            }
        }

        form.validResultHandler((CustomFormResponse response) -> {
            int index = 0;
            while (response.hasNext()) {
                Object rawValue = response.next();
                FloodgateComponent comp = inputOrder.get(index);

                String value = String.valueOf(rawValue);

                // Convert specific floodgate components to raw strings from index values
                if (comp instanceof FloodgateDropdown dropdown && rawValue instanceof Integer) {
                    value = dropdown.getOptions().get((int) rawValue);
                } else if (comp instanceof FloodgateStepSlider slider && rawValue instanceof Integer) {
                    value = slider.getSteps().get((int) rawValue);
                }

                // Create the session data
                createSessionData(comp.getId(), value);
                index++;
            }
            // Run actions
            for (FloodgateComponent comp : inputOrder) {
                CommandRunner commands = new CommandRunner(ctx);
                RequirementRunner requirements = new RequirementRunner(ctx);

                if (!requirements.processRequirements(panel, player, comp.getClickActions().requirements())) {
                    commands.runCommands(panel, player, comp.getClickActions().fail());
                    return;
                }

                commands.runCommands(panel, player, comp.getClickActions().commands());
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form.build());
    }

    // Placeholder key will be the ID of the component and make session data from result string
    // Eg first button at the top will be index 0 %commandpanels_session_exampleslider%
    private void createSessionData(String key, String output) {
        builder.getPlayer().getPersistentDataContainer()
                .set(new NamespacedKey(ctx.plugin, key),
                        PersistentDataType.STRING, output);
    }

    private String parseText(String raw) {
        return ctx.text.parseTextToString(builder.getPlayer(), raw);
    }

    private final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private int parseInt(String raw) {
        String parsed = parseText(raw);
        if (NUMBER_PATTERN.matcher(parsed).matches()) {
            try {
                return Integer.parseInt(parsed);
            } catch (NumberFormatException ignored) {
                // fallback below
            }
        }
        return 0;
    }

    private float parseFloat(String raw) {
        String parsed = parseText(raw);
        if (NUMBER_PATTERN.matcher(parsed).matches()) {
            try {
                return Float.parseFloat(parsed);
            } catch (NumberFormatException ignored) {
                // fallback below
            }
        }
        return 0;
    }

    private boolean parseBoolean(String raw) {
        String parsed = parseText(raw);
        return Boolean.parseBoolean(parsed);
    }

}