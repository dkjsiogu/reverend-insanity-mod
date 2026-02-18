# 框架 API 参考

> 框架变更后必须同步更新此文档。本文档只记录框架用法，不记录已完成内容的详细参数。

## 修炼体系 (`core.cultivation`)

### Rank
蛊师境界枚举，一转到九转。1-5转为凡，6-9转为仙。
- `getLevel()` → 境界等级 (1-9)
- `isImmortal()` → 是否仙级
- `getBasePrimevalEssence()` → 该境界基础真元上限
- `next()` → 下一境界，最高返回 null

### SubRank
小境界：初阶、中阶、上阶、巅峰。
- `next()` → 下一小境界

### Aptitude
资质枚举：无资质、丁等、丙等、乙等、甲等、十绝体。
- `getEssenceRatio()` → 真元占空窍比例 (0~1)
- `getMaxRank()` → 该资质可达最高转数
- `canAdvanceTo(Rank)` → 判断能否突破到指定境界

### EssenceGrade
真元品质，每转 x 每小境界 = 共20种。含颜色名、RGB色值、效率系数。
- `of(Rank, SubRank)` → 获取对应真元品质
- `getEfficiency()` → 真元效率倍数（影响战斗伤害）

### Aperture
空窍，蛊师核心数据容器。
- `open(Aptitude)` → 开窍
- `tryAdvanceSubRank()` → 尝试突破小境界（消耗30%真元）
- `tryAdvanceRank()` → 尝试突破大境界（需巅峰+消耗80%真元）
- `consumeEssence(float)` / `consumeThoughts(float)` → 消耗真元/念头
- `regenerateEssence(float)` / `regenerateThoughts(float)` → 恢复
- `addGu(GuInstance)` / `removeGu(GuInstance)` → 存取蛊虫
- `setPathRealm(DaoPath, PathRealm)` / `getPathRealm(DaoPath)` → 流派境界
- `save()` / `load(CompoundTag)` → NBT 序列化

### GuMasterData
附加到玩家的完整蛊师数据，包含 Aperture + CombatState + GuBuffManager。
- `tick()` → 每 tick 更新（蛊虫饥饿、战斗状态恢复、Buff tick等）
- `getAperture()` / `getCombatState()` / `getBuffManager()` → 获取子系统
- `save()` / `load(CompoundTag)` → NBT 序列化
- 获取方式: `player.getData(ModAttachments.GU_MASTER_DATA.get())`

## 流派体系 (`core.path`)

### DaoPath
48种流派枚举，分6大类：
- **战斗**: 剑道/刀道/力道/杀道/兵道/骨道/拳道/枪道
- **精神**: 魂道/梦道/智道/幻道/魅道/音道/信道
- **元素**: 火道(炎道)/水道/冰道(冰雪道)/雷道/风道/土道/光道/暗道/木道/金道/星道
- **规则**: 宙道/宇道/运道/天道/律道/因果道/虚道/禁道
- **辅助**: 炼道/丹道/阵道/变化道/画道/食道
- **特殊**: 血道/毒道/月道/影道/云道/偷道/人道/奴道/飞行道/气道/阴阳道

### PathRealm
流派境界：普通→大师→宗师→大宗师→准无上大宗师→无上大宗师→道主。
- `getTier()` → 境界等级 (0-6)，用于伤害计算加成

## 蛊虫体系 (`core.gu`)

### GuType (record)
蛊虫类型定义（不可变模板）。
- `id` → ResourceLocation 唯一标识
- `displayName` → 中文名称
- `rank` → 蛊虫转数 (1-9)
- `path` → 所属流派 (DaoPath)
- `category` → 功能分类（攻杀/防御/移动/侦查/辅助/治疗/奴役/特殊）
- `essenceCost` → 催动消耗真元
- `feedInterval` → 喂养间隔（秒）
- `feedItem` → 喂养物品的 ResourceLocation
- `isImmortal()` → 6转及以上为仙蛊
- `canBeUsedBy(Rank)` → 判断蛊师是否有资格使用

