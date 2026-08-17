# Tasks: Discord Transient Notifications

## 1. Contract

- [x] 1.1 Define the temporary event allowlist in TotemDiscordBridge, including `player_leave`.
- [x] 1.2 Fix the lifetime at 600 seconds.
- [x] 1.3 Specify that TotemDiscordBridge is authoritative and the Worker only executes deletion instructions.

## 2. TotemDiscordBridge

- [x] 2.1 Add `delete_after_seconds: 600` to temporary text event payloads.
- [x] 2.2 Add `delete_after_seconds: 600` to server status payloads.
- [x] 2.3 Add payload-policy unit tests.
- [x] 2.4 Remove the alternate transient HTTP sender and mixin so every payload uses the same policy.

## 3. Discord Worker

- [x] 3.1 Route temporary events through a handler that captures Discord message IDs.
- [x] 3.2 Enqueue one delayed deletion job per successfully-sent message.
- [x] 3.3 Add a Queue consumer that deletes messages after 600 seconds.
- [x] 3.4 Treat Discord 204 and 404 as terminal success; retry 429 and transient 5xx failures.
- [x] 3.5 Add producer/consumer queue bindings to Wrangler configuration.
- [x] 3.6 Document queue creation and deployment requirements.
- [x] 3.7 Remove the Worker event allowlist and validate/execute module-supplied delays instead.

## 4. Verification

- [x] 4.1 DeadRecall Java 25 build and tests pass.
- [x] 4.2 Discord Worker syntax/tests pass.
- [x] 4.3 DeadRecall PR Actions pass.
- [x] 4.4 Worker deployment smoke test confirms a temporary message is deleted after 10 minutes.

## Evidence

- Worker implementation: [`Yunitrish006006/discord-bot#1`](https://github.com/Yunitrish006006/discord-bot/pull/1), merged as `112f0fe`.
- Production secret alias compatibility: [`Yunitrish006006/discord-bot#2`](https://github.com/Yunitrish006006/discord-bot/pull/2), merged as `9467b5e`.
- Production smoke observability and zero-downtime API key rotation: [`Yunitrish006006/discord-bot#3`](https://github.com/Yunitrish006006/discord-bot/pull/3), merged as `31a00ce`.
- Current Worker unit tests: 19/19 pass, covering module-supplied delay validation/execution, permanent messages without an instruction, failure isolation, deletion retry/terminal states, and deployed secret aliases.
- Worker bundle verification: `wrangler deploy --dry-run` passes with the D1 and Queue producer/consumer bindings recognized.
- GitHub Actions: Worker `Validate` passed 2/2 checks on all three pull requests.

## Production deployment verification

- Created `discord-message-deletions` with 86,400-second free-tier retention; Cloudflare reports one producer and one consumer.
- Deployed Worker `main` merge commit `31a00ce`; `/health` returned HTTP 200 before and after smoke-test cleanup.
- The previous production smoke used an authenticated `player_join` request and returned HTTP 200 with `sent: 1`, `failed: 0`, and `deletionScheduled: 1`; a new smoke is required after deploying the module-authoritative contract.
- The Worker logged `discord_message_deletion_scheduled` for message `1528233874881118401` at `2026-07-19T02:55:50Z` with `delay_seconds: 600`.
- The Queue consumer logged `discord_message_deletion_terminal` for the same message at `2026-07-19T03:06:03Z` with Discord HTTP 204, 613.218 seconds after scheduling.
- The temporary canonical smoke-test API key was deleted from Cloudflare and its local file removed; the original deployed secret aliases remain configured.

## Module-authoritative cutover verification

- [x] TotemDiscordBridge Java 25 tests pass, including the module-owned event policy and `player_leave`.
- [x] Discord Worker tests pass and Wrangler deployment dry-run recognizes the Queue and D1 bindings.
- [ ] Deploy the updated Worker and TotemDiscordBridge together, then confirm `player_leave` is deleted after 600 seconds while ordinary chat remains permanent.
