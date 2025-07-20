package me.rockyhawk.commandpanels.interaction.commands.requirements;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.RequirementTagResolver;
import me.rockyhawk.commandpanels.session.Panel;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultTag implements RequirementTagResolver {

    private final Economy economy;

    public VaultTag() {
        if(Bukkit.getServer().getPluginManager().getPlugin("Vault") == null){
            this.economy = null;
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);
        this.economy = (rsp != null) ? rsp.getProvider() : null;

        if (economy == null) {
            Bukkit.getConsoleSender().sendMessage("[CommandPanels] VaultTag failed to load Economy.");
        }
    }

    @Override
    public boolean isCorrectTag(String tag) {
        return tag.equalsIgnoreCase("[vault]");
    }

    @Override
    public boolean check(Context ctx, Panel panel, Player player, String raw, String args) {
        if (economy == null) return false;

        Double amount = parseAmount(ctx, player, args);
        return amount != null && economy.has(player, amount);
    }

    @Override
    public void execute(Context ctx, Panel panel, Player player, String raw, String args) {
        if (economy == null) return;

        Double amount = parseAmount(ctx, player, args);
        if (amount == null) return;

        economy.withdrawPlayer(player, amount);
    }

    private Double parseAmount(Context ctx, Player player, String args) {
        try {
            return Double.parseDouble(args);
        } catch (NumberFormatException e) {
            ctx.text.sendError(player, "Invalid economy value: " + args);
            return null;
        }
    }
}
