# 杀招推演 + 仙窍维度 + 灾劫系统 - 综合设计文档

## 一、杀招推演系统 (Killer Move Deduction)

### 原著设定
- 智道推演消耗真元+魂魄(念头)，次数越多消耗越大
- 境界、道痕积累影响推演能力
- 单一流派构思空间有限，复合杀招构思空间更大
- 杀招=核心蛊+支柱蛊+辅助蛊，道痕影响威力成倍增长
- 改良杀招可降低消耗/提升威力/增加效果

### 游戏机制设计

#### 1. 推演过程
- 玩家选择蛊虫组合(1核心+0~4辅助)
- 系统计算：道共鸣、道痕加成、境界系数
- 消耗真元(大量)+念头(大量)，不可恢复直到推演完成
- 推演需要多tick进行(5~30秒，取决于杀招复杂度)
- 推演期间不可移动/战斗，被打断则失败

#### 2. 成功率公式
```
base = 0.3 (基础30%)
+ daoMarkBonus (对应道路道痕/10000, 最高+0.3)
+ pathRealmBonus (流派境界tier * 0.08, 最高+0.48)
+ rankBonus (境界等级 * 0.02)
+ resonanceBonus (道共鸣系数 - 1.0, 范围-0.3~+0.5)
- complexityPenalty (辅助蛊数量 * 0.05)
- crossPathPenalty (跨道蛊虫数 * 0.1)
= clamp(0.05, 0.95)
```

#### 3. 推演结果
- **大成功(10%)**: 杀招完美版，威力+30%或消耗-30%
- **成功(按成功率)**: 正常获得/改良杀招
- **部分成功(30%失败转化)**: 获得弱化版，威力60%
- **失败**: 资源全失，道痕+少量经验(下次+2%成功率)
- **意外发现(3%)**: 极低概率发现未知杀招

#### 4. 改良层级
- 已有杀招可反复改良，每级: 威力+15% 或 消耗-15% 或 冷却-20%
- 最多改良5级(初/良/精/极/完美)
- 每级改良难度递增(成功率-10%/级)

#### 5. 代码结构
- `core/deduction/DeductionManager.java` - 推演管理器(静态单例)
- `core/deduction/DeductionSession.java` - 推演会话(进行中的推演状态)
- `core/deduction/DeductionResult.java` - 推演结果
- `core/deduction/MoveBlueprint.java` - 杀招蓝图(推演配方)
- `core/deduction/ImprovedMove.java` - 改良杀招数据
- `network/StartDeductionPayload.java` - 客户端→服务端：开始推演
- `network/DeductionResultPayload.java` - 服务端→客户端：推演结果
- `network/SyncDeductionPayload.java` - 同步推演进度
- `client/screen/DeductionScreen.java` - 推演台GUI

---

## 二、仙窍维度系统 (Immortal Aperture Dimension)

### 原著设定
- 六七转仙窍=福地，内部空间广阔如小世界
- 吸取天地二气，产出仙元/资源
- 福地分等级：下等(≤300万亩/10仙元)、中等(400-600万亩/20仙元)、上等(700-900万亩/30仙元)、特等
- 仙窍内有道痕、天地二气残留
- 可种植/养殖/存储资源

### 游戏机制设计

#### 1. 仙窍开启条件
- 境界达到六转(Rank.RANK_6)
- 消耗大量真元进行"炸窍"
- 根据资质/道痕/真元品质决定福地等级

#### 2. 福地等级(对应MC维度大小)
| 等级 | 面积(方块) | 仙元产出/MC日 | 资源密度 |
|------|-----------|--------------|---------|
| 下等 | 64x64 | 2 | 稀疏 |
| 中等 | 128x128 | 5 | 丰富 |
| 上等 | 256x256 | 10 | 密集 |
| 特等 | 512x512 | 20 | 极密 |

#### 3. 仙窍内容
- **天地二气**: 缓慢恢复真元(比外界快3x)
- **道痕区域**: 对应玩家主修道路的方块/装饰
- **资源点**: 自动产出蛊虫喂食材料
- **灵泉**: 产出仙元(高级真元石)
- **地气涌泉**: 产出天地二气(加速修炼)

