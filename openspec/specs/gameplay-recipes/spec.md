# DeadRecall Gameplay Recipe Specification

## Purpose

此規格記錄 DeadRecall 對原版或模組配方的資料層調整。配方優先使用 JSON resource，不應為單純 shaped／shapeless recipe 引入 Mixin。

## Requirements

### Requirement: Lectern recipe override

DeadRecall SHALL override `minecraft:lectern` with a shaped recipe containing four items in `minecraft:wooden_slabs` and one `minecraft:book`, producing one `minecraft:lectern`.

#### Scenario: Craft a lectern with mixed wooden slabs

- **GIVEN** the crafting grid contains wooden slabs and a book in the documented pattern
- **AND** every slab belongs to `minecraft:wooden_slabs`
- **WHEN** the recipe is resolved
- **THEN** it SHALL produce one `minecraft:lectern`

DeadRecall 覆寫 `minecraft:lectern`，使用以下配方：

```text
SSS
 B 
 S 
```

- `S`：任意符合 `minecraft:wooden_slabs` 的木半磚。
- `B`：`minecraft:book`。
- 產出：1 個 `minecraft:lectern`。

實作位置：

```text
src/main/resources/data/minecraft/recipe/lectern.json
```

### Requirement: Recipe override compatibility

The override SHALL retain the `minecraft:lectern` recipe identifier, use the Minecraft 26.2 recipe schema, and SHALL NOT modify existing block, item or world data.

#### Scenario: A data pack overrides the same recipe

- **GIVEN** another data pack supplies `minecraft:lectern`
- **WHEN** resources are loaded
- **THEN** the effective recipe SHALL follow normal resource-pack ordering
- **AND** DeadRecall SHALL NOT register a duplicate recipe under another identifier

- 覆寫原版配方時使用相同 namespace 與 recipe ID。
- 不建立第二個功能相同但 ID 不同的重複配方。
- 使用 Minecraft 26.2 現行 recipe schema。
- 資料包可能再次覆寫同一 recipe ID；實際結果由資源載入順序決定。
- 配方變更不得修改既有方塊、物品或世界資料。
