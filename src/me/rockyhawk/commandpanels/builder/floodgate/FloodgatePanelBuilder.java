package me.rockyhawk.commandpanels.builder.floodgate;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.builder.PanelBuilder;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.floodgate.FloodgatePanel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class FloodgatePanelBuilder extends PanelBuilder {

    private final SimpleForm simpleBuilder;
    private final CustomForm customBuilder;

    public FloodgatePanelBuilder(Context ctx, Player player) {
        super(ctx, player);
        this.simpleBuilder = new SimpleForm(ctx, this);
        this.customBuilder = new CustomForm(ctx, this);
    }

    @Override
    public void open(Panel openPanel){
        if (!(openPanel instanceof FloodgatePanel panel)) {
            throw new IllegalArgumentException("Expected FloodgatePanel, got " + openPanel.getClass());
        }

        // If no floodgate return
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("floodgate") ||
                !FloodgateApi.getInstance().isFloodgatePlayer(this.getPlayer().getUniqueId())) return;

        // Open the form
        switch (panel.getFloodgateType().toLowerCase()) {
            case "simple" -> this.simpleBuilder.sendForm(panel);
            case "custom" -> this.customBuilder.sendForm(panel);
        }
    }
}
