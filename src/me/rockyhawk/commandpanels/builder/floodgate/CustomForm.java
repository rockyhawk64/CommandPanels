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

    private record FormField(FloodgateComponent component, List<String> options) {
        private static FormField of(FloodgateComponent component) {
            return new FormField(component, null);
        }
    }

    public void sendForm(FloodgatePanel panel) {
        Player player = builder.getPlayer();
        org.geysermc.cumulus.form.CustomForm.Builder form = org.geysermc.cumulus.form.CustomForm.builder()
                .title(ctx.text.parseTextToString(player, panel.getTitle()));

        List<FormField> fields = new ArrayList<>();

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
                    case FloodgateLabel input -> form.label(
                            parseText(
                                    input.getName().replaceAll("\\\\n", "\n"))
                    );
                    case FloodgateInput input -> {
                        form.input(
                                parseText(input.getName()),
                                parseText(input.getPlaceholder()),
                                parseText(input.getDefault())
                        );
                        fields.add(FormField.of(input));
                    }
                    case FloodgateSlider slider -> {
                        form.slider(
                                parseText(slider.getName()),
                                parseFloat(slider.getMinimum()),
                                parseFloat(slider.getMaximum()),
                                parseInt(slider.getStep()),
                                parseFloat(slider.getDefault())
                        );
                        fields.add(FormField.of(slider));
                    }
                    case FloodgateDropdown dropdown -> {
                        List<String> parsedOptions = dropdown.getOptions().stream()
                                .map(opt -> parseText(opt).trim())
                                .filter(opt -> !opt.isEmpty())
                                .toList();
                        form.dropdown(
                                parseText(dropdown.getName()),
                                parsedOptions,
                                parseInt(dropdown.getDefault())
                        );
                        fields.add(new FormField(dropdown, parsedOptions));
                    }
                    case FloodgateToggle toggle -> {
                        form.toggle(
                                parseText(toggle.getName()),
                                parseBoolean(toggle.getDefault())
                        );
                        fields.add(FormField.of(toggle));
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
                        fields.add(new FormField(stepSlider, parsedSteps));
                    }
                    // unknown component, skip
                    default -> {}
                }

                // Only one component per order slot
                break;
            }
        }

        form.validResultHandler((CustomFormResponse response) -> {
            for (FormField field : fields) {
                if (!response.hasNext()) break;
                Object rawValue = response.next();

                // options snapshot taken at build time.
                String value;
                if (field.options() != null && rawValue instanceof Integer selected
                        && selected >= 0 && selected < field.options().size()) {
                    value = field.options().get(selected);
                } else {
                    value = String.valueOf(rawValue);
                }

                createSessionData(field.component().getId(), value);
            }

            // Run actions
            for (FormField field : fields) {
                FloodgateComponent comp = field.component();
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