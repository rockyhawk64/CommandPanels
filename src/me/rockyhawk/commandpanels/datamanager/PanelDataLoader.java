package me.rockyhawk.commandpanels.datamanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PanelDataLoader {
    CommandPanels plugin;
    public PanelDataLoader(CommandPanels pl) {
        this.plugin = pl;
    }
    public YamlConfiguration dataConfig;

    public String getUserData(UUID playerUUID, String dataPoint){
        return dataConfig.getString("playerData." + playerUUID + "." + dataPoint);
    }

    public void setUserData(UUID playerUUID, String dataPoint, String dataValue, boolean overwrite){
        if(!overwrite && dataConfig.isSet("playerData." + playerUUID + "." + dataPoint)){
            return;
        }
        dataConfig.set("playerData." + playerUUID + "." + dataPoint, dataValue);
    }

    public void delUserData(UUID playerUUID, String dataPoint){
        dataConfig.set("playerData." + playerUUID + "." + dataPoint, null);
    }

    public void saveDataFile(){
        try {
            dataConfig.save(plugin.getDataFolder() + File.separator + "data.yml");
        } catch (IOException s) {
            s.printStackTrace();
            plugin.debug(s);
        }
    }
}
