package com.reverendinsanity.command;

import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.aperture.calamity.CalamityManager;
import com.reverendinsanity.core.aperture.calamity.Calamity;
import com.reverendinsanity.core.clone.CloneManager;
import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.combat.SealManager;
import com.reverendinsanity.core.combat.TrapManager;
import com.reverendinsanity.core.combat.LifeDeathGateManager;
import com.reverendinsanity.core.combat.SelfDestructManager;
import com.reverendinsanity.core.combat.AmbushManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.ability.GuAbilityRegistry;
import com.reverendinsanity.core.combat.buff.GuBuffManager;
import com.reverendinsanity.core.combat.custom.PathEffectComponent;
import com.reverendinsanity.core.combat.custom.PathReactionRegistry;
import com.reverendinsanity.core.combat.custom.PathStackingRule;
import com.reverendinsanity.core.combat.killermove.MoveEffectRegistry;
import com.reverendinsanity.core.cultivation.*;
import com.reverendinsanity.core.deduction.DeductionManager;
import com.reverendinsanity.core.dream.DreamExplorationManager;
import com.reverendinsanity.core.event.WorldEventManager;
import com.reverendinsanity.core.faction.FactionReputation;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.gu.RefinementRecipe;
import com.reverendinsanity.core.heavenwill.HeavenWillManager;
import com.reverendinsanity.core.oath.PoisonOathManager;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.transformation.TransformationManager;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

// 诊断命令：测试所有模组系统的数据和状态
public class DiagnosticCommand {

    private static final ChatFormatting HEAD = ChatFormatting.GOLD;
    private static final ChatFormatting OK = ChatFormatting.GREEN;
    private static final ChatFormatting WARN = ChatFormatting.YELLOW;
    private static final ChatFormatting ERR = ChatFormatting.RED;
    private static final ChatFormatting INFO = ChatFormatting.GRAY;

    public static int diagnoseAll(ServerPlayer player) {
        send(player, "========== [蛊真人] 全系统诊断 ==========", HEAD);
        send(player, "");
        diagnoseRegistry(player);
        diagnoseAperture(player);
        diagnoseCombat(player);
        diagnoseHeavenWill(player);
        diagnoseLifespan(player);
        diagnoseClone(player);
        diagnoseFortune(player);
        diagnoseBloodline(player);
        diagnoseOath(player);
        diagnoseSeclusion(player);
        diagnoseDaoMarks(player);
        diagnoseImmortal(player);
        diagnoseDeduction(player);
        diagnoseWorldEvent(player);
        diagnoseDream(player);
        diagnoseTransformation(player);
        diagnoseFaction(player);
        diagnoseDamage(player);
        diagnoseDaoEngine(player);
        send(player, "");
        send(player, "========== 诊断完毕 ==========", HEAD);
        return 1;
    }

    public static int diagnoseModule(ServerPlayer player, String module) {
        send(player, "===== [蛊真人] 模块诊断: " + module + " =====", HEAD);
        return switch (module.toLowerCase()) {
            case "registry" -> { diagnoseRegistry(player); yield 1; }
            case "aperture" -> { diagnoseAperture(player); yield 1; }
            case "combat" -> { diagnoseCombat(player); yield 1; }
            case "heavenwill" -> { diagnoseHeavenWill(player); yield 1; }
            case "lifespan" -> { diagnoseLifespan(player); yield 1; }
            case "clone" -> { diagnoseClone(player); yield 1; }
            case "fortune" -> { diagnoseFortune(player); yield 1; }
            case "bloodline" -> { diagnoseBloodline(player); yield 1; }
            case "oath" -> { diagnoseOath(player); yield 1; }
            case "seclusion" -> { diagnoseSeclusion(player); yield 1; }
            case "daomarks" -> { diagnoseDaoMarks(player); yield 1; }
            case "immortal" -> { diagnoseImmortal(player); yield 1; }
            case "deduction" -> { diagnoseDeduction(player); yield 1; }
            case "worldevent" -> { diagnoseWorldEvent(player); yield 1; }
            case "dream" -> { diagnoseDream(player); yield 1; }
            case "transformation" -> { diagnoseTransformation(player); yield 1; }
            case "faction" -> { diagnoseFaction(player); yield 1; }
            case "damage" -> { diagnoseDamage(player); yield 1; }
            case "daoengine" -> { diagnoseDaoEngine(player); yield 1; }
            default -> {
                send(player, "未知模块: " + module, ERR);
                send(player, "可用模块: registry, aperture, combat, heavenwill, lifespan, clone, fortune, bloodline, oath, seclusion, daomarks, immortal, deduction, worldevent, dream, transformation, faction, damage, daoengine", INFO);
                yield 0;
            }
        };
    }