#### 4. 仙窍养成
- 投入资源扩展面积
- 种植月兰/灵药提升资源产出
- 安置蛊虫管理资源
- 布置阵法抵御灾劫

#### 5. 出入方式
- 蛊师可随时"神识内视"进入仙窍(类似传送)
- 在仙窍内时外界身体处于冥想状态(无敌但不可操作)
- 仙窍内时间流速可调(道痕越多越快)

#### 6. 代码结构
- `world/dimension/ImmortalApertureType.java` - 维度类型注册
- `world/dimension/ApertureChunkGenerator.java` - 维度地形生成
- `world/dimension/ApertureBiomeSource.java` - 生物群系
- `core/aperture/ImmortalAperture.java` - 仙窍数据管理
- `core/aperture/BlessedLandGrade.java` - 福地等级枚举
- `core/aperture/ApertureResourceManager.java` - 资源产出管理
- `network/EnterAperturePayload.java` - 进入仙窍
- `network/ExitAperturePayload.java` - 离开仙窍

---

## 三、灾劫系统 (Calamity System)

### 原著设定
- 十年一地灾，百年一天劫(游戏中压缩)
- 天劫=天空降雷/怪物，地灾=地面裂变/岩浆
- 灾劫威力与福地等级/道痕数量相关
- 炼仙蛊会额外触发灾劫
- 灾劫破坏仙窍资源和结构
- 排难蛊可削弱灾劫

### 游戏机制设计

#### 1. 灾劫周期(游戏时间)
- 地灾: 每3个MC日(约1小时现实时间)
- 天劫: 每15个MC日(约5小时)
- 特殊灾劫: 炼仙蛊/突破时触发

#### 2. 灾劫类型
| 类型 | 表现 | 破坏 |
|------|------|------|
| 地裂 | 地面开裂+岩浆涌出 | 破坏方块+伤害 |
| 雷劫 | 密集闪电 | 范围伤害+火灾 |
| 兽潮 | 怪物涌入仙窍 | 破坏资源+攻击 |
| 火灾 | 仙窍着火 | 烧毁植物/资源 |
| 虚空侵蚀 | 边缘虚空扩散 | 缩减仙窍面积 |

#### 3. 灾劫强度
- 基础强度 = 福地等级 * 2 + (道痕总数 / 1000)
- 天劫强度 = 基础 * 3
- 玩家在仙窍内可直接对抗灾劫
- 不在仙窍内时灾劫自动造成损失

#### 4. 对抗手段
- 直接战斗消灭灾劫怪物
- 使用特定蛊虫(排难蛊)削弱灾劫
- 阵法石提供被动防御
- 消耗道痕强化仙窍抵抗力

#### 5. 代码结构
- `core/aperture/calamity/CalamityManager.java` - 灾劫调度器
- `core/aperture/calamity/CalamityType.java` - 灾劫类型枚举
- `core/aperture/calamity/Calamity.java` - 灾劫实例
- `core/aperture/calamity/CalamityExecutor.java` - 灾劫效果执行
- `entity/CalamityBeastEntity.java` - 灾劫兽(灾劫生成的怪物)

---

## 四、复合战斗体系增强

### 道痕影响战斗
- 道痕使同道杀招威力倍增: 1 + (daoMarks/1000) * 0.5, 最高3.0x
- 不匹配道痕降低效果: 0.7x
- 仙体(六转+)身上道痕使所有杀招额外+20%
- 战场杀招(高级杀招)可临时复刻道痕

### 改良杀招战斗集成
- 改良后的杀招替代原版在战斗中使用
- 改良等级显示在UI上
- 完美杀招有特殊VFX效果

---

## 实现优先级

1. **P0 - 杀招推演核心** (DeductionManager + DeductionSession + Result)
2. **P0 - 推演网络/UI** (Payloads + Screen)
3. **P1 - 仙窍维度注册** (Dimension type + ChunkGenerator)
4. **P1 - 仙窍数据管理** (ImmortalAperture + Grade + Resources)
5. **P1 - 仙窍出入机制** (Enter/Exit + Network)
6. **P2 - 灾劫系统** (CalamityManager + Types + Executor)
7. **P2 - 复合战斗增强** (DaoResonance改进 + 道痕倍增)
