package me.rockyhawk.commandpanels.commandtags.tags.other;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.commandtags.CommandTagEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DataTags implements Listener {
    CommandPanels plugin;
    public DataTags(CommandPanels pl) {
        this.plugin = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("set-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                plugin.panelData.setUserData(plugin.panelData.getOffline(plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[2])),
                        plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                        plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),true);
                return;
            }
            //this will overwrite data. set-data= [data point] [data value] [optional player]
            plugin.panelData.setUserData(e.p.getUniqueId(),
                    plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                    plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),true);
            return;
        }
        if(e.name.equalsIgnoreCase("add-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                plugin.panelData.setUserData(plugin.panelData.getOffline(plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[2])),
                        plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                        plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),false);
                return;
            }
            //this will not overwrite existing data. add-data= [data point] [data value] [optional player]
            plugin.panelData.setUserData(e.p.getUniqueId(),
                    plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                    plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),false);
            return;
        }
        if(e.name.equalsIgnoreCase("math-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                plugin.panelData.doDataMath(plugin.panelData.getOffline(plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[2])),
                        plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                        plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]));
                return;
            }
            //only works if data is number, goes math-data= [data point] [operator:number] [optional player] eg, math-data= -1 OR /3
            plugin.panelData.doDataMath(e.p.getUniqueId(),
                    plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                    plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]));
            return;
        }
        if(e.name.equalsIgnoreCase("clear-data=")){
            e.commandTagUsed();
            //will clear all data for player clear-data= [playerName]
            plugin.panelData.clearData(plugin.panelData.getOffline(plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0])));
            return;
        }
        if(e.name.equalsIgnoreCase("del-data=")){
            e.commandTagUsed();
            if(e.args.length == 2){
                plugin.panelData.delUserData(plugin.panelData.getOffline(plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1])),
                        plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]));
                return;
            }
            //this will remove data. del-data= [data point] [optional player]
            plugin.panelData.delUserData(e.p.getUniqueId(),plugin.tex.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]));
        }
    }
}
