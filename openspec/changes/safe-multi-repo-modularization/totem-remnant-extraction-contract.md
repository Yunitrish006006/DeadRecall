# TotemRemnant extraction contract

`TotemRemnant` owns death backpack capture, recovery, items, inventory flows,
the Trinkets adapter, Remnant payloads/Mixins/client UI, and the existing
`com.adaptor.deadrecall.api.death` addon API during the compatibility window.
It is also the sole production owner of the preserved backpack item
definitions, models, texture, recipes, advancements and
`deadrecall:portable_containers` item tag. DeadRecall retains server-side
copies of the data resources only in its root-only GameTest fixture; they are
not packaged in the production compatibility JAR beside Remnant.

The public addon interfaces retain their current package and behavior in the
DeadRecall bundle.  When the implementation moves, DeadRecall supplies
forwarding compatibility types for at least two lockstep bundle releases.
Addon providers must not import Remnant internals or Nexus types.

Nexus integration is optional: Remnant publishes the Core
`DeathBackpackNodeLifecycle` contract and owns the stable backpack binding;
Nexus registers an adapter when installed.  Core + Remnant therefore works
without Nexus, while Core + Remnant + Nexus preserves the death-node flow.
After spawning the authoritative backpack ItemEntity, Remnant supplies its UUID
through the optional lifecycle `bind` callback. Nexus persists that reverse
link on the death-node record; no module searches or loads chunks to reconstruct
the link. Older lifecycle providers remain compatible through Core's default
no-op callback.

The external repository starts from a new independent history.  Production
files are copied only after standalone, bundle, legacy-world, restart,
multi-player and Dedicated Server validation all pass; DeadRecall code is not
deleted during the first cutover.

## Portable-container safety cutover

TotemRemnant `0.1.3` is the first external authority for portable-container
safety. It owns the policy facade, backpack-menu enforcement, Shulker Box
insertion Mixins, automation rejection diagnostics and the read-only
administrator scan. Automata consumes
`dev.totem.remnant.api.v1.PortableContainerSafetyApi` only through an optional
reflection bridge and retains its conservative vanilla fallback without
Remnant.

When `totem-remnant >=0.1.3` is installed, DeadRecall disables only its legacy
Shulker Box safety Mixins and legacy container-scan command registration.
Older Remnant versions keep the guarded DeadRecall implementation active, so
rollback bundles do not lose enforcement.

## Completion cutover

TotemRemnant `0.1.4` is the first external authority for the death-backpack
beam, optional Trinkets Updated inventory provider and shared
`deadrecall:main` creative tab. DeadRecall disables those legacy registrations
only at `totem-remnant >=0.1.4`; older Remnant artifacts continue to use the
compiled root fallback.
