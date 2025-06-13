package me.rockyhawk.commandpanels.items;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;

public class HasSections {
    Context ctx;
    public HasSections(Context pl) {
        ctx = pl;
    }

    public String hasSection(Panel panel, PanelPosition position, ConfigurationSection cf, Player p) {
        // Use a TreeMap to automatically sort the sections by the extracted number.
        Map<Integer, String> sortedSections = new TreeMap<>();

        // Loop through the section names and filter for the ones starting with "has".
        for (String key : cf.getKeys(false)) {
            if (!cf.isConfigurationSection(key)) continue;

            // Check if the section starts with "has" and is followed by a number.
            if (key.startsWith("has")) {
                try {
                    // Extract the number after "has" and put it in the map for sorting.
                    int number = Integer.parseInt(key.substring(3));
                    sortedSections.put(number, key);
                } catch (NumberFormatException ignore) {
                    // If the section name doesn't have a valid number after "has", skip it.
                }
            }
        }

        for (String hasSection : sortedSections.values()) {
            if (!cf.isConfigurationSection(hasSection)) continue;

            ConfigurationSection currentSection = cf.getConfigurationSection(hasSection);
            int numberOfConditions = currentSection.getKeys(false).size();

            Boolean currentBlockResult = null; // This will store the result of the current block (a set of conditions combined by AND or OR).
            String previousOperator = "AND"; // Default logical operator to start with.

            for (int a = 0; a < numberOfConditions; a++) {
                if (!currentSection.isSet("value" + a) || !currentSection.isSet("compare" + a)) {
                    continue;
                }

                String value = ChatColor.stripColor(ctx.text.placeholders(panel, position, p, currentSection.getString("value" + a)));
                String compare = ChatColor.stripColor(ctx.text.placeholders(panel, position, p, currentSection.getString("compare" + a)));

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
                    if (hasProcess(val, compare)) {
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
                return "." + hasSection + hasSection(panel, position, currentSection, p);
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

    private boolean hasProcess(String value, String compare){
        //check to see if the value should be reversed
        boolean outputValue = true;
        if(value.startsWith("NOT ")){
            value = value.substring(4);
            outputValue = false;
        }

        //the current has section with all the functions implemented inside it
        if(value.endsWith(" HASPERM")) {
            String playername = value.substring(0, value.length()-8);
            Player player = Bukkit.getPlayerExact(playername);
            if(player != null){
                return player.hasPermission(compare) == outputValue;
            }
        }else if (value.endsWith(" ISGREATER")) {
            String numericPart = value.replaceAll("[^0-9.\\-]", ""); // allows negative and decimal numbers
            BigDecimal target = new BigDecimal(numericPart.replace(",", ""));
            return (new BigDecimal(compare).compareTo(target) <= 0) == outputValue;
        }else{
            return compare.equals(value) == outputValue;
        }
        return false;
    }
}
