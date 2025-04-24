package me.rockyhawk.commandpanels.interaction.commands.tags.other;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.interaction.commands.CommandTagEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DataTags implements Listener {
    Context ctx;
    public DataTags(Context pl) {
        this.ctx = pl;
    }

    @EventHandler
    public void commandTag(CommandTagEvent e){
        if(e.name.equalsIgnoreCase("set-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                ctx.panelData.setUserData(ctx.panelDataPlayers.getOffline(ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[2])),
                        ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                        ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),true);
                return;
            }
            //this will overwrite data. set-data= [data point] [data value] [optional player]
            ctx.panelData.setUserData(e.p.getUniqueId(),
                    ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                    ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),true);
            return;
        }
        if(e.name.equalsIgnoreCase("add-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                ctx.panelData.setUserData(ctx.panelDataPlayers.getOffline(ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[2])),
                        ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                        ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),false);
                return;
            }
            //this will not overwrite existing data. add-data= [data point] [data value] [optional player]
            ctx.panelData.setUserData(e.p.getUniqueId(),
                    ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                    ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]),false);
            return;
        }
        if(e.name.equalsIgnoreCase("math-data=")){
            e.commandTagUsed();
            if(e.args.length == 3){
                ctx.panelData.doDataMath(ctx.panelDataPlayers.getOffline(ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[2])),
                        ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                        ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]));
                return;
            }
            //only works if data is number, goes math-data= [data point] [operator:number] [optional player] eg, math-data= -1 OR /3
            ctx.panelData.doDataMath(e.p.getUniqueId(),
                    ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]),
                    ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1]));
            return;
        }
        if(e.name.equalsIgnoreCase("clear-data=")){
            e.commandTagUsed();
            //will clear all data for player clear-data= [playerName]
            ctx.panelData.clearData(ctx.panelDataPlayers.getOffline(ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0])));
            return;
        }
        if(e.name.equalsIgnoreCase("del-data=")){
            e.commandTagUsed();
            if(e.args.length == 2){
                ctx.panelData.delUserData(ctx.panelDataPlayers.getOffline(ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[1])),
                        ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]));
                return;
            }
            //this will remove data. del-data= [data point] [optional player]
            ctx.panelData.delUserData(e.p.getUniqueId(), ctx.text.placeholdersNoColour(e.panel,e.pos,e.p,e.args[0]));
        }
    }
}
