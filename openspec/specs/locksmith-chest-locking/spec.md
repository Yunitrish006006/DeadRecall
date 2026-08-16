# 圖靈騰鎖匠：箱鎖規格

## Purpose

TotemLocksmith 為多人 Fabric 伺服器提供可見、可管理且由 Server 掌權的
固定式容器鎖。它保護的是容器內容存取與 Hopper 儲存網路，不承擔領地
所有權，也不把玩家可偽造的 Client 狀態當作權限依據。

## Requirements

### Requirement: 鎖定必須原子且由 Server 決定

Server SHALL 驗證目標、距離、權限、物品、容器拓撲及鎖數上限後，才建立
LockRecord、位置索引與 BlockEntity Lock UUID attachment，並消耗掛鎖。
失敗不得消耗物品或留下半套 marker／索引。

#### Scenario: 兩名玩家同 tick 上鎖

- **GIVEN** 一個尚未上鎖的箱子
- **WHEN** 兩名玩家在同一 Server tick 嘗試套用掛鎖
- **THEN** 只能有一筆 LockRecord 成功
- **AND** 只能消耗成功者的一個掛鎖
- **AND** 另一名玩家收到本地化失敗原因

### Requirement: 所有內容存取必須通過同一政策

Server SHALL 讓玩家開啟、Menu click、快捷移動、漏斗、漏斗礦車、投擲器、
發射器及模組直接 Container 操作解析同一個權威 LockRecord 與 AccessOperation。

#### Scenario: 未授權玩家嘗試開箱

- **GIVEN** 私人鎖定容器
- **WHEN** 非 Owner、非成員且未持有效鑰匙的玩家互動
- **THEN** Server 不建立容器 Menu
- **AND** 不向該 Client 傳送內容或 ACL
- **AND** Client 只收到泛用拒絕訊息與原版風格鎖聲

### Requirement: 權限優先序必須固定

存取 SHALL 依 Administrator bypass、Owner、Blocked、Manager、有效鑰匙、
模式啟用的 User/Friend/Public、拒絕的順序判定。Blocked 必須覆蓋鑰匙、
好友及公開模式，但不得覆蓋 Owner 或明確 Administrator bypass。

#### Scenario: 公開箱封鎖特定玩家

- **GIVEN** 一個 PUBLIC 容器且玩家被標記為 Blocked
- **WHEN** 該玩家持有效鑰匙互動
- **THEN** 存取仍被拒絕

### Requirement: 鑰匙必須可撤銷

每把 Bound Key SHALL 有 Lock UUID、Key UUID 與 Key Epoch。Server 只接受
仍存在於該鎖 active-key registry 且 epoch 相符的鑰匙。

#### Scenario: 輪替全部鑰匙

- **GIVEN** 多把有效鑰匙
- **WHEN** Owner 確認 Rotate Keys
- **THEN** Server 遞增 epoch 並清空 active-key registry
- **AND** 所有舊鑰匙在下一次存取與已開啟 Menu 重驗證時失效

### Requirement: 雙箱必須共用一把鎖

相連的雙箱 SHALL 使用一筆 LockRecord、同一個 revision 與兩個位置索引。
不得存在只保護其中一半的可用狀態。

#### Scenario: Owner 拆除雙箱一半

- **GIVEN** 一個已鎖雙箱
- **WHEN** Owner 合法破壞其中一半
- **THEN** 剩餘單箱保留相同 Lock UUID 與所有權
- **AND** 被破壞一半依原版處理內容
- **AND** 不額外掉落第二個掛鎖

### Requirement: Hopper 網路必須共用一把鎖

玩家對支援容器套用 Padlock 時，Server SHALL 將經固定 Hopper transfer
route 連接的支援容器與 Hopper 解析成一個弱連通網路。整個網路 SHALL
只使用一筆 LockRecord 與一個 Padlock，並將玩家實際套鎖的邏輯容器保存為
root container。雙箱在此圖中是一個具有兩個 BlockPos 的邏輯容器節點，
不是本 Requirement 所稱的「相連容器網路」。

網路分裂後 SHALL 只有包含 root container 的 component 保留 Lock UUID、
Owner、ACL、keys 與模式。其他 surviving components SHALL 在同一 topology
commit 原子解除鎖定，不得複製 LockRecord、Key 權限或 Padlock。

#### Scenario: 中間 Hopper 被拆除

- **GIVEN** root 箱與其他箱子透過一串 Hopper 共用一把鎖
- **WHEN** 中間 Hopper 成功被破壞並使網路分成兩側
- **THEN** 包含最初上鎖 root 箱的那一側保留原 Lock UUID 與全部權限
- **AND** 另一側的容器與 Hopper attachment／index 原子移除並立即解除鎖定
- **AND** 不複製或掉落 Padlock

#### Scenario: Root 容器被拆除

- **GIVEN** root 容器仍連接至少一個其他支援容器
- **WHEN** root 容器成功被破壞
- **THEN** Server 在 commit 前的 root component 中選出確定性 successor
- **AND** 只有 successor 所在的 surviving component 保留原 Lock UUID
- **AND** successor 依最短 graph distance、再依 Dimension + BlockPos 排序
- **AND** 直到該鎖最後一個支援容器被拆除前都不掉落 Padlock

### Requirement: 非 Owner 破壞必須允許並警報

Locksmith SHALL NOT 只因破壞者不是 Owner 而取消已鎖容器或 member Hopper
的玩家破壞。
方塊依原版及其他 protection 規則成功移除、鎖 topology 完成更新後，若
actor UUID 不等於 Owner UUID，Server SHALL 發布一個破壞事件供
DiscordBridge 傳送永久警報。

