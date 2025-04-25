package me.rockyhawk.commandpanels.items.builder.materialcomponents;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.items.builder.MaterialComponent;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;
import java.util.stream.Collectors;

public class BookComponent implements MaterialComponent {

    @Override
    public boolean matches(String tag) {
        return tag.toLowerCase().startsWith("book=");
    }

    @Override
    public ItemStack createItem(String tag, Player player, Context ctx, ConfigurationSection section, Panel panel, PanelPosition pos) {
        ItemStack s = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) s.getItemMeta();
        bookMeta.setTitle(tag.split("\\s")[1]);
        bookMeta.setAuthor(tag.split("\\s")[1]);
        List<String> bookLines = ctx.text.placeholdersList(panel ,pos ,player,section.getStringList("write"));
        String result = bookLines.stream().map(String::valueOf).collect(Collectors.joining("\n" + ChatColor.RESET, "", ""));
        bookMeta.setPages(result);
        s.setItemMeta(bookMeta);
        return s;
    }
}
