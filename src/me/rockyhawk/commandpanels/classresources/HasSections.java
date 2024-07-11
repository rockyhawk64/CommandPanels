package me.rockyhawk.commandpanels.classresources;

import me.rockyhawk.commandpanels.CommandPanels;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.openpanelsmanager.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class HasSections {
    CommandPanels plugin;
    public HasSections(CommandPanels pl) {
        plugin = pl;
    }

    public String hasSection(Panel panel, PanelPosition position, ConfigurationSection cf, Player p) {
        for (String setName : cf.getKeys(false)) {
            if (!cf.isConfigurationSection(setName)) continue;

            ConfigurationSection currentSection = cf.getConfigurationSection(setName);
            int numberOfConditions = currentSection.getKeys(false).size();

            Boolean currentBlockResult = null; // This will store the result of the current block (a set of conditions combined by AND or OR).
            String previousOperator = "AND"; // Default logical operator to start with.

            for (int a = 0; a < numberOfConditions; a++) {
                if (!currentSection.isSet("value" + a) || !currentSection.isSet("compare" + a)) {
                    continue;
                }

                String value = ChatColor.stripColor(plugin.tex.placeholders(panel, position, p, currentSection.getString("value" + a)));
                String compare = ChatColor.stripColor(plugin.tex.placeholders(panel, position, p, currentSection.getString("compare" + a)));

                String operator = "AND"; // Default operator for the current condition.
                if (compare.endsWith(" OR")) {
                    compare = compare.substring(0, compare.length() - 3);
                    operator = "OR";
                } else if (compare.endsWith(" AND")) {
                    compare = compare.substring(0, compare.length() - 4);
                }

                HashSet<String> values = doOperators(new HashSet<>(Collections.singletonList(value)));
                boolean localResult = false; // This tracks the result of the current condition.
                for (String val : values) {
                    if (hasProcess(setName, val, compare, p)) {
                        localResult = true;
                        break;
                    }
                }

                if (currentBlockResult == null) {
                    // Initialize the result of the block with the result of the first condition.
                    currentBlockResult = localResult;
                } else {
                    // Combine the result of the current condition with the block result based on the previous operator.
                    if (previousOperator.equals("AND")) {
                        currentBlockResult = currentBlockResult && localResult;
                    } else if (previousOperator.equals("OR")) {
                        currentBlockResult = currentBlockResult || localResult;
                    }
                }

                previousOperator = operator; // Update the operator for the next condition.
            }

            if (currentBlockResult != null && currentBlockResult) {
                // If the result of this section is true, check nested sections.
                return "." + setName + hasSection(panel, position, currentSection, p);
            }
            // If the result is false, continue to the next 'has' section.
        }
        return "";
    }


    private HashSet<String> doOperators(HashSet<String> value){
        for(String val : value){
            if(val.contains(" OR ")){
                value.remove(val);
                value.addAll(Arrays.asList(val.split(" OR ")));
                return doOperators(value);
            }
        }
        return value;
    }

    private boolean hasProcess(String setName, String value, String compare,Player p){
        //check to see if the value should be reversed
        boolean outputValue = true;
        if(value.startsWith("NOT ")){
            value = value.substring(4);
            outputValue = false;
        }

        //the current has section with all the functions implemented inside it
        if(setName.startsWith("has")) {
            if(value.endsWith(" HASPERM")) {
                String playername = value.substring(0, value.length()-8);
                Player player = Bukkit.getPlayerExact(playername);
                if(player != null){
                    return player.hasPermission(compare) == outputValue;
                }
            }else if(value.endsWith(" ISGREATER")) {
                return (new BigDecimal(compare).compareTo(new BigDecimal(value.substring(0, value.length()-10).replace(",",""))) <= 0 == outputValue);
            }else{
                return compare.equals(value) == outputValue;
            }
        }
        return false;
    }
}