#### Scenario: 非 Owner 拆除中間 Hopper

- **GIVEN** 多個箱子以 Hopper 形成共用單一 Lock UUID 的已鎖網路
- **WHEN** 非 Owner 成功破壞使網路分裂的中間 Hopper
- **THEN** 該 Hopper 依原版處理方塊與內容
- **AND** root 側保留同一 Lock UUID、Owner、ACL、keys 及模式
- **AND** 非 root 側原子解除鎖定
- **AND** 不掉落 Padlock
- **AND** 只發布一個含 remainingLockedContainers 與
  detachedUnlockedContainers 的破壞事件

#### Scenario: Discord 傳送失敗

- **GIVEN** 非 Owner 已成功破壞上鎖容器
- **WHEN** DiscordBridge 未安裝、停用或傳送失敗
- **THEN** 已完成的破壞與鎖 topology 不得回滾
- **AND** Server 保留非敏感的本地 audit／失敗診斷

### Requirement: 匿名自動化預設拒絕

新鎖 SHALL 預設為 DENY automation。沒有可驗證 actor UUID 的漏斗、
漏斗礦車、投擲器及發射器不得輸入或輸出。

#### Scenario: 漏斗嘗試抽取

- **GIVEN** 預設自動化模式的鎖定箱
- **WHEN** 任一方向的漏斗嘗試抽取
- **THEN** 物品與數量完全不變
- **AND** 不每 tick 洗版玩家訊息或 Server log

### Requirement: 不一致資料必須 fail closed

位置索引、容器拓撲、LockRecord 或 adapter 結果互相矛盾時 SHALL 拒絕一般
存取並建立診斷，不得推測擁有者、清空內容或自動刪除資料。

#### Scenario: 世界編輯器移除箱子

- **GIVEN** SavedData 仍有一筆鎖但方塊已被外部工具替換
- **WHEN** 該位置再次載入或由管理員掃描
- **THEN** 記錄標記為 ORPHANED
- **AND** 只有確認過的管理修復可移除或重新綁定記錄

### Requirement: UI 與視覺必須符合原版語言

管理畫面 SHALL 使用原版字型、Widget、焦點、Narration、Tooltip 與整數
GUI 座標。掛鎖 SHALL 是低解析度原版材質語言的附著模型，而非螢幕浮圖。

#### Scenario: 雙箱開啟

- **GIVEN** 掛鎖位於雙箱正面接縫
- **WHEN** 已授權玩家開啟箱蓋
- **THEN** 鎖模型方向與動畫保持對齊
- **AND** 不渲染兩把重疊掛鎖

## Detailed contract

## 1. 平台

- Fabric。
- Minecraft 26.2。
- Java 25。
- 必要依賴 TotemCore。
- Mod ID `totem-locksmith`。
- Package `dev.totem.locksmith`。
- Registry namespace `totem:locksmith/*`。
- 第一版模組版本 `0.1.0`。

DeadRecall 是 compatibility bundle，不是箱鎖資料權威。獨立模組及 bundle
不得同時載入兩份相同註冊。

## 2. 容器

原生支援：

- `minecraft:chest`。
- `minecraft:trapped_chest`。
- `minecraft:barrel`。

網路 connector 支援：

- `minecraft:hopper`。Hopper 不能單獨套用 Padlock，但若其 vanilla pull
  source 或 facing destination 連到已鎖網路，就成為同一 LockRecord 的
  connector／inventory member，開啟、內容 transfer、破壞與分裂皆受規格
  管理。

Hopper route 是有方向的 vanilla transfer route；Locksmith 計算網路
component 時忽略方向，以 weak connectivity 決定同一保護邊界。只有實際
可作為 Hopper pull source 或 facing destination 的 BlockEntity 邊界建立
edge；單純物理相鄰、Hopper Minecart、Dropper、Dispenser、item entity 及
未註冊模組管線不建立網路 membership，仍只作為 automation actor 評估。

不支援：

- Ender Chest，因其內容本來就依玩家隔離。
- Shulker Box，因其可搬動且需要 ItemStack 所有權與防複製規格。
- Furnace、Smoker、Blast Furnace、Brewing Stand、Crafter 等工作容器。
- Remnant backpack、Bundle 及其他可攜式容器。
- 未註冊 adapter 的第三方容器。

第三方 adapter 必須定義：

- 可鎖拓撲與所有成員位置。
- 是否可搬動。
- 開啟、插入、抽取及破壞 hook。
- 視覺 anchor、朝向與動畫。
- Block replacement、chunk unload 與資料修復行為。

第三方 network connector adapter 另須定義 transfer edges、connector
inventory、分裂行為及 traversal 上限；只有容器 adapter 不會自動加入
Hopper 網路。

可搬動容器在第一版 API 中一律拒絕註冊。

具有尚未展開 loot table 的 Randomizable Container 預設不可上鎖，避免玩家
以低成本占有結構戰利品。設定允許時，Server 必須先依原版流程安全展開
loot table，再建立鎖。

## 3. 物品

註冊三個物品：

| ID | 用途 | 堆疊 |
| --- | --- | --- |
| `totem:locksmith/padlock` | 套用一把新鎖 | 16 |
| `totem:locksmith/key_blank` | 製作可綁定鑰匙 | 16 |
| `totem:locksmith/key` | 承載一筆 KeyBinding | 1 |

初始資料驅動配方：

- Padlock：四個 Iron Ingot 圍繞一個 Tripwire Hook，產出一個。
- Key Blank：一個 Iron Ingot 加兩個 Iron Nugget，產出兩個。

配方可由 data pack 覆寫；registry ID 不因平衡調整改變。

