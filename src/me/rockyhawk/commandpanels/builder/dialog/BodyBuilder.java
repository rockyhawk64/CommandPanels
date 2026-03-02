package me.rockyhawk.commandpanels.builder.dialog;

import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.session.dialog.DialogPanel;
import me.rockyhawk.commandpanels.session.dialog.components.DialogItem;
import me.rockyhawk.commandpanels.session.dialog.components.DialogBodyText;

public class BodyBuilder {
    private final Context ctx;
    private final DialogPanelBuilder builder;

    public BodyBuilder(Context ctx, DialogPanelBuilder builder) {
        this.ctx = ctx;
        this.builder = builder;
    }

    public DialogBody createItem(DialogItem item, DialogPanel panel) {
        String text = item.getText();
        PlainMessageDialogBody description = text == null || text.isEmpty() ? null : DialogBody.plainMessage(
                ctx.text.parseTextToComponent(builder.getPlayer(), text)
        );
        return DialogBody
                .item(item.getItemStack(ctx, panel, builder.getPlayer()))
                .description(description)
                .width(item.getWidth())
                .height(item.getHeight())
                .build();
    }

    public DialogBody createText(DialogBodyText text, DialogPanel panel) {
        return DialogBody.plainMessage(
                ctx.text.parseTextToComponent(builder.getPlayer(), text.getName()),
                text.getWidth()
        );
    }
}

