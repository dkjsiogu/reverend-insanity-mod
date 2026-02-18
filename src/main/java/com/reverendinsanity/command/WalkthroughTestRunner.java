package com.reverendinsanity.command;

import com.reverendinsanity.core.clone.CloneManager;
import com.reverendinsanity.core.combat.CombatState;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.combat.TrapManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.combat.ability.GuAbilityRegistry;
import com.reverendinsanity.core.combat.killermove.KillerMoveExecutor;
import com.reverendinsanity.core.cultivation.*;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.heavenwill.HeavenWillManager;
import com.reverendinsanity.core.oath.PoisonOathManager;
import com.reverendinsanity.core.transformation.TransformationManager;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

// 自动化走查测试：逐步执行+暂停确认+最终报告
public class WalkthroughTestRunner {

    private static final Map<UUID, TestSession> sessions = new ConcurrentHashMap<>();

    public static int start(ServerPlayer player) {
        if (sessions.containsKey(player.getUUID())) {
            send(player, "测试已在进行中。输入 /gu test stop 终止当前测试。", ChatFormatting.RED);
            return 0;
        }
        TestSession session = new TestSession(player);
        sessions.put(player.getUUID(), session);
        send(player, "");
        send(player, "╔══════════════════════════════════════╗", ChatFormatting.GOLD);
        send(player, "║   蛊真人 · 全流程走查测试            ║", ChatFormatting.GOLD);
        send(player, "╚══════════════════════════════════════╝", ChatFormatting.GOLD);
        send(player, "");
        send(player, "测试将逐步执行每个系统功能。", ChatFormatting.GRAY);
        send(player, "每步执行后请确认表现：", ChatFormatting.GRAY);
        send(player, "  /gu test ok    - 表现正常", ChatFormatting.GREEN);
        send(player, "  /gu test fail  - 有问题", ChatFormatting.RED);
        send(player, "  /gu test skip  - 跳过此步", ChatFormatting.YELLOW);
        send(player, "  /gu test stop  - 终止测试", ChatFormatting.GRAY);
        send(player, "");
        send(player, "总计 " + session.steps.size() + " 个测试步骤。准备开始...", ChatFormatting.AQUA);
        send(player, "");
        session.executeCurrentStep();
        return 1;
    }

    public static int respond(ServerPlayer player, String response) {
        TestSession session = sessions.get(player.getUUID());
        if (session == null) {
            send(player, "没有进行中的测试。使用 /gu test 开始。", ChatFormatting.RED);
            return 0;
        }

        switch (response.toLowerCase()) {
            case "ok" -> {
                session.recordResult(TestResult.PASS);
                send(player, "  ✓ 已记录：通过", ChatFormatting.GREEN);
            }
            case "fail" -> {
                session.recordResult(TestResult.FAIL);
                send(player, "  ✗ 已记录：失败", ChatFormatting.RED);
            }
            case "skip" -> {
                session.recordResult(TestResult.SKIP);
                send(player, "  - 已跳过", ChatFormatting.YELLOW);
            }
            case "stop" -> {
                send(player, "测试已终止。", ChatFormatting.RED);
                showReport(player, session);
                sessions.remove(player.getUUID());
                return 1;
            }
            default -> {
                send(player, "无效响应。可选: ok / fail / skip / stop", ChatFormatting.RED);
                return 0;
            }
        }

        session.currentStep++;
        if (session.currentStep >= session.steps.size()) {
            send(player, "");
            send(player, "所有测试步骤已完成！", ChatFormatting.GOLD);
            showReport(player, session);
            sessions.remove(player.getUUID());
        } else {
            send(player, "");
            session.executeCurrentStep();
        }
        return 1;
    }

    public static boolean hasActiveSession(ServerPlayer player) {
        return sessions.containsKey(player.getUUID());
    }

    public static void onPlayerLogout(ServerPlayer player) {
        sessions.remove(player.getUUID());
    }

