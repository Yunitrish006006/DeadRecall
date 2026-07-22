# Discord Bridge lockstep observation

## Candidate baseline (not yet a release pin)

| Component | Version | Build SHA-512 |
| --- | --- | --- |
| DeadRecall compatibility bundle | `2.4.1` | `6ba10cca9719e8806e8341d3822ede80e7c6d69e2e94f1e1c4efa95f565c1729cfe8ef0ba6b65a0b63c48d7fc49998f959edfb5df4edb5573b7f98c242ed2f3a` |
| TotemCore | `0.1.0` | Git tag `v0.1.0`; GitHub Packages publish run `29914147524` |
| TotemDiscordBridge | `0.1.0` | Git tag `v0.1.0`; GitHub Packages publish run `29914150296` |

The bundle was assembled with Fabric Loader `0.19.3`, Minecraft `26.2`, Java 25 and Fabric API `0.154.2+26.2`. The official Dedicated Server loaded all three artifacts, initialized TotemCore and TotemDiscordBridge once, reached `Done`, and saved all dimensions normally. The latest revalidation also proved the DeadRecall legacy event facade dispatches to the external transport when the bridge is present.

## Promotion requirements

This is the first immutable lockstep pin. Before the Discord migration can complete its two-release observation window:

1. Consume these exact `0.1.0` artifacts from the compatibility bundle manifest and repeat the assembled Dedicated Server validation.
2. Cut and validate a second immutable lockstep release, retaining this `0.1.0` manifest as the rollback target.
3. Only then remove the legacy Discord implementation.

## Release 1 verification

The compatibility bundle consumed the GitHub Packages `0.1.0` artifacts directly (Core SHA-512 `7e9b8bcb6f9ee1b7687d607f6a4e8f7d385af608a549ddd635e541d6f4f3c9ab9ab21a2e701e0ee6467691ee38a6ba7c17a8de5e61f1236a4073e14f9b2ba2cc`; Discord SHA-512 `f379687d56e4af1128d972fb57fa318b427d6c007859547e5156a9f110f0a0057626fc0754f58c169073bcabcf876b2181bb6211ebaf518e5ee4669c099b8a69`). With DeadRecall `2.4.1`, Fabric Loader `0.19.3`, Minecraft `26.2`, Java 25 and Fabric API `0.154.2+26.2`, the official Dedicated Server initialized Core and Discord Bridge once, reached `Done (10.945s)`, and saved all dimensions normally.
