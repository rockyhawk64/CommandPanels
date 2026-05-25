![TitleLogo](https://commandpanels.net/resource_images/main_logo.png)

# 🏆 Trusted GUI Plugin since 2019

CommandPanels is a menu plugin built for server owners who want full control over the player experience. It is lightweight and focused on the newest Minecraft versions, keeping the plugin clean and avoiding the complexity that comes from supporting outdated game versions.

The idea behind it is simple: focus only on what matters and do it well. By keeping a tight scope, it stays fast, stable, and easy to maintain, while still including the advanced features you need to build anything from a basic kit menu to a complex system that tracks player data.

# Useful Links

### [**Documentation**](https://docs.commandpanels.net)

### [**Online Editor**](https://commandpanels.net/editor)

### [**Discord**](https://discord.gg/WFQMTZxa53)

# Online Editor

The online editor includes support for **every panel type available**.  
It’s not just a basic GUI builder, it’s a **live YAML editor** with structure checks and visual previews, helping you work faster and avoid YAML errors.

See a showcase of the editor [here](https://youtu.be/6m4KRHe1jkA?si=sDNiFU3PsJzPBKbB)!

![ExampleScreenshot](https://commandpanels.net/resource_images/example_editor.png)

# Real Logic & Real Data

CommandPanels lets you build menus that **react to players**, not just display static items.

Inline `$AND`, `$OR`, `$NOT` operators and grouping. Multiple items in a single slot with logical fallbacks. **Persistent** or **session-based data** usable anywhere, even in other plugins via PlaceholderAPI

**Example Condition:**
~~~
conditions: "$NOT (%player_name% $EQUALS Steve) $AND %vault_eco_balance% $ATLEAST 5000"
~~~

This allows for **powerful, dynamic behavior** without scripting.

# Inventory Panels
Create fully interactive GUIs for shops, kits, navigation menus, or custom tools.

![RawInventory](https://commandpanels.net/resource_images/raw_inventory.webp)

# Dialog Panels
Build structured, custom interfaces that can be used to request custom input.

![RawDialog](https://commandpanels.net/resource_images/raw_dialog.webp)

# Floodgate Panels
Bring **full GUI support to Bedrock players** using Geyser and Floodgate.
CommandPanels is one of the few plugins that natively supports this.

![RawFloodgate](https://commandpanels.net/resource_images/raw_floodgate.webp)

# About

CommandPanels is the industry-standard GUI framework for modern Minecraft server administrators.
Designed specifically for Paper and Folia, it enables you to create custom menus, interactive shops,
and advanced GUI dialogs with a streamlined, high-performance YAML scripting engine.

Why Server Owners Choose CommandPanels:
- Native Bedrock Integration: One of the few solutions offering full Floodgate GUI support and full dialog support,
bridging the gap between Java and Bedrock players seamlessly.
- Advanced Logic Engine: Build dynamic, reactive interfaces using $AND,
$OR, and $NOT operators. No complex scripting required.
- Workflow Efficiency: Reduce setup time with our integrated
Online GUI Editor—the most advanced visual menu builder for modern servers. 
- Data-Driven Performance: Native support for persistent player data, session variables,
and deep integration with PlaceholderAPI.
- Modern Architecture: Built for the future of Minecraft, ensuring stability
on the latest versions without legacy bloat.

Whether you are managing a Survival, MMO, or Custom Network,
CommandPanels provides the tools to build a professional, responsive server interface.

CommandPanels is fully compatible with **Paper** and **Folia** servers.