Key 的 Data Component 為 `totem:locksmith/key_binding`，至少包含：

- `dataVersion`。
- `lockId`。
- `keyId`。
- `epoch`。
- 非權威的顯示標籤。

Bound Key 不保存位置、Owner 名稱、ACL 或容器內容。將 Bound Key 單獨放入
crafting grid 可清除 binding 並回收成一把 Key Blank。這是刻意銷毀權限，
不需要接觸原鎖。

## 4. 上鎖

玩家以 Padlock 對支援容器使用。Server 依序驗證：

1. 玩家仍在線且不在 Spectator。
2. 互動距離、Dimension、目標位置及 hit result 合法。
3. 原版與 protection hook 允許玩家修改該方塊。
4. 目標 adapter 支援，且完整 Hopper weak component 已載入並在節點上限內。
5. 所有容器與 connector 位置尚未被另一把鎖索引。
6. 容器沒有有效原版 LockCode。
7. 未展開 loot table 符合設定。
8. 玩家未超過鎖數上限，或具有 bypass permission。
9. 主手或副手仍持有提交互動的同一疊 Padlock。

成功流程：

1. 建立 UUID LockRecord、root container 與全部位置索引。
2. 在每個容器及 Hopper connector BlockEntity 寫入相同 Lock UUID
   persistent attachment。
3. 設定 Owner、PRIVATE、DENY、epoch 1、revision 1。
4. 將 SavedData 與 BlockEntity 標記 dirty。
5. 非 Creative 玩家消耗一個 Padlock。
6. 發布視覺更新、音效及建立稽核事件。

任一步失敗都必須 rollback 記錄、索引與物品變更。普通世界存檔與 restart
後必須維持一致；規格不宣稱能跨越作業系統在檔案寫入中的不可恢復損壞。

Padlock 套用與 Owner 擴充網路不得 force-load chunk。任何可達 edge 進入
未載入 chunk、遇到未完成 BlockEntity 或超過 `maxNetworkNodesPerLock` 時，
整個 topology mutation fail closed 且不消耗物品。已保存網路之後單純
chunk unload 不構成分裂，也不得因此解除鎖定。

## 5. 角色

每把鎖恰有一位 Owner。成員表可包含 Manager、User 或 Blocked。

| 能力 | Owner | Manager | User | Key/Friend/Public |
| --- | --- | --- | --- | --- |
| 開啟及修改內容 | 是 | 是 | 是 | 是 |
| 檢視一般設定 | 是 | 是 | 否 | 否 |
| 新增、移除 User/Blocked | 是 | 是 | 否 | 否 |
| 發行、撤銷一般鑰匙 | 是 | 是 | 否 | 否 |
| 新增或移除 Manager | 是 | 否 | 否 | 否 |
| 更改 AccessMode | 是 | 否 | 否 | 否 |
| 更改 AutomationMode | 是 | 否 | 否 | 否 |
| 輪替全部鑰匙 | 是 | 否 | 否 | 否 |
| 轉移所有權 | 是 | 否 | 否 | 否 |
| 移除鎖並保留容器 | 是 | 否 | 否 | 否 |
| 破壞容器，若原版／領地允許 | 是 | 是* | 是* | 是* |

Administrator 不因 Creative、Spectator 或 OP 身分自動混入一般角色。
只有明確 permission node 或其設定的原版 permission-level fallback 才能
執行 inspect 或 bypass。管理 bypass 必須留下稽核。

Manager 不得修改 Owner、其他 Manager 或自己角色。Owner 不能被 Blocked。

`*` 任何非 Owner 的成功破壞都會產生 DiscordBridge 安全警報。Manager、
User、Key Holder、Friend、Public 或 Blocked 身分都不會豁免；角色只影響
開啟與管理，不把破壞改成硬性禁止。

## 6. 模式

AccessMode：

- `PRIVATE`：Owner、Manager 或有效鑰匙；已保存的 User 暫不生效。
- `ALLOWLIST`：上述來源加上 User。
- `FRIENDS`：上述來源加上 Nexus 回報的 Owner 雙向好友。
- `PUBLIC`：上述來源加上所有未被 Blocked 的玩家。

切換模式不刪除 ACL。這讓 Owner 能暫時切回 PRIVATE，而不必刪除之後還要
恢復的 User 名單。

Nexus 未安裝、API 版本不相容或查詢失敗時，FRIENDS 動態授權視為 false，
但 Owner、明確成員及鑰匙仍正常運作。Client 顯示本地化警告，不讓
Locksmith 因可選模組缺席而崩潰。

授權優先序：

1. 明確 Administrator bypass。
2. Owner。
3. Blocked 拒絕。
4. Manager。
5. 目前持有的有效 Bound Key。
6. 依目前模式啟用的 User、Friend 或 Public。
7. 拒絕。

公開、好友及鑰匙只授予內容使用，不授予設定或 Remove Lock。玩家破壞是
獨立的原版行為；非 Owner 成功破壞一律依本規格警報。

## 7. 鑰匙

發行鑰匙時，Owner 或 Manager 開啟管理 Menu，把 Key Blank 放入權威 key
slot，並確認 Bind。Server：

1. 重驗證管理 session、距離、角色、revision 及 active key 上限。
2. 從 slot 消耗一把 Key Blank。
3. 建立隨機 Key UUID 並加入 LockRecord。
4. 產生 max-stack-size 1 的 Bound Key。
5. 將新鑰匙放回 slot；若無空間則保持在 Menu，關閉時安全歸還或掉落。

KeyGrant 至少保存 Key UUID、建立者 UUID、建立時間、可選標籤及 epoch。
鑰匙只提供內容使用權，不提供 Manager 權限。

