# TotemAutomata bundle-cutover contract

## Current state

`TotemAutomata` contains the extracted Copper Golem data model, sorting and
gathering runtime, Wrench interaction planner, menu model, payload codecs and
client-screen model.  Its `TotemAutomata` and `TotemAutomataClient` entrypoints
activate the external ownership composition in version `0.1.1`. DeadRecall's
`AutomataCutover` selects that composition only when the installed module is
at least `0.1.1`; the existing additive `0.1.0` graph therefore continues to
leave DeadRecall as the one live authority.

This is deliberate: enabling only the external callbacks, menu, payloads or
mixins would register preserved `deadrecall:*` identifiers twice.  A valid
cutover must activate all ownership below in one external path and disable the
matching DeadRecall path before any of the preserved identifiers are touched.

## Atomic external ownership

The external `totem-automata` entrypoints must compose these units exactly
once:

1. Register `deadrecall:copper_wrench`, `deadrecall:copper_golem` and
   `deadrecall:first_copper_golem_binding` from Automata-owned registration
   classes.
2. Construct the persisted Wrench/menu authority, including selection,
   permission/revision checks, sorting, gathering, LLM configuration, player
   feedback, server ticks and Copper Golem death/destruction cleanup.
3. Register the complete preserved payload family: serverbound codecs and
   receivers, clientbound codecs, menu snapshot refresh and visualization.
4. Register the client menu screen, snapshot receiver and visualization
   renderer in the external client entrypoint.
5. Move the `CopperGolemEntityMixin` and
   `TransportItemsBetweenContainersMixin` behavior to an Automata mixin
   configuration.  The legacy portable-container redirect must not be copied
   as a direct DeadRecall dependency: Automata's `ContainerSafetyBridge` is
   the optional integration seam for that policy.

The external source now has a preserved-schema mutation authority
(`CopperGolemStateMutation`), an activity resolver
(`CopperGolemActivityResolver`), concrete
`PersistedCopperGolemPayloadHandler`, and
`PersistedCopperGolemSnapshotSender`.  Together they own receiver permission,
revision and binding validation, LLM connection testing, persisted mutations,
visualization output and the full legacy-compatible menu snapshot without a
`com.adaptor.deadrecall` import.  The sender supplies the same snapshot path
to the menu opener and payload handler, including the preserved API-key
visibility rule and destination preview.  These classes are still
registered by the final composition with the callback, menu, lifecycle and
Mixin owners below.

Automata now also owns `CopperGolemLifecycle` and its one-shot
`CopperGolemLifecycleRegistration`: the preserved death/destruction path
clears the virtual gathering hand item, drops persisted fuel/tool/storage and
clears those copies before entity removal. The external composition registers
this path once, while the root version gate prevents a second server tick or
death callback.

`PersistedCopperGolemRuntime` now composes discovery, loaded-only source and
destination pruning, blocked-sorting recovery, the gathering tick, virtual
hand display, and the stopped-golem LLM warmup.  Its gathering classifier
writes decisions through `PersistingGatheringDecisionSink`, guarded by the
persisted prompt revision. The external mixin owner now activates normal
sorting transport with the same composition.

The external source now also contains the preserved Entity-destruction and
`TransportItemsBetweenContainers` mixins, plus
`totem-automata.mixins.json`. Those mixins use only
`CopperGolemSortingAuthority` and Automata's `ContainerSafetyBridge`; the
old portable-container redirect is consequently not copied. Version `0.1.1`
declares this configuration (and its client accessor configuration) in
`fabric.mod.json`; the matching DeadRecall configurations are disabled by the
same version gate.

`AutomataServerCutoverComposition` now joins the server registry, payload,
callback, lifecycle and runtime owners and is invoked once by the `0.1.1`
entrypoint. The server authority includes sorting return-to-source and
gathering death/destruction cleanup. Optional Remnant and container-safety
behavior remains behind the reflection adapters already owned by Automata.

`AutomataClientCutoverComposition` provides the equivalent client activation
for the menu snapshot bridge, visualization renderer and external screen
registry. Its MenuScreens/Slot accessors live in the declared
`totem-automata.client.mixins.json`. The current external screen
now consumes the authoritative snapshot for operation/mode state, a
source/destination/LLM summary, API configuration and gathering-LLM prompt
edits. It now also owns the sorting binding selection, per-binding LLM prompt
and cache-decision editor, responsive slot placement through its cutover-only
accessor, and gathering target browsing/right-click removal. It still needs
full legacy visual/layout parity and client-side in-game inspection before the
cutover can be declared complete.

## Matching DeadRecall gates

When `AutomataCutover` finds `totem-automata >= 0.1.1`, DeadRecall skips all
of the following legacy registrations as one choice:

- `TotemAutomataItemRegistration`, `TotemAutomataMenuRegistration` and
  `TotemAutomataCriteriaRegistration` in `DeadRecallRegistryBootstrap`;
- `TotemAutomataBootstrap` callbacks, server tick and lifecycle cleanup;
- `TotemAutomataPayloadRegistration` payload types and receivers;
- `TotemAutomataClientBootstrap` menu/renderer/network receiver setup; and
- every entry in `deadrecall.automata.mixins.json` and the matching client
  accessor configuration.

DeadRecall's unrelated registry owners, legacy gameplay and Nexus bootstrap
must stay active.  The legacy Automata classes remain source-visible as the
rollback implementation during the two-release observation window, but none
may register in an assembled runtime containing the cutover-capable Automata
artifact. `ModItemGroups`, `ModMenus` and `ModCriteriaTriggers` also avoid
class-loading a legacy Automata registration when the external authority is
selected.

## Evidence required before cutover

- Core + Automata standalone build and required GameTests;
- exact-version compatibility bundle with one registration of each preserved
  item, menu, criterion, payload receiver, callback and mixin;
- legacy-world load plus the Copper Golem restart probe using the external
  authority;
- Dedicated Server start and normal shutdown; and
- an immutable Automata JAR pin with its previous pin retained as rollback.

Passing the existing additive qualification alone does not satisfy this
contract or task 5.4.
