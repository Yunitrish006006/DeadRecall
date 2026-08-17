# TotemDiscordBridge

## Overview

TotemDiscordBridge relays selected Minecraft server activity to Discord through a separately deployed HTTPS Worker. It keeps Discord credentials outside the Minecraft server and provides one configurable path for chat, status, attachments, events, and administrative audits.

The bridge is disabled by default and does nothing until an operator explicitly configures and enables it.

## Events

- Minecraft chat and supported attachments.
- Player joins, leaves, deaths, and server lifecycle events.
- TotemCore events such as death-backpack recovery and Space Unit updates.
- One deduplicated alert after a non-owner successfully breaks a TotemLocksmith protected container.
- Sanitized administrative audit summaries.
- Live status and presence updates.
- An optional in-game administration screen when the client module is installed.

## Privacy

Enabling the bridge sends configured server and player activity to the operator's Worker and Discord destination. Server owners are responsible for choosing which events are relayed, protecting the API key, securing the Worker, and informing players as required by their rules or jurisdiction.

Never publish the Worker API key in logs, screenshots, issue reports, or source control.

## Setup

Deploy the companion HTTPS Worker, configure its Discord destination, then add the Worker URL and matching API key to the server configuration. Install TotemDiscordBridge 0.1.6, TotemCore 0.6.0, and Fabric API on the server. The client is optional and is needed only for the administration interface.

Do not install this standalone JAR beside DeadRecall 2.4.13; the bundle already embeds it.

## Compatibility

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Java 25 or newer
- Server required; client optional
