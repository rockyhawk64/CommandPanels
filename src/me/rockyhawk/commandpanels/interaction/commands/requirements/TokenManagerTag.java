package me.rockyhawk.commandpanels.interaction.commands.requirements;

import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.TokenManager;
import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.interaction.commands.RequirementTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.OptionalLong;

public class TokenManagerTag implements RequirementTagResolver {

    private final TokenManager tokenManager;

    public TokenManagerTag() {
        if (Bukkit.getServer().getPluginManager().getPlugin("TokenManager") == null) {
            this.tokenManager = null;
            return;
        }
        this.tokenManager = TokenManagerPlugin.getInstance();
    }

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[tokenmanager]");
    }

    @Override
    public boolean check(Context ctx, Panel panel, Player player, String raw, String args) {
        if (tokenManager == null) return false;

        Long amount = parseAmount(ctx, player, args);
        if (amount == null) return false;

        OptionalLong tokens = tokenManager.getTokens(player);
        return tokens.isPresent() && tokens.getAsLong() >= amount;
    }

    @Override
    public void execute(Context ctx, Panel panel, Player player, String raw, String args) {
        if (tokenManager == null) return;

        Long amount = parseAmount(ctx, player, args);
        if (amount == null) return;

        tokenManager.removeTokens(player, amount);
    }

    private Long parseAmount(Context ctx, Player player, String args) {
        try {
            return Long.parseLong(args);
        } catch (NumberFormatException e) {
            ctx.text.sendError(player, Message.REQUIREMENT_ECONOMY_INVALID, args);
            return null;
        }
    }
}
