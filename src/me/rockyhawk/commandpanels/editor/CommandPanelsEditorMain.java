package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandPanelsEditorMain {
    CommandPanels plugin;
    public CommandPanelsEditorMain(CommandPanels pl) {
        this.plugin = pl;
    }
    public Map<UUID, EditorSettings> settings = new HashMap<>();

    public void openGuiPage(String fileName, Player p, PanelPosition position){
        try {
            Panel panel = new Panel(YamlConfiguration.loadConfiguration(plugin.getReaderFromStream(plugin.getResource(fileName + ".yml"))), fileName);

            panel.placeholders.addPlaceholder("panel-name",settings.get(p.getUniqueId()).panelName);
            panel.placeholders.addPlaceholder("item-slot",settings.get(p.getUniqueId()).slotSelected);

            panel.open(p, position);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