**定义新蛊虫示例:**
```java
new GuType(id("my_gu"), "名称", rank, DaoPath.XXX, GuType.GuCategory.ATTACK, essenceCost, feedInterval, "minecraft:item_id")
```

### GuInstance
蛊虫实例（可变状态）。
- `tick()` → 每 tick 更新饥饿度
- `feed()` → 喂养，恢复饥饿度
- `refine(float)` → 炼化蛊虫（消耗真元）
- `isAlive()` → 饥饿度 > 0
- `isActive()` → 已炼化且饥饿度 > 20%
- `save()` / `load(CompoundTag)` → NBT 序列化

### GuRegistry
蛊虫注册表（静态单例）。
- `register(GuType)` → 注册蛊虫类型
- `get(ResourceLocation)` → 查询
- `getAll()` / `getByPath()` / `getByRank()` / `getByCategory()` → 查询列表
- `registerDefaults()` → 注册所有内置蛊虫

## 蛊虫技能体系 (`core.combat.ability`)

### GuAbility (抽象基类)
蛊虫单独催动技能（区别于杀招的多蛊组合）。
- `canUse(ServerPlayer, Aperture)` → 检查：空窍开启、真元充足、蛊虫活跃、非冷却中
- `execute(ServerPlayer, Aperture, CombatState)` → 消耗真元+设置冷却+进入战斗+手臂挥动+调用 onActivate
- `AbilityType` 枚举 → PROJECTILE / BUFF / INSTANT

**子类需要重写:**
- `onActivate(ServerPlayer, Aperture)` → 实现具体效果

**编写新技能步骤:**
1. 继承 `GuAbility`，构造函数传入 GuType id、AbilityType、essenceCost、cooldownTicks
2. 重写 `onActivate()` 实现效果逻辑
3. 在 `GuAbilityRegistry` 中注册

### GuAbilityRegistry
技能注册表，映射 GuTypeId → GuAbility。
- `register(ResourceLocation guTypeId, GuAbility ability)` → 注册
- `get(ResourceLocation)` → 查询
- `registerDefaults()` → 注册所有内置技能

## 杀招体系 (`core.combat`)

### KillerMove (record)
杀招定义，核心蛊+辅助蛊组合产生的战斗招式。
- `id` → ResourceLocation 唯一标识
- `displayName` → 杀招名称
- `primaryPath` → 主要流派（影响伤害计算）
- `minRank` → 最低境界要求
- `coreGu` → 核心蛊 ResourceLocation
- `supportGu` → 辅助蛊列表 List<ResourceLocation>
- `getAllRequiredGu()` → 核心蛊+辅助蛊完整列表
- `essenceCost` / `thoughtsCost` → 真元/念头消耗
- `power` → 基础威力
- `cooldownTicks` → 冷却时间
- `moveType` → 招式类型（攻击/防御/移动/控制/治疗/增益/减益/必杀）
- `canUse(Rank)` → 判断蛊师境界是否足够

### KillerMoveRegistry
杀招注册表。共61招（51基础+10原著标志性）。
- `register(KillerMove)` → 注册杀招
- `get(ResourceLocation)` → 查询
- `getAll()` / `getByType()` / `getUsableByRank()` → 查询列表
- `registerDefaults()` → 注册所有内置杀招

**原著标志性杀招（10招）:**
- `beast_phantom` 兽影 (力道rank3 BUFF) - 方源标志性杀招
- `hair_armor` 发甲 (力道rank1 DEFENSE) - 低消耗防御
- `ice_blade_storm` 冰刃风暴 (冰道rank3 ATTACK) - 白凝冰标志
- `primordial_light_fist` 太古光拳 (光道rank3 ATTACK) - 萧芒标志
- `five_finger_fist_sword` 五指拳心剑 (剑道rank3 ATTACK)
- `steal_sky_change_sun` 偷天换日 (偷道rank3 CONTROL)
- `white_bone_chariot` 白骨战车 (骨道rank3 ATTACK) - 傲骨魔君标志
- `myriad_self` 万我 (力道rank3 ULTIMATE) - 方源后期核心
- `bat_wing_return` 返实蝠翼 (风道rank2 MOVEMENT)
- `three_hearts_soul` 三心合魂 (奴道rank3 CONTROL)

