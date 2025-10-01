package me.rockyhawk.commandpanels.builder.inventory.items.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CustomHeads {

    private static final Map<String, PlayerProfile> profileCache = new HashMap<>();

    public ItemStack getCustomHead(String base64Texture) {
        PlayerProfile profile = getOrCreateProfile(base64Texture);
        if (profile == null) return new ItemStack(Material.PLAYER_HEAD);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        if (!(skull.getItemMeta() instanceof SkullMeta skullMeta)) return skull;

        skullMeta.setPlayerProfile(profile);
        skull.setItemMeta(skullMeta);

        return skull; // New item each time, only shares profile (skin)
    }

    public ItemStack getPlayerHead(String playerName) {
        PlayerProfile profile = profileCache.computeIfAbsent(playerName, key -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

            return offlinePlayer.getPlayerProfile();
        });

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        if (!(skull.getItemMeta() instanceof SkullMeta skullMeta)) return skull;

        skullMeta.setPlayerProfile(profile);
        skull.setItemMeta(skullMeta);

        return skull;
    }

    private PlayerProfile getOrCreateProfile(String base64Texture) {
        return profileCache.computeIfAbsent(base64Texture, key -> {
            String skinUrl = extractSkinUrlFromBase64(base64Texture);
            if (skinUrl == null) return null;

            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(new URL(skinUrl));
                profile.setTextures(textures);
                return profile;
            } catch (MalformedURLException e) {
                return null;
            }
        });
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