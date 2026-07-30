# Module Item ID Migration Specification

## ADDED Requirements

### Requirement: Item-owning modules expose canonical IDs

Automata and Alchemy SHALL register their custom items under the canonical
`totem:<module>/<path>` identifiers and SHALL use canonical items for all new
acquisition paths.

#### Scenario: Craft a copper wrench

- **WHEN** the copper wrench recipe is assembled
- **THEN** the result is `totem:automata/copper_wrench`

#### Scenario: Produce an alchemy item

- **WHEN** a crafting, smelting, cauldron or gameplay drop path creates an
  Alchemy item
- **THEN** the result uses the matching `totem:alchemy/*` identifier

### Requirement: Legacy IDs remain functional

Every migrated `deadrecall:*` item SHALL remain registered and SHALL retain its
original gameplay properties.

#### Scenario: Load a legacy item stack

- **GIVEN** saved ItemStack data using a migrated `deadrecall:*` identifier
- **WHEN** the stack is decoded
- **THEN** it resolves to a functional legacy Item
- **AND** its Components remain available

### Requirement: Wrench interaction migrates safely

Server-authoritative copper wrench interaction SHALL replace a held legacy
wrench with its canonical counterpart before updating selection state.

#### Scenario: Bind a golem with a legacy wrench

- **GIVEN** a held legacy wrench with existing Components
- **WHEN** the player interacts with a copper golem
- **THEN** the held item becomes the canonical wrench
- **AND** existing Components are unchanged
- **AND** the selected golem UUID is written to the canonical stack

### Requirement: Alchemy transformation paths bridge ID generations

Alchemy recipes and cauldron inputs SHALL accept applicable legacy and
canonical ingredients while producing canonical custom items and remainders.

#### Scenario: Use a legacy stone bowl

- **GIVEN** a custom recipe that consumes or returns a stone bowl
- **WHEN** its input contains `deadrecall:stone_bowl`
- **THEN** the recipe matches
- **AND** any returned custom bowl uses `totem:alchemy/stone_bowl`

#### Scenario: Brew with a legacy ingredient

- **GIVEN** a supported cauldron recipe and its legacy Alchemy ingredient
- **WHEN** the ingredient is added
- **THEN** the ingredient matches the same recipe step as its canonical form
- **AND** the final custom result uses a canonical Alchemy ID

### Requirement: Migration does not invent mappings

Modules without custom Item registrations SHALL NOT receive synthetic custom
item identifiers as part of this migration.

#### Scenario: Review Nexus interface items

- **GIVEN** Nexus uses vanilla compass, recovery compass, book and map items
- **WHEN** module item mappings are enumerated
- **THEN** those vanilla identifiers remain unchanged
