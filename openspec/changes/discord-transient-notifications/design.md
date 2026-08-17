# Design: Discord Transient Notifications

## Architecture

```text
DeadRecall Server
  └─ module policy attaches delete_after_seconds when temporary
       └─ POST event/status to Cloudflare Worker
       └─ Discord Create Message
            ├─ no valid deletion instruction: finish
            └─ valid deletion instruction: enqueue delayed deletion job
                 └─ Queue consumer
                      └─ DELETE /channels/{channel_id}/messages/{message_id}
```

## Event policy

TotemDiscordBridge owns the temporary-event allowlist:

- `player_join`
- `player_first_join`
- `player_leave`
- `death_backpack_created`
- `death_backpack_recovered`
- all messages produced by `/api/mc/server/status`

The module applies the fixed 600-second lifetime while constructing both text-event and server-status payloads. All event send paths use that one policy; no temporary-event mixin or alternate HTTP sender remains.

The Worker owns only transport validation and execution. It accepts a valid positive integer `delete_after_seconds` from an authenticated module request without mapping event names to policy. Missing or invalid values remain permanent.

## Delivery and deletion

1. Resolve requested Discord channel IDs, falling back to configured sync channels and then the default environment channel.
2. Send the Discord message using the Bot Token.
3. Parse the returned Discord message object and retain only `channel_id` and `message_id`.
4. Validate the module-supplied deletion delay and publish one queue message per successfully-created Discord message using that exact `delaySeconds`.
5. The queue consumer calls Discord Delete Message.
6. HTTP 204 and 404 are terminal success states.
7. HTTP 429 and 5xx responses are retried with bounded delay.
8. Authentication, permission and malformed-job failures are logged without retry loops.

## Failure isolation

- Failure to enqueue deletion does not roll back the already-sent Discord notification.
- Failure to delete does not affect Minecraft gameplay or server lifecycle.
- No Discord token, webhook secret or API key is included in queue payloads or response diagnostics.

## Deployment

The `discord-bot` Worker requires a producer and consumer binding for `discord-message-deletions`. The queue must exist before deployment. DeadRecall does not directly call Discord and does not need Cloudflare credentials.
