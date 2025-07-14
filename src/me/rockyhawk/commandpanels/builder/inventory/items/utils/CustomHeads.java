package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CustomHeads {

    // Static to remember saved heads across different instances
    private static final Map<String, ItemStack> savedCustomHeads = new HashMap<>();

    public ItemStack getCustomHead(String base64Texture) {
        if (savedCustomHeads.containsKey(base64Texture)) {
            return savedCustomHeads.get(base64Texture);
        }

        trimCacheIfNeeded();

        String decodedTexture = extractSkinUrlFromBase64(base64Texture);
        if (decodedTexture == null) return new ItemStack(Material.PLAYER_HEAD);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        if (!(skull.getItemMeta() instanceof SkullMeta skullMeta)) return skull;

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL(decodedTexture));
        } catch (MalformedURLException ignore) {}

        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);
        skull.setItemMeta(skullMeta);

        savedCustomHeads.put(base64Texture, skull);
        return skull;
    }

    public ItemStack getPlayerHead(String playerName) {
        if (savedCustomHeads.containsKey(playerName)) {
            return savedCustomHeads.get(playerName);
        }

        trimCacheIfNeeded();

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        if (!(skull.getItemMeta() instanceof SkullMeta skullMeta)) return skull;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        PlayerProfile profile = Bukkit.createPlayerProfile(offlinePlayer.getUniqueId());

        skullMeta.setOwnerProfile(profile);
        skull.setItemMeta(skullMeta);

        savedCustomHeads.put(playerName, skull);
        return skull;
    }

    private void trimCacheIfNeeded() {
        int CACHE_LIMIT = 2000;
        if (savedCustomHeads.size() <= CACHE_LIMIT) return;
        Iterator<Map.Entry<String, ItemStack>> iterator = savedCustomHeads.entrySet().iterator();
        while (iterator.hasNext() && savedCustomHeads.size() > CACHE_LIMIT) {
            iterator.next();
            iterator.remove();
        }
    }

    private String extractSkinUrlFromBase64(String base64Texture) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Texture);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(decodedString).getAsJsonObject();
            JsonObject textures = jsonObject.getAsJsonObject("textures");
            JsonObject skin = textures.getAsJsonObject("SKIN");
            return skin.has("url") ? skin.get("url").getAsString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
