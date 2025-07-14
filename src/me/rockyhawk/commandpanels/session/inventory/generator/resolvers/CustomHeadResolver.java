package me.rockyhawk.commandpanels.session.inventory.generator.resolvers;

import com.google.gson.JsonObject;
import me.rockyhawk.commandpanels.session.inventory.generator.ItemResolver;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class CustomHeadResolver implements ItemResolver {

    @Override
    public void resolve(ItemStack item, Map<String, Object> itemData) {
        if (item.getType() != Material.PLAYER_HEAD) return;
        if (!(item.getItemMeta() instanceof SkullMeta meta)) return;

        PlayerProfile profile = meta.getOwnerProfile();
        if (profile == null) return;

        PlayerTextures textures = profile.getTextures();
        URL skinUrl = textures.getSkin();
        if (skinUrl != null) {
            // Construct the texture JSON
            JsonObject skin = new JsonObject();
            JsonObject texturesObj = new JsonObject();
            JsonObject skinObj = new JsonObject();

            skinObj.addProperty("url", skinUrl.toString());
            texturesObj.add("SKIN", skinObj);
            skin.add("textures", texturesObj);

            // Convert to Base64
            String json = skin.toString();
            String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

            // Store in itemData
            itemData.put("material", "[head] " + base64);
        }

    }
}