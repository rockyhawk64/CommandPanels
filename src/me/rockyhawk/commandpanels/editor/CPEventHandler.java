package me.rockyhawk.commandpanels.editor;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.api.PanelCommandEvent;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CPEventHandler implements Listener {
    CommandPanels plugin;
    public CPEventHandler(CommandPanels pl) { this.plugin = pl; }

    @EventHandler
    public void onCommandEventOpen(PanelCommandEvent e){
        if(!e.getMessage().startsWith("CommandPanels_")) {
            return;
        }
        if (e.getMessage().equals("CommandPanels_OpenPanelSettings")) {
            plugin.editorMain.openGuiPage("PanelEditMenu", e.getPlayer(), PanelPosition.Middle);
            plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).setMenuOpen("PanelEditMenu");
            return;
        }
        if (e.getMessage().equals("CommandPanels_OpenItemSettings")) {
            plugin.editorMain.openGuiPage("ItemEditMenu", e.getPlayer(), PanelPosition.Middle);
            plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).setMenuOpen("ItemEditMenu");
            return;
        }
        if (e.getMessage().equals("CommandPanels_EditorOpened")) {
            plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).hasEditorOpen = true;
            return;
        }
        if (e.getMessage().equals("CommandPanels_EditorClosed")) {
            plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).hasEditorOpen = false;
        }
    }

    void savePanelFile(Panel p){
        try {
            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(p.getFile());
            newConfig.set("panels." + p.getName(), p.getConfig());
            newConfig.save(p.getFile());
        } catch (Exception io) {
            plugin.debug(io,null);
        }
    }

    @EventHandler
    public void onCommandEventView(PanelCommandEvent e){
        if(!e.getMessage().startsWith("CPEditorItem_") && !e.getMessage().startsWith("CPEditorPanel_")){
            return;
        }

        EditorSettings editor = plugin.editorMain.settings.get(e.getPlayer().getUniqueId());

        Panel panel = null;
        for(Panel pnl : plugin.panelList) {
            if (pnl.getName().equals(plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).panelName)) {
                panel = pnl.copy();
                break;
            }
        }
        assert panel != null;

        if(e.getMessage().startsWith("CPEditorItem_")){
            String input = e.getMessage().replace("CPEditorItem_","");
            viewContents(e.getPlayer(), panel, "item." + editor.slotSelected + "." + input);
        }
        if(e.getMessage().startsWith("CPEditorPanel_")){
            String input = e.getMessage().replace("CPEditorPanel_","");
            viewContents(e.getPlayer() , panel, input);
        }
    }

    @EventHandler
    public void onCommandEventSettings(PanelCommandEvent e){
        if(!e.getMessage().startsWith("CPEditor_")) {
            return;
        }

        String editType = e.getMessage().split("\\s")[0].replace("CPEditor_","");
        String playerInput = e.getMessage().replace("CPEditor_" + editType + " ","");
        EditorSettings editor = plugin.editorMain.settings.get(e.getPlayer().getUniqueId());
        Panel panel = null;
        for(Panel pnl : plugin.panelList) {
            if (pnl.getName().equals(plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).panelName)) {
                panel = pnl.copy();
                break;
            }
        }
        assert panel != null;

        if(editType.startsWith("item")) {
            switch (editType) {
                case "itemslot":
                    plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).slotSelected = playerInput;
                    break;
                case "itemmaterial":
                    panel.getConfig().set("item." + editor.slotSelected + ".material", playerInput);
                    break;
                case "itemname":
                    panel.getConfig().set("item." + editor.slotSelected + ".name", playerInput);
                    break;
                case "itemstack":
                    panel.getConfig().set("item." + editor.slotSelected + ".stack", playerInput);
                    break;
                case "itemdelete":
                    if (playerInput.toLowerCase().startsWith("c")) {
                        panel.getConfig().set("item." + editor.slotSelected, null);
                    }
                    break;
                case "itemmove":
                    ConfigurationSection oldLocation = panel.getConfig().getConfigurationSection("item." + editor.slotSelected);
                    if (panel.getConfig().isSet("item." + playerInput)) {
                        ConfigurationSection newLocation = panel.getConfig().getConfigurationSection("item." + playerInput);
                        panel.getConfig().set("item." + editor.slotSelected, newLocation);
                    }
                    panel.getConfig().set("item." + playerInput, oldLocation);
                    break;
                case "itemmodeldata":
                    panel.getConfig().set("item." + editor.slotSelected + ".customdata", playerInput);
                    break;
                case "itemdurability":
                    panel.getConfig().set("item." + editor.slotSelected + ".damage", playerInput);
                    break;
                case "itemarmour":
                    panel.getConfig().set("item." + editor.slotSelected + ".leatherarmor", playerInput);
                    break;
                case "itemduplicate":
                    panel.getConfig().set("item." + editor.slotSelected + ".duplicate", playerInput);
                    break;
                case "itempotion":
                    panel.getConfig().set("item." + editor.slotSelected + ".potion", playerInput);
                    break;
                case "itemid":
                    panel.getConfig().set("item." + editor.slotSelected + ".ID", playerInput);
                    break;
                case "itemlore":
                    listChanger(playerInput, panel, "item." + editor.slotSelected + ".lore");
                    if (!panel.getConfig().isSet("item." + editor.slotSelected + ".name")) {
                        e.getPlayer().sendMessage(ChatColor.RED + "Your item needs to have a name for your lore to be visible!");
                    }
                    break;
                case "itemcommands":
                    listChanger(playerInput, panel, "item." + editor.slotSelected + ".commands");
                    break;
                case "iteminput":
                    listChanger(playerInput, panel, "item." + editor.slotSelected + ".player-input");
                    break;
                case "itemtypes":
                    listChanger(playerInput, panel, "item." + editor.slotSelected + ".itemType");
                    break;
                case "itemenchantment":
                    listChanger(playerInput, panel, "item." + editor.slotSelected + ".enchanted");
                    break;
                case "itemnbt":
                    if (playerInput.startsWith("add")) {
                        String[] str = playerInput.split("\\s", 3);
                        panel.getConfig().set("item." + editor.slotSelected + ".nbt." + str[1], str[2]);
                    } else if (playerInput.startsWith("remove")) {
                        String element = playerInput.split("\\s")[1];
                        panel.getConfig().set("item." + editor.slotSelected + ".nbt." + element, null);
                    }
                    break;
            }
        }else{
            switch (editType) {
                case "panelpermission":
                    panel.getConfig().set("perm", playerInput);
                    break;
                case "panelenabledworlds":
                    listChanger(playerInput, panel, "enabled-worlds");
                    break;
                case "paneldisabledworlds":
                    listChanger(playerInput, panel, "disabled-worlds");
                    break;
                case "panelopensound":
                    panel.getConfig().set("sound-on-open", playerInput);
                    break;
                case "panelemptyid":
                    panel.getConfig().set("emptyID", playerInput);
                    break;
                case "panelempty":
                    panel.getConfig().set("empty", playerInput);
                    break;
                case "paneltitle":
                    panel.getConfig().set("title", playerInput);
                    break;
                case "panelrows":
                    if(isNumeric(playerInput)){
                        panel.getConfig().set("rows", Integer.parseInt(playerInput));
                    }else {
                        panel.getConfig().set("rows", playerInput);
                    }
                    break;
                case "panelrefreshdelay":
                    panel.getConfig().set("refresh-delay", playerInput);
                    break;
                case "paneltype":
                    listChanger(playerInput, panel, "panelType");
                    break;
                case "panelcommands":
                    listChanger(playerInput, panel, "commands");
                    break;
                case "panelprecommands":
                    listChanger(playerInput, panel, "pre-load-commands");
                    break;
                case "panelopencommands":
                    listChanger(playerInput, panel, "commands-on-open");
                    break;
                case "panelclosecommands":
                    listChanger(playerInput, panel, "commands-on-close");
                    break;
                case "paneloutsidecommands":
                    listChanger(playerInput, panel, "outside-commands");
                    break;
                case "panelplayerinputmessage":
                    listChanger(playerInput, panel, "custom-messages.player-input");
                    break;
                case "panelmaxinputmessage":
                    panel.getConfig().set("custom-messages.input", playerInput);
                    break;
                case "panelpermissionmessage":
                    panel.getConfig().set("custom-messages.perm", playerInput);
                    break;
                case "paneldelete":
                    if (!playerInput.toLowerCase().startsWith("c")) {
                        break;
                    }
                    try {
                        //clear panel from file contents
                        YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(panel.getFile());
                        newConfig.set("panels." + panel.getName(), null);
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Panel deleted!");
                        if(newConfig.getKeys(true).size() == 1){
                            //file is empty
                            if(panel.getFile().delete()){
                                plugin.reloadPanelFiles();
                                return;
                            }
                        }
                        newConfig.save(panel.getFile());
                    } catch (Exception io) {
                        plugin.debug(io,e.getPlayer());
                    }
                    plugin.reloadPanelFiles();
                    return;
                case "panelname":
                    YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(panel.getFile());
                    newConfig.set("panels." + playerInput.split("\\s")[0], panel.getConfig());
                    newConfig.set("panels." + panel.getName(), null);
                    try {
                        newConfig.save(panel.getFile());
                    } catch (Exception io) {
                        plugin.debug(io,e.getPlayer());
                    }
                    plugin.reloadPanelFiles();
                    e.getPlayer().sendMessage(ChatColor.GREEN + "Panel name changed!");
                    return;
            }
        }
        savePanelFile(panel);

        //This will open the editor back up
        panel.open(e.getPlayer(),PanelPosition.Top);
        plugin.editorMain.openGuiPage(plugin.editorMain.settings.get(e.getPlayer().getUniqueId()).menuOpen,e.getPlayer(),PanelPosition.Middle);
        plugin.editorMain.openGuiPage("BottomSettings",e.getPlayer(),PanelPosition.Bottom);
    }

    /*
    this will allow for add, edit and remove settings
    add msg= test
    edit 1 msg= changed
    insert 1 msg= new line
    remove 1
    */
    public void listChanger(String playerInput, Panel panel, String location){
        List<String> contents = panel.getConfig().getStringList(location);
        if(playerInput.startsWith("add")){
            String str = playerInput.split("\\s", 2)[1];
            contents.add(str);
        }else if(playerInput.startsWith("edit")){
            List<String> str = new ArrayList<>(Arrays.asList(playerInput.split("\\s")));
            str.subList(0,2).clear();
            int element = Integer.parseInt(playerInput.split("\\s")[1])-1;
            contents.set(element,String.join(" ",str));
        }else if(playerInput.startsWith("insert")){
            List<String> str = new ArrayList<>(Arrays.asList(playerInput.split("\\s")));
            str.subList(0,2).clear();
            int element = Integer.parseInt(playerInput.split("\\s")[1])-1;
            contents.add(element,String.join(" ",str));
        }else if(playerInput.startsWith("remove")){
            int element = Integer.parseInt(playerInput.split("\\s")[1])-1;
            contents.remove(element);
        }
        if(contents.isEmpty()){
            panel.getConfig().set(location, null);
        }else {
            panel.getConfig().set(location, contents);
        }
    }

    public void viewContents(Player player, Panel panel, String location){
        if(panel.getConfig().isList(location)){
            player.sendMessage("Current Value: ");
            int n = 1;
            for(String value : panel.getConfig().getStringList(location)){
                player.sendMessage("(" + n + ") " + value);
                n++;
            }
        }else{
            player.sendMessage("Current Value: " + panel.getConfig().getString(location));
        }
    }

    //if a string is a number
    public boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
