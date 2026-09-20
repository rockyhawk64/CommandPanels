package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import me.rockyhawk.commandpanels.Context;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CustomHeads {

    private final Context ctx;

    public CustomHeads(Context ctx) {
        this.ctx = ctx;
    }

    public ItemStack getCustomHead(String base64Texture) {
        ItemStack skull = ItemStack.of(Material.PLAYER_HEAD);
        if (base64Texture == null || base64Texture.isBlank()) return skull;
        skull.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile()
                .addProperty(new ProfileProperty("textures", base64Texture))
                .build());
        return skull;
    }

    public ItemStack getPlayerHeadSync(String playerName) {
        ItemStack skull = ItemStack.of(Material.PLAYER_HEAD);
        if (playerName == null || !playerName.matches("^[!-~]{1,16}$")) return skull;
        skull.setData(DataComponentTypes.PROFILE,
                ResolvableProfile.resolvableProfile().name(playerName).build());
        return skull;
    }
}