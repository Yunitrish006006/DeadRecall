# Tasks: Discord Transient Notifications

## 1. Contract

- [x] 1.1 Define the temporary event allowlist.
- [x] 1.2 Fix the lifetime at 600 seconds.
- [x] 1.3 Specify that the Worker remains authoritative and older mod payloads remain compatible.

## 2. DeadRecall

- [x] 2.1 Add `delete_after_seconds: 600` to temporary text event payloads.
- [x] 2.2 Add `delete_after_seconds: 600` to server status payloads.
- [x] 2.3 Add payload-policy unit tests.

## 3. Discord Worker

- [x] 3.1 Route temporary events through a handler that captures Discord message IDs.
- [x] 3.2 Enqueue one delayed deletion job per successfully-sent message.
- [x] 3.3 Add a Queue consumer that deletes messages after 600 seconds.
- [x] 3.4 Treat Discord 204 and 404 as terminal success; retry 429 and transient 5xx failures.
- [x] 3.5 Add producer/consumer queue bindings to Wrangler configuration.
- [x] 3.6 Document queue creation and deployment requirements.

## 4. Verification

- [x] 4.1 DeadRecall Java 25 build and tests pass.
- [x] 4.2 Discord Worker syntax/tests pass.
- [x] 4.3 DeadRecall PR Actions pass.
- [ ] 4.4 Worker deployment smoke test confirms a temporary message is deleted after 10 minutes.

## Evidence

- Worker implementation: [`Yunitrish006006/discord-bot#1`](https://github.com/Yunitrish006006/discord-bot/pull/1), merged as `112f0fe`.
- Worker unit tests: 14/14 pass, covering the allowlist, 600-second enqueue policy, failure isolation, and deletion retry/terminal states.
- Worker bundle verification: `wrangler deploy --dry-run` passes with the D1 and Queue producer/consumer bindings recognized.
- GitHub Actions: the Worker `Validate` workflow passed both checks on the pull request.

## Remaining deployment verification

Task 4.4 requires production Cloudflare credentials, creation of `discord-message-deletions`, deployment of merge commit `112f0fe`, and an observed Discord message deletion after 600 seconds. It remains open until that external smoke test succeeds.
