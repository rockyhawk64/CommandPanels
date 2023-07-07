package me.rockyhawk.commandpanels.classresources;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class GetCustomHeads {
    CommandPanels plugin;
    public GetCustomHeads(CommandPanels pl) {
        this.plugin = pl;
    }

    //contains cached player name and then base64 value (clears on /cpr reload)
    //also will clear if the map reaches a length of 1000 which is roughly 135 KB RAM usage
    public HashMap<String, String> playerHeadTextures = new HashMap<>();

    public String getHeadBase64(ItemStack head) {
        if (plugin.getHeads.ifSkullOrHead(head.getType().toString()) && head.hasItemMeta()) {
            try {
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                assert meta != null;
                if (!meta.hasOwner()) {
                    Field fld = meta.getClass().getDeclaredField("profile");
                    fld.setAccessible(true);
                    GameProfile prof = (GameProfile) fld.get(meta);
                    Iterator itr = prof.getProperties().get("textures").iterator();
                    if (itr.hasNext()) {
                        Property var5 = (Property) itr.next();
                        return var5.getValue();
                    }
                }
            }catch(Exception exc){/*skip return null*/}
        }
        return null;
    }

    //getting the head from a Player Name
    @SuppressWarnings("deprecation")
    public ItemStack getPlayerHead(String name) {
        byte id = 0;
        if (plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            id = 3;
        }

        //get texture if already cached
        if(playerHeadTextures.containsKey(name)) {
            return getCustomHead(playerHeadTextures.get(name));
        }

        try {
            // Fetch the player UUID from the Mojang API
            URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            URLConnection uuidConnection = uuidUrl.openConnection();
            uuidConnection.setConnectTimeout(2000); // Set connection timeout to 2 seconds
            uuidConnection.setReadTimeout(2000); // Set read timeout to 2 seconds
            Reader uuidReader = new InputStreamReader(uuidConnection.getInputStream(), StandardCharsets.UTF_8);
            JSONObject uuidResponse = (JSONObject) new JSONParser().parse(uuidReader);
            String uuid = (String) uuidResponse.get("id");

            // Fetch the skin texture from the Mojang API using the player UUID
            URL texturesUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            URLConnection texturesConnection = texturesUrl.openConnection();
            texturesConnection.setConnectTimeout(2000); // Set connection timeout to 2 seconds
            texturesConnection.setReadTimeout(2000); // Set read timeout to 2 seconds
            Reader texturesReader = new InputStreamReader(texturesConnection.getInputStream(), StandardCharsets.UTF_8);
            JSONObject texturesResponse = (JSONObject) new JSONParser().parse(texturesReader);
            JSONArray propertiesArray = (JSONArray) texturesResponse.get("properties");
            JSONObject texturesProperty = (JSONObject) propertiesArray.get(0);
            String base64Texture = (String) texturesProperty.get("value");
            playerHeadTextures.put(name, base64Texture);

            // Create a custom head using the Base64 texture string
            return getCustomHead(base64Texture);
        } catch (Exception e) {
            // Fallback to setting the owner if the Mojang API request fails
            ItemStack itemStack = new ItemStack(Material.matchMaterial(plugin.getHeads.playerHeadString()), 1, id);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setOwner(name);
            itemStack.setItemMeta(meta);
            return itemStack;
        }
    }

    //used to get heads from Base64 Textures
    @SuppressWarnings("deprecation")
    public ItemStack getCustomHead(String b64stringtexture) {
        //get head from base64
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        PropertyMap propertyMap = profile.getProperties();
        if (propertyMap == null) {
            throw new IllegalStateException("Profile doesn't contain a property map");
        } else {
            propertyMap.put("textures", new Property("textures", b64stringtexture));
            byte id = 0;
            if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)){
                id = 3;
            }
            ItemStack head = new ItemStack(Material.matchMaterial(plugin.getHeads.playerHeadString()), 1,id);
            ItemMeta headMeta = head.getItemMeta();
            assert headMeta != null;

            Field profileField;
            Method setProfileMethod = null;
            try {
                profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
                try {
                    setProfileMethod = headMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                } catch (NoSuchMethodException ignore) {}
            } catch (SecurityException ignored) {}
            try {
                if (setProfileMethod == null) {
                    profileField = headMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(headMeta, profile);
                } else {
                    setProfileMethod.setAccessible(true);
                    setProfileMethod.invoke(headMeta, profile);
                }
            } catch (Exception e1) {
                plugin.debug(e1,null);
            }

            head.setItemMeta(headMeta);
            return head;
        }
    }
}