玩家必須在主手或副手持有效鑰匙才能以鑰匙開啟。Menu 存續期間每次變更前
重新驗證；移走、丟棄、撤銷或輪替鑰匙後，Server 在下一次 mutation 前
關閉 Menu。名單授權者不需要持續持有鑰匙。

單把 Revoke 只移除指定 Key UUID。Rotate Keys 遞增 epoch 並清空 registry。
兩者都增加 LockRecord revision。撤銷不嘗試搜尋或刪除世界中的實體物品；
舊鑰匙顯示 Invalid/Revoked tooltip，且可回收為 Key Blank。

## 8. 容器拓撲

### 雙箱節點

雙箱的兩個 BlockPos 依 Dimension、座標穩定排序，在 Hopper 網路圖中視為
一個邏輯容器節點。Client 只在該節點正面接縫渲染一個鎖；箱子與陷阱箱
不會互相合併。

- 對雙箱任一半套用 Padlock，兩半同時加入同一 LockRecord。
- 非 Owner 放置會與已鎖箱合併的新箱時，取消放置且不消耗物品。
- Owner 放置第二半時，可將新位置原子加入既有 logical node。
- 兩個不同 Lock UUID 的箱子永不合併。
- 已鎖箱不得透過方塊狀態更新形成半鎖雙箱。
- 任一玩家成功破壞一半時，剩餘半仍是原 network node；若這次移除同時
  造成 Hopper 網路分裂，接著套用 root-component 規則。
- 任一半未載入或不吻合時，拓撲修改 fail closed。

### Hopper 網路

LockRecord 保存一個 root logical container、全部 container positions、全部
Hopper connector positions 及 topology revision。最初實際套用 Padlock 的
單箱、雙箱或木桶是 root；一個 Padlock 鎖住 resolver 找到的整個 weak
component。

- 同一 Lock UUID 內部的 Hopper transfer 永遠視為保護邊界內移動；即使
  AutomationMode 是 DENY 也允許原版 transfer。
- Owner 放置新支援容器或固定 Hopper 並接到一個已鎖 component 時，Server
  在完整 component 已載入且未超過上限後，原子加入同一 LockRecord，不
  消耗第二個 Padlock。
- 非 Owner 不得以 placement 擴充已鎖 component；會建立 edge 的 placement
  取消並依原版安全返還方塊物品。
- 會連接兩個不同 Lock UUID 的 placement 一律拒絕；第一版不自動合併鎖、
  不猜測要保留哪一把，也不自動返還 Padlock。
- Hopper facing 或鄰接狀態變更若會新增 edge，套用與 placement 相同的
  Owner、完整載入、上限及 multi-lock 驗證。

### 網路分裂

成功移除容器或 Hopper connector 後，Server 在同一 transaction 重算
surviving components：

1. 若 commit 前的 root container 仍存在，只有包含它的 component 保留鎖。
2. 其他 surviving components 移除 index 與 attachment，立即成為未鎖；
   它們不取得 LockRecord、ACL、keys 或 Padlock。
3. 若 root 本身被移除，先在 commit 前的 root component 中選出仍存在的
   支援容器 successor：graph distance 離舊 root 最近者優先，再依
   Dimension identifier 與 BlockPos 升冪決勝。只有 successor 所在的
   surviving component 保留鎖並將 successor 設為新 root。
4. Hopper connector 不能成為 root。若已沒有任何支援容器，刪除整筆
   LockRecord 與所有剩餘 connector marker，並最多產生一個 Padlock。
5. 每次 topology commit 都增加 revision、同步所有 attachment，並關閉
   既有舊 Menu。

因此從中間拆開一串 Hopper 箱時，鎖只留在「最初上鎖的箱子那邊」；另一
半不是第二把鎖，也不會保持鎖定。拆分本身不掉 Padlock，唯一 Padlock
只在 Remove Lock 或根側最後一個支援容器被拆除時回收／掉落。

## 9. 自動化

AutomationMode：

- `DENY`：拒絕所有非玩家插入與抽取。
- `TRUSTED`：只接受 adapter 提供可驗證 actor UUID 且該 actor 有內容使用權
  的操作。
- `ALL`：允許原版匿名與已識別自動化。

新鎖預設 DENY。只有 Owner 可切換至 ALL，GUI 必須警告已核准的邊界
automation endpoint 可匿名輸入或取出內容；非 Owner 仍不能靠 placement
擴充已鎖 Hopper component。

AutomationMode 只控制跨 LockRecord 保護邊界的 transfer。同一 Lock UUID
內部的 container-to-Hopper、Hopper-to-Hopper 及 Hopper-to-container route
即使在 DENY 仍可依原版移動，因為物品沒有離開受保護網路。未鎖 endpoint、
不同 Lock UUID 或 unsupported inventory 都是邊界；必須依來源 EXTRACT 與
目的地 INSERT 分別通過政策，不能因其中一端是 member 而自動放行。

必須涵蓋：

- Hopper 六面輸入與輸出。
- Hopper Minecart。
- Dropper。
- Dispenser。
- Fabric Transfer API 或當前平台等價入口。
- TotemAutomata 直接 Container remove/set/insert 路徑。
- Addon 透過公開 API 宣告的操作。

被拒絕時不消耗、不複製、不改 cooldown 作為成功，也不改變來源或目的地。
診斷採每個 Lock UUID + route 的 rate limit，不每 tick 寫 log。

Comparator 讀值預設維持原版。鎖保證內容不可取用，不承諾隱藏箱內大致
裝滿程度。

## 10. 玩家 Menu

開箱前 Server 計算權限。授權後才建立原版容器 Menu。Menu 的
`stillValid` 或等價 authority 必須重驗證：

