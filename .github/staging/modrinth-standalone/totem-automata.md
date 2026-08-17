# TotemAutomata

## Overview

TotemAutomata turns vanilla Copper Golems into configurable sorting and gathering helpers. A copper wrench links each golem to storage, work areas, tools, fuel, and explicit item or block rules while the server remains authoritative over every operation.

## Jobs

- Sorting jobs pull bounded batches from one copper chest and route matching stacks to ordered destinations.
- Gathering jobs work inside a two-corner area, scan only loaded chunks, use real tools and fuel, and return materials to a home copper chest.
- Manual rules take priority, and empty destinations do not become accidental catch-all storage.
- Optional TotemRemnant integration respects portable-container safety.
- Optional TotemExcavation support recognizes its hammers without using a player's area selection.
- Optional TotemLocksmith integration rejects unauthorized source and destination mutations using the persisted golem operator identity.

## LLM

Each golem can optionally use an OpenAI-compatible Chat Completions endpoint for classification decisions. The API URL, model, prompts, and key are configured by the operator. Failures time out safely and do not block the server tick.

API keys are secrets. Never include them in screenshots, logs, issue reports, resource packs, or public configuration files.

## Setup

Install TotemAutomata 0.1.12, TotemCore 0.6.0, and Fabric API on both client and server. TotemExcavation, TotemRemnant, and TotemLocksmith are optional integrations.

Do not install this standalone JAR beside DeadRecall 2.4.13; the bundle already embeds it.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Client and server required