    private static void showReport(ServerPlayer player, TestSession session) {
        send(player, "");
        send(player, "╔══════════════════════════════════════╗", ChatFormatting.GOLD);
        send(player, "║         走查测试报告                 ║", ChatFormatting.GOLD);
        send(player, "╚══════════════════════════════════════╝", ChatFormatting.GOLD);

        int pass = 0, fail = 0, skip = 0, untested = 0;
        List<String> failedItems = new ArrayList<>();

        for (int i = 0; i < session.steps.size(); i++) {
            TestStep step = session.steps.get(i);
            TestResult result = session.results.getOrDefault(i, TestResult.UNTESTED);
            switch (result) {
                case PASS -> pass++;
                case FAIL -> { fail++; failedItems.add(step.name); }
                case SKIP -> skip++;
                case UNTESTED -> untested++;
            }
        }

        int total = session.steps.size();
        send(player, String.format("  总计: %d 步", total), ChatFormatting.GRAY);
        send(player, String.format("  通过: %d", pass), ChatFormatting.GREEN);
        send(player, String.format("  失败: %d", fail), fail > 0 ? ChatFormatting.RED : ChatFormatting.GREEN);
        send(player, String.format("  跳过: %d", skip), ChatFormatting.YELLOW);
        send(player, String.format("  未测: %d", untested), ChatFormatting.GRAY);
        send(player, "");

        if (!failedItems.isEmpty()) {
            send(player, "失败项目：", ChatFormatting.RED);
            for (String item : failedItems) {
                send(player, "  ✗ " + item, ChatFormatting.RED);
            }
        }

        float passRate = total > 0 ? (float) pass / (total - skip) * 100 : 0;
        ChatFormatting rateColor = passRate >= 90 ? ChatFormatting.GREEN : passRate >= 70 ? ChatFormatting.YELLOW : ChatFormatting.RED;
        send(player, String.format("通过率: %.0f%%", passRate), rateColor);
    }

    enum TestResult { PASS, FAIL, SKIP, UNTESTED }

    static class TestStep {
        final String name;
        final String category;
        final String[] expectedFeedback;
        final Consumer<ServerPlayer> action;

        TestStep(String category, String name, String[] expected, Consumer<ServerPlayer> action) {
            this.category = category;
            this.name = name;
            this.expectedFeedback = expected;
            this.action = action;
        }
    }

    static class TestSession {
        final ServerPlayer player;
        final List<TestStep> steps;
        final Map<Integer, TestResult> results = new HashMap<>();
        int currentStep = 0;

        TestSession(ServerPlayer player) {
            this.player = player;
            this.steps = buildSteps();
        }

        void executeCurrentStep() {
            TestStep step = steps.get(currentStep);
            send(player, String.format("[步骤 %d/%d] [%s] %s",
                currentStep + 1, steps.size(), step.category, step.name), ChatFormatting.AQUA);
            send(player, "  执行中...", ChatFormatting.GRAY);

            try {
                step.action.accept(player);
                send(player, "  ✓ 执行完成", ChatFormatting.GREEN);
            } catch (Exception e) {
                send(player, "  ✗ 执行出错: " + e.getMessage(), ChatFormatting.RED);
            }

            send(player, "  期望效果：", ChatFormatting.YELLOW);
            for (String fb : step.expectedFeedback) {
                send(player, "    → " + fb, ChatFormatting.YELLOW);
            }
            send(player, "  输入 /gu test ok|fail|skip 确认", ChatFormatting.GRAY);
        }

        void recordResult(TestResult result) {
            results.put(currentStep, result);
        }

