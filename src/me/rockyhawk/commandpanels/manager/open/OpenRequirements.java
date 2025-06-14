package me.rockyhawk.commandpanels.manager.open;

import me.rockyhawk.commandpanels.Context;
import me.rockyhawk.commandpanels.api.Panel;
import me.rockyhawk.commandpanels.manager.session.PanelPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class OpenRequirements {
    private final Context ctx;

    public OpenRequirements(Context ctx) {
        this.ctx = ctx;
    }

    // Store the last matched section for fail message purposes
    private String lastMatchedSectionForFailMessage = "";

    /**
     * Validates if a player can open a panel based on open-requirements section
     * Returns true if player can open the panel, false otherwise
     */
    public boolean canOpenPanel(Panel panel, Player player, PanelPosition position) {
        ConfigurationSection config = panel.getConfig();
        
        // If no open-requirements section exists, allow opening (default behavior)
        if (!config.contains("open-requirements")) {
            return true;
        }
        
        ConfigurationSection openRequirements = config.getConfigurationSection("open-requirements");
        if (openRequirements == null) {
            return true;
        }
        
        // Default can-open value is false as requested
        boolean defaultCanOpen = openRequirements.getBoolean("can-open", false);
        
        // Use the existing hasSection method - it will automatically skip non-has keys like can-open and message
        String matchedSection = ctx.has.hasSection(panel, position, openRequirements, player);
        boolean hasMatch = !matchedSection.isEmpty();
        
        if (hasMatch) {
            // Extract the section name from the result (remove the leading dot)
            String sectionName = matchedSection.startsWith(".") ? matchedSection.substring(1) : matchedSection;
            this.lastMatchedSectionForFailMessage = sectionName;
            
            // Get the can-open value from the matched section
            boolean sectionCanOpen = openRequirements.getConfigurationSection(sectionName).getBoolean("can-open", false);
            return sectionCanOpen;
        }
        
        // No sections matched, clear the stored section and return default
        this.lastMatchedSectionForFailMessage = "";
        return defaultCanOpen;
    }
    
    /**
     * Gets the error message for when a player cannot open a panel
     */
    public String getOpenRequirementFailMessage(Panel panel) {
        ConfigurationSection config = panel.getConfig();
        
        // Check if we have a matched section with a custom message
        if (!lastMatchedSectionForFailMessage.isEmpty()) {
            String sectionPath = "open-requirements." + lastMatchedSectionForFailMessage;
            
            // Check if this section has a message
            if (config.contains(sectionPath + ".message")) {
                String sectionMessage = config.getString(sectionPath + ".message");
                return sectionMessage;
            }
        }
        
        // Fall back to default message
        if (config.contains("open-requirements.message")) {
            String defaultMessage = config.getString("open-requirements.message");
            return defaultMessage;
        }
        
        // Use the same format as permission errors
        String customMessage = panel.getConfig().getString("custom-messages.perms");
        if (customMessage != null) {
            return customMessage;
        }
        
        return ctx.configHandler.config.getString("config.format.perms");
    }
} 