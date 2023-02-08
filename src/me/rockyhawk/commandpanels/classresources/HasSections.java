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

    public String hasSection(Panel panel, PanelPosition position, ConfigurationSection cf, Player p){
        for (int count = 0; cf.getKeys(false).size() > count; count++) {
            String setName;
            if(cf.isSet("has" + count)) {
                setName = "has" + count;
            }else{
                continue;
            }

            boolean endProcess = true;
            //loop through possible values and compares for hypothetical and operators
            for (int a = 0; cf.getConfigurationSection(setName).getKeys(false).size() > a; a++) {
                if(cf.isSet(setName + ".value" + a) && cf.isSet(setName + ".compare" + a)){
                    //ensure the endProcess variable has been reset for another operation
                    endProcess = true;
                    //get the values of this statement
                    String value = ChatColor.stripColor(plugin.tex.placeholders(panel, position, p, cf.getString(setName + ".value" + a)));
                    String compare = ChatColor.stripColor(plugin.tex.placeholders(panel, position, p, cf.getString(setName + ".compare" + a)));

                    String operator = "AND";
                    if(compare.endsWith(" OR")){
                        compare = compare.substring(0, compare.length()-3);
                        operator = "OR";
                    }else if(compare.endsWith(" AND")){
                        compare = compare.substring(0, compare.length()-4);
                    }

                    //list of values with the or operator
                    HashSet<String> values = doOperators(new HashSet<>(Collections.singletonList(value)));
                    //go through all values with the or operator
                    for(String val : values){
                        if (hasProcess(setName, val, compare, p)) {
                            endProcess = false;
                            //if it is true and it is OR, there is no need to check the next value in the line
                            if(operator.equals("OR")){
                                a++;
                            }
                        }
                    }
                    if(endProcess){
                        //check if the operator link between the next value/compare is OR
                        if(operator.equals("OR")){
                            //I can just continue because the algorithm already assumes the last sequence was true
                            endProcess = false;
                            continue;
                        }
                        break;
                    }
                }
            }
            //if the has section is false move to the next has section
            if(endProcess){
                continue;
            }
            //proceed if none of the values were false
            return "." + setName + hasSection(panel, position, cf.getConfigurationSection(setName), p);
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

        //the original has sections as TinyTank800 wanted to keep them
        if(setName.startsWith("hasvalue")) {
            return compare.equals(value) == outputValue;
        }
        if(setName.startsWith("hasperm")) {
            return p.hasPermission(value) == outputValue;
        }
        if(setName.startsWith("hasgreater")) {
            return (Long.parseLong(compare) >= Long.parseLong(value)) == outputValue;
        }
        //the current has section with all the functions implemented inside it
        if(setName.startsWith("has")) {
            if(value.endsWith(" HASPERM")) {
                return Bukkit.getPlayer(value.substring(0, value.length()-8)).hasPermission(compare) == outputValue;
            }else if(value.endsWith(" ISGREATER")) {
                return (new BigDecimal(compare).compareTo(new BigDecimal(value.substring(0, value.length()-10))) <= 0 == outputValue);
            }else{
                return compare.equals(value) == outputValue;
            }
        }
        return false;
    }
}