    private static void diagnoseRegistry(ServerPlayer player) {
        send(player, "[注册表]", HEAD);
        int guCount = GuRegistry.getAll().size();
        int moveCount = KillerMoveRegistry.getAll().size();
        int abilityCount = GuAbilityRegistry.getAll().size();
        int recipeCount = RefinementRecipe.getAllRecipes().size();
        int effectCount = 0;
        for (KillerMove m : KillerMoveRegistry.getAll()) {
            if (MoveEffectRegistry.hasEffect(m.id())) effectCount++;
        }
        int pathEffectCount = PathEffectComponent.getAll().size();

        status(player, "蛊虫种类", guCount, guCount >= 148 ? OK : WARN, "(期望>=148)");
        status(player, "杀招种类", moveCount, moveCount >= 61 ? OK : WARN, "(期望>=61)");
        status(player, "技能种类", abilityCount, abilityCount >= 146 ? OK : WARN, "(期望>=146)");
        status(player, "炼蛊配方", recipeCount, recipeCount >= 52 ? OK : WARN, "(期望>=52)");
        status(player, "杀招专属效果", effectCount, effectCount > 0 ? OK : WARN, "/" + moveCount);
        status(player, "道路效果映射", pathEffectCount, pathEffectCount >= 48 ? OK : WARN, "/48");

        Map<Integer, Integer> rankDist = new HashMap<>();
        Map<DaoPath, Integer> pathDist = new HashMap<>();
        for (GuType type : GuRegistry.getAll()) {
            rankDist.merge(type.rank(), 1, Integer::sum);
            pathDist.merge(type.path(), 1, Integer::sum);
        }
        StringBuilder rankStr = new StringBuilder("蛊虫转数分布: ");
        rankDist.entrySet().stream().sorted(Map.Entry.comparingByKey())
            .forEach(e -> rankStr.append(e.getKey()).append("转:").append(e.getValue()).append(" "));
        send(player, rankStr.toString(), INFO);

        int coveredPaths = 0;
        for (DaoPath p : DaoPath.values()) {
            if (pathDist.getOrDefault(p, 0) > 0) coveredPaths++;
        }
        status(player, "道路覆盖", coveredPaths, coveredPaths >= 48 ? OK : WARN, "/48");
    }

