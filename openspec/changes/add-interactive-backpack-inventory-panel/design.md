## Context

The current client Mixin reads `DataComponents.CONTAINER` and draws item copies.
InventoryScreen's `InventoryMenu` has no corresponding backpack slots, so normal
container input cannot address the rendered cells.

## Goals / Non-Goals

- Goals: full vanilla slot interaction, server validation, bounded overhead,
  multi-backpack selection, and native visual/accessibility behavior.
- Non-Goals: editing death backpacks from the side panel, remote/container
  inventories, custom slot textures, or a second prediction protocol.

## Decisions

### Real slots in InventoryMenu

A common Mixin appends a fixed legitimate-capacity set of virtual backpack
slots to `InventoryMenu` on both client and server. Their backing container
tracks one ordinary Remnant backpack in the player's own inventory and writes
changes through `ItemContainerContents`. Inactive capacity slots report
`isActive == false` and reject insertion.

Because the slots belong to the real menu, Minecraft's existing click packets,
carried-stack synchronization, quick-craft stages, keyboard swaps, pickup-all,
and narration remain authoritative instead of being reimplemented in a custom
client protocol.

### Selected backpack synchronization

The client retains the current behavior of selecting an ordinary backpack when
its player-inventory slot is hovered. It updates its local dynamic container and
sends one bounded inventory-slot payload when the selection changes. The server
accepts the payload only while the player's active container is their own
InventoryMenu and only when the target slot still contains an ordinary Remnant
backpack. Network ordering ensures selection reaches the server before the
subsequent vanilla click packet.

### Quick movement and safety

InventoryMenu quick-move handling is extended only when one endpoint is an
active backpack panel slot. Shift-click from the backpack targets main inventory
and hotbar; Shift-click from those player slots first targets the selected
backpack. All insertion paths call the existing portable-container policy, and
the selected backpack itself cannot be nested.

### Rendering

The client panel continues using native slot sprites, 18-pixel spacing, integer
placement, translated titles, item tooltips, and vanilla hover/focus handling.
The custom Mixin draws only the panel frame and empty slot sprites; the normal
container screen renders and narrates real slot contents.

## Risks / Trade-offs

- Appending inactive slots increases InventoryMenu synchronization width.
  - Mitigation: cap it at the maximum legitimate configured capacity (72) and
    perform no per-tick inventory-wide or nested-container scans.
- Selection can become stale after inventory movement.
  - Mitigation: validate the inventory index and item type for every selection
    and make stale virtual slots inactive before mutation.
- Other InventoryMenu Mixins could append slots.
  - Mitigation: capture the actual appended start index at construction rather
    than relying on a hard-coded total slot count.

## Migration Plan

No persistent migration is required. Backpack contents remain the same vanilla
container component and the existing right-click BackpackMenu remains available
as a fallback and for upgrades/crafting.

## Open Questions

- None. The user approved direct side-panel interaction on 2026-08-16.
