package me.rockyhawk.commandpanels.configuration;

import me.rockyhawk.commandpanels.Context;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ConfigHandler {
    private final Context ctx;
    public ConfigHandler(Context pl) {
        ctx = pl;
        panelsFolder = new File(ctx.plugin.getDataFolder() + File.separator + "panels");
        onPluginLoad();
    }

    //Config files
    public YamlConfiguration config;
    public YamlConfiguration blockConfig; //where panel block locations are stored
    public final File panelsFolder;

    //Useful functions for other classes
    public boolean eqlNoCase(String path, String value){
        return config.getString(path).equalsIgnoreCase(value);
    }
    public boolean isTrue(String path){
        return config.getString(path).equalsIgnoreCase("true");
    }

    //this reads the encrypted resource files in the jar file
    public Reader getReaderFromStream(InputStream initialStream) throws IOException {
        byte[] buffer = IOUtils.toByteArray(initialStream);
        return new CharSequenceReader(new String(buffer));
    }

    public void onPluginLoad(){
        //register config files
        blockConfig = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder() + File.separator + "blocks.yml"));
        ctx.panelData.dataConfig = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder() + File.separator + "data.yml"));
        ctx.inventorySaver.inventoryConfig = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder() + File.separator + "inventories.yml"));
        config = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder() + File.separator + "config.yml"));

        // CONVERT data.yml OLD layout to NEW layout
        ctx.panelData.dataFileConverter.convertOldProfileLayout();

        //Initialise the config files on load
        File configFile = new File(ctx.plugin.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            //generate a new config file from internal resources
            try {
                FileConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("config.yml")));
                configFileConfiguration.save(configFile);
                config = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the config file!");
            }
        }else{
            //check if the config file has any missing elements
            try {
                YamlConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("config.yml")));
                config.addDefaults(configFileConfiguration);
                config.options().copyDefaults(true);
                config.save(new File(ctx.plugin.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var10) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the config file!");
            }
        }
        saveExamplePanels();
        
        // Initialize autosave system after config is loaded
        if (ctx.autoSave != null) {
            ctx.autoSave.initialize();
        }
    }

    private void saveExamplePanels(){
        //save the example files and the template.yml file
        if (!panelsFolder.exists()) {
            try {
                if(ctx.version.isBelow("1.13")){
                    FileConfiguration exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("exampleLegacy.yml")));
                    exampleFileConfiguration.save(new File(panelsFolder + File.separator + "example.yml"));
                }else {
                    //top
                    FileConfiguration exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("example_top.yml")));
                    exampleFileConfiguration.save(new File(panelsFolder + File.separator + "example_top.yml"));
                    //middle one
                    exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("example_middle_one.yml")));
                    exampleFileConfiguration.save(new File(panelsFolder + File.separator + "example_middle_one.yml"));
                    //middle two
                    exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("example_middle_two.yml")));
                    exampleFileConfiguration.save(new File(panelsFolder + File.separator + "example_middle_two.yml"));
                    //bottom
                    exampleFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("example_bottom.yml")));
                    exampleFileConfiguration.save(new File(panelsFolder + File.separator + "example_bottom.yml"));
                }
                FileConfiguration templateFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("template.yml")));
                templateFileConfiguration.save(new File(panelsFolder + File.separator + "template.yml"));
            } catch (IOException var11) {
                Bukkit.getConsoleSender().sendMessage("[CommandPanels]" + ChatColor.RED + " WARNING: Could not save the example file!");
            }
        }
    }
}
