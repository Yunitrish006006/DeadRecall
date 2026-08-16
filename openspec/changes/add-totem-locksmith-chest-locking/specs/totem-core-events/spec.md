# Delta Spec: Locksmith Events

## ADDED Requirements

### Requirement: Core exposes a typed locked-network break event

TotemCore SHALL expose an immutable versioned
`LockedContainerNetworkBrokenEvent` so TotemLocksmith can publish a completed
domain event for a protected container or Hopper connector and DiscordBridge
can subscribe without either feature module directly depending on the other.

The event SHALL contain:

- unique event UUID;
- Lock UUID;
- Server-verified actor UUID and display name;
- Owner UUID and last-known display name;
- broken-member kind, Dimension and broken BlockPos;
- remaining locked logical-container count on the root side;
- detached unlocked logical-container count;
- whether the root moved to a successor;
- whether the lock was removed;
- Server occurrence time.

It SHALL NOT contain contents, ACL entries, Key UUIDs, Discord routing or
secrets.

#### Scenario: A middle Hopper splits the network

- **GIVEN** the root side and a remote side share one Lock UUID
- **WHEN** TotemLocksmith publishes after the connecting Hopper is successfully
  removed
- **THEN** remainingLockedContainers counts only the surviving root component
- **AND** detachedUnlockedContainers counts containers released on every other
  component
- **AND** lockRemoved is false

#### Scenario: The root is removed but a successor survives

- **WHEN** TotemLocksmith commits the deterministic successor component
- **THEN** rootMoved is true
- **AND** only the successor component contributes to remainingLockedContainers

#### Scenario: The final root-side container is removed

- **WHEN** TotemLocksmith publishes after the last protected logical container
  is successfully removed
- **THEN** remainingLockedContainers is 0
- **AND** lockRemoved is true

### Requirement: Subscriber failures are isolated

`TotemEventBus` SHALL isolate a failing listener from the publisher's completed
gameplay transaction.

#### Scenario: DiscordBridge listener throws

- **GIVEN** the block and lock topology already committed
- **WHEN** the DiscordBridge subscriber throws
- **THEN** event publication reports/logs the listener failure
- **AND** the completed block break, contents, topology and Padlock result are
  not rolled back
