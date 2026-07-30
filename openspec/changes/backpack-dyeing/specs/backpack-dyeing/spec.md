# Backpack Dyeing Specification

## ADDED Requirements

### Requirement: Tiered backpacks support vanilla dye crafting

The system SHALL allow canonical and legacy tiered Remnant backpacks to be
crafted with one or more vanilla dyes using vanilla dye mixing semantics.

#### Scenario: Dye an undyed backpack

- **GIVEN** an undyed tiered backpack and a vanilla dye
- **WHEN** the player crafts them together
- **THEN** the result is the same backpack tier under its canonical ID
- **AND** the result contains a `minecraft:dyed_color` Component

#### Scenario: Mix an existing backpack color

- **GIVEN** a tiered backpack that already has `minecraft:dyed_color`
- **WHEN** the player crafts it with one or more vanilla dyes
- **THEN** the result color follows the vanilla leather-item mixing algorithm

### Requirement: Dye crafting preserves backpack data

Dye crafting SHALL preserve every Component from the input backpack except for
the intentional `minecraft:dyed_color` update.

#### Scenario: Dye a populated named backpack

- **GIVEN** a tiered backpack with stored contents and a custom name
- **WHEN** the player dyes the backpack
- **THEN** all stored items remain unchanged
- **AND** the custom name remains unchanged
- **AND** no unrelated Component is removed or rewritten

### Requirement: Water cauldrons remove backpack dye

The system SHALL allow a water cauldron to remove `minecraft:dyed_color` from a
tiered backpack while preserving every unrelated Component.

#### Scenario: Wash a dyed backpack

- **GIVEN** a dyed tiered backpack and a non-empty water cauldron
- **WHEN** the player uses the backpack on the cauldron
- **THEN** `minecraft:dyed_color` is removed
- **AND** one cauldron water level is consumed
- **AND** backpack contents and custom name remain unchanged

### Requirement: Backpack color is visible on the item model

The system SHALL render `minecraft:dyed_color` on tiered backpack item models
in inventory, held-item and dropped-item contexts.

#### Scenario: Render an undyed legacy backpack

- **GIVEN** a tiered backpack without `minecraft:dyed_color`
- **WHEN** the client renders its item model
- **THEN** the existing brown texture remains visible

### Requirement: Death backpacks remain non-dyeable

The system SHALL exclude both `deadrecall:death_backpack` and
`totem:remnant/death_backpack` from dye recipes and cauldron dye removal.

#### Scenario: Attempt to dye a death backpack

- **GIVEN** a death backpack and one or more dyes
- **WHEN** the crafting recipe manager evaluates the input
- **THEN** no backpack dye recipe matches
