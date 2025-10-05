package me.rockyhawk.commandpanels;

import me.rockyhawk.commandpanels.formatter.language.Message;
import me.rockyhawk.commandpanels.session.Panel;
import me.rockyhawk.commandpanels.session.dialog.DialogPanel;
import me.rockyhawk.commandpanels.session.floodgate.FloodgatePanel;
import me.rockyhawk.commandpanels.session.inventory.InventoryPanel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FileHandler {
    private final Context ctx;
    public YamlConfiguration config;

    public FileHandler(Context ctx) {
        this.ctx = ctx;
        updateConfigFiles();
        reloadPanels();
        createLangFile();
    }


    public String fileToName(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * Reloads all panels. Must be called from an async context to avoid blocking the main thread.
     */
    public void reloadPanels() {
        // Create panels folder and add example panels if not there
        if (!ctx.plugin.folder.exists()) {
            if (!ctx.plugin.folder.mkdirs()) {
                Bukkit.getGlobalRegionScheduler().run(ctx.plugin, task -> ctx.text.sendError(ctx.plugin.getServer().getConsoleSender(), Message.FILE_CREATE_PANELS_FAIL));
                return;
            }
            createExamplePanels();
        }

        // Load panels
        HashMap<String, Panel> panels = loadYamlFilesRecursively(ctx.plugin.folder);
        Bukkit.getGlobalRegionScheduler().run(ctx.plugin, task -> {
            ctx.plugin.panels.clear();
            ctx.plugin.panels.putAll(panels);
            ctx.panelCommand.populateCommands();
        });
    }

    private HashMap<String, Panel> loadYamlFilesRecursively(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return new HashMap<>();

        HashMap<String, Panel> loaded = new HashMap<>();
        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively enter subfolder
                loaded.putAll(loadYamlFilesRecursively(file));
            } else if (file.isFile() && (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) {
                // Load YAML config and put into panels map
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String panelType = config.getString("type", "inventory").toLowerCase();
                if (panelType.equals("inventory")) {
                    String panelName = fileToName(file);
                    loaded.put(panelName, new InventoryPanel(panelName, config));
                }
                if (panelType.equals("dialog")) {
                    String panelName = fileToName(file);
                    loaded.put(panelName, new DialogPanel(panelName, config));
                }
                if (panelType.equals("floodgate")) {
                    String panelName = fileToName(file);
                    loaded.put(panelName, new FloodgatePanel(panelName, config));
                }
            }
        }
        return loaded;
    }

    // Code for config files
    //this reads the encrypted resource files in the jar file
    private Reader getReaderFromStream(InputStream initialStream) throws IOException {
        if(initialStream == null) return new StringReader("Missing resource for this file!");
        // Read all bytes from the input stream
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;
        while ((bytesRead = initialStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();

        // Convert to String, then to Reader
        String content = buffer.toString(StandardCharsets.UTF_8);
        return new StringReader(content);
    }

    private void createExamplePanels() {
        //create the example panel files
        try {
            //inventory panel file
            FileConfiguration inventoryFile = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("inventory.yml")));
            inventoryFile.save(new File(ctx.plugin.folder, "inventory.yml"));
            //dialog panel file
            FileConfiguration dialogFile = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("dialog.yml")));
            dialogFile.save(new File(ctx.plugin.folder, "dialog.yml"));
            //floodgate simple panel file
            FileConfiguration floodgateSimpleFile = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("floodgate_simple.yml")));
            floodgateSimpleFile.save(new File(ctx.plugin.folder, "floodgate_simple.yml"));
            //floodgate custom panel file
            FileConfiguration floodgateCustomFile = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("floodgate_custom.yml")));
            floodgateCustomFile.save(new File(ctx.plugin.folder, "floodgate_custom.yml"));
        } catch (IOException | NullPointerException e) {
            Bukkit.getGlobalRegionScheduler().run(ctx.plugin, task -> ctx.text.sendError(ctx.plugin.getServer().getConsoleSender(), Message.FILE_CREATE_EXAMPLE_FAIL));
        }
    }

    // if lang file is missing add it back
    private void createLangFile() {
        File messagesFile = new File(ctx.plugin.getDataFolder(), "lang.yml");
        if (messagesFile.exists()) {
            // Return, lang file already exists
            return;
        }

        YamlConfiguration messagesYaml = Message.toYaml();
        try {
            messagesYaml.save(messagesFile);
        } catch (IOException ex) {
            Bukkit.getGlobalRegionScheduler().run(ctx.plugin,
                    task -> ctx.text.sendError(
                            ctx.plugin.getServer().getConsoleSender(),
                            Message.FILE_CREATE_LANG_FAIL
                    )
            );
        }
    }

    public void updateConfigFiles(){
        // Register config files
        config = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder(), "config.yml"));

        // Initialise the config files on load
        File configFile = new File(ctx.plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            // Generate a new config file from internal resources
            try {
                FileConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("config.yml")));
                configFileConfiguration.save(configFile);
                config = YamlConfiguration.loadConfiguration(new File(ctx.plugin.getDataFolder(), "config.yml"));
            } catch (IOException var11) {
                Bukkit.getGlobalRegionScheduler().run(ctx.plugin, task -> ctx.text.sendError(ctx.plugin.getServer().getConsoleSender(), Message.FILE_CREATE_CONFIG_FAIL));
            }
        }else{
            // Check if the config file has any missing elements
            try {
                YamlConfiguration configFileConfiguration = YamlConfiguration.loadConfiguration(getReaderFromStream(ctx.plugin.getResource("config.yml")));
                config.addDefaults(configFileConfiguration);
                config.options().copyDefaults(true);
                config.save(new File(ctx.plugin.getDataFolder() + File.separator + "config.yml"));
            } catch (IOException var10) {
                Bukkit.getGlobalRegionScheduler().run(ctx.plugin, task -> ctx.text.sendError(ctx.plugin.getServer().getConsoleSender(), Message.FILE_UPDATE_CONFIG_FAIL));
            }
        }
    }
}
