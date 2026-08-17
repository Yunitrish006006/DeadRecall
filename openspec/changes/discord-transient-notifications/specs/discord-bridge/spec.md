# Delta Spec: Discord Bridge Transient Notifications

## ADDED Requirements

### Requirement: Temporary operational notifications expire

The Discord Worker MUST automatically delete the following Discord messages 600 seconds after successful creation:

- player join (`player_join`)
- first player join (`player_first_join`)
- player leave (`player_leave`)
- death backpack created (`death_backpack_created`)
- death backpack recovered (`death_backpack_recovered`)
- Dedicated Server status messages produced from `/api/mc/server/status`

#### Scenario: Player joins

- **WHEN** DeadRecall sends a `player_join` event
- **THEN** the Worker sends it to the configured Discord channels
- **AND** records each returned Discord message ID
- **AND** schedules deletion after 600 seconds

#### Scenario: Player leaves

- **WHEN** TotemDiscordBridge sends a `player_leave` event
- **THEN** its payload includes `delete_after_seconds: 600`
- **AND** the Worker schedules deletion after 600 seconds

#### Scenario: Death backpack is recovered

- **WHEN** DeadRecall sends `death_backpack_recovered`
- **THEN** the Discord message is deleted 600 seconds after creation
- **AND** no backpack contents or coordinates are included in the deletion job

#### Scenario: Server status is reported

- **WHEN** DeadRecall posts to `/api/mc/server/status`
- **THEN** the Worker updates the persisted status values
- **AND** sends a temporary Discord status message
- **AND** schedules it for deletion after 600 seconds

### Requirement: Temporary lifetime policy is module-owned

TotemDiscordBridge MUST be the only component that maps event types to notification lifetimes. The Worker MUST NOT maintain or apply an event-type allowlist.

#### Scenario: Permanent event omits deletion instruction

- **WHEN** TotemDiscordBridge sends a permanent event
- **THEN** it omits `delete_after_seconds`
- **AND** does not enqueue a deletion job

#### Scenario: Worker receives a valid deletion instruction

- **WHEN** an authenticated module request includes a valid positive integer `delete_after_seconds`
- **THEN** the Worker schedules deletion using that exact delay
- **AND** does not inspect the event name to override the module decision

#### Scenario: Worker receives an invalid deletion instruction

- **WHEN** `delete_after_seconds` is missing, non-integer, non-positive or outside the Worker's supported queue delay range
- **THEN** the Worker delivers the Discord message without scheduling deletion

### Requirement: Deletion failures are isolated

Discord message deletion failures MUST NOT affect Minecraft gameplay, server lifecycle, or the original event delivery result.

#### Scenario: Message was already deleted

- **WHEN** the deletion consumer receives Discord HTTP 404
- **THEN** the job is acknowledged as complete

#### Scenario: Discord is temporarily unavailable

- **WHEN** deletion receives HTTP 429 or 5xx
- **THEN** the queue job is retried with a bounded delay
- **AND** no secret is logged or placed in the queue payload