- 玩家仍在線。
- 距離及 Dimension。
- Lock UUID 與拓撲。
- Lock revision 或目前授權來源。
- Blocked、ACL、friend、public 或 key 狀態。

權限撤銷、鎖移除、拓撲變化、鑰匙離手或玩家離開距離後，Server 關閉
Menu；未通過重驗證的 click、shift-click、drag、number-key、double-click
及 pickup-all 不得修改 ItemStack。

Spectator 不可讀取內容，除非具有 admin inspect permission。Creative 不
自動 bypass。

未授權回覆不得包含內容、成員、Key UUID、Owner UUID 或精確稽核資訊。
是否顯示 Owner 名稱由 Server config 決定，預設關閉。

## 11. 管理 Menu

Owner 以空手 Sneak + Use 開啟管理 Menu；Manager 以相同入口開啟受限版本。
若玩家手持 Padlock 或 Bound Key，物品本身的互動優先。

畫面使用原版 176-pixel container panel；內容超出時使用原生分頁或滾動
列表，不縮放字型。至少包含：

- 狀態：容器種類、模式、自動化、有效 keys 數及成員數。
- Access：成員列表、角色、加入、移除與 Blocked。
- Keys：單一 Server-backed key slot、綁定、命名、撤銷及輪替。
- Settings：模式、自動化與安全選項。
- Owner-only：轉移、移除鎖。

加入玩家接受線上選擇、Server profile cache 中的確切名稱或 UUID。Server
不依賴 Client 傳回的名稱作權威，也不向第三方網路查詢玩家。

每個管理 session 有隨機 session UUID、目標 Lock UUID、開啟位置、到期時間
及 snapshot revision。所有 mutation payload 只送 session UUID、預期
revision 與操作所需最小值。Server 不信任 Client 座標、Owner 或現有 ACL。

Stale revision 回傳最新安全 snapshot，不套用部分變更。轉移、Rotate Keys、
Remove Lock 需要短效確認 token，綁定 actor、lock、operation、revision。

## 12. 指令

玩家指令：

    /locksmith info
    /locksmith access add <player|uuid> <user|blocked>
    /locksmith access remove <player|uuid>
    /locksmith mode <private|allowlist|friends|public>
    /locksmith automation <deny|trusted|all>
    /locksmith keys revoke <key-uuid>
    /locksmith keys rotate
    /locksmith transfer <player|uuid>
    /locksmith remove

一般指令以玩家準星指向、可互動距離內的容器為目標，不能提交任意遠端
BlockPos。權限與 GUI 相同；危險操作需要可點擊確認或再次帶 confirm token。

管理指令：

    /locksmith admin inspect <dimension> <x> <y> <z>
    /locksmith admin list [owner] [state]
    /locksmith admin bypass-open <dimension> <x> <y> <z>
    /locksmith admin transfer <dimension> <x> <y> <z> <player|uuid>
    /locksmith admin remove <dimension> <x> <y> <z>
    /locksmith admin repair <dimension> <x> <y> <z>
    /locksmith admin orphans scan [dimension]
    /locksmith admin orphans purge <confirmation>

`list` 與 `scan` 分頁且有每 tick budget；不 force-load 全世界 chunk。
`purge` 必須先 dry-run，confirmation 綁定掃描摘要與 expiry。Admin transfer
採安全重設：清空 ACL 與 active keys、epoch +1、PRIVATE、DENY，再交給新
Owner。

## 13. 方塊生命週期

玩家破壞：

- Locksmith 不因 actor 不是 Owner 而取消破壞。
- 原版 GameMode、Adventure tool restrictions、spawn protection、Fabric
  break event 與領地模組仍可拒絕；Locksmith 不繞過它們。
- 方塊未真正移除時不更新 topology、不掉鎖，也不送成功警報。
- Owner 成功破壞不送安全警報。
- Manager、User、Key Holder、Friend、Public、Blocked 或一般陌生玩家只要
  不是 Owner，成功破壞後都送一個警報。
- 具有 admin permission 的玩家若用一般挖掘破壞，仍按非 Owner 警報；只有
  明確 admin command mutation 改走管理 audit。
- 破壞容器或已納入鎖的 Hopper connector 後重算 component；只有 root
  container 所在側保留同一把鎖，其他 surviving components 原子移除 index
  與 attachment 並解除鎖定。
- 拆分不複製或掉落 Padlock。root 被拆除時依確定性 successor 規則移交；
  根側最後一個支援容器被破壞時才移除 LockRecord 與剩餘 connector marker，
  並在該破壞位置最多掉落一個 Padlock。
- 非 Owner 成功破壞使網路分裂的 Hopper connector 也必須警報；不能把拆
  connector 當成無警報解除半邊鎖的繞過路徑。
- Owner 可先由管理 Menu Remove Lock，取得一個 Padlock 並保留全部容器。
- 非 Owner 不能使用 Remove Lock 取得掛鎖而保留容器；只能依原版挖掘。

環境：

- `protectFromExplosions=true` 時，爆炸、Wither 及其他非 actor
  destruction 不得摧毀鎖定容器或已納入鎖的 Hopper connector。
- 設定關閉時，容器與內容依原版破壞規則處理；記錄與索引必須移除，
  Padlock 最多生成一次且可被爆炸繼續摧毀。
- 支援容器與已納入鎖的 Hopper connector 活塞移動一律拒絕，即使其他
  模組允許搬動 BlockEntity。
- Fire、waterlogging、redstone 與 comparator 保留原版非破壞行為。
- 陷阱箱只有在授權 Menu 真正開啟時產生原版 redstone 狀態。

