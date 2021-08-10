package me.rockyhawk.commandpanels.datamanager;

import me.rockyhawk.commandpanels.CommandPanels;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Pattern;

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
        //if it exists no overwriting
        if(!overwrite && dataConfig.isSet("playerData." + playerUUID + "." + dataPoint)){
            return;
        }

        //check if string is numeric
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if(pattern.matcher(dataValue).matches()){
            doDataMath(playerUUID, dataPoint, dataValue);
            return;
        }

        dataConfig.set("playerData." + playerUUID + "." + dataPoint, dataValue);
    }

    public void delUserData(UUID playerUUID, String dataPoint){
        dataConfig.set("playerData." + playerUUID + "." + dataPoint, null);
    }

    public void clearData(UUID playerUUID){
        dataConfig.set("playerData." + playerUUID, null);
    }

    public void saveDataFile(){
        try {
            dataConfig.save(plugin.getDataFolder() + File.separator + "data.yml");
        } catch (IOException s) {
            s.printStackTrace();
            plugin.debug(s,null);
        }
    }

    public void doDataMath(UUID playerUUID, String dataPoint, String dataValue){
        BigDecimal originalValue;
        BigDecimal newValue;
        try {
            originalValue = new BigDecimal(dataConfig.getString("playerData." + playerUUID + "." + dataPoint));
        }catch(Exception ex){
            plugin.debug(ex,null);
            originalValue = new BigDecimal("1");
        }

        BigDecimal output;
        switch(dataValue.charAt(0)){
            case '+':{
                newValue = new BigDecimal(dataValue.substring(1));
                output = originalValue.add(newValue);
                break;
            }
            case '-':{
                newValue = new BigDecimal(dataValue.substring(1));
                output = originalValue.subtract(newValue);
                break;
            }
            case '*':{
                newValue = new BigDecimal(dataValue.substring(1));
                output = originalValue.multiply(newValue);
                break;
            }
            case '/':{
                newValue = new BigDecimal(dataValue.substring(1));
                try {
                    output = originalValue.divide(newValue);
                }catch (ArithmeticException ex){
                    plugin.debug(ex,null);
                    output = originalValue;
                }
                break;
            }
            default:{
                newValue = new BigDecimal(dataValue);
                output = newValue;
            }
        }

        //if number is integer
        if(output.stripTrailingZeros().scale() <= 0){
            dataConfig.set("playerData." + playerUUID + "." + dataPoint, output.stripTrailingZeros().toPlainString());
            return;
        }

        dataConfig.set("playerData." + playerUUID + "." + dataPoint, output.toPlainString());
    }

    @SuppressWarnings("deprecation")
    public UUID getOffline(String playerName){
        //making this a separate function as it is long and deprecated
        return Bukkit.getOfflinePlayer(playerName).getUniqueId();
    }
}
