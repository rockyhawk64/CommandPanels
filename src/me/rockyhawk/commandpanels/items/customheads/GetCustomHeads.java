package me.rockyhawk.commandpanels.items.customheads;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.items.customheads.methods.CustomHeadGameProfile;
import me.rockyhawk.commandpanels.items.customheads.methods.CustomHeadPlayerProfile;
import org.bukkit.inventory.ItemStack;

public class GetCustomHeads {
    private final CustomHeadProvider headProvider;

    public GetCustomHeads(Context ctx) {
        this.headProvider = ctx.version.isAtLeast("1.18")
                ? new CustomHeadPlayerProfile()
                : new CustomHeadGameProfile(ctx);
    }

    public ItemStack getCustomHead(String base64) {
        return headProvider.getCustomHead(base64);
    }

    public ItemStack getPlayerHead(String playerName) {
        return headProvider.getPlayerHead(playerName);
    }
}