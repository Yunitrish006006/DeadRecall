# Totem Villagers

## Overview

Totem Villagers replaces free, abstract sell stock with a server-authoritative village economy. Villagers carry real materials, tools, food, goods, and emeralds; observable work creates the stock that appears in normal Minecraft trading screens.

## Economy

- Every adult worker has a persistent, protected 27-slot work inventory separate from vanilla AI inventory.
- Sell offers exist only when the exact physical merchandise is in stock.
- Purchases move player-sold materials into the economy and require real villager emerald payment.
- Crafting and processing reserve inputs and commit results atomically.
- Sold-out rows disappear instead of receiving free restocks.
- Per-world modes support enforcement, intentional disabling, and a reversible vanilla rollback.

## Professions

Farmers, fishers, butchers, fletchers, masons, leatherworkers, cartographers, librarians, and toolsmiths use loaded workstations and current server recipes. Librarians make real enchanted books and equipment at Enchanting Tables. Cartographers can create real explorer maps. Toolsmiths supply tools and buckets through physical resource chains.

Custom miners, lumberjacks, builders, and guards extend the village loop with assigned work zones, construction sites, guard posts, food markets, and terrain-aware mangrove villages. TotemRemnant optionally adds backpack smithing.

## Operations

Administrative commands inspect or change economy mode, roles, work zones, builder sites, needs, and guard posts. They do not silently grant sell stock. Work remains limited to loaded entities, chunks, recipes, inventories, and validated destinations.

## Setup

Install Totem Villagers 0.1.23, TotemCore 0.6.0, and Fabric API on both client and server. TotemRemnant is optional.

Do not install this standalone JAR beside DeadRecall 2.4.11; the bundle already contains it.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Client and server required