第一版的非 Owner Discord 警報只針對有明確 ServerPlayer actor 的已鎖容器
或 connector 成功破壞。無法可靠歸因的爆炸或管理型 world edit 使用環境／
管理 audit，不把不確定的玩家標成破壞者。

由 `/setblock`、structure、WorldEdit 或其他管理工具繞過 hook 的變更視為
管理行為。下一次 chunk validation 標記孤兒或衝突，不自行猜測修復。

## 14. 資料

Server-wide `LocksmithSavedData` 由 Overworld data storage 掛載，但位置
鍵包含 Dimension。SavedData key：

    totem_locksmith_locks

頂層：

- `dataVersion`。
- `records`：Lock UUID 到 LockRecord。
- codec 載入後重建的 position index；index 不另存第二份權威資料。

每個受保護 Chest／Trapped Chest／Barrel 與納入網路的 Hopper BlockEntity
另保存一個 Fabric persistent attachment：

    totem:locksmith/lock_id

Attachment 只是一個 Lock UUID marker，不含 Owner、ACL、keys 或內容，也不
單獨授權。它用來在 SavedData/index 遺失、structure copy 或外部 replacement
時偵測矛盾。Client 視覺仍使用最小 visual payload，不直接同步 attachment。

LockRecord：

- `lockId`。
- `ownerUuid`。
- `ownerLastKnownName`，僅顯示。
- `rootContainer`：logical container kind 與其一或兩個排序後的位置。
- `containers`：排序後的 logical container nodes；雙箱 node 有兩個位置。
- `connectors`：排序後的 Hopper positions。
- `topologySchema`：Hopper edge resolver 版本。
- `accessMode`。
- `automationMode`。
- `members`：UUID、最後已知名稱、role。
- `activeKeys`：Key UUID、creator UUID、createdAt、label、epoch。
- `keyEpoch`。
- `revision`。
- `createdAt`、`updatedAt`。
- `state`：ACTIVE、REPAIR_REQUIRED、ORPHANED、CONFLICT。
- `dataVersion`。

不保存：

- 容器內容副本。
- 玩家密碼。
- Client session。
- 原版 Menu state。
- 每一次正常物品 click 的永久 audit。

限制：

- 每位 Owner 預設 128 鎖。
- 每鎖 32 members。
- 每鎖 32 active keys。
- 每鎖預設 128 個 topology positions，包含容器與 Hopper；套鎖或擴充超限
  時整筆拒絕，不截斷、不留下未受保護尾端。
- label 最多 32 Unicode code points，移除控制字元。
- 成員顯示名稱有上限且不作主鍵。

資料變更只在 Server thread。Codec 拒絕負數 epoch/revision、重複位置、
超量集合、無效 Dimension ID 與重複 Lock UUID。局部損壞不得讓整份檔案
靜默歸零；可隔離的壞記錄進入 diagnostics，整體解碼失敗則保留備份並停止
一般鎖操作。

## 15. 修復

每次互動、chunk load 及有限管理掃描可執行 O(1) 或該 chunk 內的驗證：

- 位置是否仍是預期容器。
- BlockEntity attachment、position index 與 LockRecord UUID 是否一致。
- 雙箱 partner 是否吻合。
- rootContainer 是否仍是同一 record 的有效 logical container。
- Hopper connector 是否仍形成已保存的 vanilla transfer edge，且所有成員
  是否仍屬 root weak component。
- 一個位置是否指向多筆記錄。
- 原版 LockCode 是否形成雙重鎖。
- Owner、revision、epoch 與集合限制是否合法。

一般玩家遇到不一致時 fail closed。Admin inspect 顯示診斷，不顯示容器內容
除非另有 inspect-content 權限。

Repair 只允許：

- 重建遺失的衍生 position index。
- 依仍有效的唯一 LockRecord 補回缺少的 attachment。
- 將同一 Lock UUID 的合法剩餘半降為單箱。
- 在 root 明確有效時，依目前已載入 topology 移除與 root 分離的 stale
  member markers；被分離 component 只解除鎖定，不取得新 record 或 Padlock。
- root 已不存在時，只有完整 commit journal／audit 足以證明唯一 successor
  才可重綁；否則維持 REPAIR_REQUIRED，不以座標猜測。
- 由明確位置與 Owner 確認重新綁定一筆孤兒記錄。
- 移除已確認不存在的孤兒。

Repair 不合併不同 Lock UUID、不猜 Owner、不覆寫容器內容，也不自動解除
原版 LockCode。

## 16. 所有權

所有權以 UUID 保存。玩家改名只更新 last-known name。

Owner transfer：

1. 驗證新 Owner UUID 不是目前 Owner 且符合鎖數限制。
2. 取得短效確認。
3. 清空 members 及 active keys。
4. epoch +1。
5. 設為 PRIVATE、DENY。
6. 更換 Owner、revision +1。
7. 關閉所有既有 Menu 並發布 audit。

玩家死亡、離線或改名不解除鎖。預設沒有 inactivity expiry。Server 若日後
需要棄置規則，必須另提 OpenSpec，不能只用 `lastSeen` 自動刪除。

## 17. Networking

Payload 每個只表示一項操作，不使用字串 operation + 任意資料。

Clientbound 類別：

- Management snapshot。
- Mutation result。
- Lock visual state。
- Session invalidation。

Serverbound 類別：

- Add/remove member。
- Change role。
- Change access mode。
- Change automation mode。
- Bind/revoke/rotate key。
- Transfer owner。
- Remove lock。
- Confirm destructive action。

每個 mutation 都包含 session UUID 與 expected revision。Receiver 切回 Server
thread，重新驗證 session、距離、Dimension、LockRecord、角色、物品及上限。