### MoveEffect (接口)
杀招效果接口，`@FunctionalInterface`。
- `execute(ServerPlayer, Aperture, KillerMove, float calculatedDamage)` → 执行杀招效果

### MoveEffectRegistry
三级效果查找注册表。
- **Level 1**: 精确杀招ID匹配 `registerForMove(ResourceLocation, MoveEffect)`
- **Level 2**: 流派默认效果 `registerForPath(DaoPath, MoveEffect)`
- **Level 3**: 全局默认（对前方5格最近实体造成魔法伤害）
- `resolve(KillerMove)` → 三级查找返回最匹配的 MoveEffect
- `hasEffect(ResourceLocation)` → 检查杀招是否已注册效果（推演系统用）

### DaoResonance
道共鸣计算器。
- `calculate(KillerMove)` → 返回共鸣倍数 (0.7~1.5)
- 同道蛊虫 +0.1，同大类 +0.05，异大类 -0.1

## 自创杀招组件系统 (`core.combat.custom`)

道行为层叠系统——让每种大道的蛊虫在杀招中贡献**行为效果**而非仅数值。

### PathEffectComponent
48道行为效果映射表（静态注册）。

**核心数据结构:**
- `EffectContribution` record → 一种道贡献的完整效果（目标效果+自身效果+投射物+区域+颜色+VFX）
- `TargetEffect` 枚举(20种): FREEZE/SLOW/IGNITE/POISON_DOT/CHAIN_BOUNCE/KNOCKBACK/BLEED/ARMOR_PIERCE/ENTANGLE/TRUE_DAMAGE/GRAVITY_PULL/BLIND/WITHER/CONFUSE/EXECUTION/SUPPRESS/DISPEL/AGGRO_REDIRECT/SUMMON_STRIKE
- `SelfEffect` 枚举(9种): LIFESTEAL/SELF_DAMAGE/SPEED_BOOST/SHIELD/ESSENCE_RECOVER/INVISIBILITY/HEAL/THORNS
- `ProjectileType` 枚举(6种): MOON_BLADE/BLOOD_BOLT/FIRE_BOLT/ICE_BOLT/GOLD_BEAM/STAR_FALL
- `AreaEffectType` 枚举(4种): POISON_CLOUD/FROST_FIELD/FIRE_FIELD/FORMATION_FIELD

**API:**
- `get(DaoPath)` → 返回该道的 EffectContribution
- `getAll()` → 返回所有道的 EffectContribution（不可变Map）
- `getColor(DaoPath)` → 返回该道的 VFX 颜色
- `blendColors(int...)` → 多道颜色混合

### PathReactionRegistry
道反应注册表——杀招中出现特定道路对时触发融合效果。

**核心数据结构:**
- `ReactionRule` record → 触发条件(Set<DaoPath>) + 融合效果(ReactionEffect)
- `ReactionEffect` record → 名称 + 反应类型 + 强度 + VFX颜色 + VFX覆盖 + MoveType覆盖
- `ReactionType` 枚举(43种)

**API:**
- `findReactions(List<DaoPath>)` → 返回所有匹配的 ReactionEffect 列表

