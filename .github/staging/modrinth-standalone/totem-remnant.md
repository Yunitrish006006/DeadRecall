# TotemRemnant

## Overview

TotemRemnant adds upgradeable backpacks, server-authoritative death-item recovery, and safety rules for portable containers. It is designed to make long survival trips practical without turning deaths or nested storage into duplication risks.

## Features

- Four backpack tiers with 9, 18, 27, or 36 base slots.
- Upgrade bays for crafting, raw-ore compression, matching-item pickup, extra capacity, soulbound retention, fire protection, explosion protection, despawn protection, and void rescue.
- Dyeable backpacks and a fully interactive inventory-side panel for carried storage.
- Death backpacks that collect eligible inventory contents, preserve ownership, survive fire and the void, display a beacon, and disappear only after recovery.
- Optional Trinkets Updated inventory capture and TotemNexus death-node integration.
- Automatic death-time scanning keeps one eligible soulbound Nexus interface even when it has never teleported before.

## Safety

Backpack operations are validated by the server. Portable-container nesting rules cover normal clicks, shift-clicking, hoppers, hopper minecarts, droppers, dispensers, and compatible automation. Existing invalid contents can be removed but are never silently deleted.

Administrators can control death-backpack generation, owner-only pickup, and the additional nesting restriction with game rules.

## Setup

Install TotemRemnant 0.2.11, TotemCore 0.6.0, and Fabric API on both client and server. Trinkets Updated 4.1.0-beta.2 or newer is optional.

Do not install this standalone JAR beside DeadRecall 2.4.13; the bundle already contains the identical module.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Client and server required