Clientbound management snapshot 只送 actor 有權查看的欄位。公開 visual
snapshot 只包含位置、容器 kind、anchor/orientation、locked state 與 visual
revision，不包含 Owner、ACL、keys 或內容。

追蹤 chunk 時傳送初始 visual snapshot；離開追蹤範圍清除 Client cache。
封包重送、亂序或重複到達不得重複消耗 Key Blank、Padlock 或產生 audit。

## 18. API

Locksmith 擁有 `dev.totem.locksmith.api.v1`，至少提供：

- `LocksmithAccessApi.evaluate(context)`。
- `LockableContainerAdapterRegistry`。
- `AccessOperation`：OPEN、INSERT、EXTRACT、BREAK、CONFIGURE。
- `AccessActor`：PLAYER、IDENTIFIED_AUTOMATION、ANONYMOUS_AUTOMATION、
  ENVIRONMENT、ADMIN。
- immutable `AccessDecision`，包含 allow/deny 與非敏感 reason key。
- lock-created、lock-removed、owner-transferred 事件。

公開 API 不回傳 mutable LockRecord、完整 ACL、active Key UUID 清單或內容。
第三方查詢必須在 Server thread，未知 adapter 及例外預設拒絕並節流記錄。

BREAK decision 不以 Lock ownership 阻擋玩家挖掘，而是回傳 break
disposition：

- `OWNER_BREAK`：成功後更新 topology，不送安全警報。
- `NON_OWNER_ALERT`：成功後更新 topology 並發布破壞事件。
- `ADMIN_MUTATION`：只供明確 admin command mutation，依管理 audit 處理。
- `ENVIRONMENT`：依 explosion 設定處理。

原版或 protection hook 的 deny 永遠優先，且 deny 不發布「成功破壞」事件。

Core 新增版本化 `LockedContainerNetworkBrokenEvent`，適用於上鎖容器或網路
Hopper connector 的成功破壞，至少包含：

- event UUID 與 Lock UUID。
- actor UUID／最後已知名稱。
- Owner UUID／最後已知名稱。
- broken member kind、Dimension 與被破壞的 BlockPos。
- commit 後 root component 的 `remainingLockedContainers`。
- 本次分裂後解除鎖定的 `detachedUnlockedContainers`。
- `rootMoved`。
- `lockRemoved`。
- Server timestamp。

`remainingLockedContainers` 與 `detachedUnlockedContainers` 只計 logical
container nodes，不計 Hopper connector；`lockRemoved` 恰等於
`remainingLockedContainers == 0`。事件不包含容器內容、ACL、Key UUID、
完整 root 座標清單或 Discord secret。Locksmith 在方塊移除與 topology
commit 成功後只發布一次。DiscordBridge 訂閱後使用 event ID 做 bounded
dedup，轉成永久 `locked_container_network_broken` 訊息；subscriber、Worker
或 Discord 失敗都不能回滾 gameplay。

TotemAutomata 以可選 adapter 傳入可驗證 operator UUID；不能取得 operator
時視為匿名。TotemVillagers 只有在其 owner-aware adapter 驗證 Work Chest
Owner 與動作 actor 後才能使用 TRUSTED。任一整合缺席時兩個模組仍可獨立
啟動。

## 19. 原版相容

Minecraft 26.2 的 BaseContainerBlockEntity 仍有 LockCode。Locksmith 不把
LockCode 當作 UUID ownership，也不修改其 codec。

- 套用 Padlock 前若 `isLocked()` 為 true，拒絕並說明已有另一種鎖。
- 後續由管理指令加入 LockCode 時，原版與 Locksmith 兩者都要通過。
- Locksmith key 不偽裝成能通過任意 ItemPredicate 的原版 key。
- 移除 Locksmith lock 不移除原版 LockCode。
- Pick Block、clone item 或 Silk Touch 不把位置鎖資料寫入容器 ItemStack。
- 複製出的 attachment 若沒有相符位置 record，必須 fail closed 並由管理員
  移除 marker；不得據此複製 ownership。

## 20. 保護整合

上鎖、拆鎖、成員變更、破壞及管理操作先尊重：

- 原版 `mayInteract`、Adventure restrictions 及 spawn protection。
- Fabric interaction/break events。
- Locksmith cancellable protection hook。
- 已安裝且版本相容的領地 adapter。

Locksmith 不是領地模組。沒有標準 adapter 的第三方管理工具可能繞過方塊
事件；此情況由 fail-closed diagnostics 與 admin repair 處理，不宣稱完全
攔截未知 bytecode 修改。

## 21. 稽核

持久或 Server log audit 至少涵蓋：

- 建立及移除鎖。
- 成員與角色變更。
- 鑰匙發行、撤銷及輪替。
- 模式及自動化變更。
- 所有權轉移。
- Admin inspect-content、bypass、repair、remove 及 purge。

事件包含 actor UUID、action、Lock UUID 簡寫、Dimension、位置摘要、結果與
時間，不包含容器內容或完整 Key UUID。成功的管理變更完成後發布 Core
`AdminAuditEvent` 或相容的版本化事件；Discord subscriber 失敗不能回滾
已完成操作。

非 Owner 成功破壞上鎖容器或網路 Hopper 另發布
`LockedContainerNetworkBrokenEvent`，不冒充 `AdminAuditEvent`。Discord
格式至少包含破壞者、Owner、方塊種類、Dimension、座標，以及「root 側仍
鎖定 N 個容器／分離側 M 個容器已解除鎖定」或「根側最後容器已破壞，鎖已
掉落」。事件送往 DiscordBridge 已設定的事件頻道，沒有設定時只寫本地
audit，不自行選擇未知公開頻道。