**已注册反应(53条), 覆盖45/48道路:**
- 冰+风→冰风龙卷, 光+力→太古光拳, 土+力→山岳重击, 剑+力→拳心剑气, 金+力→金刚穿甲
- 魂+火→魂焰, 冰+火→寒热冲击, 血+风→血雨漫天, 血+水→血潮
- 暗+魂→暗魂侵蚀, 奴+魂→精神支配(→CONTROL), 风+变化→蝠翼飞行(→MOVEMENT)
- 变化+土→大地形态(→BUFF), 变化+风→疾风形态(→BUFF), 力+奴→分身协攻
- 偷+魂→偷魂夺魄, 偷+血→偷命, 雷+星→星雷交击
- 骨+剑→白骨飞剑, 骨+刀→白骨战刀, 梦+幻→梦幻双重
- 毒+火→毒焰, 水+冰→冰封万里, 天+律→天罚裁决
- 暗+宇→暗漩, 风+雷→风雷吼, 血+火→血肉盛炎, 魂+金→灼魂太金, 音+冰→碧玉歌
- 力+变化→六臂天尸王(→BUFF), 运+宙→流年不利, 智+魂→魂智共鸣
- 星+火→星火遁(→MOVEMENT), 木+血→飞花溅血, 音+剑→长空音刃
- 气+变化→龙人变化(→BUFF), 食+魂→吃心, 画+人→安居乐业(→CONTROL)
- 风+人→送友风(→CONTROL), 阵+土→自动构筑(→DEFENSE)
- 杀+魂→杀魂, 杀+暗→暗杀, 影+暗→影暗融合(→BUFF), 影+光→光影分身
- 月+魂→月魂, 月+光→月华, 虚+宇→虚空崩塌, 魅+幻→魅幻双重(→CONTROL)
- 兵+阵→军阵(→DEFENSE), 飞行+风→御空冲锋(→MOVEMENT)
- 云+水→云海, 阴阳+雷→阴阳雷, 禁+宇→虚空封禁(→CONTROL)

### PathStackingRule
道叠加规则——同道蛊虫数量达到阈值时效果质变。

**核心数据结构:**
- `StackThreshold` record → 需要数量 + 叠加效果 + 强度系数 + 描述
- `StackEffect` 枚举(39种)

**API:**
- `check(Map<DaoPath, Integer>)` → 返回所有触发的 StackThreshold 列表

**已注册规则(42条), 覆盖37条道路:**
- 力×2→发甲(护盾翻倍), 力×3→兽影(巨兽变身)
- 冰×2→冰域, 火×2→火海, 血×2→血暴, 魂×2→魂爆
- 骨×2→骨甲, 骨×3→白骨战车, 偷×2→偷天
- 暗×2→暗蚀, 雷×2→雷暴, 风×2→风暴
- 剑×2→万剑, 毒×2→瘴气, 梦×2→梦境, 星×2→六幻星身
- 土×2→大地根, 水×2→潮汐, 光×2→圣光裁决, 音×2→天地寂静
- 变化×2→变化质变, 律×2→净空, 智×2→心意散
- 云×2→九云环, 金×2→碎城锤, 禁×2→封禁, 刀×2→万刀
- 气×2→无量气海, 人×2→众望所归
- 杀×2→杀域, 影×2→影域, 阴阳×2→阴阳逆转, 月×2→月域
- 虚×2→虚化, 魅×2→魅域, 兵×2→兵魂, 飞行×2→御空领域
- 幻×2→幻域, 宇×2→空间扭曲, 奴×2→傀儡军, 天×2→天怒, 运×2→运转

### CompositeEffectExecutor
自创杀招效果执行器，道组合引擎核心。

**执行管线:**
```
杀招 = 基础模板(MoveType) + Σ(道行为) + 道反应(道对融合) + 道叠加(同道阈值)
```

**API:**
- `execute(ServerPlayer, Aperture, CustomKillerMove)` → 执行自创杀招（完整流程：验证→消耗→效果）
- `executeFromKillerMove(ServerPlayer, Aperture, KillerMove, float)` → 从 KillerMove 执行（推演杀招桥接用）

**内部容器:** `CompositeEffects` 持有所有道贡献+数值加成+道反应列表+道叠加列表

**辅助蛊 GuCategory 额外数值:** ATTACK +15%伤害, DEFENSE +10%护盾, MOVEMENT +30%范围, SUPPORT +10%减耗, DETECTION +20%范围

### CompositeBasedMoveEffect
桥接 MoveEffect 接口，让推演生成的杀招通过 KillerMoveExecutor 执行时使用组件系统。
- DeductionManager.handleResult 自动为推演杀招注册此效果