        private List<TestStep> buildSteps() {
            List<TestStep> s = new ArrayList<>();

            // === 基础系统 ===
            s.add(new TestStep("基础", "重置数据", new String[]{
                "聊天栏显示重置确认",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                data.getAperture().reset();
                p.displayClientMessage(Component.literal("数据已重置"), false);
            }));

            s.add(new TestStep("开窍", "开窍 (A级资质)", new String[]{
                "聊天栏显示开窍成功消息",
                "应有音效反馈",
                "HUD左上角出现修炼状态条",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                Aperture ap = data.getAperture();
                if (!ap.isOpened()) {
                    ap.open(Aptitude.A);
                }
                data.setLifespan(LifespanManager.getMaxLifespan(1));
                p.displayClientMessage(Component.literal("开窍完成！资质A 1转·初阶").withStyle(ChatFormatting.GOLD), false);
                p.level().playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);
            }));

            s.add(new TestStep("开窍", "HUD覆层显示", new String[]{
                "屏幕左上角显示: 境界、真元条、念头条",
                "真元和念头应在缓慢恢复",
            }, p -> {
                p.displayClientMessage(Component.literal("请查看屏幕左上角HUD覆层"), false);
            }));

            // === 蛊虫系统 ===
            s.add(new TestStep("蛊虫", "给予蛊虫 (月光蛊+熊力蛊+铜皮蛊)", new String[]{
                "背包中出现3个蛊虫物品",
                "物品有tooltip描述（鼠标悬停查看）",
            }, p -> {
                giveGuItem(p, "moonlight_gu");
                giveGuItem(p, "bear_strength_gu");
                giveGuItem(p, "copper_skin_gu");
                p.displayClientMessage(Component.literal("已给予3只蛊虫物品"), false);
            }));

            s.add(new TestStep("蛊虫", "蛊虫放入空窍", new String[]{
                "蛊虫从背包消失，进入空窍",
                "按G打开空窍界面应能看到蛊虫列表",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                Aperture ap = data.getAperture();
                addGuToAperture(ap, "moonlight_gu");
                addGuToAperture(ap, "bear_strength_gu");
                addGuToAperture(ap, "copper_skin_gu");
                p.displayClientMessage(Component.literal("3只蛊虫已放入空窍。按G查看"), false);
            }));

            s.add(new TestStep("蛊虫", "空窍管理界面 (G键)", new String[]{
                "打开空窍管理界面（暗色面板）",
                "左侧显示蛊虫列表（月光/熊力/铜皮）",
                "右侧显示蛊虫详情和喂养按钮",
                "界面无模糊效果",
            }, p -> {
                p.displayClientMessage(Component.literal("请按 G 键打开空窍管理界面"), false);
            }));

            // === 技能系统 ===
            s.add(new TestStep("技能", "催动蛊虫技能 (R键)", new String[]{
                "催动月光蛊技能",
                "应有月道音效播放",
                "应有视觉粒子/特效",
                "HUD真元条减少",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                Aperture ap = data.getAperture();
                CombatState cs = data.getCombatState();
                GuAbility ability = GuAbilityRegistry.get(GuRegistry.id("moonlight_gu"));
                if (ability != null && ability.canUse(p, ap, cs)) {
                    ability.execute(p, ap, cs);
                    p.displayClientMessage(Component.literal("月光蛊技能已催动！"), false);
                } else {
                    p.displayClientMessage(Component.literal("直接催动了月光蛊技能（手动）"), false);
                    if (ability != null) {
                        ap.setCurrentEssence(ap.getMaxEssence());
                        ability.execute(p, ap, cs);
                    }
                }
            }));

            s.add(new TestStep("技能", "催动熊力蛊技能 (F键)", new String[]{
                "催动熊力蛊技能",
                "应有力道音效",
                "角色攻击力应提升",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                Aperture ap = data.getAperture();
                CombatState cs = data.getCombatState();
                ap.setCurrentEssence(ap.getMaxEssence());
                GuAbility ability = GuAbilityRegistry.get(GuRegistry.id("bear_strength_gu"));
                if (ability != null) {
                    ability.execute(p, ap, cs);
                    p.displayClientMessage(Component.literal("熊力蛊技能已催动！"), false);
                }
            }));

