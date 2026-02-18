package com.reverendinsanity.core.tutorial;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 新手引导系统：首次进入触发分步教程
public class TutorialManager {

    private static final Map<UUID, Integer> playerStep = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> cooldownTicks = new ConcurrentHashMap<>();
    private static final int STEP_DELAY = 200;

    private static final String[][] TUTORIAL_STEPS = {
        {
            "欢迎来到蛊真人世界！",
            "你是一名初出茅庐的蛊师，需要开窍、炼蛊、修炼杀招。",
            "按住 [~] 键打开转轮菜单，这是你的主要操作界面。"
        },
        {
            "[第一步：开窍]",
            "寻找并获取「希望蛊」(Hope Gu)，右键使用即可开窍。",
            "开窍后你将获得资质等级和真元，正式成为蛊师。"
        },
        {
            "[第二步：获取蛊虫]",
            "蛊虫是你的力量之源。获取方式：",
            "  1. 击杀怪物掉落蛊虫",
            "  2. 捕获野蛊 (自然生成的WildGu实体)",
            "  3. 与蛊商人交易",
            "  4. 在炼蛊炉中升炼高转蛊虫"
        },
        {
            "[第三步：催动技能]",
            "蛊虫放入空窍后自动获得对应技能。",
            "按 R/F/V/C 催动技能栏1-4。",
            "每种蛊虫有不同道路属性，影响战斗效果。"
        },
        {
            "[第四步：杀招]",
            "杀招是多只蛊虫协同的强力组合技。",
            "按 [~] 打开菜单 -> 空窍管理 -> 装备杀招 (最多2个)",
            "按 Z/X 施展杀招。威力远超单只蛊虫技能。"
        },
        {
            "[第五步：境界突破]",
            "修炼积累真元和念头，小境界冥想突破。",
            "大境界需要「突破石」，高境界还会触发天劫。",
            "境界越高，可使用的蛊虫转数越高。"
        },
        {
            "[第六步：高级系统]",
            "推演 (J键): 研究新杀招组合",
            "分身/闭关/毒誓/变身: 通过转轮菜单 [~] 激活",
            "仙窍 (H键): 4转以上开辟个人空间",
            "蛊虫图鉴 (K键): 记录发现的蛊虫"
        },
        {
            "[第七步：诊断系统]",
            "管理员可使用 /gu diagnose 检测全部系统状态。",
            "/gu diagnose <模块名> 检测单个模块。",
            "这是开发测试的核心工具。"
        },
        {
            "教程完成！祝你成为一代蛊仙！",
            "温馨提示：天意系统会关注你的行为，小心行事。",
            "使用 /gu tutorial 可随时重新查看教程。"
        }
    };

    public static void onPlayerJoin(ServerPlayer player) {
        if (!playerStep.containsKey(player.getUUID())) {
            playerStep.put(player.getUUID(), 0);
            cooldownTicks.put(player.getUUID(), 100);
        }
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!playerStep.containsKey(uuid)) return;

        int step = playerStep.get(uuid);
        if (step >= TUTORIAL_STEPS.length) return;

        int cd = cooldownTicks.getOrDefault(uuid, 0);
        if (cd > 0) {
            cooldownTicks.put(uuid, cd - 1);
            return;
        }

        showStep(player, step);
        playerStep.put(uuid, step + 1);
        cooldownTicks.put(uuid, STEP_DELAY);
    }

    public static void showAllSteps(ServerPlayer player) {
        for (int i = 0; i < TUTORIAL_STEPS.length; i++) {
            showStep(player, i);
        }
        playerStep.put(player.getUUID(), TUTORIAL_STEPS.length);
    }

    public static void resetTutorial(ServerPlayer player) {
        playerStep.put(player.getUUID(), 0);
        cooldownTicks.put(player.getUUID(), 40);
        player.sendSystemMessage(
            Component.literal("教程已重置，即将重新开始引导。").withStyle(ChatFormatting.GREEN));
    }

    private static void showStep(ServerPlayer player, int step) {
        if (step < 0 || step >= TUTORIAL_STEPS.length) return;
        String[] lines = TUTORIAL_STEPS[step];

        player.sendSystemMessage(Component.literal(""));
        ChatFormatting titleColor = step == 0 || step == TUTORIAL_STEPS.length - 1
            ? ChatFormatting.GOLD : ChatFormatting.AQUA;

        for (int i = 0; i < lines.length; i++) {
            ChatFormatting color = i == 0 ? titleColor : ChatFormatting.GRAY;
            player.sendSystemMessage(Component.literal(lines[i]).withStyle(color));
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        int step = playerStep.getOrDefault(player.getUUID(), 0);
        if (step >= TUTORIAL_STEPS.length) {
            playerStep.remove(player.getUUID());
            cooldownTicks.remove(player.getUUID());
        }
    }

    public static boolean isComplete(ServerPlayer player) {
        int step = playerStep.getOrDefault(player.getUUID(), 0);
        return step >= TUTORIAL_STEPS.length;
    }
}
