package me.rockyhawk.commandpanels.formatter.data;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.formatter.language.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class DataLoader {
    Context ctx;
    public DataLoader(Context pl) {
        this.ctx = pl;
        this.dataConfig = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder(), "data.yml"));
    }

    public YamlConfiguration dataConfig;
    public DataManager dataPlayers = new DataManager(this);

    public String getUserData(String playerName, String dataPoint){
        return dataConfig.getString(dataPlayers.getDataProfile(playerName) + ".data." + dataPoint);
    }

    public void setUserData(String playerName, String dataPoint, String dataValue, boolean overwrite){
        //if it exists no overwriting
        String profile = dataPlayers.getDataProfile(playerName);
        if(!overwrite && dataConfig.isSet(profile + ".data." + dataPoint)){
            return;
        }
        dataConfig.set(profile + ".data." + dataPoint, dataValue);
    }

    public void delUserData(String playerName, String dataPoint){
        String profile = dataPlayers.getDataProfile(playerName);
        dataConfig.set(profile + ".data." + dataPoint, null);
    }

    public void clearData(String playerName){
        String profile = dataPlayers.getDataProfile(playerName);
        dataConfig.set(profile, null);
    }

    public void saveDataFileSync() {
        try {
            File file = new File(ctx.plugin.getDataFolder(), "data.yml");
            dataConfig.save(file);
        } catch (IOException e) {
            Bukkit.getGlobalRegionScheduler().run(ctx.plugin, task ->
                    ctx.text.sendError(Bukkit.getConsoleSender(), Message.FILE_SAVE_DATA_FAIL));
        }
    }
    public void saveDataFileAsync() {
        Bukkit.getAsyncScheduler().runNow(ctx.plugin, task -> saveDataFileSync());
    }

    public void doDataMath(String playerName, String dataPoint, String dataValue){
        String profile = dataPlayers.getDataProfile(playerName);
        BigDecimal originalValue;
        BigDecimal newValue;
        try {
            originalValue = new BigDecimal(dataConfig.getString(profile + ".data." + dataPoint));
        }catch(Exception ex){
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
                    output = originalValue;
                }
                break;
            }
            default:{
                output = originalValue;
            }
        }

        //if number is integer
        if(output.stripTrailingZeros().scale() <= 0){
            dataConfig.set(profile + ".data." + dataPoint, output.stripTrailingZeros().toPlainString());
            return;
        }

        dataConfig.set(profile + ".data." + dataPoint, output.toPlainString());
    }
}
