# Remaining gameplay repository proposals

There are no remaining approved gameplay extractions. Alchemy, Automata,
Discord, Enchanting, Nexus, Remnant and the general vanilla rules all have
standalone owners. Small crafting, bookshelf and world-interaction rules belong
to TotemVanillaTweaks unless a future OpenSpec establishes a genuinely separate
bounded context.

DeadRecall no longer owns `/back` or a last-death-location record. Death return
uses the separately specified Nexus soulbound teleport interface and
Remnant-owned death retention transaction. Compiled legacy implementations
remain only as observation-window rollback paths and are disabled by versioned
authority cutovers in the complete bundle.

## Cross-feature notification wiring

TotemCore owns the process-local typed event contracts and event bus. Remnant
and Nexus publish their own completed domain events; Discord Bridge subscribes
to those events independently. DeadRecall must not install feature-specific
reflective listeners between those standalone modules.

The existing `com.adaptor.deadrecall.*` facades remain observation-window
compatibility surfaces inside DeadRecall. Moving those facades into another
published module would change the lockstep release graph and requires its own
approved compatibility change; it is not part of the notification-wiring
extraction.
