# Next lockstep candidate

Status: content-addressed local exact graph; remote reachability, CI and formal
release promotion are still pending.

| Repository | Candidate version | Previous immutable version | Reason |
| --- | --- | --- | --- |
| DeadRecall | `2.4.4` | `2.4.1` | Single-file Fabric nested-JAR distribution of the verified module graph |
| TotemCore | `0.2.0` | `0.1.2` | Backwards-compatible public lifecycle API addition |
| TotemRemnant | `0.1.4` | `0.1.1` | Death-backpack beam, Trinkets adapter and shared creative-tab ownership |
| TotemNexus | `0.2.0` | `0.1.4` | Persisted reverse binding, diagnostics and authority coverage |
| TotemAutomata | `0.1.6` | `0.1.3` | Candidate UI/resource fixes and Remnant container-policy integration |
| TotemAlchemy | `0.1.4` | `0.1.3` | Candidate ownership/resource fixes |
| TotemEnchanting | `0.1.1` | `0.1.0` | Candidate ownership/resource fixes |
| TotemVanillaTweaks | `0.1.3` | `0.1.0` | Survival and generated-structure bookshelf rules |
| TotemDiscordBridge | `0.1.2` | `0.1.1` | Candidate compatibility/resource fixes |

All nine candidate production source trees now have intentional local commits.
`lockstep-manifest.json` records the eight exact module source commits and
Java 25 production-JAR SHA-512 values as the current graph, while retaining
the complete `2.4.1` graph as rollback. `validate.yml` checks out those exact
commits and uses the corresponding artifact names.

Promotion status:

1. [x] Commit each candidate production source tree intentionally.
2. [x] Build the production JARs from those exact commits with Java 25.
3. [x] Record the source commits and SHA-512 values in the current graph and
   retain the complete previous graph as rollback.
4. [x] Update the CI checkout pins and module artifact paths.
5. [x] Locally verify current and rollback assembly, the compatibility surface,
   Dedicated Server startup, and the Automata and Nexus legacy-world migrations.
6. [ ] Push every source commit so the graph is remote-reachable and pass the
   exact-source GitHub Actions workflow.
7. [ ] Promote or copy final release artifacts only after the remote workflow
   and release observation gates pass.

## Local candidate evidence

All nine candidate production JARs build with Java 25 from their recorded
commits. Their embedded `fabric.mod.json` versions match this matrix, and every
feature candidate declares the exact runtime dependency `totem-core =0.2.0`.
All eight standalone repositories completed their builds; the required Fabric
GameTest counts included Remnant 23/23, Automata 16/16, Nexus 26/26, Alchemy
6/6, Enchanting 3/3 and Vanilla Tweaks 13/13. The DeadRecall root build
completed with 113/113 required GameTests.

The current and rollback graphs both passed checksum-locked local assembly.
The workflow YAML, compatibility baseline, module-resource checks and
cross-feature dependency boundary checks also passed. An isolated Dedicated
Server loaded all nine current versions together, reached `Done`, retained the
migrated Death Node reverse backpack binding and wrote `verify.ok`.

The 2026-07-30 portable-container revalidation additionally reported all 23
TotemRemnant required GameTests passing and all 13 TotemVanillaTweaks required
GameTests passing. The complete candidate bundle loaded Remnant `0.1.4`,
VanillaTweaks `0.1.3` and Automata `0.1.6`, reached `Done`, handled
`/deadrecall containers scan` through the single Remnant authority, and stopped
with every dimension saved. The assembled compatibility-surface and both
module-resource checks passed without duplicate resources or locale conflicts.
The same graph also reached `Done` with the production Trinkets Updated and
Yumi Core artifacts installed; the death-backpack inventory provider was
registered once by TotemRemnant.

DeadRecall `2.4.4` additionally packages the same eight verified module bytes
under `META-INF/jars` and declares them through Fabric's nested-JAR metadata.
With only that single DeadRecall artifact and Fabric API installed, Fabric
loaded the complete graph, the server reached `Done`, the Remnant container
scan executed, and all dimensions saved during clean shutdown.

The cutover migration gates used three independent Java 25 Dedicated Server
JVMs per feature. Automata `0.1.6` loaded a root-authority Copper Golem world,
migrated its persisted sorting, gathering, inventory and target state, and
reloaded the changed world; `seed.ok`, `migrate.ok` and `verify.ok` were all
produced. Nexus `0.2.0` likewise loaded the four root-owned legacy SavedData
sets, retained Space Units, discovery/favorites, friendship, distributed spawn
and the Death Node reverse backpack binding, renamed the unit through external
authority, then verified the saved result in a third JVM. Each exact bundle
reported one live initializer per Totem module and no duplicate compatibility
resources.

This is strong local qualification, but it is not remote release evidence.
The commits remain ahead of their upstream branches, so the OpenSpec parent
tasks that require a release observation window stay open.
