# Tasks: Discord zh-TW Event Localization

## 1. Localization infrastructure

- [x] 1.1 新增 Server-only `DiscordLocalizationService`，Discord-facing locale 固定為 `zh_tw`。
- [x] 1.2 純 Dedicated Server runtime 載入 DeadRecall 內建、Minecraft 26.2 鎖定的 immutable translation snapshot，不依賴 Client-only class。
- [ ] 1.3 Runtime resource reload 與原子 snapshot 替換；目前 Dedicated Server 不提供 Client `zh_tw.json`，第一階段採版本發布時更新的 bundled snapshot。
- [x] 1.4 實作 literal、translatable、巢狀參數、placeholder 與 sibling component 純文字解析。
- [x] 1.5a 未知 key 使用安全中文 fallback，最終 payload 不洩漏 raw translation key。
- [ ] 1.5b 對重複 missing-key warning 增加節流；目前 missing key 直接使用 deterministic fallback，不逐筆寫 warning。

## 2. Semantic event formatting

- [x] 2.1 Formatter、immutable payload、test observer 與既有 HTTP transport 分離。
- [x] 2.2 Advancement 傳遞未提前解析的 title Component 與 semantic frame type。
- [x] 2.3 `task`／`goal`／`challenge` 顯示為「進度」／「目標」／「挑戰」。
- [x] 2.4 村民升級傳遞 custom name、profession path、previous level 與 current level。
- [x] 2.5 未命名村民、profession 與 level 1–5 使用繁中名稱；自訂名稱維持 literal。
- [ ] 2.6 死亡訊息、Boss／實體預設名稱、raid result 與 difficulty display name 接入共用 localization service。
- [x] 2.7 玩家名稱、物品／村民自訂名稱與 nested literal text 不被翻譯或改寫。

## 3. Safety and compatibility

- [x] 3.1 保留既有 Worker endpoint、payload 欄位、event 名稱、多頻道路由與 Webhook／Bot Token fallback。
- [x] 3.2 Localization 例外在 formatter 邊界安全 fallback，不中止 advancement 或村民升級流程。
- [x] 3.3 非同步 HTTP worker 只接收 immutable localized strings，不讀取 Entity、Level 或 registry mutable state。
- [x] 3.4 不新增設定 migration、SavedData、世界資料或 identifier 變更。

## 4. Tests

- [x] 4.1 Vanilla advancement key 解析為預期繁中 title。
- [x] 4.2 Advancement task／goal／challenge 中文格式矩陣。
- [x] 4.3 巢狀 Component 保留玩家名稱、物品自訂名稱與格式參數。
- [x] 4.4 未知 translation key 使用安全 fallback，payload 不包含 raw key。
- [x] 4.5 未命名村民＋圖書管理員＋level 1→2 產生完整中文訊息。
- [x] 4.6 自訂村民名稱原樣保留，profession 與 level 仍中文化。
- [x] 4.7 Advancement 與村民各自 exactly-once 建立 Discord payload。
- [x] 4.8 Dedicated Server GameTest 載入 bundled snapshot，不依賴 Client language class。
- [ ] 4.9 真實 Worker／Discord 失敗時 localization event 不影響遊戲流程。
- [ ] 4.10 最新文件 head 的 Java 25 Validate、完整 Server GameTests 與兩套 restart probes 全部通過。

## 5. Documentation

- [ ] 5.1 更新 Discord Bridge 主規格與事件格式文件。
- [ ] 5.2 更新 `docs/discord/` 說明固定繁中、custom text 與 fallback 規則。
- [ ] 5.3 加入版本變更紀錄與人工 Discord 顯示驗收矩陣。
