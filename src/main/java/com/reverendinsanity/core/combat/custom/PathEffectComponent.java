package com.reverendinsanity.core.combat.custom;

import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.client.vfx.VfxType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

// 道行为效果组件：定义每种大道(DaoPath)在杀招中贡献的行为效果
public class PathEffectComponent {

    private static final Map<DaoPath, EffectContribution> CONTRIBUTIONS = new EnumMap<>(DaoPath.class);

    public record EffectContribution(
        TargetEffect targetEffect,
        float targetParam,
        SelfEffect selfEffect,
        float selfParam,
        ProjectileType projectile,
        AreaEffectType areaEffect,
        float areaParam,
        int vfxColor,
        VfxType vfxOverride
    ) {}

    public enum TargetEffect {
        NONE,
        FREEZE,
        SLOW,
        IGNITE,
        POISON_DOT,
        CHAIN_BOUNCE,
        KNOCKBACK,
        BLEED,
        ARMOR_PIERCE,
        ENTANGLE,
        TRUE_DAMAGE,
        GRAVITY_PULL,
        BLIND,
        WITHER,
        CONFUSE,
        EXECUTION,
        SUPPRESS,
        DISPEL,
        AGGRO_REDIRECT,
        SUMMON_STRIKE
    }

    public enum SelfEffect {
        NONE,
        LIFESTEAL,
        SELF_DAMAGE,
        SPEED_BOOST,
        SHIELD,
        ESSENCE_RECOVER,
        INVISIBILITY,
        HEAL,
        THORNS
    }

    public enum ProjectileType {
        MOON_BLADE,
        BLOOD_BOLT,
        FIRE_BOLT,
        ICE_BOLT,
        GOLD_BEAM,
        STAR_FALL
    }

    public enum AreaEffectType {
        POISON_CLOUD,
        FROST_FIELD,
        FIRE_FIELD,
        FORMATION_FIELD
    }

