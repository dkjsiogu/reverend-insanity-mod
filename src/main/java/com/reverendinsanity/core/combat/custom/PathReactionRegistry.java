package com.reverendinsanity.core.combat.custom;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.client.vfx.VfxType;
import java.util.*;

// 道反应注册表：特定道路对同时出现时触发融合效果
public class PathReactionRegistry {

    private static final List<ReactionRule> RULES = new ArrayList<>();

    public record ReactionRule(
        Set<DaoPath> requiredPaths,
        ReactionEffect effect
    ) {}

    public record ReactionEffect(
        String name,
        ReactionType type,
        float power,
        int vfxColor,
        VfxType vfxOverride,
        KillerMove.MoveType moveTypeOverride
    ) {}

    public enum ReactionType {
        VORTEX_FREEZE,
        GIANT_STRIKE,
        PIERCING_STRIKE,
        SOUL_FIRE,
        ICE_FIRE_SHOCK,
        BLOOD_RAIN,
        DARK_SOUL_DRAIN,
        MIND_DOMINATE,
        FLIGHT_DASH,
        EARTH_FORM,
        WIND_FORM,
        CLONE_ARMY,
        STEAL_BUFF,
        THUNDER_RAIN,
        BONE_BLADE,
        DREAM_ILLUSION,
        POISON_FIRE,
        WATER_ICE_SEAL,
        HEAVEN_RULE,
        CORROSIVE_BURST,
        STORM_FUSION,
        BLOOD_FLAME,
        SOUL_RADIANCE,
        CRYSTALLIZE,
        BEAST_SHIFT,
        FATE_MANIPULATION,
        MIND_SOUL_SYNERGY,
        STAR_FIRE_ESCAPE,
        WOOD_BLOOD_BLOOM,
        SOUND_BLADE,
        PHASE_SHIFT,
        SOUL_DEVOUR,
        MURAL_PRISON,
        FORCED_BANISH,
        AUTO_CONSTRUCT,
        SOUL_REAP,
        SHADOW_KILL,
        SHADOW_MERGE,
        LIGHT_SHADOW_SPLIT,
        MOON_SOUL,
        MOONLIGHT_DOMAIN,
        VOID_COLLAPSE,
        CHARM_ILLUSION,
        BATTLE_FORMATION,
        SKY_CHARGE,
        CLOUD_SEA,
        YIN_YANG_THUNDER,
        VOID_SEAL
    }

    public static List<ReactionEffect> findReactions(List<DaoPath> paths) {
        if (paths == null || paths.isEmpty()) return Collections.emptyList();
        Set<DaoPath> pathSet = EnumSet.noneOf(DaoPath.class);
        pathSet.addAll(paths);
        List<ReactionEffect> results = new ArrayList<>();
        for (ReactionRule rule : RULES) {
            if (pathSet.containsAll(rule.requiredPaths())) {
                results.add(rule.effect());
            }
        }
        return results;
    }

    private static void reg(Set<DaoPath> paths, String name, ReactionType type,
                             float power, int color, VfxType vfx, KillerMove.MoveType override) {
        RULES.add(new ReactionRule(paths, new ReactionEffect(name, type, power, color, vfx, override)));
    }

    private static Set<DaoPath> pair(DaoPath a, DaoPath b) {
        return EnumSet.of(a, b);
    }

    static {
        reg(pair(DaoPath.ICE, DaoPath.WIND), "冰风龙卷",
            ReactionType.VORTEX_FREEZE, 1.3f, 0xFF88DDEE, VfxType.TORNADO, null);

        reg(pair(DaoPath.LIGHT, DaoPath.STRENGTH), "太古光拳",
            ReactionType.GIANT_STRIKE, 1.5f, 0xFFFFDD88, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.EARTH, DaoPath.STRENGTH), "山岳重击",
            ReactionType.GIANT_STRIKE, 1.4f, 0xFFAA8844, VfxType.IMPACT_BURST, null);

        reg(pair(DaoPath.SWORD, DaoPath.STRENGTH), "拳心剑气",
            ReactionType.PIERCING_STRIKE, 1.3f, 0xFFDDDDFF, VfxType.ENERGY_BEAM, null);

        reg(pair(DaoPath.METAL, DaoPath.STRENGTH), "金刚穿甲",
            ReactionType.PIERCING_STRIKE, 1.4f, 0xFFCCCC66, VfxType.ENERGY_BEAM, null);

        reg(pair(DaoPath.SOUL, DaoPath.FIRE), "魂焰",
            ReactionType.SOUL_FIRE, 1.3f, 0xFFCC44FF, null, null);