### MoveComposer
杀招组合计算器。
- `MAX_SUPPORT_GU = 5` → 最多5个辅助蛊
- `MAX_CUSTOM_MOVES = 8` → 最多8个自创杀招
- `compose(name, coreGuId, supportGuIds)` → 创建自创杀招（自动检测道反应决定MoveType）
- `determineMoveType(coreType, supportGuIds)` → 检测道反应MoveType覆盖，否则用GuCategory默认
- `calculateSynergy(coreType, supportGuIds)` → 协同度(含互补道加成+道反应+道叠加奖励)
- `validate(aperture, coreGuId, supportGuIds)` → 验证合法性

**协同度加成:**
- 互补道对 +0.12: 冰↔火、光↔暗、宇↔宙、风↔土、水↔火、血↔魂、剑↔刀、梦↔幻、力↔骨、毒↔丹
- 每触发一条道反应 +0.15
- 每触发一条道叠加 +0.10

### KillerMoveExecutor
杀招执行器。
- **5步验证管线**: 空窍→境界→资源→冷却→蛊虫
- **4层伤害计算**: 基础威力 × 真元效率 × 流派境界加成 × 道共鸣
- `canExecute(ServerPlayer, Aperture, CombatState, KillerMove)` → 完整验证
- `calculateDamage(Aperture, KillerMove)` → 多层伤害计算
- `execute(ServerPlayer, Aperture, CombatState, KillerMove)` → 验证→消耗→冷却→手臂挥动→效果→战斗

### CombatState
战斗状态管理器。
- `tick()` → 每 tick 更新（战斗中恢复念头，非战斗恢复真元+念头，冷却倒计时）
- `canUseMove(KillerMove)` / `useMove(KillerMove)` → 判断/施放杀招
- `calculateDamage(KillerMove)` → 基础伤害计算
- `equipMove()` / `unequipMove()` / `getEquippedMoves()` → 装备管理
- `enterCombat()` / `exitCombat()` → 战斗状态切换
- `setMoveCooldown()` / `isMoveCooldown()` → 杀招冷却
- `setAbilityCooldown()` / `isAbilityOnCooldown()` / `getAbilityCooldownRemaining()` → 技能冷却

## GuBuff 系统 (`core.combat.buff`)

自定义增益系统，完全替代 MobEffect。通过 Attribute 修改、事件拦截、自定义逻辑实现。

### GuBuff (抽象基类)
- `getId()` → ResourceLocation 唯一标识
- `getRemainingTicks()` → 剩余持续时间
- `isActive()` → 是否激活中
- `tick(ServerPlayer)` → 每 tick 更新
- `apply(ServerPlayer)` → 激活
- `remove(ServerPlayer)` → 移除

**子类钩子（编写新Buff需要重写）:**
- `onApply(ServerPlayer)` → 激活时（添加 AttributeModifier 等）
- `onRemove(ServerPlayer)` → 移除时（清除 AttributeModifier 等）
- `onTick(ServerPlayer)` → 每 tick 持续逻辑
- `modifyIncomingDamage(player, source, amount)` → 修改受到伤害（返回修改后数值）
- `modifyOutgoingDamage(player, target, amount)` → 修改输出伤害
- `preventMobTargeting(player, attacker)` → 阻止怪物锁定
- `onPlayerAttack(player, target)` → 攻击回调
- `shouldBreakOnDamage()` → 受伤是否打破此 Buff

### GuBuffManager
每玩家 Buff 管理器（存储在 GuMasterData 中）。
- `applyBuff(player, buff)` → 应用（同 ID 替换旧的）
- `removeBuff(player, buffId)` → 按 ID 移除
- `clearAll(player)` → 清除所有
- `tick(player)` → 每 tick 更新
- `processIncomingDamage(player, source, amount)` → 遍历 Buff 修改受到伤害
- `processOutgoingDamage(player, target, amount)` → 遍历 Buff 修改输出伤害
- `shouldPreventTargeting(player, attacker)` → 任一 Buff 阻止锁定
- `onPlayerAttack(player, target)` → 通知所有 Buff
- `addGlowingTarget(entity, ticks)` → setGlowingTag 计时管理