    static {
        // === 战斗类 ===
        reg(DaoPath.STRENGTH, TargetEffect.KNOCKBACK, 3.0f, SelfEffect.NONE, 0, null, null, 0, 0xFFCC4422, null);
        reg(DaoPath.BLOOD, TargetEffect.BLEED, 100f, SelfEffect.LIFESTEAL, 0.2f, ProjectileType.BLOOD_BOLT, null, 0, 0xFFAA0000, null);
        reg(DaoPath.SWORD, TargetEffect.BLEED, 60f, SelfEffect.NONE, 0, null, null, 0, 0xFFCCCCEE, VfxType.SLASH_ARC);
        reg(DaoPath.BLADE, TargetEffect.BLEED, 80f, SelfEffect.SPEED_BOOST, 20f, null, null, 0, 0xFFDDDDFF, VfxType.SLASH_ARC);
        reg(DaoPath.KILL, TargetEffect.EXECUTION, 0.25f, SelfEffect.NONE, 0, null, null, 0, 0xFFFF0000, null);
        reg(DaoPath.SOLDIER, TargetEffect.SUMMON_STRIKE, 2f, SelfEffect.NONE, 0, null, null, 0, 0xFF887744, null);

        // === 精神类 ===
        reg(DaoPath.SOUL, TargetEffect.TRUE_DAMAGE, 0.3f, SelfEffect.NONE, 0, null, null, 0, 0xFF8844FF, null);
        reg(DaoPath.DREAM, TargetEffect.CONFUSE, 60f, SelfEffect.NONE, 0, null, null, 0, 0xFFCC88FF, null);
        reg(DaoPath.WISDOM, TargetEffect.NONE, 0, SelfEffect.NONE, 0, null, null, 0, 0xFFFFDD44, null);
        reg(DaoPath.CHARM, TargetEffect.AGGRO_REDIRECT, 0, SelfEffect.NONE, 0, null, null, 0, 0xFFFF66AA, null);
        reg(DaoPath.ILLUSION, TargetEffect.CONFUSE, 40f, SelfEffect.INVISIBILITY, 20f, null, null, 0, 0xFFAA66DD, null);

        // === 元素类 ===
        reg(DaoPath.FIRE, TargetEffect.IGNITE, 100f, SelfEffect.NONE, 0, ProjectileType.FIRE_BOLT, AreaEffectType.FIRE_FIELD, 60f, 0xFFFF6600, null);
        reg(DaoPath.WATER, TargetEffect.SLOW, 60f, SelfEffect.HEAL, 0.15f, null, null, 0, 0xFF4488FF, null);
        reg(DaoPath.EARTH, TargetEffect.GRAVITY_PULL, 3f, SelfEffect.SHIELD, 0.2f, null, null, 0, 0xFF886622, null);
        reg(DaoPath.METAL, TargetEffect.ARMOR_PIERCE, 0.4f, SelfEffect.NONE, 0, ProjectileType.GOLD_BEAM, null, 0, 0xFFCCCC44, VfxType.ENERGY_BEAM);
        reg(DaoPath.WOOD, TargetEffect.ENTANGLE, 80f, SelfEffect.HEAL, 0.1f, null, null, 0, 0xFF44CC44, null);
        reg(DaoPath.WIND, TargetEffect.KNOCKBACK, 2.5f, SelfEffect.SPEED_BOOST, 40f, null, null, 0, 0xFF88CCAA, null);
        reg(DaoPath.LIGHTNING, TargetEffect.CHAIN_BOUNCE, 2f, SelfEffect.NONE, 0, null, null, 0, 0xFFFFFF44, VfxType.ENERGY_BEAM);
        reg(DaoPath.ICE, TargetEffect.FREEZE, 80f, SelfEffect.NONE, 0, ProjectileType.ICE_BOLT, AreaEffectType.FROST_FIELD, 80f, 0xFF88DDFF, null);
        reg(DaoPath.LIGHT, TargetEffect.BLIND, 40f, SelfEffect.NONE, 0, null, null, 0, 0xFFFFFFCC, VfxType.GLOW_BURST);
        reg(DaoPath.DARK, TargetEffect.WITHER, 80f, SelfEffect.LIFESTEAL, 0.15f, null, null, 0, 0xFF442266, VfxType.SHADOW_FADE);
        reg(DaoPath.SHADOW, TargetEffect.NONE, 0, SelfEffect.INVISIBILITY, 40f, null, null, 0, 0xFF333355, VfxType.SHADOW_FADE);
        reg(DaoPath.CLOUD, TargetEffect.SLOW, 40f, SelfEffect.NONE, 0, null, null, 0, 0xFFCCCCFF, null);

        // === 规则类 ===
        reg(DaoPath.SPACE, TargetEffect.KNOCKBACK, 5.0f, SelfEffect.SPEED_BOOST, 20f, null, null, 0, 0xFF4466CC, VfxType.IMPACT_BURST);
        reg(DaoPath.TIME, TargetEffect.SLOW, 100f, SelfEffect.SPEED_BOOST, 60f, null, null, 0, 0xFF66AACC, null);
        reg(DaoPath.RULE, TargetEffect.DISPEL, 0, SelfEffect.NONE, 0, null, null, 0, 0xFFFFD700, null);
        reg(DaoPath.LUCK, TargetEffect.NONE, 0, SelfEffect.NONE, 0, null, null, 0, 0xFF44DD44, null);
        reg(DaoPath.HEAVEN, TargetEffect.CHAIN_BOUNCE, 3f, SelfEffect.NONE, 0, null, null, 0, 0xFFEEEEFF, VfxType.ENERGY_BEAM);
        reg(DaoPath.HUMAN, TargetEffect.NONE, 0, SelfEffect.HEAL, 0.1f, null, null, 0, 0xFFDDAA66, null);
        reg(DaoPath.STAR, TargetEffect.KNOCKBACK, 2.0f, SelfEffect.NONE, 0, ProjectileType.STAR_FALL, null, 0, 0xFFAAAAFF, VfxType.GLOW_BURST);

        // === 辅助类 ===
        reg(DaoPath.REFINEMENT, TargetEffect.NONE, 0, SelfEffect.NONE, 0, null, null, 0, 0xFFCC8844, null);
        reg(DaoPath.FORMATION, TargetEffect.NONE, 0, SelfEffect.NONE, 0, null, AreaEffectType.FORMATION_FIELD, 100f, 0xFF44CCAA, VfxType.AURA_RING);
        reg(DaoPath.PILL, TargetEffect.NONE, 0, SelfEffect.HEAL, 0.2f, null, null, 0, 0xFF88CC44, null);
        reg(DaoPath.ENSLAVE, TargetEffect.AGGRO_REDIRECT, 0, SelfEffect.NONE, 0, null, null, 0, 0xFF664488, null);
        reg(DaoPath.FOOD, TargetEffect.NONE, 0, SelfEffect.HEAL, 0.15f, null, null, 0, 0xFFCC8866, null);
        reg(DaoPath.PAINT, TargetEffect.CONFUSE, 30f, SelfEffect.NONE, 0, null, null, 0, 0xFFCC66CC, null);
        reg(DaoPath.STEAL, TargetEffect.DISPEL, 0, SelfEffect.LIFESTEAL, 0.1f, null, null, 0, 0xFF666666, VfxType.SHADOW_FADE);
        reg(DaoPath.BONE, TargetEffect.NONE, 0, SelfEffect.SHIELD, 0.25f, null, null, 0, 0xFFDDDDCC, null);
        reg(DaoPath.SOUND, TargetEffect.KNOCKBACK, 1.5f, SelfEffect.NONE, 0, null, null, 0, 0xFFAABBCC, VfxType.PULSE_WAVE);
        reg(DaoPath.INFORMATION, TargetEffect.NONE, 0, SelfEffect.NONE, 0, null, null, 0, 0xFF88AACC, null);

        // === 特殊类 ===
        reg(DaoPath.POISON, TargetEffect.POISON_DOT, 100f, SelfEffect.NONE, 0, null, AreaEffectType.POISON_CLOUD, 80f, 0xFF448844, null);
        reg(DaoPath.TRANSFORMATION, TargetEffect.NONE, 0, SelfEffect.SPEED_BOOST, 40f, null, null, 0, 0xFFCC44CC, null);
        reg(DaoPath.YIN_YANG, TargetEffect.TRUE_DAMAGE, 0.15f, SelfEffect.HEAL, 0.1f, null, null, 0, 0xFFAAAAAA, null);
        reg(DaoPath.FLIGHT, TargetEffect.KNOCKBACK, 2.0f, SelfEffect.SPEED_BOOST, 30f, null, null, 0, 0xFF88CCFF, null);
        reg(DaoPath.MOON, TargetEffect.SLOW, 40f, SelfEffect.NONE, 0, ProjectileType.MOON_BLADE, null, 0, 0xFFCCDDFF, VfxType.SLASH_ARC);
        reg(DaoPath.QI, TargetEffect.NONE, 0, SelfEffect.ESSENCE_RECOVER, 0.15f, null, null, 0, 0xFF88FFCC, null);
        reg(DaoPath.VOID, TargetEffect.TRUE_DAMAGE, 0.2f, SelfEffect.NONE, 0, null, null, 0, 0xFF220044, VfxType.SHADOW_FADE);
        reg(DaoPath.RESTRICTION, TargetEffect.SUPPRESS, 100f, SelfEffect.NONE, 0, null, null, 0, 0xFFAA4444, null);
    }

