# Remnant Item ID Migration Specification

## ADDED Requirements

### Requirement: Remnant exposes canonical item IDs

The system SHALL register each Remnant backpack under its canonical
`totem:remnant/*` item ID and SHALL use canonical items for every new
acquisition path.

#### Scenario: Craft a new tiered backpack

- **GIVEN** a player uses a supported backpack smithing recipe
- **WHEN** the recipe result is assembled
- **THEN** the output item has the corresponding `totem:remnant/*` ID

#### Scenario: Capture a new death backpack

- **GIVEN** Remnant captures a player's death inventory
- **WHEN** the death backpack ItemStack is created
- **THEN** its item ID is `totem:remnant/death_backpack`

### Requirement: Legacy item IDs remain readable

The system SHALL retain registrations for all five legacy `deadrecall:*`
backpack IDs.

#### Scenario: Decode a legacy saved stack

- **GIVEN** serialized ItemStack data whose item ID is a legacy backpack ID
- **WHEN** Minecraft decodes the stack
- **THEN** the item resolves without becoming missing or empty
- **AND** all serialized Components remain available

### Requirement: Interactive migration preserves components

The system SHALL replace a held legacy backpack with its canonical equivalent
before opening its menu and SHALL preserve the complete ItemStack component
patch.

#### Scenario: Use a populated legacy backpack

- **GIVEN** a held legacy backpack with contents, custom name, color and addon
  custom data
- **WHEN** the player uses the backpack
- **THEN** the held stack becomes the mapped canonical item
- **AND** its contents, name, color and addon data remain unchanged
- **AND** the backpack menu opens against the canonical stack

### Requirement: Recipes bridge both ID generations

Upgrade recipes SHALL accept legacy and canonical base backpacks while
producing only canonical outputs. Dye recipes SHALL support both generations,
and dyeing a legacy backpack SHALL migrate it to canonical.

#### Scenario: Upgrade a legacy tier

- **GIVEN** a legacy backpack accepted by an upgrade recipe
- **WHEN** the smithing result is assembled
- **THEN** the output is the next canonical tier
- **AND** components copied by vanilla smithing semantics are preserved

#### Scenario: Dye a legacy tier

- **GIVEN** a legacy tiered backpack and one or more dyes
- **WHEN** the crafting result is assembled
- **THEN** the output is the matching canonical tier
- **AND** vanilla dye mixing is applied
- **AND** unrelated Components remain unchanged

### Requirement: Migration is lazy and non-destructive

The system SHALL NOT require loading or rewriting offline players, unloaded
chunks or existing containers during startup.

#### Scenario: Start a world containing untouched legacy stacks

- **GIVEN** legacy backpacks exist outside the currently interacted inventory
- **WHEN** the server starts with the migration release
- **THEN** startup does not scan or rewrite those locations
- **AND** the legacy stacks remain valid until a supported migration action
  occurs
