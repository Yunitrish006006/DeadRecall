# TotemVanillaTweaks

## Overview

TotemVanillaTweaks collects focused quality-of-life and survival-rule changes that do not belong to a larger Totem feature module. Each change stays server-authoritative and uses vanilla items, blocks, recipes, and interfaces where possible.

## Tweaks

- Middle-click sorting for the hovered side of player and container screens, with stable component-aware stack ordering.
- A direct Lectern recipe using four wooden slabs and one book.
- Survival conversion of ordinary Bookshelf items into books, plus book-filled Chiseled Bookshelves in supported structures.
- All sixteen Concrete Powder item entities harden only when they actually contact water.
- Hoppers extracting furnace, smoker, or blast-furnace output release the recipe experience that automation would otherwise lose.

Sorting requests, slot ranges, item movement, conversion, and experience bookkeeping are validated by the server. The client supplies the configurable sorting key and the currently hovered side.

## Setup

Install TotemVanillaTweaks 0.1.7, TotemCore 0.5.0, and Fabric API on both client and server.

Do not install this standalone JAR beside DeadRecall 2.4.10; the bundle already embeds it.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Client and server required
