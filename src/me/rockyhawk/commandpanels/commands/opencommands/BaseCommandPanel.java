package me.rockyhawk.commandpanels.commands.opencommands;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseCommandPanel extends Command {

    private final Context ctx;
    private final Panel panel;

    public BaseCommandPanel(Context ctx, Panel panel, String name, List<String> aliases) {
        super(name);
        this.ctx = ctx;
        this.panel = panel;
        setAliases(aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            panel.copy().open(player, PanelPosition.Top);
            return true;
        }

        List<String> args = Arrays.asList(strings);
        List<String[]> placeholders = new ArrayList<>();
        String[] phEnds = ctx.placeholders.getPlaceholderEnds(panel,true);

        for(int i = 0; i < args.size(); i++){
            if(args.get(i).startsWith(phEnds[0])){
                placeholders.add(new String[]{args.get(i).replace(phEnds[0],"").replace(phEnds[1],""), args.get(i+1)});
                i++;
            }else if(!args.get(i).equals(args.get(i+1))){
                return false;
            }
        }

        Panel openPanel = panel.copy();
        for(String[] placeholder : placeholders){
            openPanel.placeholders.addPlaceholder(placeholder[0],placeholder[1]);
        }

        openPanel.open(player,PanelPosition.Top);
        return true;
    }
}
