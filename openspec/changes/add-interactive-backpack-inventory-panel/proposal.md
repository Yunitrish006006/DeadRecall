## Why

Remnant currently renders backpack contents beside the vanilla inventory but
the panel is a read-only copy of the item container component. It looks like a
container and shows hover feedback, yet players cannot take or place items.

## What Changes

- Add real server-authoritative backpack slots to the vanilla Inventory menu
  while Remnant is installed.
- Let the side panel use vanilla pickup, placement, drag, number-key swap,
  double-click, throw, and Shift-click behavior.
- Keep multi-backpack hover selection and synchronize the selected inventory
  slot before an interaction.
- Reuse the existing portable-container nesting policy and legitimate maximum
  backpack capacity.

## Impact

- Affected specs: `interactive-backpack-inventory-panel` (new capability).
- Affected code: Remnant InventoryMenu/client screen Mixins, a dynamic backpack
  container view, selection payload registration, and tests.
- Visual impact: the existing vanilla-styled side panel becomes interactive;
  no new texture or baked text is introduced.
