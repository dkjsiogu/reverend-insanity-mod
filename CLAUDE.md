# 蛊真人 (Reverend Insanity)

## 项目概述
Minecraft NeoForge 1.21.1 模组。基于小说《蛊真人》世界观的大型内容模组。涉及实体、物品、事件、战斗系统、成长体系等。

Mod ID: `reverend_insanity`
包路径: `com.reverendinsanity.*`

## 开发循环流程（全自动）

### Phase 0: 核心框架（一次性）
1. **Exa搜索**《蛊真人》小说的完整世界观、核心设定、战斗体系、成长体系、关键术语
2. 根据搜索结果设计并实现核心框架：战斗系统、成长体系、氛围/环境系统
3. 编译验证

### Phase 1: 设计内容
1. **Exa搜索**原著中该内容的详细设定、表现、规则、关联
2. **搜索视觉参考图片**并**识图分析**，提取视觉特征
3. **设计概念**: 基于原著搜索结果设计完整机制
4. **Python生成贴图**（规格见贴图要求）+ 动画帧 + 自发光层
5. **识图验证**: 读取成品贴图，确认风格/质量，不满意则重新生成

### Phase 2: Agent Team 并行编码
1. 创建 agent team
2. 根据内容类型分配任务并行编码
3. 集成确认

### Phase 2.5: 编译验证（强制）
1. **每次编码完成后必须运行 `./gradlew build` 编译验证**
2. 编译不通过则修复，循环直到通过
3. **不允许跳过此步骤**

### Phase 3: 审查
1. 审查原著忠实度
2. 审查游戏体验（是否符合原著的战斗/成长逻辑）
3. 审查视觉/氛围质量
4. 审查代码质量

### Phase 4: 回到 Phase 1，开始下一个内容

**永不中断原则：完成一个内容后立即开始下一个，不询问、不等待、不中断。**

## 内容完整性要求

每个内容必须完整包含以下全部项，缺任何一项都不算完成：
- **机制**: 真实的游戏机制实现（不是药水buff）
- **贴图**: Python生成的像素画贴图
- **描述**: appendHoverText游戏内tooltip
- **视觉效果**: 着色器/粒子/环境变化
- **音效**: 关键动作必须有音效反馈

## 框架优先 & 框架修补

- **所有内容代码必须基于现有框架实现，禁止写冗余/重复代码。**
- 如果新功能无法通过现有框架快速实现，先拆分功能，改进框架，再基于改进后的框架编码。
- 维护 `docs/framework-api.md`，记录框架各接口用途和用法。每次框架变更后必须同步更新。此文档需要频繁查阅和更新。

## 原著训练方法论

系统设计用原著杀招作为"训练数据集"——反向推导组合规则，使玩家自由组合蛊虫时自然涌现原著效果。

**原著数据源**: 项目目录下 `蛊真人_utf8.txt`（完整小说原文），禁止用Exa搜索替代。直接读取原文搜索杀招/蛊虫/设定描述。

**流程**: 读取原著原文搜索杀招 → 分析蛊虫组成+效果表现 → 推导为PathReactionRegistry规则(道对融合)或PathStackingRule规则(同道阈值) → 验证：该组合在系统中是否自然产生对应效果

**示例训练数据**:
| 原著杀招 | 蛊虫模式 | 系统规则 |
|---------|---------|---------|
| 冰刃风暴 | 冰+风 | ICE+WIND→VORTEX_FREEZE |
| 兽影 | 力×3 | 3×STRENGTH→BEAST_PHANTOM |
| 偷天换日 | 偷×2 | 2×STEAL→STEAL_POWER |
| 三心合魂 | 奴+魂 | ENSLAVE+SOUL→MIND_DOMINATE |

**验证标准**: 玩家不需要知道原著杀招配方，只要用对应道路的蛊虫自由组合，系统就自然产生类似原著的效果。如果某类原著杀招无法被系统覆盖，说明缺少反应规则或叠加规则，需补充。

## 设计原则

### 必须遵循
1. **忠于原著**: 所有设计必须先搜索原著设定，基于搜索结果设计，不允许凭空创作
2. **还原而非翻译**: 原著里的蛊虫/杀招怎么运作，游戏里就怎么运作。不是"把设定翻译成药水buff"
3. **真实机制实现**: 用Attribute修改、事件拦截、自定义伤害计算、投射物、AI操纵等实现效果。禁止直接套MobEffect(药水效果)偷懒。比如"铜皮蛊"应该是修改护甲Attribute+LivingHurtEvent自定义减伤计算+皮肤变色渲染，不是给个抗性II了事——效果可以类似，但实现必须是自己的系统
4. **成长有代价**: 变强的方式和代价必须符合原著基调
5. **氛围优先**: 视觉/音效/环境设计服务于原著氛围
6. **技术不妥协**: 不因为实现困难而降低设计，shader/反射/mixin想用就用

