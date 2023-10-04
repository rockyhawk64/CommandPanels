package me.rockyhawk.commandpanels.classresources;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;

public class GetCustomHeads {
    CommandPanels plugin;
    public GetCustomHeads(CommandPanels pl) {
        this.plugin = pl;
    }

    //will not go above 2000 elements in the list which is roughly 270 KB RAM usage
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
    public ItemStack getPlayerHead(String name) {
        byte id = 0;
        if (plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)) {
            id = 3;
        }

        //get texture if already cached
        if(playerHeadTextures.containsKey(name)) {
            return getCustomHead(playerHeadTextures.get(name));
        }

        //create ItemStack
        ItemStack itemStack = new ItemStack(Material.matchMaterial(plugin.getHeads.playerHeadString()), 1, id);

        //Run fallback code, if API call fails, use legacy setOwner
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(name);
        itemStack.setItemMeta(meta);

        // Fetch and cache the texture asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if(plugin.debug.consoleDebug){
                    plugin.getServer().getConsoleSender().sendMessage(plugin.tex.colour(plugin.tag +
                            ChatColor.WHITE +
                            "Attempting to Download & Cache Head Texture for " + name));
                }

                // Fetch the player UUID from the Mojang API
                URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
                URLConnection uuidConnection = uuidUrl.openConnection();
                uuidConnection.setConnectTimeout(2000); // Set connection timeout to 2 seconds
                uuidConnection.setReadTimeout(2000); // Set read timeout to 2 seconds
                //Json is simple and structured so a hard code solution will avoid the need for a library
                String uuidReader = new Scanner(uuidConnection.getInputStream(),
                        StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
                String uuid = uuidReader.split("\"id\" : \"")[1].split("\"")[0];

                // Fetch the skin texture from the Mojang API using the player UUID
                URL texturesUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
                URLConnection texturesConnection = texturesUrl.openConnection();
                texturesConnection.setConnectTimeout(2000); // Set connection timeout to 2 seconds
                texturesConnection.setReadTimeout(2000); // Set read timeout to 2 seconds
                //Json is simple and structured so a hard code solution will avoid the need for a library
                String valueReader = new Scanner(texturesConnection.getInputStream(),
                        StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
                String value = valueReader.split("\"value\" : \"")[1].split("\"")[0];
                playerHeadTextures.put(name, value);

                // Once the API call is finished, update the ItemStack on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    itemStack.setItemMeta(getCustomHead(value).getItemMeta());
                });
            } catch (Exception e) {
                // Handle exceptions
                if(plugin.debug.consoleDebug) {
                    plugin.debug(e,null);
                }
            }
        });

        return itemStack;
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