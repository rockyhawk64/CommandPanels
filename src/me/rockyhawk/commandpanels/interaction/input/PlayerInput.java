package me.rockyhawk.commandpanels.interaction.input;

import me.rockyhawk.commandpanels.api.Panel;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class PlayerInput {
    public Panel panel;
    public ClickType click;
    public List<String> commands;
    public List<String> cancelCommands;

    public PlayerInput(Panel panel1, List<String> commands1, List<String> cancelCommands1, ClickType click1){
        panel = panel1;
        click = click1;
        commands = commands1;
        cancelCommands = cancelCommands1;
    }
}