### 敌对蛊师设计规范
GuMasterEntity不能是泛用Monster，必须是**有道路、有蛊虫、有杀招的真蛊师**。参考AncientGuImmortalEntity的3阶段战斗系统。
- **生成时分配道路**: 根据生成点生物群系/结构类型，随机分配1-2条道路（PathType）和对应境界
- **蛊虫组合**: 根据道路和境界，从GuRegistry中选取对应道路+转数的蛊虫装备到蛊师身上
- **杀招序列**: 根据蛊虫组合，从KillerMoveRegistry中匹配可用杀招，构成战斗招式序列
- **多阶段战斗**: 仿照AncientGuImmortalEntity的Phase系统（>60%正常 / 30-60%加速+特殊招 / <30%全力+终极招）
- **战斗AI应体现道路特色**: 力道蛊师近战猛攻、幻道蛊师召唤幻象、偷道蛊师潜行偷袭、剑道蛊师剑气远程
- **掉落**: 击败后掉落该蛊师携带的蛊虫（概率）和杀招蓝图（稀有）
- **道痕可视化**: 不同道路的蛊师有对应视觉光环/粒子（冰道冰霜、炎道火焰等）

### 禁止
- 没有搜索原著就开始设计
- **用药水效果(MobEffect)代替真实机制**——力量/抗性/速度/夜视等原版buff不是设计工具，是偷懒
- 战斗体系传统RPG化，必须体现原著特色的战斗逻辑
- 所有蛊虫/杀招共用"给buff/debuff"模板——每种蛊虫必须有独特的实现方式
- 用战斗力数值碾压一切的设计
- "太难实现所以简化设计"

## 贴图要求
- 实体贴图: 64x64 / 128x128，需要完整的模型贴图（正面/侧面/背面）
- 方块贴图: 16x16 / 32x32，六面独立或统一视需求
- 物品贴图: 64x64 / 128x128
- 必须保持Minecraft像素画风格
- 必须尊重原著视觉风格
- 需要动态效果: 生成多帧 + .mcmeta动画配置
- 需要发光效果: 生成emissive自发光贴图层
- 生成前搜索参考图片并识图分析，生成后识图验证，不满意则重新生成

## 内容列表

对照此列表确认内容是否已存在，禁止读取描述文件，节省上下文。
完成一个内容的完整开发流程后，将概念设计归档到 `docs/` 对应子目录。

