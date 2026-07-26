# Discord Bridge lockstep observation

## Release 1 baseline

| Component | Version | Build SHA-512 |
| --- | --- | --- |
| DeadRecall compatibility bundle | `2.4.1` | `6ba10cca9719e8806e8341d3822ede80e7c6d69e2e94f1e1c4efa95f565c1729cfe8ef0ba6b65a0b63c48d7fc49998f959edfb5df4edb5573b7f98c242ed2f3a` |
| TotemCore | `0.1.0` | Git tag `v0.1.0`; GitHub Packages publish run `29914147524` |
| TotemDiscordBridge | `0.1.0` | Git tag `v0.1.0`; GitHub Packages publish run `29914150296` |

The bundle was assembled with Fabric Loader `0.19.3`, Minecraft `26.2`, Java 25 and Fabric API `0.154.2+26.2`. The official Dedicated Server loaded all three artifacts, initialized TotemCore and TotemDiscordBridge once, reached `Done`, and saved all dimensions normally. The latest revalidation also proved the DeadRecall legacy event facade dispatches to the external transport when the bridge is present.

## Promotion status

Discord has completed its two-release lockstep observation window:

1. The compatibility bundle consumed and validated the exact `0.1.0` Core and
   Discord artifacts.
2. A second immutable `0.1.1` graph was validated with `0.1.0` retained as the
   rollback target.
3. The machine-readable manifest and assembler now enforce the current and
   rollback pins before a local compatibility bundle is staged.

This evidence completes the Discord-specific observation requirement. The
remaining modularization work is repository-wide: cut over Automata and Nexus,
turn DeadRecall into the exact-version compatibility/E2E bundle, and collect
the corresponding lockstep and rollback evidence before independent releases.

## Release 1 verification

The compatibility bundle consumed the GitHub Packages `0.1.0` artifacts directly (Core SHA-512 `7e9b8bcb6f9ee1b7687d607f6a4e8f7d385af608a549ddd635e541d6f4f3c9ab9ab21a2e701e0ee6467691ee38a6ba7c17a8de5e61f1236a4073e14f9b2ba2cc`; Discord SHA-512 `f379687d56e4af1128d972fb57fa318b427d6c007859547e5156a9f110f0a0057626fc0754f58c169073bcabcf876b2181bb6211ebaf518e5ee4669c099b8a69`). With DeadRecall `2.4.1`, Fabric Loader `0.19.3`, Minecraft `26.2`, Java 25 and Fabric API `0.154.2+26.2`, the official Dedicated Server initialized Core and Discord Bridge once, reached `Done (10.945s)`, and saved all dimensions normally.

## Release 2 verification and rollback baseline

The second immutable graph consumed GitHub Packages `0.1.1` artifacts (Core SHA-512 `c6f8ded2e184b44eda917c956a01f89feb6722ba8c9a604812a23ff8a9bbfd6758fcf78dc6d77915085a2c18dee5f3c768b865bcbe8f2dd981b460c07bd3eef2`; Discord SHA-512 `ce84021bafbfe99aedc72a640b228bf3916b80e384080833b807d9c49e7c32587d113324a36cfbb98a71478d84cea61ec0ea19ff1591130e34fa35718f50eb0b`). The same official bundle reached `Done (0.611s)` and saved all dimensions. The retained `0.1.0` artifact hashes and its successful official-server verification above are the rollback baseline.

`lockstep-manifest.json` now records these current and rollback pins in a
machine-readable form. `assemble-lockstep-bundle.sh` rejects missing, extra or
checksum-mismatched JARs before staging a local compatibility-bundle mods
directory.

The assembler was exercised against the retained published `0.1.1` Core and
Discord JARs. It recomputed the two recorded SHA-512 values, staged only those
two JARs plus the manifest, and rejected an invocation that omitted a required
pin. This proves both the immutable current-graph assembly path and the
missing-artifact guard without relying on a network download.

The retained `0.1.0` Core and Discord artifacts also assembled successfully
with `--rollback`, proving that the manifest can stage the documented rollback
graph as well as the current graph.

## Remnant bundle cutover verification

The development compatibility bundle now loads the external Remnant authority:
the DeadRecall Remnant Mixin plugin disables its legacy death hooks when
`totem-remnant` is present, its registry bootstrap leaves the preserved
`deadrecall:*` backpack IDs to Remnant, and the external module owns the
capture Mixin, item registration, recovery lifecycle and Core node adapter.
When Remnant is absent, the same conditional gates keep the legacy
implementation available for rollback throughout the lockstep observation
window.

