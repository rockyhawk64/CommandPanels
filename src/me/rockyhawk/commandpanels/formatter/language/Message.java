package me.rockyhawk.commandpanels.formatter.language;

public enum Message {
    PREFIX("[CommandPanels] "),

    // Command
    COMMAND_NO_PERMISSION("No permission."),
    COMMAND_UNKNOWN_SUBCOMMAND("Unknown subcommand. Use /pa help."),
    COMMAND_SUBCOMMAND_HELP("Use /pa help for a list of subcommands."),

    // Converter
    CONVERT_PANEL_CONFIG("Converts v3 -> v4 Panel config layouts"),
    CONVERT_OLD_DIR_NOT_FOUND("Old panels directory not found: {0}"),
    CONVERT_NO_PANELS_SECTION("No panels section in file: {0}"),
    CONVERT_FILE_FAILED("Failed to convert file: {0}"),
    CONVERT_NO_OLD_FILES("No old panel files found."),
    CONVERT_SUCCESS("Converted panel: {0} -> {1}"),

    // FileHandler / DataLoader / GenerateManager
    FILE_CREATE_PANELS_FAIL("Failed to create panels folder!"),
    FILE_CREATE_EXAMPLE_FAIL("Could not create example panels!"),
    FILE_CREATE_CONFIG_FAIL("Could not create the config file!"),
    FILE_UPDATE_CONFIG_FAIL("Could not update the config file!"),
    FILE_SAVE_DATA_FAIL("Could not save data file."),
    FILE_SAVE_PANEL_FAIL("Could not save new panel file."),

    // PanelBuilder / PanelFactory / OpenCommand
    PANEL_LAYOUT_NUMBER_MISSING("Panel layout is missing/skipping a number."),
    PANEL_NO_ITEMS("Panel must contain at least one item."),
    PANEL_INVALID_TYPE("Invalid inventory type."),
    PANEL_NOT_FOUND("Panel not found."),
    PANEL_OPEN_TRIGGERED("Panel open triggered for player."),
    PANEL_OPEN_LOG("{0} opened {1}."),
    PANEL_OPEN_USAGE("Usage: /pa open <panelName> [playerName]"),
    PANEL_OPEN_PLAYER_REQUIRED("You must be a player to open a panel for yourself."),
    PANEL_OPEN_PLAYER_OFFLINE("Player is not online."),

    // ItemBuilder / ItemActionTag / GiveTag / GrantTag
    ITEM_CREATE_FAIL("Failed to create item {0} issue with {1}"),
    ITEM_DECORATION_FAIL("Failed to add item decoration to {0} issue with {1}"),
    ITEM_HEAD_LOAD_FAIL("Head Material Tag: Could not load head: {0}"),
    ITEM_MODEL_INVALID("Invalid Item Model format. Must be namespace:key."),
    ITEM_CUSTOM_MODEL_INVALID("Invalid Custom Model Data. Must not be empty."),
    ITEM_GIVE_ERROR("Error giving item."),
    ITEM_GRANT_SYNTAX_INVALID("Invalid syntax. Usage: [grant] permission command"),

    // Item Action
    ITEM_ACTION_SYNTAX_INVALID("Invalid item action syntax. Usage: slot action [params]"),
    ITEM_ACTION_UNKNOWN("Unknown item action"),
    ITEM_ACTION_EXECUTE_FAIL("An error occurred while executing item action."),
    ITEM_ACTION_USAGE_IMPROPER("Improper usage."),

    // Item Enchant
    ITEM_ENCHANT_MISSING_ARGS("Missing enchantment name or level."),
    ITEM_ENCHANT_INVALID("Invalid enchantment"),
    ITEM_ENCHANT_REMOVE_MISSING("Missing enchantment to remove."),
    ITEM_ENCHANT_ACTION_UNKNOWN("Unknown enchant action."),

