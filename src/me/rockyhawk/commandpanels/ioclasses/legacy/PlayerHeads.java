package me.rockyhawk.commandpanels.ioclasses.legacy;

import me.rockyhawk.commandpanels.Context;

public class PlayerHeads {
    Context ctx;
    public PlayerHeads(Context pl) {
        this.ctx = pl;
    }

    public boolean ifSkullOrHead(String material) {
        return material.equalsIgnoreCase("PLAYER_HEAD") || material.equalsIgnoreCase("SKULL_ITEM");
    }

    public String playerHeadString() {
        if(ctx.legacy.MAJOR_VERSION.lessThanOrEqualTo(MinecraftVersions.v1_12)){
            return "SKULL_ITEM";
        }else{
            return "PLAYER_HEAD";
        }
    }
}