The cutover was rebuilt against the separate `TotemCore 0.1.1` artifact and a
locally built `TotemRemnant 0.1.0-SNAPSHOT` artifact. Its standalone build
passed, including the Remnant Fabric GameTests. A seed and verify restart probe
then ran in two independent Dedicated Server JVMs; both wrote their success
markers and verified the preserved `deadrecall:death_backpack` item ID,
contents and bound node UUID after world reload.

An isolated Dedicated Server bundle containing only DeadRecall `2.4.1`, Fabric
API, Core `0.1.1` and Remnant reached `Done (0.914s)`, loaded 1,594 recipes
and 1,699 advancements, and saved all three dimensions. The runtime reported
one TotemCore and one TotemRemnant initializer, with no duplicate registry or
Mixin registration failure.

This completes the reversible development cutover. The next compatibility
bundle release must replace the local SNAPSHOT with a published immutable
Remnant artifact and retain this legacy fallback through the two-release
lockstep observation window before independent versioning is enabled.

## Automata qualification before cutover

TotemAutomata remains additive, so DeadRecall is still the live Wrench/menu
authority and no preserved `deadrecall:*` registry or payload is registered
twice. Its qualification gate is now complete: the standalone Core + Fabric
API server loaded the external module without Cognition; the JUnit suite
passed; and a real Fabric GameTest server reported all seven required tests
passed. Those tests cover sorting merge behavior, gathering area/cursor state,
request-backoff pressure, fuel persistence, and legacy binding migration.

The persistence check was strengthened with a two-JVM Dedicated Server probe.
The seed process persisted a marked Copper Golem with gathering mode,
revision/activity/fuel values, a source binding plus bindings list, and named /
damaged ItemStack components. The verify process reloaded the same world and
validated every value; both processes wrote their respective success markers.

This completes task 5.3, not task 5.4. The remaining Automata work is to move
the complete live Wrench callback/menu/payload/gathering runtime as one
authority unit, gate the legacy owner on external-module presence, and then
perform the reversible compatibility-bundle cutover.

## Initial exact-version additive staging for Automata and Nexus

The current graph now stages the following remapped, content-addressed module
artifacts.  Each feature entry records the exact local source commit used for
the build; no `SNAPSHOT` or floating version is accepted by the manifest.

| Component | Version | Source commit | SHA-512 |
| --- | --- | --- | --- |
| TotemCore | `0.1.2` | `1d055144f9f2b9dcd79d8bb44479ce76e3c242bc` | `1af05f3aa190b935739ad05f54d77d57da4607fdd76863afdf06f049ae296e79bf74a79889368f6f8e6b95c57ae0b0ec6c4fe48346662cadf326983442a3707f` |
| TotemDiscordBridge | `0.1.1` | `0ba1e98d06c936a8032fa16eb84498453a3d5018` | `ce84021bafbfe99aedc72a640b228bf3916b80e384080833b807d9c49e7c32587d113324a36cfbb98a71478d84cea61ec0ea19ff1591130e34fa35718f50eb0b` |
| TotemAutomata | `0.1.0` | `a0fbb6c3357a06c7c8bf735d2707c42652011c6e` | `35f4d983049fdb6c8cf7d554bd0cebea569558fb6ac8cc088a6fde5fbd0453cc1e969fb695509d1dfd581d20e1ae2c0ba00f03d7c6265991cf2a9ad05d06b2ca` |
| TotemNexus | `0.1.0` | `8905359ee2990d073d89c86683addbcf5ce6926c` | `511654ab0dddb55ca20b3c9b63ce05265d53849dfc3cf35dbd35c8313866f6d9aa0762c3a306e2b798c074b96054e2d498bf6cde860334b0ccfea81afa84e873` |

This historical `0.1.0` graph was additive: Automata and Nexus entrypoints
remained intentionally inactive and DeadRecall was their sole live gameplay
owner.  It was therefore evidence for exact-version assembly, not evidence
that 5.4 or 6.4 had cut over.  The later Automata `0.1.1` cutover and its
separate legacy-world evidence are recorded below; at this point Nexus still
remained additive.
The `0.1.0` Core/Discord rollback graph remains unchanged.

## Exact-version assembled-bundle E2E