### GuBuffEventHandler
NeoForge 事件拦截器，自动处理 Buff 与游戏事件的交互。
- `onIncomingDamage(LivingIncomingDamageEvent)` → 输出/受击修改
- `onPlayerAttack(AttackEntityEvent)` → 攻击回调
- `onTargetChange(LivingChangeTargetEvent)` → 隐身阻止锁定

## VFX 渲染系统 (`client.vfx`)

### VfxType
VFX效果类型枚举（13种）：
- **平面**: SLASH_ARC(斩击弧) / ENERGY_BEAM(光束) / AURA_RING(旋转光环) / HEAL_SPIRAL(上升螺旋) / RIPPLE(涟漪) / PULSE_WAVE(脉冲波) / GLOW_BURST(闪光爆发) / SHADOW_FADE(暗影消散) / IMPACT_BURST(冲击爆破)
- **3D体积**: BLACK_HOLE(黑洞: 暗球体+旋涡流+吸积环+事件视界) / TORNADO(龙卷: 锥形多层旋转环+螺旋) / SKY_STRIKE(天降: 垂直光柱+落点闪光+扩散环) / DOME_FIELD(半球领域: 旋转半球+底部光环)

### VfxEffect
活跃 VFX 效果实例（位置、方向、ARGB颜色、缩放、持续时间、年龄）。
- `tick()` → 递增年龄
- `isExpired()` → 是否过期
- `getProgress()` → 生命周期进度 (0~1)

### ModRenderTypes
自定义 RenderType。
- `VFX_GLOW` → POSITION_COLOR + ADDITIVE_TRANSPARENCY（发光效果）
- `VFX_TRANSLUCENT` → POSITION_COLOR + TRANSLUCENT_TRANSPARENCY（半透明效果）

### VfxManager
单例 VFX 管理器。
- `getInstance()` → 获取单例
- `addEffect(VfxEffect)` → 添加新效果
- `tick()` → 每 tick 更新，移除过期效果
- `render(PoseStack, Camera, float)` → 在世界空间渲染所有活跃效果

### VfxGeometry
静态几何体绘制工具类。
- **2D**: `drawBillboard()`(面向摄像机四边形) / `drawSlashArc()`(弧形斩击) / `drawRing()`(水平光环) / `drawSpiral()`(上升螺旋) / `drawBeamLine()`(光束) / `drawExpandingRing()`(扩散环)
- **3D**: `drawSphere()`(UV球体 latSeg×lonSeg) / `drawHemisphere()`(上半球) / `drawVortexStreams()`(向心旋涡流 N条螺旋臂)

### VfxHelper（服务端触发VFX）
- `spawn(ServerPlayer, VfxType, int color, float scale, int ticks)` → 使用玩家位置和视线方向
- `spawn(ServerPlayer, VfxType, x, y, z, dirX, dirY, dirZ, color, scale, ticks)` → 指定位置和方向
- 通过 `PacketDistributor.sendToPlayersTrackingEntityAndSelf()` 发送

### SpawnVfxPayload
服务端→客户端 VFX 触发网络包。
- `byte vfxType` + `double x,y,z` + `float dirX,dirY,dirZ` + `int color` + `float scale` + `int durationTicks`

### VfxEventHandler
NeoForge 游戏事件总线订阅。
- `onRenderLevel(RenderLevelStageEvent)` → AFTER_TRANSLUCENT_BLOCKS 阶段渲染
- `onClientTick(ClientTickEvent.Post)` → 每 tick 更新 VfxManager

## 实体 (`entity`)

### 投射物基类模式
所有投射物实体继承 ThrowableProjectile，共同模式：
- 无重力飞行，设定 tick 后消失
- `onHitEntity()` 造成魔法伤害（基础伤害 × 真元效率 × 流派境界加成）
- 飞行时产生粒子效果
- 渲染器基于 ThrownItemRenderer

### 管理器（静态单例模式）
- `PoisonCloudManager` → 毒云区域 DPS + 粒子
- `FrostManager` → 减速/冻结 AttributeModifier 计时管理
- 均通过 `ServerTickEvent.Post` 驱动

