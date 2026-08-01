# Legacy Item Migration

## Requirements

### Requirement: Single alias authority

DeadRecall MUST be the only mod that registers the 14 retained
`deadrecall:*` item identifiers. Feature modules MUST register only their
`totem:*` canonical identifiers.

### Requirement: Component-preserving conversion

When a supported legacy stack is converted, the system MUST preserve its
count and complete Data Component patch while replacing only the Item target.

### Requirement: Decode without global scan

All retained legacy identifiers MUST decode when the compatibility bundle is
installed. The system MUST NOT require a startup scan of offline players,
unloaded chunks, or arbitrary containers.

### Requirement: Thin outer artifact

The DeadRecall outer JAR MUST NOT contain gameplay Mixins, GUI, Payload,
command, SavedData, or fallback implementation classes. Gameplay MUST come
only from the exact nested module graph.
