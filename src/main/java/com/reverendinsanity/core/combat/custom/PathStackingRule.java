package com.reverendinsanity.core.combat.custom;

import com.reverendinsanity.core.path.DaoPath;
import java.util.*;

// 道叠加规则：同道蛊虫数量达阈值时效果质变
public class PathStackingRule {

    private static final Map<DaoPath, List<StackThreshold>> RULES = new EnumMap<>(DaoPath.class);

    public record StackThreshold(
        int requiredCount,
        StackEffect effect,
        float multiplier,
        String description
    ) {}

    public enum StackEffect {
        BEAST_PHANTOM,
        ENHANCED_SHIELD,
        ICE_DOMAIN,
        FIRE_DOMAIN,
        BLOOD_FRENZY,
        SOUL_SHATTER,
        BONE_CONSTRUCT,
        STEAL_POWER,
        DARK_DOMAIN,
        LIGHTNING_STORM,
        WIND_STORM,
        SWORD_RAIN,
        POISON_MIASMA,
        DREAM_REALM,
        STAR_PHANTOM,
        EARTH_FORTRESS,
        TIDAL_SURGE,
        LIGHT_JUDGMENT,
        SOUND_SUPPRESS,
        TRANSFORMATION_EVOLVE,
        NULLIFY,
        MIND_SCATTER,
        CLOUD_RING,
        METAL_CRUSH,
        SEAL_POWER,
        BLADE_STORM,
        QI_SURGE,
        PEOPLES_WILL,
        KILL_DOMAIN,
        SHADOW_DOMAIN,
        YIN_YANG_REVERSAL,
        MOON_DOMAIN,
        VOID_PHASE,
        CHARM_AURA,
        WAR_SPIRIT,
        SKY_SOVEREIGNTY,
        ILLUSION_REALM,
        SPACE_WARP,
        PUPPET_ARMY,
        HEAVEN_WRATH,
        FORTUNE_SHIFT
    }

    public static List<StackThreshold> check(Map<DaoPath, Integer> pathCounts) {
        if (pathCounts == null || pathCounts.isEmpty()) return Collections.emptyList();
        List<StackThreshold> triggered = new ArrayList<>();
        for (var entry : pathCounts.entrySet()) {
            List<StackThreshold> thresholds = RULES.get(entry.getKey());
            if (thresholds == null) continue;
            for (StackThreshold t : thresholds) {
                if (entry.getValue() >= t.requiredCount()) {
                    triggered.add(t);
                }
            }
        }
        return triggered;
    }

    private static void reg(DaoPath path, int count, StackEffect effect, float mult, String desc) {
        RULES.computeIfAbsent(path, k -> new ArrayList<>())
             .add(new StackThreshold(count, effect, mult, desc));
    }

