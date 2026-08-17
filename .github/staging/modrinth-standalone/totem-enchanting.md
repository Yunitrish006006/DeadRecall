# TotemEnchanting

## Overview

TotemEnchanting makes Chiseled Bookshelves contribute enchanting power based on the books they actually contain. It raises the usable power ceiling to 64 while preserving Minecraft's item and enchantment compatibility rules.

## Power

- Each normal book contributes one point.
- Each enchanted book contributes the sum of its enchantment levels.
- Valid Chiseled Bookshelves combine up to a total power of 64.
- Displayed costs can rise beyond 30, while power 30–64 is mapped into a vanilla-compatible candidate range.
- Deterministic extra candidates can improve high-power results without making the same setup worse than its level-30 baseline.
- Particles show which shelves are contributing and scale with their contents.

The mod does not add custom enchantment IDs or bypass vanilla item compatibility.

## Setup

Install TotemEnchanting 0.1.5, TotemCore 0.6.0, and Fabric API on both client and server. Place filled Chiseled Bookshelves in valid vanilla bookshelf positions with an unobstructed gap to the Enchanting Table.

Do not install this standalone JAR beside DeadRecall 2.4.13; the bundle already embeds it.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Client and server required
