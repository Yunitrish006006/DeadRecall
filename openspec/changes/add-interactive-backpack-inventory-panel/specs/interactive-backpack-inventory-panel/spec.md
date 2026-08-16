## ADDED Requirements

### Requirement: Inventory backpack panel exposes real container slots

The system SHALL expose real side-panel menu slots when the vanilla player
inventory contains an ordinary Remnant backpack. The slots SHALL be backed by
that backpack's container
component. Players SHALL be able to use vanilla pickup, place, right-click
split, drag, Shift-click, number-key swap, throw, and pickup-all interactions.

#### Scenario: Player moves a stack through the side panel

- **WHEN** the player picks up a displayed backpack stack and places it in a
  player-inventory slot
- **THEN** the server removes it from the selected backpack component
- **AND** normal carried-stack and inventory synchronization updates the client

#### Scenario: Player drags across backpack slots

- **WHEN** the player performs vanilla quick-craft across active panel slots
- **THEN** the server distributes the carried stack using vanilla slot rules

#### Scenario: Player uses Shift-click

- **WHEN** the player Shift-clicks between an active backpack slot and a main
  inventory or hotbar slot
- **THEN** the stack moves to the opposite inventory when capacity permits

### Requirement: Selected backpack is validated server-side

The server SHALL validate every selected backpack before exposing mutable panel
slots. The client MAY select an ordinary backpack by hovering its
player-inventory slot, but the server SHALL accept a selection only while the active menu is the
player's own InventoryMenu and the bounded inventory index still contains an
ordinary Remnant backpack. Invalid or stale selection SHALL expose no mutable
backpack slots.

#### Scenario: Player selects a second backpack

- **WHEN** the player hovers another ordinary backpack in the open inventory
- **THEN** the client and server select that bounded inventory slot
- **AND** subsequent real-slot interactions target only that backpack

#### Scenario: Selected stack moves before click

- **WHEN** the selected inventory index no longer contains the validated
  backpack when an interaction arrives
- **THEN** no backpack component is mutated

### Requirement: Panel enforces capacity and nesting safety

The panel SHALL expose at most the selected backpack's current legitimate
configured capacity and SHALL apply the same portable-container insertion
policy as the standalone BackpackMenu. Inactive slots and forbidden nested
portable containers SHALL reject every insertion path.

#### Scenario: Capacity module adds a row

- **WHEN** the selected backpack has a valid capacity module
- **THEN** the corresponding nine additional panel slots become active

#### Scenario: Player attempts forbidden nesting

- **WHEN** any click, drag, swap, or Shift-click attempts to insert a restricted
  portable container into the backpack panel
- **THEN** the server rejects the insertion without item loss or duplication

### Requirement: Panel retains vanilla visual and accessibility behavior

The interactive panel SHALL use native item rendering, slot sprites, 18-pixel
spacing, tooltips, hover/focus behavior, translated text, narration, and integer
coordinates. It SHALL not encode text in textures.

#### Scenario: Player changes language or GUI scale

- **WHEN** the player uses English or Traditional Chinese at a supported GUI
  scale
- **THEN** the panel title, counters, slots, and tooltips remain legible and do
  not overlap the vanilla inventory
