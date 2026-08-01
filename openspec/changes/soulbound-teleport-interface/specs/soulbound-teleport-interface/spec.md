# Soulbound Teleport Interface Specification

## ADDED Requirements

### Requirement: Latest completed teleport selects one active interface

Nexus SHALL make the Server-owned interface ItemStack used by the latest
successfully completed player teleport the player's active soulbound interface.

#### Scenario: Teleport completes

- **GIVEN** a player holds a valid Nexus interface for the entire session
- **WHEN** cost deduction, safe landing and teleport all complete
- **THEN** Nexus issues a new owner-bound token to that ItemStack
- **AND** the new token replaces the player's previous active token

#### Scenario: Teleport does not complete

- **WHEN** a player only opens the map, requests a quote, cancels or fails
- **THEN** the active soulbound token remains unchanged

### Requirement: Death retains exactly one authorized item

Remnant SHALL retain at most one item whose owner and token are authorized by
the current Core death-retained-item policy.

#### Scenario: Authorized stack contains multiple items

- **GIVEN** an active soulbound interface stack with a count greater than one
- **WHEN** the player dies with `keepInventory=false`
- **THEN** exactly one item is staged for respawn
- **AND** the remaining stack follows the normal death-backpack transaction

#### Scenario: Item has vanishing behavior

- **GIVEN** an otherwise active interface with the vanilla prevent-drop
  enchantment effect
- **WHEN** the player dies
- **THEN** Remnant does not stage or restore it

### Requirement: Retained item restoration is persistent and exactly once

Remnant SHALL persist the staged ItemStack until it is successfully restored
to the same player after death.

#### Scenario: Player respawns

- **GIVEN** one staged soulbound item
- **WHEN** the player respawns with an available inventory slot
- **THEN** Remnant restores exactly one copy
- **AND** removes the pending record only after insertion succeeds

### Requirement: DeadRecall does not own death return gameplay

DeadRecall SHALL NOT register `/back` or keep a separate last-death-location
record after the soulbound Nexus/Remnant flow is available.
