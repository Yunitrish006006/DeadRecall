# TotemRemnant extraction contract

`TotemRemnant` owns death backpack capture, recovery, items, inventory flows,
the Trinkets adapter, Remnant payloads/Mixins/client UI, and the existing
`com.adaptor.deadrecall.api.death` addon API during the compatibility window.

The public addon interfaces retain their current package and behavior in the
DeadRecall bundle.  When the implementation moves, DeadRecall supplies
forwarding compatibility types for at least two lockstep bundle releases.
Addon providers must not import Remnant internals or Nexus types.

Nexus integration is optional: Remnant publishes the Core
`DeathBackpackNodeLifecycle` contract and owns the stable backpack binding;
Nexus registers an adapter when installed.  Core + Remnant therefore works
without Nexus, while Core + Remnant + Nexus preserves the death-node flow.

The external repository starts from a new independent history.  Production
files are copied only after standalone, bundle, legacy-world, restart,
multi-player and Dedicated Server validation all pass; DeadRecall code is not
deleted during the first cutover.
