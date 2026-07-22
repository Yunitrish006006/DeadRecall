# Discord Bridge lockstep observation

## Candidate baseline (not yet a release pin)

| Component | Version | Build SHA-512 |
| --- | --- | --- |
| DeadRecall compatibility bundle | `2.4.1` | `6ba10cca9719e8806e8341d3822ede80e7c6d69e2e94f1e1c4efa95f565c1729cfe8ef0ba6b65a0b63c48d7fc49998f959edfb5df4edb5573b7f98c242ed2f3a` |
| TotemCore | `0.1.0-SNAPSHOT` | `d993f6a2141be4d1de19f321e17503bac3cdaaaa1b79fabd79d9f425ff0bea2f9453eaed0b35be021c38369db2d0f2247ee30350696172858bf8cf4de9b73e91` |
| TotemDiscordBridge | `0.1.0-SNAPSHOT` | `97b0b2c806985a1df6685864562a315f6721b1bb9d9e970a7f843dbe4484c8682300406c32d8c2eaf5c238fd64e5a8527d0d135613c4b69fc601e414e8ee8f47` |

The bundle was assembled with Fabric Loader `0.19.3`, Minecraft `26.2`, Java 25 and Fabric API `0.154.2+26.2`. The official Dedicated Server loaded all three artifacts, initialized TotemCore and TotemDiscordBridge once, reached `Done`, and saved all dimensions normally. The latest revalidation also proved the DeadRecall legacy event facade dispatches to the external transport when the bridge is present.

## Promotion requirements

This candidate is not an immutable release pin: the extracted repositories have uncommitted migration work and use snapshot versions. Before it can count toward the two-release observation window:

1. Commit and publish immutable Core and Discord Bridge artifacts, including a Core Maven publication whose sources artifact is compatible with the Fabric Loom consumer build.
2. Replace snapshot versions with exact immutable versions in the compatibility manifest.
3. Repeat the assembled Dedicated Server validation for two DeadRecall releases, retaining the previous manifest as the rollback target.
4. Only then remove the legacy Discord implementation.