        reg(pair(DaoPath.ICE, DaoPath.FIRE), "寒热冲击",
            ReactionType.ICE_FIRE_SHOCK, 1.5f, 0xFFFF88CC, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.BLOOD, DaoPath.WIND), "血雨漫天",
            ReactionType.BLOOD_RAIN, 1.2f, 0xFFCC2222, null, null);

        reg(pair(DaoPath.BLOOD, DaoPath.WATER), "血潮",
            ReactionType.BLOOD_RAIN, 1.2f, 0xFFAA2244, null, null);

        reg(pair(DaoPath.DARK, DaoPath.SOUL), "暗魂侵蚀",
            ReactionType.DARK_SOUL_DRAIN, 1.4f, 0xFF442266, VfxType.SHADOW_FADE, null);

        reg(pair(DaoPath.ENSLAVE, DaoPath.SOUL), "精神支配",
            ReactionType.MIND_DOMINATE, 1.3f, 0xFF8844AA, VfxType.PULSE_WAVE, KillerMove.MoveType.CONTROL);

        reg(pair(DaoPath.WIND, DaoPath.TRANSFORMATION), "蝠翼飞行",
            ReactionType.FLIGHT_DASH, 1.2f, 0xFF88CCAA, VfxType.IMPACT_BURST, KillerMove.MoveType.MOVEMENT);

        reg(pair(DaoPath.TRANSFORMATION, DaoPath.EARTH), "大地形态",
            ReactionType.EARTH_FORM, 1.3f, 0xFF886622, VfxType.AURA_RING, KillerMove.MoveType.BUFF);

        reg(pair(DaoPath.TRANSFORMATION, DaoPath.WIND), "疾风形态",
            ReactionType.WIND_FORM, 1.3f, 0xFF88CCAA, VfxType.AURA_RING, KillerMove.MoveType.BUFF);

        reg(pair(DaoPath.STRENGTH, DaoPath.ENSLAVE), "分身协攻",
            ReactionType.CLONE_ARMY, 1.4f, 0xFFCC8844, null, null);

        reg(pair(DaoPath.STEAL, DaoPath.SOUL), "偷魂夺魄",
            ReactionType.STEAL_BUFF, 1.2f, 0xFF666688, VfxType.SHADOW_FADE, null);

        reg(pair(DaoPath.STEAL, DaoPath.BLOOD), "偷命",
            ReactionType.STEAL_BUFF, 1.2f, 0xFF884444, VfxType.SHADOW_FADE, null);

        reg(pair(DaoPath.LIGHTNING, DaoPath.STAR), "星雷交击",
            ReactionType.THUNDER_RAIN, 1.4f, 0xFFFFFF88, VfxType.SKY_STRIKE, null);

        reg(pair(DaoPath.BONE, DaoPath.SWORD), "白骨飞剑",
            ReactionType.BONE_BLADE, 1.3f, 0xFFDDDDCC, VfxType.SLASH_ARC, null);

        reg(pair(DaoPath.BONE, DaoPath.BLADE), "白骨战刀",
            ReactionType.BONE_BLADE, 1.3f, 0xFFDDDDCC, VfxType.SLASH_ARC, null);

        reg(pair(DaoPath.DREAM, DaoPath.ILLUSION), "梦幻双重",
            ReactionType.DREAM_ILLUSION, 1.3f, 0xFFCC88EE, VfxType.DOME_FIELD, null);

        reg(pair(DaoPath.POISON, DaoPath.FIRE), "毒焰",
            ReactionType.POISON_FIRE, 1.3f, 0xFF88CC22, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.WATER, DaoPath.ICE), "冰封万里",
            ReactionType.WATER_ICE_SEAL, 1.3f, 0xFF88CCFF, VfxType.DOME_FIELD, null);

        reg(pair(DaoPath.HEAVEN, DaoPath.RULE), "天罚裁决",
            ReactionType.HEAVEN_RULE, 1.5f, 0xFFFFEEDD, VfxType.SKY_STRIKE, null);

        reg(pair(DaoPath.DARK, DaoPath.SPACE), "暗漩",
            ReactionType.CORROSIVE_BURST, 1.6f, 0xFF110022, VfxType.BLACK_HOLE, null);

        reg(pair(DaoPath.WIND, DaoPath.LIGHTNING), "风雷吼",
            ReactionType.STORM_FUSION, 1.4f, 0xFF88CCFF, VfxType.TORNADO, null);

        reg(pair(DaoPath.BLOOD, DaoPath.FIRE), "血肉盛炎",
            ReactionType.BLOOD_FLAME, 1.3f, 0xFFCC2200, null, null);

        reg(pair(DaoPath.SOUL, DaoPath.METAL), "灼魂太金",
            ReactionType.SOUL_RADIANCE, 1.5f, 0xFFFFCC44, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.SOUND, DaoPath.ICE), "碧玉歌",
            ReactionType.CRYSTALLIZE, 1.3f, 0xFF44DDCC, VfxType.DOME_FIELD, KillerMove.MoveType.CONTROL);

        reg(pair(DaoPath.STRENGTH, DaoPath.TRANSFORMATION), "六臂天尸王",
            ReactionType.BEAST_SHIFT, 1.5f, 0xFFBB6633, VfxType.AURA_RING, KillerMove.MoveType.BUFF);

        reg(pair(DaoPath.LUCK, DaoPath.TIME), "流年不利",
            ReactionType.FATE_MANIPULATION, 1.3f, 0xFFDD99FF, VfxType.PULSE_WAVE, null);

        reg(pair(DaoPath.WISDOM, DaoPath.SOUL), "魂智共鸣",
            ReactionType.MIND_SOUL_SYNERGY, 1.4f, 0xFF9988DD, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.STAR, DaoPath.FIRE), "星火遁",
            ReactionType.STAR_FIRE_ESCAPE, 1.3f, 0xFFFF8844, VfxType.IMPACT_BURST, KillerMove.MoveType.MOVEMENT);

        reg(pair(DaoPath.WOOD, DaoPath.BLOOD), "飞花溅血",
            ReactionType.WOOD_BLOOD_BLOOM, 1.3f, 0xFFDD4488, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.SOUND, DaoPath.SWORD), "长空音刃",
            ReactionType.SOUND_BLADE, 1.3f, 0xFFAABBEE, VfxType.ENERGY_BEAM, null);

        reg(pair(DaoPath.QI, DaoPath.TRANSFORMATION), "龙人变化",
            ReactionType.PHASE_SHIFT, 1.5f, 0xFF66AACC, VfxType.AURA_RING, KillerMove.MoveType.BUFF);

        reg(pair(DaoPath.FOOD, DaoPath.SOUL), "吃心",
            ReactionType.SOUL_DEVOUR, 1.4f, 0xFF662244, VfxType.SHADOW_FADE, null);

        reg(pair(DaoPath.PAINT, DaoPath.HUMAN), "安居乐业",
            ReactionType.MURAL_PRISON, 1.3f, 0xFFEEBB88, VfxType.PULSE_WAVE, KillerMove.MoveType.CONTROL);

        reg(pair(DaoPath.WIND, DaoPath.HUMAN), "送友风",
            ReactionType.FORCED_BANISH, 1.2f, 0xFF88DDAA, VfxType.IMPACT_BURST, KillerMove.MoveType.CONTROL);

        reg(pair(DaoPath.FORMATION, DaoPath.EARTH), "自动构筑",
            ReactionType.AUTO_CONSTRUCT, 1.3f, 0xFFBB9966, VfxType.AURA_RING, KillerMove.MoveType.DEFENSE);

        reg(pair(DaoPath.KILL, DaoPath.SOUL), "杀魂",
            ReactionType.SOUL_REAP, 1.5f, 0xFFAA1133, VfxType.SHADOW_FADE, null);

        reg(pair(DaoPath.KILL, DaoPath.DARK), "暗杀",
            ReactionType.SHADOW_KILL, 1.4f, 0xFF220011, VfxType.SHADOW_FADE, null);

        reg(pair(DaoPath.SHADOW, DaoPath.DARK), "影暗融合",
            ReactionType.SHADOW_MERGE, 1.3f, 0xFF110033, VfxType.SHADOW_FADE, KillerMove.MoveType.BUFF);

        reg(pair(DaoPath.SHADOW, DaoPath.LIGHT), "光影分身",
            ReactionType.LIGHT_SHADOW_SPLIT, 1.4f, 0xFFBBAA88, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.MOON, DaoPath.SOUL), "月魂",
            ReactionType.MOON_SOUL, 1.3f, 0xFFBBCCFF, VfxType.GLOW_BURST, null);

        reg(pair(DaoPath.MOON, DaoPath.LIGHT), "月华",
            ReactionType.MOONLIGHT_DOMAIN, 1.4f, 0xFFDDEEFF, VfxType.DOME_FIELD, null);

        reg(pair(DaoPath.VOID, DaoPath.SPACE), "虚空崩塌",
            ReactionType.VOID_COLLAPSE, 1.6f, 0xFF220044, VfxType.BLACK_HOLE, null);

        reg(pair(DaoPath.CHARM, DaoPath.ILLUSION), "魅幻双重",
            ReactionType.CHARM_ILLUSION, 1.3f, 0xFFEE88CC, VfxType.DOME_FIELD, KillerMove.MoveType.CONTROL);

        reg(pair(DaoPath.SOLDIER, DaoPath.FORMATION), "军阵",
            ReactionType.BATTLE_FORMATION, 1.4f, 0xFF886644, VfxType.AURA_RING, KillerMove.MoveType.DEFENSE);

        reg(pair(DaoPath.FLIGHT, DaoPath.WIND), "御空冲锋",
            ReactionType.SKY_CHARGE, 1.3f, 0xFF88DDCC, VfxType.IMPACT_BURST, KillerMove.MoveType.MOVEMENT);

        reg(pair(DaoPath.CLOUD, DaoPath.WATER), "云海",
            ReactionType.CLOUD_SEA, 1.3f, 0xFFCCDDEE, VfxType.DOME_FIELD, null);

        reg(pair(DaoPath.YIN_YANG, DaoPath.LIGHTNING), "阴阳雷",
            ReactionType.YIN_YANG_THUNDER, 1.4f, 0xFFDDCC88, VfxType.SKY_STRIKE, null);

        reg(pair(DaoPath.RESTRICTION, DaoPath.SPACE), "虚空封禁",
            ReactionType.VOID_SEAL, 1.5f, 0xFF442255, VfxType.DOME_FIELD, KillerMove.MoveType.CONTROL);
    }
}
