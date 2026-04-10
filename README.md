![TitleLogo](https://commandpanels.net/resource_images/main_logo.png)

## 🏆 Trusted GUI Plugin since 2019

CommandPanels is built for the admin who demands lean, total control over the player experience. It’s a high-performance interface builder that puts logic and data at the forefront.

By stripping away the bloat of traditional menu plugins, it allows you to design ultra stable, logic driven GUIs from simple kits to complex, data-heavy systems using a streamlined architecture that respects your server's resources.

---

## Useful Links

[**Documentation**](https://docs.commandpanels.net)

[**Online Editor**](https://commandpanels.net/editor)

[**Discord**](https://discord.gg/WFQMTZxa53)

---

## Online Editor

The online editor includes support for **every panel type available**.  
It’s not just a basic GUI builder, it’s a **live YAML editor** with structure checks and visual previews, helping you work faster and avoid YAML errors.

See a showcase of the editor [here](https://youtu.be/6m4KRHe1jkA?si=sDNiFU3PsJzPBKbB)!

![ExampleScreenshot](https://commandpanels.net/resource_images/example_editor.png)

---

## Real Logic & Real Data

CommandPanels lets you build menus that **react to players**, not just display static items.

Inline `$AND`, `$OR`, `$NOT` operators and grouping. Multiple items in a single slot with logical fallbacks. **Persistent** or **session-based data** usable anywhere, even in other plugins via PlaceholderAPI

**Example Condition:**
~~~
conditions: "$NOT (%player_name% $EQUALS Steve) $AND %vault_eco_balance% $ATLEAST 5000"
~~~

This allows for **powerful, dynamic behavior** without scripting.

---

## GUI Types

### Inventory Panels
Create fully interactive GUIs for shops, kits, navigation menus, or custom tools.

![RawInventory](https://commandpanels.net/resource_images/raw_inventory.webp)

---

### Dialog Panels
Build structured, custom interfaces that can be used to request custom input.

![RawDialog](https://commandpanels.net/resource_images/raw_dialog.webp)

---

### Floodgate Panels
Bring **full GUI support to Bedrock players** using Geyser and Floodgate.
CommandPanels is one of the only plugins that natively supports this.

![RawFloodgate](https://commandpanels.net/resource_images/raw_floodgate.webp)

---

## About

Minecraft servers rely on GUIs for everything from shops and lobbies to quests and server tools.  
CommandPanels provides a **streamlined YAML scripting format** designed to make GUI creation both **accessible** and **deeply customizable**.

- Inline logic & conditions
- Dynamic placeholders
- Persistent & session data
- Full PlaceholderAPI support
- Modern, clean codebase

---

## Trusted by Servers Worldwide

CommandPanels is fully compatible with **Paper** and **Folia** servers.

For over 6 years, CommandPanels has powered **thousands of Minecraft servers**.
Whether you're running survival, MMO, minigames, or custom networks, it can handle it.