## 网络 (`network`)

### 客户端→服务端
| Payload | 用途 |
|---|---|
| OpenAperturePayload | 请求打开空窍界面 |
| EquipMovePayload | 装备/卸下杀招 (moveId + equip) |
| ActivateAbilityPayload | 技能激活 (slotIndex 0-3，按键 R/F/V/C) |
| UseKillerMovePayload | 杀招施展 (slotIndex 0-1，按键 Z/X) |
| StartDeductionPayload | 开始推演 (coreGuId + supportGuIds + targetPath) |
| CancelDeductionPayload | 取消推演 |
| OpenDeductionScreenPayload | 请求推演界面数据 |
| OpenImmortalAperturePayload | 请求仙窍界面数据 |
| ExtractResourcePayload | 提取仙窍资源 (resourceOrdinal + amount) |
| RepairAperturePayload | 修复仙窍 (amount) |
| ResistCalamityPayload | 抵抗灾劫 (amount) |
| FeedGuPayload | 喂养蛊虫 (slotIndex) |

### 服务端→客户端
| Payload | 用途 |
|---|---|
| SyncGuMasterDataPayload | 每20tick同步修炼数据（境界/真元/念头等） |
| SyncApertureContentsPayload | 空窍详细内容（蛊虫列表+杀招列表） |
| SpawnVfxPayload | VFX效果触发 |
| SyncDeductionPayload | 推演进度同步 (active + progress + successRate + message) |
| DeductionResultPayload | 推演结果 (outcome + moveName + improvementLevel + message) |
| SyncDeductionScreenPayload | 推演界面数据 (guList + 推演状态) |
| SyncImmortalAperturePayload | 仙窍完整数据 (等级/气/资源/道痕/灾劫) |
| SyncCodexPayload | 蛊虫图鉴数据 |

### ServerPayloadHandler
- `handleActivateAbility` → 获取活跃蛊虫→映射 slot→执行 GuAbility
- `handleUseKillerMove` → 获取已装备杀招→映射 slot→委托 KillerMoveExecutor

## 客户端 (`client`)

### ModKeybindings
- 技能键位: R(栏位1) F(栏位2) V(栏位3) C(栏位4)
- 杀招键位: Z(栏位1) X(栏位2)
- 界面键位: G(空窍管理) H(仙窍管理) J(推演界面)

### ClientEvents
监听 ClientTickEvent.Post，消费所有注册键位并发送对应网络包。

### GUI 界面
- `ApertureScreen` → 空窍管理（蛊虫列表/杀招装备/喂养）
- `ImmortalApertureScreen` → 仙窍管理（等级/气/资源/道痕/灾劫/修复）
- `DeductionScreen` → 杀招推演（蛊虫选择/道路选择/道共鸣预览/进度/结果）
- `CodexScreen` → 蛊虫图鉴
- `CultivationOverlay` → HUD（真元/念头/境界/推演进度条）

### 渲染器创建模式
投射物渲染器统一基于 ThrownItemRenderer，在 EntityRendererProvider 中注册。
NPC 实体渲染器使用 HumanoidMobRenderer + PlayerModel，自定义贴图。

## 注册流程 (`registry`)

### ModAttachments
- `GU_MASTER_DATA` → AttachmentType<GuMasterData>，绑定到玩家实体

### ModItems
延迟注册容器 (`DeferredRegister.Items`)。
- GuItem: 蛊虫物品基类，构造传入 GuType id
- 特殊物品: HopeGuItem / PrimevalStoneItem / SpringAutumnCicadaItem / BreakthroughStoneItem

### ModEntities
实体类型延迟注册容器 (`DeferredRegister<EntityType<?>>`)。

### ModBlocks / ModBlockEntities
方块及方块实体延迟注册容器。

### ModCreativeTabs
创造模式标签页注册。

### ModRegistries
统一注册入口，在主类构造函数中调用。

## 炼蛊配方 (`core.gu`)

