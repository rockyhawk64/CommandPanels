package me.rockyhawk.commandpanels.classresources.customheads;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.classresources.customheads.methods.CustomHeadGameProfile;
import me.rockyhawk.commandpanels.classresources.customheads.methods.CustomHeadPlayerProfile;
import me.rockyhawk.commandpanels.ioclasses.legacy.MinecraftVersions;
import org.bukkit.inventory.ItemStack;

public class GetCustomHeads {
    CustomHeadGameProfile gameProfileHeadClass;
    CustomHeadPlayerProfile playerProfileHeadClass;
    Context ctx;

    public GetCustomHeads(Context pl) {
        this.ctx = pl;
        gameProfileHeadClass = new CustomHeadGameProfile(pl);
        playerProfileHeadClass = new CustomHeadPlayerProfile();
    }

    public ItemStack getCustomHead(String base64){
        if(ctx.legacy.MAJOR_VERSION.greaterThanOrEqualTo(MinecraftVersions.v1_18)){
            return playerProfileHeadClass.getCustomHead(base64);
        }else{
            return gameProfileHeadClass.getCustomHead(base64);
        }
    }
    public ItemStack getPlayerHead(String playerName){
        if(ctx.legacy.MAJOR_VERSION.greaterThanOrEqualTo(MinecraftVersions.v1_21)){
            return playerProfileHeadClass.getPlayerHead(playerName);
        }else{
            return gameProfileHeadClass.getPlayerHead(playerName);
        }
    }
}