    private static void diagnoseAperture(ServerPlayer player) {
        send(player, "[空窍系统]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture ap = data.getAperture();

        if (!ap.isOpened()) {
            send(player, "状态: 未开窍", WARN);
            send(player, "验证: 使用希望蛊/命令开窍后重新诊断", INFO);
            return;
        }

        status(player, "境界", ap.getRank().getLevel() + "转·" + ap.getSubRank().getDisplayName(), OK, "");
        status(player, "资质", ap.getAptitude().getDisplayName(), OK, "");
        status(player, "真元", String.format("%.0f/%.0f", ap.getCurrentEssence(), ap.getMaxEssence()),
            ap.getCurrentEssence() > 0 ? OK : WARN, "");
        status(player, "念头", String.format("%.0f/%.0f", ap.getThoughts(), ap.getMaxThoughts()),
            ap.getThoughts() > 0 ? OK : WARN, "");

        List<GuInstance> guList = ap.getStoredGu();
        int alive = 0, dead = 0, damaged = 0, refined = 0;
        for (GuInstance gu : guList) {
            if (gu.isAlive()) alive++; else dead++;
            if (gu.isDamaged()) damaged++;
            if (gu.isRefined()) refined++;
        }
        status(player, "蛊虫总数", guList.size(), guList.size() > 0 ? OK : WARN, "");
        status(player, "存活/死亡", alive + "/" + dead, dead == 0 ? OK : WARN, "");
        status(player, "受损", damaged, damaged == 0 ? OK : WARN, "");
        status(player, "已炼化", refined, OK, "/" + guList.size());

        if (ap.getPrimaryPath() != null) {
            status(player, "主要道路", ap.getPrimaryPath().getDisplayName(), OK, "");
        }
    }

    private static void diagnoseCombat(ServerPlayer player) {
        send(player, "[战斗系统]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture ap = data.getAperture();
        CombatState cs = data.getCombatState();
        GuBuffManager bm = data.getBuffManager();

        if (!ap.isOpened()) {
            send(player, "空窍未开，跳过战斗诊断", INFO);
            return;
        }

        int availableAbilities = 0;
        for (GuInstance gu : ap.getStoredGu()) {
            if (!gu.isActive()) continue;
            GuAbility ability = GuAbilityRegistry.get(gu.getTypeId());
            if (ability != null) availableAbilities++;
        }
        status(player, "可用技能", availableAbilities, availableAbilities > 0 ? OK : WARN, "");

        List<ResourceLocation> moves = cs.getEquippedMoves();
        status(player, "装备杀招", moves.size(), OK, "/2");
        for (ResourceLocation moveId : moves) {
            KillerMove move = KillerMoveRegistry.get(moveId);
            if (move != null) {
                boolean onCd = cs.isMoveCooldown(moveId);
                send(player, "  " + move.displayName() + " [" + move.primaryPath().getDisplayName() + "] " +
                    (onCd ? "冷却中" : "就绪"), onCd ? WARN : OK);
            }
        }

        int buffCount = bm.getActiveBuffs().size();
        status(player, "活跃增益", buffCount, OK, "");

        boolean sealed = SealManager.isSealed(player);
        status(player, "封印状态", sealed ? "被封印" : "正常", sealed ? ERR : OK, "");
    }

    private static void diagnoseHeavenWill(ServerPlayer player) {
        send(player, "[天意系统]", HEAD);
        float attention = HeavenWillManager.getAttention(player);
        ChatFormatting color = attention < 25 ? OK : attention < 50 ? WARN : attention < 75 ? ChatFormatting.GOLD : ERR;
        status(player, "天意关注度", String.format("%.1f", attention), color, "/100");

        String level;
        if (attention < 25) level = "安全 - 天意未注意";
        else if (attention < 50) level = "注意 - 真元消耗加快";
        else if (attention < 75) level = "警告 - 可能遭受落雷";
        else if (attention < 90) level = "危险 - 属性被压制";
        else level = "极危 - 随时可能遭受天罚";
        send(player, "  威胁等级: " + level, color);
    }

    private static void diagnoseLifespan(ServerPlayer player) {
        send(player, "[寿元系统]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        int lifespan = data.getLifespan();
        int rankLevel = data.getAperture().isOpened() ? data.getAperture().getRank().getLevel() : 0;
        int maxLifespan = LifespanManager.getMaxLifespan(rankLevel);

        if (lifespan == 0 && !data.getAperture().isOpened()) {
            send(player, "未开窍，寿元系统未激活", INFO);
            return;
        }

        float pct = maxLifespan > 0 ? (float) lifespan / maxLifespan * 100 : 0;
        ChatFormatting color = pct > 50 ? OK : pct > 25 ? WARN : pct > 10 ? ChatFormatting.GOLD : ERR;
        status(player, "寿元", lifespan + "/" + maxLifespan, color, String.format("(%.0f%%)", pct));

        if (pct <= 10) send(player, "  [!] 寿元即将耗尽！", ERR);
    }

    private static void diagnoseClone(ServerPlayer player) {
        send(player, "[分身系统]", HEAD);
        boolean active = CloneManager.isActive(player);
        status(player, "分身状态", active ? "已激活" : "未激活", active ? OK : INFO, "");
        if (active) {
            send(player, "  效果: 20%闪避 + 40%额外伤害 + 速度/攻击提升", OK);
        }
    }

    private static void diagnoseFortune(ServerPlayer player) {
        send(player, "[气运系统]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float luck = data.getLuck();

        ChatFormatting color;
        String desc;
        if (luck > 1.2f) { color = OK; desc = "鸿运当头"; }
        else if (luck > 1.0f) { color = OK; desc = "好运"; }
        else if (luck >= 1.0f) { color = INFO; desc = "正常"; }
        else if (luck >= 0.7f) { color = WARN; desc = "运气不佳"; }
        else if (luck >= 0.5f) { color = ERR; desc = "厄运缠身"; }
        else { color = ERR; desc = "大凶之兆"; }

        status(player, "气运值", String.format("%.2f", luck), color, "(" + desc + ")");
        status(player, "炼蛊加成", String.format("%.0f%%", FortunePlunderManager.getRefinementBonus(player) * 100), OK, "");
        status(player, "掉落加成", String.format("%.0f%%", FortunePlunderManager.getLootDropBonus(player) * 100), OK, "");
    }

    private static void diagnoseBloodline(ServerPlayer player) {
        send(player, "[血脉系统]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        int bloodlineId = data.getBloodlineId();

        if (bloodlineId == 0) {
            send(player, "未觉醒血脉 (开窍时自动分配)", INFO);
            return;
        }

        BloodlineManager.Bloodline bl = BloodlineManager.getBloodline(player);
        if (bl != null) {
            status(player, "血脉", bl.displayName, OK, "(ID:" + bloodlineId + ")");
            DaoPath affinity = BloodlineManager.getAffinityPath(bl);
            if (affinity != null) {
                status(player, "亲和道路", affinity.getDisplayName(), OK, "");
            }
        } else {
            status(player, "血脉ID", bloodlineId, WARN, "(未识别)");
        }
    }

    private static void diagnoseOath(ServerPlayer player) {
        send(player, "[毒誓系统]", HEAD);
        boolean active = PoisonOathManager.hasActiveOath(player);
        status(player, "毒誓状态", active ? "有效" : "无", active ? WARN : INFO, "");
    }

    private static void diagnoseSeclusion(ServerPlayer player) {
        send(player, "[闭关系统]", HEAD);
        boolean inSeclusion = SeclusionManager.isInSeclusion(player);
        status(player, "闭关状态", inSeclusion ? "闭关中" : "未闭关", inSeclusion ? OK : INFO, "");
        if (inSeclusion) {
            send(player, "  效果: 加速真元恢复 + 定期获得道痕", OK);
        }
    }

    private static void diagnoseDaoMarks(ServerPlayer player) {
        send(player, "[道痕系统]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        int total = data.getTotalDaoMarks();
        status(player, "道痕总数", total, total > 0 ? OK : INFO, "");

        Map<DaoPath, Integer> marks = data.getAllDaoMarks();
        List<Map.Entry<DaoPath, Integer>> sorted = new ArrayList<>(marks.entrySet());
        sorted.sort(Comparator.<Map.Entry<DaoPath, Integer>>comparingInt(Map.Entry::getValue).reversed());

        int shown = 0;
        for (Map.Entry<DaoPath, Integer> e : sorted) {
            if (e.getValue() <= 0) continue;
            send(player, "  " + e.getKey().getDisplayName() + "道: " + e.getValue() +
                " (加成:" + String.format("%.0f%%", data.getDaoMarkBonus(e.getKey()) * 100) + ")", INFO);
            if (++shown >= 8) {
                send(player, "  ... 省略 " + (sorted.size() - 8) + " 条", INFO);
                break;
            }
        }
    }

    private static void diagnoseImmortal(ServerPlayer player) {
        send(player, "[仙窍系统]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImmortalAperture iap = data.getImmortalAperture();

        if (!iap.isFormed()) {
            send(player, "仙窍未开辟 (需要4转以上)", INFO);
            return;
        }

        status(player, "品阶", iap.getGrade().getDisplayName(), OK, "");
        status(player, "完整度", String.format("%.1f%%", iap.getIntegrity()), iap.getIntegrity() > 50 ? OK : WARN, "");
        status(player, "天气", String.format("%.0f", iap.getStoredHeavenQi()), OK, "/" + (int) iap.getMaxQi());
        status(player, "地气", String.format("%.0f", iap.getStoredEarthQi()), OK, "/" + (int) iap.getMaxQi());
        status(player, "仙元石", iap.getImmortalEssenceStones(), OK, "");
        status(player, "发展等级", iap.getDevelopmentLevel(), OK, "");
        status(player, "漏洞数", iap.getBreachCount(), iap.getBreachCount() == 0 ? OK : WARN, "");

        boolean inCalamity = CalamityManager.isInCalamity(player.getUUID());
        if (inCalamity) {
            Calamity cal = CalamityManager.getActiveCalamity(player.getUUID());
            if (cal != null) {
                status(player, "灾劫", cal.getType().getDisplayName(), ERR,
                    String.format("进度:%.0f%%", cal.getProgress() * 100));
            }
        } else {
            status(player, "灾劫", "无", OK, "");
        }
    }

    private static void diagnoseDeduction(ServerPlayer player) {
        send(player, "[推演系统]", HEAD);
        UUID uuid = player.getUUID();
        boolean deducting = DeductionManager.isDeducting(uuid);
        status(player, "推演状态", deducting ? "推演中" : "空闲", deducting ? OK : INFO, "");

        if (deducting) {
            var session = DeductionManager.getSession(uuid);
            if (session != null) {
                send(player, "  进度: " + String.format("%.0f%%", session.getProgress() * 100) +
                    " 成功率: " + String.format("%.0f%%", session.getSuccessRate() * 100), OK);
            }
        }

        var improved = DeductionManager.getAllImprovedMoves(uuid);
        status(player, "推演过的杀招", improved != null ? improved.size() : 0, OK, "");
    }

    private static void diagnoseWorldEvent(ServerPlayer player) {
        send(player, "[天地异象]", HEAD);
        var activeEvent = WorldEventManager.getActiveEvent(player.level());
        if (activeEvent != null) {
            status(player, "当前异象", activeEvent.getType().name(), OK, "");
        } else {
            status(player, "当前异象", "无", INFO, "");
        }
    }

    private static void diagnoseDream(ServerPlayer player) {
        send(player, "[梦境系统]", HEAD);
        boolean dreaming = DreamExplorationManager.isDreaming(player);
        status(player, "梦境状态", dreaming ? "入梦中" : "清醒", dreaming ? OK : INFO, "");
    }

    private static void diagnoseTransformation(ServerPlayer player) {
        send(player, "[变身系统]", HEAD);
        boolean transformed = TransformationManager.isTransformed(player);
        status(player, "变身状态", transformed ? "变身中" : "未变身", transformed ? OK : INFO, "");
        if (transformed) {
            var form = TransformationManager.getCurrentForm(player.getUUID());
            if (form != null) {
                send(player, "  当前形态: " + form.name(), OK);
            }
        }
    }

    private static void diagnoseFaction(ServerPlayer player) {
        send(player, "[势力声望]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        FactionReputation rep = data.getFactionReputation();
        send(player, "  势力系统已集成到蛊师/商人实体", INFO);
    }

    private static void diagnoseDamage(ServerPlayer player) {
        send(player, "[蛊虫损伤]", HEAD);
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture ap = data.getAperture();
        if (!ap.isOpened()) {
            send(player, "空窍未开", INFO);
            return;
        }

        int total = ap.getStoredGu().size();
        int damaged = 0;
        for (GuInstance gu : ap.getStoredGu()) {
            if (gu.isDamaged()) damaged++;
        }
        status(player, "蛊虫损伤", damaged + "/" + total, damaged == 0 ? OK : WARN, "");
        if (damaged > 0) {
            for (GuInstance gu : ap.getStoredGu()) {
                if (gu.isDamaged()) {
                    GuType type = GuRegistry.get(gu.getTypeId());
                    send(player, "  [损] " + (type != null ? type.displayName() : gu.getTypeId().toString()) +
                        " (效果降至50%)", WARN);
                }
            }
        }
    }

    private static void diagnoseDaoEngine(ServerPlayer player) {
        send(player, "[道组合引擎]", HEAD);
        int pathEffects = PathEffectComponent.getAll().size();
        status(player, "道路效果映射", pathEffects, pathEffects >= 48 ? OK : WARN, "/48");
        send(player, "  道反应系统: PathReactionRegistry 已加载", OK);
        send(player, "  道叠加系统: PathStackingRule 已加载", OK);

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture ap = data.getAperture();
        if (ap.isOpened() && !ap.getStoredGu().isEmpty()) {
            Map<DaoPath, Integer> pathCounts = new EnumMap<>(DaoPath.class);
            for (GuInstance gu : ap.getStoredGu()) {
                if (!gu.isActive()) continue;
                GuType type = GuRegistry.get(gu.getTypeId());
                if (type != null) {
                    pathCounts.merge(type.path(), 1, Integer::sum);
                }
            }

            List<PathStackingRule.StackThreshold> stacks = PathStackingRule.check(pathCounts);
            if (!stacks.isEmpty()) {
                send(player, "  当前蛊虫可触发叠加效果:", OK);
                for (var st : stacks) {
                    send(player, "    x" + st.requiredCount() +
                        " -> " + st.effect().name() + " (" + st.description() + ")", OK);
                }
            }

            List<DaoPath> paths = new ArrayList<>(pathCounts.keySet());
            if (paths.size() >= 2) {
                var reactions = PathReactionRegistry.findReactions(paths);
                if (!reactions.isEmpty()) {
                    send(player, "  当前蛊虫可触发道反应:", OK);
                    for (var r : reactions) {
                        send(player, "    " + r.name() + " -> " + r.type().name(), OK);
                    }
                }
            }
        }
    }

    private static void send(ServerPlayer player, String msg, ChatFormatting... formats) {
        MutableComponent comp = Component.literal(msg);
        for (ChatFormatting f : formats) comp = comp.withStyle(f);
        player.sendSystemMessage(comp);
    }

    private static void status(ServerPlayer player, String label, Object value, ChatFormatting color, String suffix) {
        MutableComponent comp = Component.literal("  " + label + ": ")
            .withStyle(INFO)
            .append(Component.literal(String.valueOf(value)).withStyle(color));
        if (suffix != null && !suffix.isEmpty()) {
            comp = comp.append(Component.literal(" " + suffix).withStyle(INFO));
        }
        player.sendSystemMessage(comp);
    }
}
