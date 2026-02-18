# 玩家体验缺口分析报告

基于对核心代码的完整审阅：CombatState, KillerMoveExecutor, ToggleMoveManager, GuMasterEntity, GuMerchantEntity, WildGuEntity, CultivationOverlay, ApertureScreen, RadialMenuScreen, KillerMoveRegistry, ModKeybindings, CultivationEvents, 以及世界生成/维度系统。

---

## 高优先级

### 1. 战斗无伤害数字反馈

**问题**: 玩家释放杀招/技能后完全看不到造成了多少伤害。KillerMoveExecutor.execute() 只调用 effect.execute() 然后结束，没有任何伤害数字显示机制。MoveEffect执行后的伤害值对玩家完全不可见。

**影响**: 玩家无法评估杀招强弱、道路选择的影响、推演改良是否生效。战斗变成盲打，破坏了整个"计算-优化"循环。

**当前实现**:
- `KillerMoveExecutor.java:73` 计算出damage后直接传给MoveEffect
- `MoveEffect.execute()` 内部调用 `target.hurt()` 但不显示数字
- 原版Minecraft有DAMAGE_INDICATOR粒子但只显示心形图标，不显示数字

**建议方案**:
- 新增 DamageNumberRenderer (客户端)：在受伤实体位置生成浮动伤害数字
- 通过自定义 Payload 发送伤害数值到客户端
- 不同类型伤害用不同颜色：物理白色/真元蓝色/DOT绿色/暴击红色放大
- 偷袭/暴击额外显示特殊文字
- 参考风格：数字从受击点上浮0.5秒后消失，带轻微随机偏移防重叠

**涉及文件**:
- 新建: `client/render/DamageNumberRenderer.java`, `network/DamageNumberPayload.java`
- 修改: `core/combat/killermove/MoveEffectRegistry.java` (每个effect.execute后发送数字)
- 修改: `event/ModEvents.java` (LivingHurtEvent中捕获伤害数字)

**新增内容**: DamageNumberPayload, DamageNumberRenderer
**复杂度**: 中

---

### 2. NPC蛊师信息暴露过多——缺乏情报战机制

**问题**: GuMasterEntity 通过 SynchedEntityData 同步了 GU_RANK, PRIMARY_PATH, SECONDARY_PATH, DATA_FACTION 到客户端。getDisplayName() 直接显示 `[正道] 蛊师·月道`。玩家一看名牌就知道对手所有信息。这完全违背原著"蛊师对战首先是情报战"的核心设定。

**影响**: 消灭了战斗前的侦察和判断过程。每次遇敌都是"看名牌→判断打不打"，没有"探查→推测→试探→全力"的过程。

**当前实现**:
- `GuMasterEntity.java:65-72` 四个SynchedEntityData全量同步
- `GuMasterEntity.java:1140-1143` getDisplayName直接拼 `[阵营] 蛊师·道路`
- `GuMasterEntity.java:978-993` spawnPathAuraParticles 直接暴露道路粒子

**建议方案**:
**分层情报系统**：
1. **默认层**：只显示"蛊师"和阵营（阵营通过衣着颜色可见，合理）
2. **观察层**：注视目标2秒，显示大致境界范围（"一转~二转"而非准确数值）
3. **天眼蛊层**：拥有天眼蛊并激活后，显示准确境界、主道路
4. **完全扫描**：使用智道"所罗门之眼"类杀招，短暂显示全部信息（蛊虫装备、杀招列表）

**技术方案**：
- GuMasterEntity的SynchedEntityData只同步最小信息（阵营+大致强度）
- 详细信息通过IntelPayload按需发送（玩家观察/使用侦察蛊时）
- 客户端渲染器根据情报等级显示不同信息

**涉及文件**:
- 修改: `entity/GuMasterEntity.java` (限制SynchedEntityData, 分层名牌)
- 新建: `core/combat/IntelligenceManager.java` (情报收集和等级管理)
- 新建: `network/IntelPayload.java` (按需同步详细信息)
- 修改: `client/gui/CultivationOverlay.java` (添加目标信息HUD区域)

**新增内容**: IntelligenceManager, IntelPayload, 目标信息HUD
**复杂度**: 高

---

### 3. 蛊商交易品种严重不足——只卖20种商品

**问题**: GuMerchantEntity.updateTrades() 硬编码了20种固定商品。148种蛊虫中只有约13种可购买（9%覆盖率）。三转商人只多卖4种高级蛊。没有卖材料、配方、情报等服务。声望折扣是唯一的交易深度。

**影响**: 蛊商没有成为重要的资源获取渠道。中后期玩家完全不需要蛊商。

**当前实现**:
- `GuMerchantEntity.java:107-136` 固定20个MerchantOffer
- 没有根据蛊商自身道路定制商品的机制
- 没有轮换/刷新机制

