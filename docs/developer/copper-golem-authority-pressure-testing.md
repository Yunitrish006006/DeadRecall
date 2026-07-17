# Copper Golem authority and pressure regression

OpenSpec 13.8 is verified with `CopperGolemAuthorityPressureGameTest`.

## Revision authority

- A valid server revision may mutate authoritative state and advances the revision.
- Reusing the consumed revision cannot change running state, mode, or Gathering LLM configuration.
- Two players submitting actions in the same server tick are serialized by the server thread: the first accepted mutation consumes the revision and later actions using the same revision are rejected.

## Pressure fixture

The pressure fixture creates 64 managed Gathering Copper Golems with loaded, bounded scan areas, tools, fuel, and manual block rules. It verifies that:

- every fixture remains alive while scanner/controller ticks run;
- scanner cursors never become invalid;
- all managed entities are tracked;
- discarding half the entities removes their controller entries within subsequent ticks;
- cleanup does not require a production-only test hook.

The fixture is deterministic and server-only. It does not replace profiling on a production-sized multiplayer world, but it protects the bounded scanner budget and tracking cleanup contract from functional regressions.