### 已完成
- 核心框架（修炼/流派/蛊虫/战斗体系）
- 第一批蛊虫物品（希望蛊/月光蛊/酒虫/熊力蛊/铜皮蛊/黑豕蛊/青丝蛊）+ 月桂花瓣
- 开窍机制
- 蛊虫炼化机制
- 蛊虫技能系统
- 网络通信
- VFX渲染系统（自定义RenderType+几何体绘制）
- 杀招系统（执行器+效果注册+道共鸣）
- GuBuff系统（Attribute+事件拦截，替代MobEffect）
- 二转蛊虫（四味酒虫/一气金光虫/铁骨蛊/奴蛇蛊/月痕蛊/移形蛊）
- 三转蛊虫（银月蛊/白玉蛊/天眼蛊）
- 六转仙蛊（春秋蝉·死亡重生）
- 治疗蛊（肉骨蛊）
- 野蛊实体（WildGuEntity·自然生成+捕获）
- NPC蛊师实体（GuMasterEntity·敌对+战斗AI）
- 炼蛊炉方块（RefinementCauldron·升炼配方）
- 空窍管理GUI（ApertureScreen）
- 修炼HUD（CultivationOverlay）
- 蛊虫喂养系统
- 境界突破系统（小境界冥想+大境界突破石）
- 死亡惩罚（蛊虫散落+修为受损+春秋蝉重生）
- 怪物掉落蛊虫/元石
- 管理员命令（/gu）
- 蛊窟世界生成（GuCaveStructure）
- 世界结构扩展（ClanSettlement家族聚居地 + InheritanceGround传承之地 + MoonOrchidCave月兰洞 + WineTravelerTomb酒旅人墓）
- 成就系统（advancements）
- 蛊商人实体（GuMerchantEntity）
- 电狼实体（LightningWolfEntity）
- 雷冠头狼实体（ThunderCrownWolfEntity）
- 野山猪实体（MountainBoarEntity）
- 玉眼石猴实体（JadeEyeMonkeyEntity）
- 草人傀儡实体（StrawPuppetEntity）
- 山地蜘蛛实体（MountainSpiderEntity）
- 远古蛊仙残魂实体（AncientGuImmortalEntity）
- 尊者实体（VenerableEntity）
- 无形之手实体（FormlessHandEntity）
- 幻影仙人实体（PhantomImmortalEntity）
- 陈酿酒
- 兽骨
- 血液瓶
- 青竹酒
- 知心草
- 玉眼石珠
- 苦酒
- 蛛丝
- 修炼入门手册
- 野月兰花丛
- 彩晶钟乳石
- 矛竹
- 蛊虫架
- 酒坛
- 阵法石
- 天真蘑菇
- 元石矿/深层元石矿
- 灵泉方块
- 月兰种子
- 传说卷轴
- 蛊虫图鉴
- 福地核心方块
- 推演系统（DeductionManager·杀招推衍研究）
- 自创杀招系统（MoveComposer·玩家自组合杀招·最多5辅助蛊）
- 天地异象系统（WorldEventManager·维度世界事件）
- **道组合引擎**:
  - PathEffectComponent（48道路→行为效果映射：冻结/点燃/连锁/真伤等20种目标效果）
  - PathReactionRegistry（24条道反应规则：冰+风→冰风龙卷等19种融合效果）
  - PathStackingRule（17条道叠加规则：力×3→兽影等14种质变效果）
  - CompositeEffectExecutor（三层执行：基础模板+行为层叠+反应+叠加）
  - CompositeBasedMoveEffect（桥接推演杀招到组件系统）
- 仙窍系统（ImmortalAperture·形态/等级/资源管理）
- 灾劫系统（CalamityManager·地灾4类+天劫3类）
- 仙窍管理GUI（ImmortalApertureScreen·福地状态/资源提取/道痕/灾劫抵抗·H键）
- 推演GUI（DeductionScreen·蛊虫选择/道路选择/成功率预估/进度追踪·J键）
- 推演HUD指示器（CultivationOverlay推演进度条）
- 推演结果闭环（KillerMoveRegistry注册+自动装备+持久化保存+结果展示）
- 推演数值统一（DeductionSession使用MoveComposer计算体系·道反应/叠加加成生效）
- 福地方块交互（右键开仙窍界面·敌对生物衰弱·所有者验证·状态展示）
- 推演道共鸣预览（DeductionScreen实时显示道反应/道叠加触发）
- DotManager持续伤害系统（替代瞬发伤害·毒素/流血/凋零每秒结算）
- 灾劫抵抗修复（damageReduced正确递减）
- 蛊虫图鉴快捷键（K键·完整注册+事件+翻译）
- 成就系统扩展（30个成就：+图鉴发现/50/100·推演成功·仙窍开辟·蛊虫喂养·击败蛊师·蛊商交易）
- **全48道路战斗系统完成**（177蛊虫/177技能/61杀招/72增益/60配方）:
  - 月道/金道/木道/血道/毒道/力道
  - 冰道/炎道/土道/风道/雷道/水道
  - 魂道/光道/暗道/梦道/幻道
  - 剑道/刀道/星道/运道/杀道/变化道
  - 兵道/音道/骨道/飞行道/气道/阴阳道
  - 宇道/宙道/魅道/智道/虚道/禁道
  - 天道/律道/影道/云道/阵道/炼道/丹道
  - 画道/偷道/信道/人道/奴道/食道