            // === 杀招系统 ===
            s.add(new TestStep("杀招", "装备杀招", new String[]{
                "聊天栏显示杀招装备成功",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                CombatState cs = data.getCombatState();
                KillerMove firstMove = null;
                for (KillerMove m : KillerMoveRegistry.getAll()) {
                    if (m.minRank() <= 1) { firstMove = m; break; }
                }
                if (firstMove != null) {
                    cs.equipMove(firstMove.id());
                    p.displayClientMessage(Component.literal("已装备杀招: " + firstMove.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                } else {
                    p.displayClientMessage(Component.literal("未找到1转杀招"), false);
                }
            }));

            s.add(new TestStep("杀招", "施展杀招 (Z键)", new String[]{
                "杀招施展，应有显著音效",
                "应有明显视觉特效（粒子/光效）",
                "真元大量消耗",
                "HUD可能出现增益图标",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                Aperture ap = data.getAperture();
                CombatState cs = data.getCombatState();
                ap.setCurrentEssence(ap.getMaxEssence());
                List<ResourceLocation> equipped = cs.getEquippedMoves();
                if (!equipped.isEmpty()) {
                    KillerMove move = KillerMoveRegistry.get(equipped.get(0));
                    if (move != null) {
                        KillerMoveExecutor.execute(p, ap, cs, move);
                        p.displayClientMessage(Component.literal("杀招施展: " + move.displayName() + "！").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                    }
                }
            }));

            // === 转轮菜单 ===
            s.add(new TestStep("界面", "转轮菜单 (~键)", new String[]{
                "出现圆形转轮菜单（12个扇区）",
                "鼠标悬停高亮显示动作名",
                "背景半透明不模糊",
                "松键或点击可选择动作",
            }, p -> {
                p.displayClientMessage(Component.literal("请按住 ~ 键打开转轮菜单"), false);
            }));

            // === 虚影分身 ===
            s.add(new TestStep("分身", "催动虚影分身", new String[]{
                "聊天栏显示分身催动成功",
                "应有紫色粒子环绕角色",
                "移动速度应明显加快",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                data.getAperture().setCurrentEssence(data.getAperture().getMaxEssence());
                if (CloneManager.tryActivate(p)) {
                    p.displayClientMessage(Component.literal("虚影分身·催动！").withStyle(ChatFormatting.LIGHT_PURPLE), false);
                } else {
                    p.displayClientMessage(Component.literal("分身催动失败（可能冷却中）"), false);
                }
            }));

            // === 闭关 ===
            s.add(new TestStep("闭关", "进入闭关状态", new String[]{
                "聊天栏显示闭关进入",
                "角色周围出现紫色粒子",
                "真元开始加速恢复",
                "移动会中断闭关",
            }, p -> {
                if (SeclusionManager.enterSeclusion(p)) {
                    p.displayClientMessage(Component.literal("进入闭关...静心凝神").withStyle(ChatFormatting.AQUA), false);
                } else {
                    p.displayClientMessage(Component.literal("闭关进入失败"), false);
                }
            }));

            // === 生死门 ===
            s.add(new TestStep("生死门", "打开生死门 (50%赌博)", new String[]{
                "生门：全回复 + 攻击/速度增益",
                "死门：扣40%HP + 减益 + 寿元消耗",
                "应有明显音效反馈",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                data.getAperture().setCurrentEssence(data.getAperture().getMaxEssence());
                if (com.reverendinsanity.core.combat.LifeDeathGateManager.openGate(p)) {
                    // openGate内部处理消息
                } else {
                    p.displayClientMessage(Component.literal("生死门冷却中"), false);
                }
            }));

            // === 陷阱 ===
            s.add(new TestStep("陷阱", "布置隐形陷阱", new String[]{
                "聊天栏显示布置成功",
                "脚下位置应有微弱粒子提示",
                "敌人靠近会触发AoE伤害",
            }, p -> {
                if (TrapManager.placeTrap(p)) {
                    p.displayClientMessage(Component.literal("陷阱已布置！").withStyle(ChatFormatting.GOLD), false);
                } else {
                    p.displayClientMessage(Component.literal("陷阱布置失败"), false);
                }
            }));

            // === 毒誓 ===
            s.add(new TestStep("毒誓", "立杀伐毒誓", new String[]{
                "聊天栏显示毒誓信息",
                "获得临时攻击/速度增益",
                "限时内需击杀目标，否则受罚",
            }, p -> {
                if (!PoisonOathManager.hasActiveOath(p)) {
                    if (PoisonOathManager.makeOath(p, PoisonOathManager.OathType.KILL_VOW)) {
                        p.displayClientMessage(Component.literal("杀伐毒誓·已立！").withStyle(ChatFormatting.DARK_RED), false);
                    }
                } else {
                    p.displayClientMessage(Component.literal("已有活跃毒誓"), false);
                }
            }));

            // === 变身 ===
            s.add(new TestStep("变身", "变身:狼形", new String[]{
                "角色移速明显增加",
                "应有灰色粒子环绕",
                "聊天栏显示变身信息",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                data.getAperture().setCurrentEssence(data.getAperture().getMaxEssence());
                if (TransformationManager.isTransformed(p)) {
                    TransformationManager.cancelTransform(p);
                }
                if (TransformationManager.tryTransform(p, TransformationManager.TransformForm.WOLF)) {
                    p.displayClientMessage(Component.literal("狼形变身！").withStyle(ChatFormatting.GRAY), false);
                }
            }));

            s.add(new TestStep("变身", "解除变身", new String[]{
                "速度恢复正常",
                "粒子效果消失",
            }, p -> {
                if (TransformationManager.isTransformed(p)) {
                    TransformationManager.cancelTransform(p);
                    p.displayClientMessage(Component.literal("变身已解除"), false);
                }
            }));

            // === 天意系统 ===
            s.add(new TestStep("天意", "天意关注度测试", new String[]{
                "聊天栏显示当前天意关注度数值",
                "关注度应从前面的技能/杀招操作中有所增长",
            }, p -> {
                float attention = HeavenWillManager.getAttention(p);
                p.displayClientMessage(Component.literal(
                    "天意关注度: " + String.format("%.1f", attention) + "/100").withStyle(
                    attention < 25 ? ChatFormatting.GREEN : attention < 50 ? ChatFormatting.YELLOW : ChatFormatting.RED), false);
            }));

            // === 寿元 ===
            s.add(new TestStep("寿元", "寿元系统状态", new String[]{
                "显示当前寿元/上限",
                "寿元应在缓慢消耗（每200tick-1）",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                int ls = data.getLifespan();
                int max = LifespanManager.getMaxLifespan(data.getAperture().getRank().getLevel());
                p.displayClientMessage(Component.literal(
                    "寿元: " + ls + "/" + max + " (" + (max > 0 ? (ls * 100 / max) : 0) + "%)").withStyle(ChatFormatting.AQUA), false);
            }));

            // === 血脉 ===
            s.add(new TestStep("血脉", "血脉状态", new String[]{
                "显示分配的血脉类型",
                "血脉提供永久属性加成",
            }, p -> {
                BloodlineManager.Bloodline bl = BloodlineManager.getBloodline(p);
                if (bl != null && bl != BloodlineManager.Bloodline.NONE) {
                    p.displayClientMessage(Component.literal("血脉: " + bl.displayName).withStyle(ChatFormatting.GOLD), false);
                } else {
                    BloodlineManager.assignBloodline(p);
                    bl = BloodlineManager.getBloodline(p);
                    p.displayClientMessage(Component.literal("血脉已分配: " + (bl != null ? bl.displayName : "无")).withStyle(ChatFormatting.GOLD), false);
                }
            }));

            // === 气运 ===
            s.add(new TestStep("气运", "气运系统状态", new String[]{
                "显示气运数值和状态描述",
                "气运影响掉落和炼蛊成功率",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                float luck = data.getLuck();
                String desc = luck > 1.0f ? "好运" : luck >= 1.0f ? "正常" : "运气不佳";
                p.displayClientMessage(Component.literal(
                    "气运: " + String.format("%.2f", luck) + " (" + desc + ")").withStyle(ChatFormatting.GOLD), false);
            }));

            // === 蛊虫图鉴 ===
            s.add(new TestStep("界面", "蛊虫图鉴 (K键)", new String[]{
                "打开图鉴界面",
                "显示已发现的蛊虫列表",
                "界面无模糊效果",
            }, p -> {
                p.displayClientMessage(Component.literal("请按 K 键打开蛊虫图鉴"), false);
            }));

            // === 推演界面 ===
            s.add(new TestStep("界面", "推演界面 (J键)", new String[]{
                "打开推演界面",
                "显示蛊虫选择/道路选择区域",
                "显示成功率预估",
            }, p -> {
                p.displayClientMessage(Component.literal("请按 J 键打开推演界面"), false);
            }));

            // === 境界突破 ===
            s.add(new TestStep("突破", "小境界突破 (蹲下冥想)", new String[]{
                "蹲下5秒后触发冥想突破",
                "应有音效和粒子效果",
                "境界从初阶提升",
            }, p -> {
                GuMasterData data = p.getData(ModAttachments.GU_MASTER_DATA.get());
                Aperture ap = data.getAperture();
                ap.setCurrentEssence(ap.getMaxEssence());
                ap.regenerateThoughts(ap.getMaxThoughts());
                p.displayClientMessage(Component.literal("真元/念头已充满。请蹲下5秒触发冥想突破。"), false);
            }));

            // === 注册表验证 ===
            s.add(new TestStep("注册表", "数据完整性验证", new String[]{
                "蛊虫>=148种 / 技能>=146种 / 杀招>=61种",
                "48道路全覆盖",
                "所有数字应为绿色（达标）",
            }, p -> {
                int gu = GuRegistry.getAll().size();
                int ab = GuAbilityRegistry.getAll().size();
                int mv = KillerMoveRegistry.getAll().size();
                ChatFormatting gc = gu >= 148 ? ChatFormatting.GREEN : ChatFormatting.RED;
                ChatFormatting ac = ab >= 146 ? ChatFormatting.GREEN : ChatFormatting.RED;
                ChatFormatting mc = mv >= 61 ? ChatFormatting.GREEN : ChatFormatting.RED;
                p.sendSystemMessage(Component.literal("  蛊虫: " + gu).withStyle(gc));
                p.sendSystemMessage(Component.literal("  技能: " + ab).withStyle(ac));
                p.sendSystemMessage(Component.literal("  杀招: " + mv).withStyle(mc));
            }));

            return s;
        }
    }

    private static void giveGuItem(ServerPlayer player, String guId) {
        ResourceLocation id = GuRegistry.id(guId);
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
        if (item != net.minecraft.world.item.Items.AIR) {
            player.getInventory().add(new net.minecraft.world.item.ItemStack(item));
        }
    }

    private static void addGuToAperture(Aperture ap, String guId) {
        ResourceLocation id = GuRegistry.id(guId);
        if (GuRegistry.get(id) != null) {
            GuInstance gu = new GuInstance(id);
            ap.addGu(gu);
        }
    }

    private static void send(ServerPlayer player, String msg, ChatFormatting... formats) {
        MutableComponent comp = Component.literal(msg);
        for (ChatFormatting f : formats) comp = comp.withStyle(f);
        player.sendSystemMessage(comp);
    }
}
