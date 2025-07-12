package me.rockyhawk.commandpanels.interaction.commands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.requirements.DataTag;
import me.rockyhawk.commandpanels.interaction.commands.requirements.ItemTag;
import me.rockyhawk.commandpanels.interaction.commands.requirements.VaultTag;
import me.rockyhawk.commandpanels.interaction.commands.requirements.XpTag;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RequirementRunner {
    private final Context ctx;
    private final List<RequirementTagResolver> resolvers = new ArrayList<>();

    public RequirementRunner(Context ctx) {
        this.ctx = ctx;
        registerResolvers();
    }

    private void registerResolvers() {
        resolvers.add(new VaultTag());
        resolvers.add(new ItemTag());
        resolvers.add(new DataTag());
        resolvers.add(new XpTag());
    }

    // True if the requirements passed and payments taken successfully
    public boolean processRequirements(Panel panel, Player player, List<String> requirements) {
        List<RequirementTagResolver> toExecute = new ArrayList<>();
        List<String> argsList = new ArrayList<>();

        for (String requirement : requirements) {
            requirement = ctx.text.parseTextToString(player, requirement.trim());
            if (requirement.isEmpty()) continue;

            String[] parts = requirement.split("\\s+", 2);
            String tag = parts[0];
            String args = (parts.length > 1) ? parts[1].trim() : "";

            boolean matched = false;
            for (RequirementTagResolver resolver : resolvers) {
                if (resolver.isCorrectTag(tag)) {
                    matched = true;
                    if (!resolver.check(ctx, panel, player, args)) {
                        return false; // Fail early
                    }
                    toExecute.add(resolver);
                    argsList.add(args);
                    break;
                }
            }

            if (!matched) {
                ctx.text.sendError(player, "Unknown requirement tag: " + tag);
                return false;
            }
        }

        // All passed, now execute
        for (int i = 0; i < toExecute.size(); i++) {
            toExecute.get(i).execute(ctx, panel, player, argsList.get(i));
        }

        return true;
    }
}
