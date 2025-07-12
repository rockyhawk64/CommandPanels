package me.rockyhawk.commandpanels.builder.dialog;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.dialog.components.*;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InputBuilder {
    private final Context ctx;
    private final DialogPanelBuilder builder;

    public InputBuilder(Context ctx, DialogPanelBuilder builder) {
        this.ctx = ctx;
        this.builder = builder;
    }

    public DialogInput createText(DialogInputText item) {
        Player player = builder.getPlayer();
        TextDialogInput.Builder inputBuilder = DialogInput
                .text(item.getId(), ctx.text.parseTextToComponent(player, item.getName()))
                .initial(ctx.text.parseTextToString(player, item.getInitial()))
                .width(builder.parseInt(item.getWidth()))
                .maxLength(builder.parseInt(item.getMaxLength()));

        // Conditionally apply multiline only if either height and rows are set
        boolean hasHeight = item.getHeight() != null && !item.getHeight().isEmpty();
        boolean hasRows = item.getMultilineRows() != null && !item.getMultilineRows().isEmpty();

        if (hasHeight && hasRows) {
            inputBuilder.multiline(TextDialogInput.MultilineOptions.create(
                    builder.parseInt(item.getMultilineRows()),
                    builder.parseInt(item.getHeight())
            ));
        }

        return inputBuilder.build();
    }

    public DialogInput createBool(DialogInputBool comp){
        Player player = builder.getPlayer();
        return DialogInput
                .bool(comp.getId(), ctx.text.parseTextToComponent(player,comp.getName()))
                .initial(Boolean.parseBoolean(
                        ctx.text.parseTextToString(player, comp.getInitial())
                ))
                .build();
    }

    public DialogInput createRange(DialogInputRange comp){
        Player player = builder.getPlayer();
        return DialogInput
                .numberRange(comp.getId(),
                        ctx.text.parseTextToComponent(player,comp.getName()),
                        builder.parseFloat(comp.getStart()),
                        builder.parseFloat(comp.getEnd()))
                .initial(builder.parseFloat(comp.getInitial()))
                .step(builder.parseFloat(comp.getStep()))
                .width(builder.parseInt(comp.getWidth()))
                .build();
    }

    public DialogInput createOption(DialogInputOption comp){
        Player player = builder.getPlayer();

        List<SingleOptionDialogInput.OptionEntry> options = new ArrayList<>();
        for(String option : comp.getOptions()){
            Component compOption = ctx.text.parseTextToComponent(player, option);
            String initial = ctx.text.parseTextToString(player, comp.getInitial());
            boolean isInitial = initial.equalsIgnoreCase(option);
            options.add(
                    SingleOptionDialogInput.OptionEntry.create(option, compOption, isInitial)
            );
        }

        return DialogInput
                .singleOption(comp.getId(),
                        ctx.text.parseTextToComponent(player,comp.getName()), options)
                .width(builder.parseInt(comp.getWidth()))
                .build();
    }
}