    private static void reg(DaoPath path, TargetEffect te, float tp, SelfEffect se, float sp,
                             ProjectileType proj, AreaEffectType area, float ap, int color, VfxType vfx) {
        CONTRIBUTIONS.put(path, new EffectContribution(te, tp, se, sp, proj, area, ap, color, vfx));
    }

    public static EffectContribution get(DaoPath path) {
        return CONTRIBUTIONS.getOrDefault(path, new EffectContribution(
            TargetEffect.NONE, 0, SelfEffect.NONE, 0, null, null, 0, 0xFF888888, null));
    }

    public static int getColor(DaoPath path) {
        EffectContribution c = CONTRIBUTIONS.get(path);
        return c != null ? c.vfxColor() : 0xFF888888;
    }

    public static Map<DaoPath, EffectContribution> getAll() {
        return Collections.unmodifiableMap(CONTRIBUTIONS);
    }

    public static int blendColors(int... colors) {
        if (colors.length == 0) return 0xFF888888;
        int r = 0, g = 0, b = 0;
        for (int c : colors) {
            r += (c >> 16) & 0xFF;
            g += (c >> 8) & 0xFF;
            b += c & 0xFF;
        }
        int n = colors.length;
        return 0xFF000000 | ((r / n) << 16) | ((g / n) << 8) | (b / n);
    }
}
