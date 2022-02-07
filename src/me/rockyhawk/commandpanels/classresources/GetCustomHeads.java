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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.UUID;

public class GetCustomHeads {
    CommandPanels plugin;
    public GetCustomHeads(CommandPanels pl) {
        this.plugin = pl;
    }

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

    //getting the head from a Player
    @SuppressWarnings("deprecation")
    public ItemStack getPlayerHead(String name) {
        byte id = 0;
        if(plugin.legacy.LOCAL_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_15)){
            id = 3;
        }
        ItemStack itemStack = new ItemStack(Material.matchMaterial(plugin.getHeads.playerHeadString()), 1,id);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwner(name);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

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