**建议方案**:
1. **按道路定制商品**：蛊商也有道路属性，优先售卖自身道路的蛊虫
2. **动态商品池**：每个蛊商从自身道路中随机选5-8种蛊虫，加上通用商品
3. **稀有商品轮换**：每1200tick刷新1个"限时珍品"（高转蛊虫/稀有材料）
4. **服务类交易**：出售情报(NPC蛊师位置)、鉴定(蛊虫品质)、修复(受损蛊虫)
5. **反向交易**：玩家可以卖蛊虫给蛊商获得元石

**涉及文件**:
- 修改: `entity/GuMerchantEntity.java` (完全重写updateTrades, 添加道路属性)
- 修改: `registry/ModItems.java` (添加道路→物品映射辅助方法)

**新增内容**: 无需新类，重写现有逻辑
**复杂度**: 中

---

### 4. 杀招快捷键只有2个——无法承载61个杀招

**问题**: ModKeybindings 只定义了 KILLER_MOVE_1 (Z) 和 KILLER_MOVE_2 (X) 两个杀招键。玩家最多同时快捷使用2个杀招，其余必须打开转轮菜单。对于战斗频繁使用的杀招系统来说极其不方便。

**影响**: 限制了战斗节奏。多杀招切换需要频繁打开菜单，打断战斗流程。

**当前实现**:
- `client/ModKeybindings.java:49-67` 只有KILLER_MOVE_1和KILLER_MOVE_2
- 技能键有4个 (R/F/V/C)，杀招只有2个
- RadialMenuScreen提供了菜单但不够快捷

**建议方案**:
1. **扩展到4-6个杀招槽**：增加 KILLER_MOVE_3~KILLER_MOVE_6
2. **杀招集切换**：Ctrl+杀招键切换杀招集（像武器栏换行），支持2-3组
3. **智能杀招推荐**：战斗中根据当前状态(距离/敌人HP/己方状态)在HUD高亮推荐杀招

**涉及文件**:
- 修改: `client/ModKeybindings.java` (添加更多键位)
- 修改: `event/ClientEvents.java` (处理新键位)
- 修改: `client/gui/CultivationOverlay.java` (显示杀招栏)

**新增内容**: 无
**复杂度**: 低

---

### 5. 战斗缺乏防御/回避选项——只能进攻

**问题**: 玩家战斗模型是"选技能/杀招 → 按键释放 → 等冷却"。没有主动防御、格挡、闪避、反击的机制。ToggleMoveManager虽然支持DEFENSE类杀招开关，但那是被动减伤，不是主动操作。

**影响**: 战斗缺乏双向互动。面对NPC蛊师的earthquakeWave/soulShockwave等强力AoE，玩家只能硬吃或跑开。

**当前实现**:
- DEFENSE类杀招(铜皮铁骨/金光铁壁)只是Toggle开/关被动减伤
- 没有格挡(timing-based)、翻滚闪避、反击框架
- NPC蛊师有teleportBehindTarget等高机动招式，玩家没有对应的应对手段

**建议方案**:
1. **真元护盾**：按住特定键消耗真元生成临时护盾，吸收伤害
2. **紧急闪避**：双击方向键消耗真元短距瞬移（2-3格），有短冷却
3. **反击窗口**：护盾激活时被命中，下一击1.5x伤害（timing-based奖励）
4. **技能中断**：某些控制类蛊虫可以打断敌人施法（看到粒子蓄力时使用）

**涉及文件**:
- 新建: `core/combat/DefenseManager.java` (护盾/闪避/反击状态管理)
- 新建: `network/DefensePayload.java` (防御动作网络包)
- 修改: `event/CultivationEvents.java` (tick防御状态)
- 修改: `client/ModKeybindings.java` (添加防御键)
- 修改: `event/GuBuffEventHandler.java` (处理护盾减伤)

**新增内容**: DefenseManager, DefensePayload
**复杂度**: 高

---

## 中优先级

### 6. 道路克制在战斗中无体现

**问题**: 虽然道组合引擎(PathReactionRegistry/PathStackingRule)处理了杀招组合效果，但战斗中道路之间没有克制关系。冰道蛊师打火道目标不会有额外效果，水道打火道也无优势。

**影响**: 48条道路在战斗中没有策略差异（除了外观和具体技能效果）。道路选择变成纯偏好，而非战术决策。

**当前实现**:
- `KillerMoveExecutor.calculateDamage()` 只考虑pathRealm, resonance, compatibility, proficiency
- 没有"攻击者道路 vs 目标道路"的相互关系
- GuMasterEntity的战斗AI也不考虑道路克制

