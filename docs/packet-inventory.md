# Packet-Backed Inventory Panels

## Summary

CommandPanels now supports a packet-backed inventory backend for standard
row-based `type: inventory` panels. Instead of placing menu items into a real
Bukkit top inventory, the plugin can open a fake chest window and keep the
server authoritative over what the player sees and what clicks are accepted.

This is intended to reduce item movement, extraction, and dupe risk while
preserving existing panel behavior.

## Supported Scope

Phase 1 packet support is limited to chest-style layouts:

- `rows: 1` through `rows: 6`
- `type: inventory`

Unsupported inventory layouts still use the legacy Bukkit inventory backend in
`auto` mode.

If a panel forces `inventory-backend: packet` for an unsupported layout, the
panel fails closed instead of silently opening with the wrong backend.

## Backend Selection

Two levels of configuration control the backend:

Global config in `resource/config.yml`:

```yml
inventory-backend-default: packet
packet-inventory-debug: false
packet-close-on-invalid-click-threshold: 5
```

Per-panel override:

```yml
inventory-backend: auto
```

Valid panel values:

- `auto`: use packet for supported chest menus, legacy otherwise
- `packet`: force packet backend
- `legacy`: force Bukkit inventory backend

## Architecture

Main classes:

- `InventoryPanelService`
  Central entry point for opening, refreshing, closing, and routing clicks.
- `InventoryBackendResolver`
  Chooses `PACKET` or `LEGACY` for a panel.
- `PacketInventoryBackend`
  Opens fake chest windows, sends item snapshots, and tracks packet sessions.
- `PacketPanelSession`
  Stores the active packet-backed panel state for one player.
- `PacketInventoryListener`
  Intercepts incoming client inventory packets for active packet sessions.
- `InventoryPanelRenderer`
  Builds a backend-independent snapshot of the panel title, slots, and items.

The packet backend still uses the normal item builder and layout resolver, so
placeholders, conditional items, filler items, and animations continue to come
from the existing panel build pipeline.

## Session Lifecycle

When a packet-backed panel opens:

1. `InventoryPanelService` resolves the backend.
2. `InventoryPanelRenderer` builds an `InventoryRenderSnapshot`.
3. `PacketInventoryBackend` creates or refreshes a `PacketPanelSession`.
4. The backend sends:
   - `OPEN_WINDOW`
   - `WINDOW_ITEMS`
   - per-slot `SET_SLOT` updates

The session is keyed by player UUID and includes:

- panel reference
- synthetic window id
- current snapshot
- state id
- previously committed items
- invalid click counter

Sessions are closed on:

- explicit `[close]`
- client close packet
- panel replacement
- quit
- death
- teleport
- plugin disable
- repeated invalid packet interactions

## Click Handling

Packet-backed menus do not rely on Bukkit `InventoryClickEvent` for top-window
behavior. Instead, `PacketInventoryListener` intercepts:

- `CLICK_WINDOW`
- `CLOSE_WINDOW`
- `CREATIVE_INVENTORY_ACTION`

`InventoryPanelService.handlePacketClick(...)` validates:

- active session exists
- window id matches the current session
- state id matches when provided
- clicked slot is inside the fake menu
- click type is supported

Accepted chest click types currently map to:

- left click
- right click
- shift-left click
- shift-right click
- outside click

All top-slot interactions are treated as non-mutating. After a click is
processed, the backend resyncs the fake window if the same packet session is
still open. This keeps display-only items and no-action items non-movable.

## Close Behavior

Packet-backed menus must always close through `InventoryPanelService`, not by
calling `player.closeInventory()` directly.

Use:

```java
ctx.inventoryPanels.closeActiveView(player, InventoryCloseReason.CLIENT_CLOSE, true);
```

This ensures the packet session is cleared and the client receives a matching
close packet. The `[close]` command tag already uses this path.

If a stale session is not cleared, CommandPanels may incorrectly treat clicks in
another plugin's menu as packet interactions for the old panel.

## Refresh and Animation

Refreshes are backend-aware:

- packet sessions use diff-based packet updates where possible
- legacy sessions still write into the Bukkit inventory

Animation support is preserved by reading the currently displayed item state and
building the next frame before applying the update.

## Debugging

Enable:

```yml
packet-inventory-debug: true
```

Useful logs include:

- backend selection and open information
- queued packet sends
- observed packet sends
- incoming click and close packets
- invalid interaction warnings
- resync operations

Typical successful open flow:

1. `Opening packet-backed panel ...`
2. `Sending initial packet-backed window ...`
3. `QUEUE OPEN_WINDOW`
4. `QUEUE WINDOW_ITEMS`
5. `SEND OPEN_WINDOW`
6. `SEND WINDOW_ITEMS`

If you only see `QUEUE ...` logs but not `SEND ...`, the PacketEvents transport
path is the first place to inspect.

## Operational Notes

- Packet support is designed around Paper/Folia-style scheduling.
- Player-targeted opens are scheduled on the player scheduler.
- Initial packet-backed window sends also run on the player scheduler.
- The backend uses PacketEvents wrapper overloads, not raw `Object` sends.

That last point is important: PacketEvents must receive `PacketWrapper<?>`
instances so it can transform them into actual network packets.

## Current Limitations

- Chest-style row menus only
- No packet port yet for arbitrary `InventoryType` layouts
- Bottom inventory remains the real player inventory
- Packet behavior still depends on PacketEvents compatibility with the server
  runtime

## Developer Checklist

When changing inventory behavior, verify all of the following on a packet-backed
panel:

- open and close
- `[open]`, `[previous]`, `[refresh]`
- `/cp open`
- updater ticks
- permission observer refresh
- no-action item clicks
- outside clicks
- menu replacement by another plugin or another panel
- quit, death, teleport cleanup

If a change touches close logic or click routing, always confirm that the packet
session is removed once the player is no longer viewing the CommandPanels menu.