    // DataCommand
    DATA_USAGE("Usage: /pa data <action> <player> [key] [value]"),
    DATA_MISSING_KEY("Missing key."),
    DATA_MISSING_KEY_OR_VALUE("Missing key or value."),
    DATA_MISSING_KEY_OR_EXPRESSION("Missing key or expression."),
    DATA_UNKNOWN_ACTION("Unknown action."),
    DATA_PLAYER_REQUIRED("You must be a player."),

    DATA_VALUE("Value: {0}"),
    DATA_SET("Set key '{0}' to '{1}' (if not existing)."),
    DATA_OVERWRITE("Overwrote key '{0}' with '{1}'."),
    DATA_MATH("Performed math '{0}' on key '{1}'."),
    DATA_DELETE("Deleted key '{0}'."),
    DATA_CLEAR("Cleared all data for '{0}'."),

    // Requirement Tags
    REQUIREMENT_UNKNOWN_TAG("Unknown requirement tag."),
    REQUIREMENT_DATA_INVALID("Invalid data requirement. Use: [data] <key> <amount>"),
    REQUIREMENT_AMOUNT_INVALID("Invalid amount."),
    REQUIREMENT_DATA_READ_FAIL("Could not read data."),
    REQUIREMENT_MATERIAL_INVALID("Invalid material."),
    REQUIREMENT_MATERIAL_REQUIRED("Material is required for item requirement."),
    REQUIREMENT_ECONOMY_INVALID("Invalid economy value: {0}"),
    REQUIREMENT_XP_INVALID("Invalid XP requirement. Use: [xp] <levels|points> <amount>"),
    REQUIREMENT_XP_AMOUNT_INVALID("Invalid XP amount."),
    REQUIREMENT_XP_TYPE_INVALID("Invalid XP type."),
    REQUIREMENT_ITEM_INVALID("Invalid item."),
    REQUIREMENT_SOURCE_INVALID("Invalid source. Must be player or panel."),

    // SoundTag / StopSoundTag
    SOUND_NO_ARGS("No sound arguments provided."),
    SOUND_PLAY_FAIL("Failed to play sound."),
    SOUND_STOP_FAIL("Failed to stop sound."),

    // GenerateManager
    GENERATE_MODE_ENABLED("Generate mode enabled."),
    GENERATE_MODE_EXPIRED("Generate mode expired."),
    GENERATE_PANEL_CREATED("Generated a new panel file."),

    // HelpCommand / ReloadCommand
    PLUGIN_COMMANDS("Plugin Commands:"),
    PLUGIN_RELOADED("Plugin Reloaded."),
    PLUGIN_DEVELOPER("Developer {0}"),
    PLUGIN_VERSION("Version {0}"),

    // HelpCommand
    HELP_OPEN_COMMAND("/pa open <panel> [player] "),
    HELP_OPEN_DESCRIPTION("Opens a panel"),
    HELP_RELOAD_COMMAND("/pa reload "),
    HELP_RELOAD_DESCRIPTION("Reloads all config and panel files"),
    HELP_GENERATE_COMMAND("/pa generate "),
    HELP_GENERATE_DESCRIPTION("Enter generate mode to generate panels"),
    HELP_DATA_COMMAND("/pa data "),
    HELP_DATA_DESCRIPTION("Modify data for players"),
    HELP_VERSION_COMMAND("/pa version "),
    HELP_VERSION_DESCRIPTION("Gets the plugin version"),
    HELP_HELP_COMMAND("/pa help "),
    HELP_HELP_DESCRIPTION("Shows this help menu"),
    HELP_CONVERT_COMMAND("/pa convert "),
    HELP_CONVERT_DESCRIPTION("Converts basic layout from v3 to v4 panels (not plug and play)"),

    // Misc
    DIALOG_NO_BUTTONS("Dialog needs at least one button"),
    TELEPORT_ERROR("Error with teleport tag"),
    REQUIRE_HEADDATABASE("Download the HeadDatabase plugin to use this feature!");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String format(Object... args) {
        String result = message;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i] != null ? args[i] : "null"));
        }
        return result;
    }
}
