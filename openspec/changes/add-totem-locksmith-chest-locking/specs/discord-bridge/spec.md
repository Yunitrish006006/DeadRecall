# Delta Spec: Locked Container Alerts

## ADDED Requirements

### Requirement: Non-owner locked-network breaks create permanent alerts

DiscordBridge SHALL subscribe to the Server-authored
`LockedContainerNetworkBrokenEvent` and dispatch one permanent
`locked_container_network_broken` notification for each unique successful
event.

#### Scenario: A non-owner breaks a middle Hopper

- **GIVEN** Hopper routes connect the root container to another locked branch
- **WHEN** a non-owner successfully breaks the middle member Hopper
- **THEN** DiscordBridge sends one notification containing actor, owner,
  member kind, dimension, position, remainingLockedContainers and
  detachedUnlockedContainers
- **AND** the notification states that only the root side remains locked and the
  detached side is now unlocked
- **AND** the event is not placed in the transient-deletion allowlist

#### Scenario: The last root-side container is broken

- **WHEN** a non-owner successfully breaks the final protected logical container
- **THEN** DiscordBridge sends one notification with
  remainingLockedContainers = 0
- **AND** the notification states that the lock was removed and dropped

### Requirement: Alerts are deduplicated and isolated

DiscordBridge SHALL deduplicate repeated callbacks carrying the same event UUID
with a bounded cache. Formatting, channel configuration, Worker, HTTP or Discord
failures SHALL NOT roll back or interrupt the completed Minecraft break.

#### Scenario: The same event is observed twice

- **WHEN** the subscriber receives the same event UUID twice
- **THEN** it dispatches at most one Discord payload

#### Scenario: Delivery fails

- **WHEN** formatting or transport throws after the Minecraft break committed
- **THEN** the Server remains running
- **AND** the block, contents, remaining lock topology and Padlock result remain
  unchanged

### Requirement: Alert payloads preserve privacy

The message SHALL omit container contents, ACL entries, Key UUIDs, full Lock
UUIDs and secrets. It SHALL use only configured Minecraft event channels and
SHALL NOT invent a fallback Discord destination.

#### Scenario: No channel is configured

- **WHEN** a valid break event is published without configured channels
- **THEN** DiscordBridge safely no-ops externally
- **AND** records only a non-sensitive local diagnostic
