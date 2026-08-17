# Totem Locksmith

## Overview

Totem Locksmith adds server-authoritative protection for fixed storage without making containers indestructible. A single Padlock protects a Chest, Trapped Chest, Barrel, double chest, or a whole network joined by fixed Hoppers.

## Lock Networks

- The first locked container is the persistent network root.
- Connected supported containers and Hoppers share one lock and consume only one Padlock.
- Breaking a middle connector immediately unlocks the detached side; only the component containing the original root keeps the lock.
- The Padlock remains assigned until the final logical container on the root side is removed, then drops at most once.
- A non-owner may still break a protected container through normal Minecraft rules, but a successful break publishes one audit event for optional Discord delivery.

## Access

Owners can choose private, friends, members, or public access; assign managers, users, or blocked players; and bind, revoke, or rotate physical keys. Separate automation policies cover insertion and extraction, including vanilla Hoppers and Fabric Transfer API access.

TotemNexus friendship, TotemAutomata operator checks, and TotemDiscordBridge alerts are optional integrations. Missing or failed authority checks deny access safely.

## Quick Start

Craft a Padlock and use it on a supported container. Crafting the source block unlocks the module root advancement; use a Book or the Totem Manual on the container to acquire the Locksmith chapter before later advancements branch out. Sneak-use a locked root with an empty hand to inspect it, or use `/locksmith` for management.

## Setup

Install Totem Locksmith 0.1.0, TotemCore 0.6.0, and Fabric API on both client and server. TotemNexus, TotemAutomata, and TotemDiscordBridge are optional.

Do not install this standalone JAR beside DeadRecall 2.4.13; the bundle already contains it.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Client and server required
