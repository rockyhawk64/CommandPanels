package me.rockyhawk.commandpanels.manager.open;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.interaction.logic.ConditionNode;
import me.rockyhawk.commandpanels.interaction.logic.ConditionParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class OpenRequirements {
    private final Context ctx;

    public OpenRequirements(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Validates if a player can open a panel based on open-requirements section
     * Returns true if player can open the panel, false otherwise
     */
    public boolean canOpenPanel(Panel panel, Player player) {
        ConfigurationSection config = panel.getConfig();
        
        // Use the logic condition method for open requirements
        String condition = config.getString("condition");
        boolean result = true;

        if(condition != null){
            ConditionNode conditionNode = new ConditionParser().parse(condition);
            result = conditionNode.evaluate(player, ctx, panel);
        }

        return result;
    }
} 