    static {
        reg(DaoPath.STRENGTH, 2, StackEffect.ENHANCED_SHIELD, 2.0f, "发甲");
        reg(DaoPath.STRENGTH, 3, StackEffect.BEAST_PHANTOM, 1.8f, "兽影");

        reg(DaoPath.ICE, 2, StackEffect.ICE_DOMAIN, 2.0f, "冰域");

        reg(DaoPath.FIRE, 2, StackEffect.FIRE_DOMAIN, 2.0f, "火海");

        reg(DaoPath.BLOOD, 2, StackEffect.BLOOD_FRENZY, 2.0f, "血暴");

        reg(DaoPath.SOUL, 2, StackEffect.SOUL_SHATTER, 2.0f, "魂爆");

        reg(DaoPath.BONE, 2, StackEffect.BONE_CONSTRUCT, 2.0f, "骨甲");
        reg(DaoPath.BONE, 3, StackEffect.BONE_CONSTRUCT, 3.0f, "白骨战车");

        reg(DaoPath.STEAL, 2, StackEffect.STEAL_POWER, 1.5f, "偷天");

        reg(DaoPath.DARK, 2, StackEffect.DARK_DOMAIN, 2.0f, "暗蚀");

        reg(DaoPath.LIGHTNING, 2, StackEffect.LIGHTNING_STORM, 2.0f, "雷暴");

        reg(DaoPath.WIND, 2, StackEffect.WIND_STORM, 2.0f, "风暴");

        reg(DaoPath.SWORD, 2, StackEffect.SWORD_RAIN, 2.0f, "万剑");

        reg(DaoPath.POISON, 2, StackEffect.POISON_MIASMA, 2.0f, "瘴气");

        reg(DaoPath.DREAM, 2, StackEffect.DREAM_REALM, 2.0f, "梦境");

        reg(DaoPath.STAR, 2, StackEffect.STAR_PHANTOM, 2.0f, "六幻星身");

        reg(DaoPath.EARTH, 2, StackEffect.EARTH_FORTRESS, 2.0f, "大地根");

        reg(DaoPath.WATER, 2, StackEffect.TIDAL_SURGE, 2.0f, "潮汐");

        reg(DaoPath.LIGHT, 2, StackEffect.LIGHT_JUDGMENT, 2.0f, "圣光裁决");

        reg(DaoPath.SOUND, 2, StackEffect.SOUND_SUPPRESS, 2.0f, "天地寂静");

        reg(DaoPath.TRANSFORMATION, 2, StackEffect.TRANSFORMATION_EVOLVE, 2.0f, "变化质变");

        reg(DaoPath.RULE, 2, StackEffect.NULLIFY, 2.0f, "净空");

        reg(DaoPath.WISDOM, 2, StackEffect.MIND_SCATTER, 2.0f, "心意散");

        reg(DaoPath.CLOUD, 2, StackEffect.CLOUD_RING, 2.0f, "九云环");

        reg(DaoPath.METAL, 2, StackEffect.METAL_CRUSH, 2.0f, "碎城锤");

        reg(DaoPath.RESTRICTION, 2, StackEffect.SEAL_POWER, 2.0f, "封禁");

        reg(DaoPath.BLADE, 2, StackEffect.BLADE_STORM, 2.0f, "万刀");

        reg(DaoPath.QI, 2, StackEffect.QI_SURGE, 2.0f, "无量气海");

        reg(DaoPath.HUMAN, 2, StackEffect.PEOPLES_WILL, 2.0f, "众望所归");

        reg(DaoPath.KILL, 2, StackEffect.KILL_DOMAIN, 2.0f, "杀域");

        reg(DaoPath.SHADOW, 2, StackEffect.SHADOW_DOMAIN, 2.0f, "影域");

        reg(DaoPath.YIN_YANG, 2, StackEffect.YIN_YANG_REVERSAL, 2.0f, "阴阳逆转");

        reg(DaoPath.MOON, 2, StackEffect.MOON_DOMAIN, 2.0f, "月域");

        reg(DaoPath.VOID, 2, StackEffect.VOID_PHASE, 2.0f, "虚化");

        reg(DaoPath.CHARM, 2, StackEffect.CHARM_AURA, 2.0f, "魅域");

        reg(DaoPath.SOLDIER, 2, StackEffect.WAR_SPIRIT, 2.0f, "兵魂");

        reg(DaoPath.FLIGHT, 2, StackEffect.SKY_SOVEREIGNTY, 2.0f, "御空领域");

        reg(DaoPath.ILLUSION, 2, StackEffect.ILLUSION_REALM, 2.0f, "幻域");

        reg(DaoPath.SPACE, 2, StackEffect.SPACE_WARP, 2.0f, "空间扭曲");

        reg(DaoPath.ENSLAVE, 2, StackEffect.PUPPET_ARMY, 2.0f, "傀儡军");

        reg(DaoPath.HEAVEN, 2, StackEffect.HEAVEN_WRATH, 2.0f, "天怒");

        reg(DaoPath.LUCK, 2, StackEffect.FORTUNE_SHIFT, 2.0f, "运转");
    }
}
