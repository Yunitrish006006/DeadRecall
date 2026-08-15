# Totem Excavation

## Overview

Totem Excavation adds seven tiers of reusable area-mining hammers: wood, stone, copper, iron, gold, diamond, and netherite. Each hammer stores its own selection, making controlled excavation repeatable without global player commands or hidden client authority.

## Hammers

1. Crouch-use an eligible block to set the first corner.
2. Use another eligible block to set the second corner.
3. Break manually with that hammer to begin bounded excavation.

Selections stay on the individual item stack, so separate hammers can retain different work areas. The local client renders only its own selection outline.

## Safety

Block eligibility, area limits, durability, drops, and the excavation transaction are controlled by the server. The client cannot request arbitrary remote mining. TotemAutomata can optionally recognize these hammers, but a Copper Golem still mines one authorized target at a time and never consumes a player's saved selection.

## Setup

Install Totem Excavation 0.1.1, TotemCore 0.5.0, and Fabric API on both client and server.

Do not install this standalone JAR beside DeadRecall 2.4.10; the bundle already contains it.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Client and server required