The exact graph was rebuilt from the source commits recorded in
`lockstep-manifest.json`, and every resulting JAR matched its pinned SHA-512.
`verify-lockstep-bundle.sh` rejects a stale output directory, a mismatched Git
HEAD, a missing artifact, a checksum mismatch, duplicate compatibility
resources, or a server that does not reach `Done`.  It stages the six runtime
JARs (DeadRecall, Fabric API, Core, Discord, Automata and Nexus) in the actual
Dedicated Server `mods/` directory rather than relying on a development run.

The fresh bundle run at
`/tmp/DeadRecallExactBundleScriptTty20260724` loaded 46 Fabric mods.  It
listed one each of `totem-core 0.1.2`, `totem-discord-bridge 0.1.1`,
`totem-automata 0.1.0` and `totem-nexus 0.1.0`; each corresponding initializer
appeared once, the assembled compatibility-surface check found no missing or
duplicate resource, and no crash report was created.  The server reached
`Done (12.678s)`, then the verifier sent the normal `stop` command and the log
recorded saving all three dimensions.

`validate.yml` now repeats this gate in a separate 35-minute CI job.  It checks
out every recorded source commit, rebuilds the four artifacts with the exact
Core input, verifies their checksums and source HEADs against the manifest, and
only then runs the isolated Dedicated Server E2E.  Console, server log and any
crash report are retained as CI artifacts.  This completes tasks 7.3.2 and
7.3.3.  At that point both feature modules were additive; the later Automata
`0.1.1` evidence made Automata the live owner, while Nexus was still in its
additive state and its cutover gate remained task 6.4.

## Development artifact-resource ownership guard

While extending the inactive Automata/Nexus cutover seams, the release JARs
were checked separately from their development JARs.  On Minecraft `26.2`,
Loom remapping removed non-class resources from the publishable Automata and
Nexus artifacts.  Both repositories now set `fabric.loom.dontRemap=true`, as
the existing Discord bridge already did.  The Automata source owns its Copper
Wrench item/model/texture, recipe and first-binding advancement; Nexus owns
the Space Unit degradation data and structure tags.  Each corresponding final
JAR was rebuilt and checked byte-for-byte against those source resources.

`check-module-artifact-resources.sh` now runs in the exact-version CI job for
Discord, Automata and Nexus.  It fails when an owner resource is absent from,
or differs in, the JAR that will enter the compatibility bundle.  The root
resource copies remain during the additive/rollback window, so this is
artifact-ownership evidence only: no legacy registration was gated and no
existing immutable Automata or Nexus pin was overwritten.

## Automata 0.1.1 cutover staging

`TotemAutomata 0.1.1` is built from
`f96094193a347795a6c14320e8c8112497db18ed` with SHA-512
`dcf6114d667aeb91b2aa328897335f85f94e6ee771080506eebb7026d6ea5cb983188439d088af7ac8af5bd58d0337183911b59e0a6fffb50c3d00a49cfdcd11`.
It activates its registry, payload, callback, lifecycle, client and Mixin
compositions as one path. DeadRecall gates each matching legacy owner only for
Automata `>=0.1.1`; its pinned `0.1.0` artifact is retained in the rollback
graph. A fresh isolated Dedicated Server bundle containing DeadRecall `2.4.1`,
Core `0.1.2`, Discord `0.1.1`, Nexus `0.1.0`, Fabric API and Automata `0.1.1`
loaded exactly one Automata module, reached `Done (10.232s)`, and saved all
dimensions after a normal `stop`.

## Pending local revalidation

On 2026-07-25, local Java 25 validation first found that removing the root
`discord_zh_tw` fallback resources broke the legacy Discord Bridge GameTests
when the external bridge was absent. The fallback resources were retained, as
required for the rollback path. A clean retry loaded all 470 zh_tw translations
and started all 121 Fabric GameTests without a reported failed test; however,
the GameTest JVM remained stuck after `Saving chunks for level 'Test Level'`.
The process was terminated after the completed test execution so it would not
hold the next run's `session.lock`.

This local run is diagnostic evidence only. It does not satisfy the required
build, restart, legacy-world or exact-version bundle evidence for task 5.4.

## Automata 0.1.1 continuation validation

On 2026-07-26, `validate.yml` was aligned with the current manifest pin:
the assembled-bundle job now checks out Automata
`f96094193a347795a6c14320e8c8112497db18ed` and uses
`totem-automata-0.1.1.jar` for both resource and bundle verification.  This
prevents CI from rebuilding the additive `0.1.0` source while asserting the
cutover-capable `0.1.1` checksum.