- HUD增益显示（CultivationOverlay·实时buff状态）
- 杀招详细描述（ApertureScreen·道路/威力/冷却/蛊虫组成）
- 状态持久化（WorldEventSavedData·灾劫计时器·CalamityManager save/load）
- 仙窍维度系统（ApertureDimensionManager + ChunkGenerator + TerrainBuilder + TimeManager + ModDimensions）
- 仙窍出口传送门方块（ApertureExitPortalBlock）
- 原著标志性杀招效果补完（兽影/发甲/冰刃风暴/太古光拳/拳心剑/偷天换日/白骨战车/万我/返实蝠翼/三心合魂 → 61杀招100%专属效果）
- 家族势力系统（Faction + FactionReputation）
- MobEffect违规清零（AncientGuImmortalEntity全部改用AttributeModifier+计时器）
- 仙窍维度生物生成（ApertureSpawnManager·道路主题生物+等级缩放+密度控制）
- 仙窍入侵事件（ApertureInvasionManager·克制道路入侵+等级规模+警告系统）
- 仙窍氛围效果（ApertureAmbientManager·道路主题粒子+双层粒子系统）
- 蛊师Boss改造（GuMasterEntity·道路分配+5战斗原型+3阶段+Boss条+道路掉落+粒子光环）
- 道路音效系统（DaoPathSounds·48道路差异化音效映射·技能/杀招按道路+类型选音效）
- 势力阵营集成（GuMasterEntity/GuMerchantEntity阵营分配·名称标签·声望动态计算·交易折扣）
- 梦境探索系统（DreamExplorationManager·入梦获取道痕/配方/真元·5种梦境事件·清明梦/噩梦增强模式）
- 变身系统（TransformationManager·3形态:狼形/熊形/缩地·AttributeModifier+粒子+真元消耗）
- 蛊阵系统（FormationArrayManager·十字布阵·困阵/盾阵/天地大阵·区域减速/护甲/真伤/治疗）
- 传承试炼系统（InheritanceTrialManager·传承之地波次战斗·3波次·道痕+真元奖励·冷却机制）
- 天意系统（HeavenWillManager·天意关注度0-100·境界/道痕/魔道驱动·落雷/压制/降罚·福地减免）
- 寿元系统（LifespanManager·境界决定上限·自然消耗·突破增寿·耗尽即死）
- 分身系统（CloneManager·虚影分身·20%闪避+40%额外伤害+速度提升·粒子效果）
- 气运夺取系统（FortunePlunderManager·击杀夺运·自然衰减/恢复·影响炼蛊/掉落）
- 自爆系统（SelfDestructManager·牺牲蛊虫AoE爆炸·伤害/范围按蛊虫转数缩放·蛊永久销毁）
- 血脉系统（BloodlineManager·开窍随机血脉·6种血脉被动加成·龙威最稀有·持久化）
- 毒誓系统（PoisonOathManager·3种毒誓·短期增益+违背惩罚·影响气运/寿元/真元）
- 闭关系统（SeclusionManager·原地入定加速修炼·道痕/真元提升·粒子效果·移动中断）
- 生死门系统（LifeDeathGateManager·50%概率赌博·生门全回复+50%增伤/死门40%HP+寿元消耗·气运影响）
- 蛊吞噬系统（GuDevourManager·牺牲蛊虫强化同道路蛊虫熟练度·低转不能吞高转）
- 蛊损伤系统（GuDamageManager·战斗受伤/死亡时蛊虫可能受损·效果降低50%·真元修复）
- 偷袭系统（AmbushManager·潜行2秒蓄力·首击1.8x伤害·偷道/影道/暗道2.5x·粒子暴击）
- 封印系统（SealManager·消耗真元封印目标·-95%移速·持续时间按境界缩放·Boss减半）
- 陷阱系统（TrapManager·布置隐形陷阱最多5个·敌人踩中触发AoE伤害·5分钟持续·粒子提示）
- 道悟系统（DaoInsightManager·随机顿悟·临时攻速增强+永久道痕·闭关3倍触发率·粒子光效）
- 系统互联（杀招→毒誓检查·击杀→毒誓完成·杀招→天意增长·偷袭→伤害加成→分身额外伤害）


## 框架技术债务 ✅ 全部已修复

1. ~~**NBT反序列化空catch**~~ ✅ Aperture/GuMasterData 添加 LOGGER.warn
2. ~~**静态状态持久化**~~ ✅ WorldEventManager改用SavedData；CalamityManager通过GuMasterData持久化
3. ~~**验证逻辑重复**~~ ✅ 移除CombatState中未使用的canUseMove/useMove，统一由KillerMoveExecutor.canExecute处理

## 代码规范
- 中文注释
- **禁止行内注释和方法注释**，只允许在类开头用简短中文注释说明类的作用。目的：减少输出token、节省上下文。

## 技术规范
- Minecraft NeoForge 1.21.1
- Java 21
- 包路径: `com.reverendinsanity.*`
- Shader: GLSL (NeoForge RenderType + Post-processing)

## 环境信息
- sudo 密码: DKJsiogus1
- Python 贴图生成需要 Pillow: `sudo apt-get install -y python3-pil`
- Build: `export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64" && ./gradlew build`