**建议方案**:
- PathCounterSystem: 定义道路间克制关系（冰→火→木→水→火循环等）
- 攻击者使用克制道路杀招时+20%伤害，被克制时-15%伤害
- NPC蛊师AI根据玩家道路调整战术（遇到克制自己的玩家更谨慎）
- 玩家HUD显示当前目标的道路弱点（需要先情报探查到道路信息）

**涉及文件**:
- 新建: `core/path/PathCounterSystem.java`
- 修改: `core/combat/killermove/KillerMoveExecutor.java` (calculateDamage加入克制)
- 修改: `entity/GuMasterEntity.java` (AI考虑克制关系)

**新增内容**: PathCounterSystem
**复杂度**: 中

---

### 7. 寿元/天意/毒誓等系统对玩家不可见

**问题**: CultivationOverlay只显示境界、真元、念头、道痕、蛊虫数、气运、增益。寿元(LifespanManager)、天意关注度(HeavenWillManager)、毒誓状态(PoisonOathManager)等重要系统完全没在HUD上显示。玩家不知道自己还剩多少寿元、天意多高要降罚了。

**影响**: 玩家无法管理这些影响生死的关键系统。寿元耗尽突然死亡会非常挫败。

**当前实现**:
- `client/gui/CultivationOverlay.java` 渲染列表没有lifespan/heavenWill/oath字段
- `client/ClientDataCache.java` 可能也没有这些字段的同步
- 这些Manager都在服务端tick但信息不下发

**建议方案**:
- 在CultivationOverlay右侧或下方添加次要信息区：
  - 寿元条（绿→黄→红，剩余寿元/最大寿元）
  - 天意指示器（仅>50时显示，红色警告图标+数值）
  - 活跃毒誓列表（图标+剩余时间）
- SyncPlayerDataPayload扩展同步这些字段

**涉及文件**:
- 修改: `client/gui/CultivationOverlay.java` (添加寿元/天意/毒誓渲染)
- 修改: `client/ClientDataCache.java` (添加新字段)
- 修改: `network/SyncPlayerDataPayload.java` (扩展同步数据)

**新增内容**: 无
**复杂度**: 中

---

### 8. 野蛊捕获缺乏挑战性——右键即得

**问题**: WildGuEntity的交互逻辑是简单的右键捕获。没有难度差异、没有失败概率、没有需要特定条件。高转蛊虫和低转蛊虫用同样方式获取。

**影响**: 蛊虫获取过于简单，没有"炼蛊"的紧张感。原著中捕获蛊虫是一个需要准备和技巧的过程。

**当前实现**:
- WildGuEntity继承PathfinderMob，有AvoidEntityGoal（会逃跑）
- 但交互应该只是简单的右键判定

**建议方案**:
1. **品质/捕获难度系统**：高转蛊更难抓，需要更高境界
2. **捕获迷你游戏**：右键后进入短暂的QTE（快速反应按键序列），失败蛊虫逃跑
3. **特定条件蛊虫**：某些蛊虫需要特定时间(夜晚)/天气(雨)/生物群系才出现
4. **竞争捕获**：附近NPC蛊师也会尝试捕获野蛊，先到先得

**涉及文件**:
- 修改: `entity/WildGuEntity.java` (添加捕获难度/条件)
- 新建: `client/gui/CaptureMinigameScreen.java` (如果做QTE)
- 新建: `network/CapturePayload.java`

**新增内容**: CaptureMinigameScreen (可选), CapturePayload
**复杂度**: 中

---

### 9. 世界结构缺乏探索奖励差异化

**问题**: 5个世界结构(蛊窟/传承之地/酒旅人墓/家族聚居地/月兰洞)虽然视觉不同，但内部奖励机制相似。传承之地有InheritanceTrialManager提供试炼，但其他结构的独特互动机制不够深入。

**影响**: 玩家缺乏针对性探索动力。不知道哪个结构值得去，也没有"反复探索同一结构获得不同体验"的设计。

**当前实现**:
- 各结构的Piece类放置箱子/方块，但奖励表较为雷同
- 传承之地有专门的试炼系统(InheritanceTrialManager)
- 其他结构主要是loot和环境差异

**建议方案**:
1. **结构专属奖励**：每个结构对应特定道路的蛊虫和配方
2. **结构谜题**：聚居地有NPCs提供任务、月兰洞有种植谜题、酒旅人墓有阵法解谜
3. **结构难度层级**：同一结构根据距世界中心距离有不同难度和奖励
4. **结构boss**：每个结构类型有对应道路的守护蛊师

**涉及文件**:
- 修改各结构Piece文件
- 可能需要新的交互方块

**新增内容**: 结构专属NPC/谜题方块
**复杂度**: 高

---

### 10. ApertureScreen杀招信息不够直观