一般開箱成功不永久記錄。拒絕事件依 actor + lock + reason 節流，避免漏斗
或惡意 Client 洗版。

## 22. 設定

Server config 初始欄位：

- `enabled=true`。
- `maxLocksPerPlayer=128`，0 表示無上限。
- `maxMembersPerLock=32`。
- `maxActiveKeysPerLock=32`。
- `maxNetworkNodesPerLock=128`，計算全部容器與 Hopper BlockPos。
- `defaultAccessMode=PRIVATE`。
- `defaultAutomationMode=DENY`。
- `protectFromExplosions=true`。
- `allowUnresolvedLootContainers=false`。
- `showOwnerOnDenied=false`。
- `adminInspectPermissionLevel=2`。
- `adminMutatePermissionLevel=3`。
- `denialLogCooldownTicks=200`。
- `orphanScanBudgetPerTick=64`。

降低上限不得刪除既有資料。超量記錄保持可讀及可移除，但不能新增成員、
keys 或鎖，直到回到限制內。設定 reload 在 Server thread 原子替換 snapshot，
無效檔案保留最後一份有效設定。

## 23. 視覺

- 模組 icon 使用共用 Totem 16x16 silhouette 與 iron padlock emblem。
- Padlock、Key Blank、Bound Key 使用實際 16x16 透明 PNG。
- gameplay padlock 先讀成原版風格鐵鎖，不強迫使用 Totem 外形。
- 材質使用原版鐵的左上光源、硬邊像素與整數 UV。
- 單箱及木桶使用一個小 cuboid attached model。
- 整個 Hopper 網路只在 root logical container 顯示一個模型，不在每個成員
  箱或 Hopper 複製掛鎖；root 是雙箱時只在前方接縫顯示。
- Chest/Trapped Chest 模型跟隨 lid openness；開啟時 shackle 狀態不得穿模。
- 網路從中間分裂而原 root 仍存在時，模型留在 root 側且分離側立即清除
  locked visual。root 被破壞時，唯一模型在同一 topology revision 移到
  successor anchor，不得有一幀同時顯示兩把鎖。
- Barrel 依 FACING 旋轉 anchor，不依 Client 猜測世界方向。
- 64x64 publishing icon 必須是 16x16 source 的精確 4x nearest-neighbor。

所有嚴格 16x16 資產先通過
`.agents/skills/totem-art-direction/scripts/verify-icon.sh`。GUI 需在 native
scale、整數放大及實際遊戲各 GUI scale 檢查。

## 24. 本地化

至少提供 `en_us` 與 `zh_tw`：

- 物品名稱與 Tooltip。
- 存取模式、角色、自動化模式。
- GUI 標籤、按鈕、placeholder、Narration。
- 拒絕、過期 revision、距離、資料修復及可選整合錯誤。
- 指令回覆及 confirmation。
- Invalid、Revoked、Orphaned Key 狀態。

程式碼不硬編碼玩家可見文字。翻譯不烘焙進 texture。繁中長字串不得碰撞
slot、按鈕或 scrollbar；必要時使用 Tooltip，不縮小原版字型。

## 25. 效能

- Lock lookup 由 Dimension + BlockPos index 提供平均 O(1)。
- 不以 global tick 掃描所有鎖。
- Chunk validation 只檢查目前載入 chunk 內有索引的位置。
- Orphan admin scan 分 tick、有取消與進度，不 force-load chunk。
- Hopper 拒絕不建立大量暫時集合、Payload 或 log。
- Client 只追蹤可見 chunk 的 visual state。
- 10,000 筆 LockRecord codec 與 lookup 壓力測試不得超出專案定義的 CI
  記憶體及時間基線；基線在實作前以空模組量測並記錄。

## 26. 驗收

必要自動化：

- Policy、role、Blocked precedence、key epoch/revoke 及 codec JUnit。
- 單箱、雙箱、陷阱箱、木桶 GameTest。
- Hopper chain 建網、root-side split、root successor、cycle、chunk boundary、
  內部 transfer，以及 Hopper Minecart、Dropper、Dispenser 與直接 Container
  邊界 transfer 測試。
- 玩家 Menu 所有 click mode 與權限中途撤銷測試。
- 同 tick 上鎖、開啟、transfer、破壞及 topology race。
- Explosion on/off、Owner break、非 Owner 容器／connector split、final break、
  Discord event 及 external replacement。
- 未展開 loot table、原版 LockCode 與可選模組缺席。
- SavedData seed、正常停止、重啟、verify 三 JVM probe。
- 偽造 session/Lock UUID、遠距、跨 Dimension、stale revision 及重送。
- Dedicated Server client-class isolation。
- Core only、Core + Locksmith、DeadRecall bundle 及 pairwise integrations。

必要視覺／人工：

- 單箱四方向、雙箱左右方向、Trapped Chest 與 Barrel 六方向。
- 箱蓋開啟、關閉及多人同時開啟動畫。
- GUI scale、鍵盤、Narration、English 與繁中。
- 1x item sprites、Tooltip 及暗／亮背景。
- 至少兩名真人玩家完成 Owner、User、Blocked、key transfer 及 revoke 流程。

視覺證據保存於：

    test-artifacts/screenshots/add-totem-locksmith-chest-locking/

完成定義：

- `./gradlew build --stacktrace` 以 Java 25 通過。
- 所有 required GameTest 與 restart probe 通過。
- GitHub CI 成功；未取得遠端成功不得宣稱 release ready。
- Modrinth 專案建立、描述與圖示審查只在獨立模組及 bundle gates 通過後執行。