The standalone Automata Java 25 build completed with all seven required
GameTests passing.  A fresh exact-version bundle at
`/tmp/DeadRecallAutomata011Continuation2` then matched the compatibility
surface without duplicate resources, reached `Done (13.877s)`, reported each
Core, Discord, Automata and Nexus initializer exactly once, and saved all
three dimensions after its normal stop.  The root Java 25 GameTest run also
completed all 121 required tests; its JVM again stalled only after test
completion while saving the test world, so it was interrupted rather than
being recorded as a successful Gradle build.

The root baseline guard now lists Automata's transferred Wrench resources and
item-model identifier in `delegated-compatibility-surface.txt`, where the
assembled JAR check owns their verification.  The retained Discord
`legacy_discord_zh_tw` files remain private rollback classpath inputs and are
not public `deadrecall` compatibility resources.  Both the baseline and
lockstep-manifest guards pass with this ownership split.

The legacy-world cutover gate was then exercised end to end.  The legacy
`CopperGolemRestartProbe` seeded a pre-cutover world and recorded its Copper
Golem UUID.  The exact `0.1.1` Automata bundle loaded that world, verified the
legacy persisted sorting, gathering, inventory and target state, then changed
and saved those values through the external authority.  Its second Dedicated
Server JVM loaded the saved world and verified every changed value.  Both
isolated bundle runs reached `Done`, logged one live initializer per Totem
module, saved all dimensions normally, and wrote the independent
`migrate.ok` and `verify.ok` markers.  `validate.yml` now performs those same
seed, migration and restart checks from the pinned source commits and
artifacts, while `verify-lockstep-bundle.sh` supports a bounded, validated
environment handoff for the probe.

The four Automata cutover implementation and evidence sub-tasks are complete:
the immutable `0.1.1` pin is remote-reachable and the `0.1.0` pin remains in
the manifest rollback graph.  The parent task remains open solely for the
two-release observation window and the documented in-game client visual
parity inspection; the legacy implementation is retained but gated during
that period.

All future lockstep evidence is pinned to Java 25.  The bundle verifier writes
its isolated server configuration with port `25570`, and the legacy-world seed
used by the Automata migration gate uses `25571`; neither validation path uses
the ambient default port `25565`.

## Nexus full authority candidate

On 2026-07-26, the still-additive `TotemNexus` branch received an inactive
`NexusSpaceUnitAuthority` port plus a `NexusGameplayAuthority` façade for the
existing server receiver surface. It preserves the four `deadrecall` SavedData
keys and the Space Unit map payload schema, contains no
`com.adaptor.deadrecall` import, and replaces direct Discord linkage with an
optional Nexus-owned notification seam. Its Java 25 standalone build passed
all 24 required GameTests, including the façade's death-node binding and
owner-only rollback path.

This is extraction evidence only. The candidate is not called by the Nexus
entrypoint, so it cannot duplicate a root registration. Distributed-spawn
hooks, external Mixins/client UI, legacy-world restart/multi-player bundle
fixtures and a new immutable Nexus artifact pin remain required before task
6.4 can be checked.

## Nexus 0.1.1 complete-authority staging

Later on 2026-07-26, the candidate was completed as a coherent `0.1.1`
external module. `NexusAuthorityBootstrap` is now the sole external
composition point for the preserved Core death-backpack lifecycle adapter,
Space Unit interaction callbacks, distributed-spawn game rule, payload codecs
and receivers, death-node administration command, server ticks and disconnect
cleanup. It retains all `deadrecall:*` SavedData and wire identifiers. The
external client source now contains the complete map, friends, registration
preview and death-node administration screens, together with the
refresh-quote client Mixin; all server/client Mixins are collected in
`totem-nexus.mixins.json`.

The Java 25 standalone `TotemNexus build` completed successfully with all 24
required Fabric GameTests passing. The GameTest server loaded `totem-nexus
0.1.1`, set Mixin compatibility to Java 25, applied the Nexus server Mixin
configuration, and reached the normal test completion and world-save path.
At that point the root bundle remained intentionally additive until the
immutable source/JAR pin, root registration gates and
legacy-world/restart/multi-player exact-bundle fixtures were validated
together.

## Nexus 0.1.1 atomic cutover validation