**问题**: ApertureScreen显示杀招时列出了道路/威力/冷却/蛊虫组成，但缺少关键信息：实际伤害（经过所有乘数计算）、与其他杀招的对比、冷却剩余时间。

**影响**: 玩家无法做出杀招装备的最优决策。

**当前实现**:
- `ApertureScreen.java` renderEquippedMoves/renderAvailableMoves 显示基础数据
- 不显示calculateDamage结果（需要服务端计算后同步）

**建议方案**:
- 同步每个杀招的实际伤害值（由KillerMoveExecutor.calculateDamage计算）
- 鼠标悬浮显示杀招详细tooltip：实际伤害、道路加成倍率、改良效果、道组合效果
- 杀招之间可拖拽排序（影响快捷键绑定顺序）
- 冷却中的杀招灰显+倒计时

**涉及文件**:
- 修改: `client/gui/ApertureScreen.java`
- 修改: `network/SyncApertureContentsPayload.java` (添加计算后的伤害值)
- 修改: 服务端杀招同步逻辑

**新增内容**: 无
**复杂度**: 低

---

## 低优先级

### 11. 蛊虫喂养缺乏策略深度

**问题**: FeedGuPayload处理蛊虫喂养，但喂养机制是简单的消耗元石恢复状态。没有食物偏好、喂养时机、过度喂养等策略。

**影响**: 蛊虫管理变成机械操作而非策略决策。

**建议方案**: 每种蛊虫有偏好食物(道路相关物品)，喂对了恢复更多+提升熟练度，喂错了浪费。

**涉及文件**: `event/FeedingEvents.java`, `core/gu/GuInstance.java`
**复杂度**: 低

---

### 12. 蛊虫图鉴缺乏发现引导

**问题**: CodexScreen记录已发现的蛊虫，但没有"提示未发现蛊虫在哪"的机制。148种蛊虫中玩家不知道还缺什么、去哪找。

**影响**: 收集后期失去方向感。

**建议方案**: 图鉴中未发现蛊虫显示剪影+模糊提示("常见于深层洞穴"/"需要二转以上才能遇到")

**涉及文件**: `client/gui/CodexScreen.java`, `core/gu/codex/GuCodex.java`
**复杂度**: 低

---

### 13. 无死亡回放/战败分析

**问题**: 蛊师对战败了只知道被打死。不知道死在什么招式、对手剩多少血、哪个阶段出了问题。

**影响**: 无法从失败中学习。

**建议方案**: 死亡时聊天栏显示：`"被[正道]蛊师·月道(二转)击杀 | 使用杀招: 月刃风暴 | 造成致命伤害: 24.5"`

**涉及文件**: `event/ModEvents.java` (LivingDeathEvent处理)
**复杂度**: 低

---

### 14. 转轮菜单项目过多时体验下降

**问题**: RadialMenuScreen把系统动作+所有蛊虫技能+所有杀招全放在一个轮盘里。后期玩家可能有10+蛊虫+5+杀招+5个系统动作=20+选项，轮盘密得无法快速选择。

**影响**: 中后期菜单使用效率急剧下降。

**建议方案**: 分层轮盘——第一层选类别(系统/技能/杀招)，第二层选具体项。或支持自定义收藏常用项。

**涉及文件**: `client/gui/RadialMenuScreen.java`
**复杂度**: 中

---

## 新系统建议（按优先级排序）

1. **伤害数字渲染系统** - 战斗伤害实时可视化 - 复杂度中
2. **情报战系统** - NPC蛊师信息分层探查 - 复杂度高
3. **主动防御框架** - 护盾/闪避/反击操作 - 复杂度高
4. **道路克制系统** - 48道路互克关系+伤害修正 - 复杂度中
5. **蛊商动态商品** - 按道路/轮换的交易系统 - 复杂度中
6. **HUD信息完善** - 寿元/天意/毒誓可视化 - 复杂度中
7. **捕获难度系统** - 野蛊捕获条件和挑战 - 复杂度中
8. **死亡回放信息** - 战败原因和数据展示 - 复杂度低
9. **杀招槽扩展** - 更多快捷键+杀招集 - 复杂度低
10. **图鉴发现引导** - 未发现蛊虫提示 - 复杂度低

---

## 总结

当前mod在**内容体量**上非常出色（48道路/148蛊虫/61杀招/多结构/多系统），但在**战斗体验反馈**和**信息透明度**上存在明显缺口。最关键的3个问题是：

1. **看不到伤害数字** → 整个战斗计算体系对玩家不透明
2. **NPC蛊师底细一览无余** → 违背原著核心设定，消灭情报战
3. **只能攻击不能防御** → 战斗缺乏双向互动

这3个问题解决后，战斗体验将有质的提升。
