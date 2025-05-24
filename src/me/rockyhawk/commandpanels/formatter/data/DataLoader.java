package me.rockyhawk.commandpanels.formatter.data;

import me.rockyhawk.commandpanels.Context;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class DataLoader {
    Context ctx;
    public DataLoader(Context pl) {
        this.ctx = pl;
    }

    public YamlConfiguration dataConfig;
    public DataManager dataPlayers = new DataManager(this);
    public DataFileConverter dataFileConverter = new DataFileConverter(this);

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

    public void saveDataFile(){
        try {
            dataConfig.save(ctx.plugin.getDataFolder() + File.separator + "data.yml");
        } catch (IOException s) {
            s.printStackTrace();
            ctx.debug.send(s,null, ctx);
        }
    }

    public void doDataMath(String playerName, String dataPoint, String dataValue){
        String profile = dataPlayers.getDataProfile(playerName);
        BigDecimal originalValue;
        BigDecimal newValue;
        try {
            originalValue = new BigDecimal(dataConfig.getString(profile + ".data." + dataPoint));
        }catch(Exception ex){
            ctx.debug.send(ex,null, ctx);
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
                    ctx.debug.send(ex,null, ctx);
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
            dataConfig.set(profile + ".data." + dataPoint, output.stripTrailingZeros().toPlainString());
            return;
        }

        dataConfig.set(profile + ".data." + dataPoint, output.toPlainString());
    }
}