`TotemNexus 0.1.1` is now pinned to source commit
`3e3a24a5803e13c4e020443ff1f8838c03a46723` and SHA-512
`27ab02f5920ee62b34fadce897039e17b21459751ebaf86027a03da7732463905e34b973fdbc0990d73e0eb1410c6da7f4cc9351526cc1487138aca6e03f5c53`.
The manifest retains Nexus `0.1.0` and its SHA-512 as the rollback pin.
DeadRecall selects this external authority only at `>=0.1.1`; it gates the
root server bootstrap, payload type/receiver registration, client networking,
death-node initializer, refresh networking and every root Nexus Mixin as one
path. The external module owns the corresponding interaction, client UI,
payload and Mixin composition. A compatibility-bundle reflection adapter
subscribes to the public Nexus notification seam and forwards its optional
Discord events without making Nexus depend directly on the Discord feature.

The fresh Java 25 bundle at `/tmp/DeadRecallNexusAdapterBundle` preserved the
assembled resource surface, logged the Nexus notification adapter and one
Nexus `0.1.1` initializer, bound its Dedicated Server to port `25570`, and
reached `Done (17.307s)` with one live initializer per Totem module.

The legacy-world test then used three separate JVMs. The root-only seed ran on
Java 25 and port `25571`, writing all four legacy `deadrecall` SavedData keys:
`space_units`, `space_discovery`, `space_friends` and `distributed_spawns`.
The exact pinned bundle on Java 25/port `25570` loaded that world, retained the
Space Unit name, ownership, privacy/discovery, friendship and distributed
spawn, renamed the unit through external Nexus authority, and saved it. A
third exact Java 25/port `25570` JVM loaded the migrated world and verified
the changed name and every retained legacy field. The independent
`seed.ok`, `migrate.ok` and `verify.ok` markers were produced. The same
three-stage proof is now a required `assembled-bundle` CI step; it uses port
`25571` only for the isolated legacy seed and port `25570` for each exact
bundle, never the ambient default `25565`.

The Nexus 6.4 implementation and evidence sub-tasks are complete. Its parent
task remains open for the two-release observation window and documented
in-game client visual-parity inspection; the gated root fallback and rollback
pin remain in place during that window.

The root-only GameTest fixture deliberately carries the five Nexus rule and
tag resources under `src/gametest/resources`. They let the legacy root
authority tests run without the external Nexus JAR, but are excluded from the
production DeadRecall JAR and the normal exact compatibility bundle, so the
production resource-ownership check continues to see exactly one Nexus owner.
The assembled-bundle CI builds Core with `remapJar`, because its plain `jar`
task only produces a development artifact in `build/devlibs`. Discord Bridge,
Automata and Nexus deliberately retain `jar`: their split-source-set
compatibility projects have no `remapJar` task and write their production JARs
directly to `build/libs`. The root-JAR surface check excludes only entries in
`delegated-compatibility-surface.txt`; the exact multi-JAR bundle continues to
require those entries and rejects duplicate resource owners.

The first remote exact-graph build exposed a reproducibility defect rather
than a code/content discrepancy: every Nexus class and resource matched the
local artifact, while Loom emitted `Fabric-Loom-Client-Only-Entries` in a
host-dependent order. Nexus commit
`a9d663ffe603293662e57bb897f2558e54f4f821` sorts that manifest value and all
archive entries with fixed timestamps. Two forced Java 25 builds produced the
same normalized artifact; its successor `3e3a24a5803e13c4e020443ff1f8838c03a46723`
also makes a live player target's exact safe position the first landing choice,
using the existing deviation search only when that position is unsafe. CI can
therefore use the pinned checksum as an immutable lockstep guard while the
root GameTest has a deterministic latest-target assertion.

The next exact remote build found the same manifest-only nondeterminism in
Automata. `31b257707da0a04d8fd1efa006ca1b1ca8e8bcc7` applies the same
canonical archive procedure. Two Java 25 forced builds produced Automata
SHA-512 `56d38fb0a6266a43a5302f5c98f7cc9faaa4e72d5ec5369eaa5a9995daca2d4b8c39590eca223bcf2b9af3ad39ad694c913022f96c72a554f5a8a3a4f416cc2f`,
which is now the immutable lockstep pin.

Exact-bundle resource ownership also removes the seven root
`assets/deadrecall/lang/discord_zh_tw/{adventure,end,events,husbandry,nether,story,system}.json`
files. They are owned by TotemDiscordBridge in the assembled graph; retaining
root copies would create duplicate `deadrecall` resource paths, regardless of
whether their historical translation content differs. The root-only legacy
Discord fallback and GameTests retain their historical snapshot under the
distinct `legacy_discord_zh_tw/` path, so it cannot collide with the Bridge's
owner paths in the assembled production graph.
