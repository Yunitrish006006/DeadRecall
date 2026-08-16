# TotemCore

## Overview

TotemCore is the shared API and compatibility foundation for the standalone Totem modules. It gives the ecosystem one stable place for cross-module events, lifecycle contracts, manual pages, version negotiation, and legacy item migration.

TotemCore does not add gameplay, items, blocks, or persistent world data by itself. Install it with at least one Totem feature module.

## Features

- Typed events for death-backpack creation and recovery, Space Unit updates, locked-container break alerts, and administrative audits.
- Optional lifecycle contracts that let modules integrate without making one feature module a hard dependency of another.
- A unified two-page Totem Manual assembled from the pages registered by installed modules, with a basic guide granted once on first join.
- Guided advancement gates that require the module's source block before its method chapter and later branches.
- API-version checks and exact client/server module checks when used inside the DeadRecall bundle.
- Safe migration hooks for legacy item identifiers.

## Setup

Install TotemCore on both the client and server together with Fabric API and the feature modules that require it. Current standalone Totem releases require exactly TotemCore 0.6.0.

Do not install this standalone JAR beside the DeadRecall bundle. DeadRecall 2.4.11 already embeds the same module, and installing both creates duplicate mod IDs.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Fabric API required
- Client and server required