### RefinementRecipe
炼蛊配方注册表。
- `registerDefaults()` → 注册所有升炼配方
- `findMatch(List<ItemStack>)` → 匹配配方（传入炼蛊炉中的物品列表）

**注册新配方:** 在 `registerDefaults()` 中添加 `register(new RefinementRecipe(id, inputs, output))`

## 世界生成 (`world.structure`)

### GuCaveStructure / GuCavePiece
蛊窟结构生成。通过 NeoForge 结构注册系统注册。

## 命令 (`command`)

### GuCommand
管理员命令 `/gu`，权限等级2。
- `/gu give <player> <gu_id>` → 给予蛊虫
- `/gu setrank <player> <rank>` → 设置境界
- `/gu info [player]` → 查看信息
- `/gu open <player> <aptitude>` → 开窍
- `/gu equip <player> <move_id>` → 装备杀招
- `/gu reset <player>` → 重置
- `/gu essence <player> <amount>` → 设置真元

## 推演系统 (`core.deduction`)

### DeductionManager
杀招推演管理器（静态单例），管理所有玩家的推演状态。
- `startDeduction(ServerPlayer, MoveBlueprint)` → 开始推演（验证资源/蛊虫→创建会话）
- `tick(ServerPlayer)` → 每 tick 推进推演进度，完成时自动解析结果
- `calculateSuccessRate(ServerPlayer, MoveBlueprint)` → 计算成功率（道痕+流派境界+共鸣+经验-复杂度）
- `findMatchingMove(MoveBlueprint)` → 查找已注册杀招中匹配的组合
- `isDeducting(UUID)` / `cancelDeduction(UUID)` → 查询/取消推演
- `savePlayerData(UUID, CompoundTag)` / `loadPlayerData(UUID, CompoundTag)` → 持久化（由GuMasterData调用）
- 推演完成后自动: `KillerMoveRegistry.register()` + `MoveEffectRegistry.register(CompositeBasedMoveEffect)` + 自动装备

### DeductionSession
单次推演会话。
- `resolve(Random)` → 解析结果（大成功/成功/部分成功/发现/失败）
- `generateRandomMove()` → 使用 MoveComposer 计算体系生成杀招
- `generateMoveName()` → 基于道反应/叠加名称生成杀招名

### MoveBlueprint
推演蓝图（核心蛊+辅助蛊+目标道路）。
- `coreGu()` / `supportGu()` / `targetPath()` / `getAllGu()` / `getGuCount()`
- `validate()` → 验证蓝图合法性

### ImprovedMove
改良杀招（大成功结果），保存玩家个人数据。

## 仙窍系统 (`core.aperture`)

### ImmortalAperture
仙窍（6转+蛊师个人小世界）。
- `form(Aperture, GuMasterData)` → 开辟仙窍（确定等级、初始化气储备）
- `tick(ServerPlayer)` → 每 tick: 气自然回复、真元增幅、每日产出元石/资源
- `consumeQi(float)` / `absorbQi(float, float)` → 天地二气操作
- `repair(float)` → 修复仙窍（消耗气）
- `addDaoMark(DaoPath, int)` / `getDaoMark(DaoPath)` → 仙窍道痕
- `getResourceManager()` → 资源管理器

### BlessedLandGrade
福地等级枚举: LOWER/MIDDLE/UPPER/SUPREME/GROTTO_HEAVEN。
- `determine(Aperture, GuMasterData)` → 根据境界+道痕决定等级
- `getEssencePerDay()` → 每日元石产出

### ApertureResourceManager
仙窍资源管理（灵石/灵草/灵液/灵土/灵兽精华/灵矿）。
- `tickProduction(grade, integrity, random)` → 每日资源产出
- `consumeResource(type, amount)` / `getResource(type)` → 消耗/查询

### CalamityManager
灾劫管理器（仙窍定期灾劫挑战）。
- `tick(ServerPlayer, ImmortalAperture)` → 灾劫进度推进
- `isInCalamity(UUID)` / `getActiveCalamity(UUID)` → 查询状态
- `resist(ServerPlayer, float)` → 抵抗灾劫（消耗真元）
