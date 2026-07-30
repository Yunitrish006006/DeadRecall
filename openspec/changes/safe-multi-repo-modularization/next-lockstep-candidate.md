# Next lockstep candidate

Status: local development candidate, not an immutable graph.

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

The current `lockstep-manifest.json` remains unchanged because it is historical,
checksum-pinned evidence. Promoting this candidate requires:

1. commit each repository intentionally;
2. build production JARs from those exact commits with Java 25;
3. record every source commit and SHA-512 in a new current graph while retaining
   the existing graph as rollback;
4. update CI module artifact paths to the new graph;
5. run assembled compatibility-surface, legacy migration, restart, Dedicated
   Server and rollback verification;
6. copy only the verified final artifacts to the release output.

## Local candidate evidence

All nine candidate production JARs build with Java 25. Their embedded
`fabric.mod.json` versions match this matrix, and every feature candidate
declares the exact runtime dependency `totem-core =0.2.0`. An isolated
Dedicated Server loaded all nine versions together, reached `Done`, retained
the migrated Death Node reverse backpack binding and wrote `verify.ok`.

This evidence qualifies the local source state only. It does not supply the
post-commit source hashes or replace the immutable manifest.

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
