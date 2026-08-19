# Legacy Item Migration

## Requirements

### Requirement: Permanent Core alias authority

TotemCore 0.7.1 or newer MUST be the sole permanent registry owner of the 14 retained `deadrecall:*` item identifiers. DeadRecall 2.4.22 MUST NOT register those item identifiers itself. Feature modules MUST continue to register only their `totem:*` canonical identifiers.

### Requirement: Deferred canonical resolution

Core MUST allow a retained legacy item to decode even when its canonical feature module is temporarily absent. If the canonical item is unavailable, migration MUST leave the legacy stack intact. Once the canonical item is available, the migration registry MUST resolve it lazily.

### Requirement: Component-preserving conversion

When a supported legacy stack is converted, the system MUST preserve its count and complete Data Component patch while replacing only the Item target.

### Requirement: Decode without global scan

All retained legacy identifiers MUST remain decodable after DeadRecall is removed as long as TotemCore 0.7.1+ is installed. The system MUST NOT require a startup scan of offline players, unloaded chunks, or arbitrary containers to prevent item loss.

### Requirement: Villagers optionality

DeadRecall 2.4.22 MUST NOT hard-depend on or nest TotemVillagers. Servers MUST be able to perform the migration checkpoint and subsequent standalone cutover without installing TotemVillagers.

### Requirement: Thin outer artifact

The DeadRecall outer JAR MUST NOT contain gameplay Mixins, GUI, Payload, command, SavedData, legacy placeholder Item implementations, or fallback gameplay classes. Gameplay MUST come only from the exact nested transition module graph.
