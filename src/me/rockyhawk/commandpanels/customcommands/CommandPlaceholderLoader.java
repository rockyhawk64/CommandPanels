package me.rockyhawk.commandpanels.customcommands;

import me.rockyhawk.commandpanels.CommandPanels;
import java.util.ArrayList;

public class CommandPlaceholderLoader {
    CommandPanels plugin;
    public CommandPlaceholderLoader(CommandPanels pl) {
        this.plugin = pl;
    }

    ArrayList<String[]> pendingPlaceholders = new ArrayList<>(); //should read: panelName, playerName, placeholder, argument

    //this will be used with the cpPlaceholder void, when the panel is closed it will remove the placeholder
    public void addCCP(String panelName, String playerName, String placeholder, String argument){
        pendingPlaceholders.add(new String[]{panelName,playerName,placeholder,argument});
    }

    //will remove all pending placeholders for a certain player & panel
    public void removeCCP(String panelName, String playerName){
        for(int i = 0; i < pendingPlaceholders.size(); i++){
            if(playerName.equals(pendingPlaceholders.get(i)[1]) && panelName.equals(pendingPlaceholders.get(i)[0])){
                pendingPlaceholders.remove(i);
                i--;
            }
        }
    }

    //will return placeholder,argument
    public ArrayList<String[]> getCCP(String playerName){
        ArrayList<String[]> returnPlaceholders = new ArrayList<>();
        for(String[] temp : pendingPlaceholders){
            if(temp[1].equals(playerName)){
                returnPlaceholders.add(new String[]{temp[2],temp[3]});
            }
        }
        return returnPlaceholders;
    }
}
