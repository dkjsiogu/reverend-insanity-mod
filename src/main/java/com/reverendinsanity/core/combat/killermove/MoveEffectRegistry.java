package com.reverendinsanity.core.combat.killermove;

import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.combat.killermove.effect.BronzeWallEffect;
import com.reverendinsanity.core.combat.killermove.effect.GoldIronBastionEffect;
import com.reverendinsanity.core.combat.killermove.effect.MoonBladeStormEffect;
import com.reverendinsanity.core.combat.killermove.effect.MoonscarChainSlashEffect;
import com.reverendinsanity.core.combat.killermove.effect.ShadowStrikeEffect;
import com.reverendinsanity.core.combat.killermove.effect.SilverMoonTempestEffect;
import com.reverendinsanity.core.combat.killermove.effect.BloodRainEffect;
import com.reverendinsanity.core.combat.killermove.effect.PoisonMistEffect;
import com.reverendinsanity.core.combat.killermove.effect.SavageBullSmashEffect;
import com.reverendinsanity.core.combat.killermove.effect.FrostDomainEffect;
import com.reverendinsanity.core.combat.killermove.effect.BlazingHeavenEffect;
import com.reverendinsanity.core.combat.killermove.effect.StonePrisonEffect;
import com.reverendinsanity.core.combat.killermove.effect.RagingGaleEffect;
import com.reverendinsanity.core.combat.killermove.effect.ThunderJudgmentEffect;
import com.reverendinsanity.core.combat.killermove.effect.TideFuryEffect;
import com.reverendinsanity.core.combat.killermove.effect.SoulAnnihilationEffect;
import com.reverendinsanity.core.combat.killermove.effect.SacredLightJudgmentEffect;
import com.reverendinsanity.core.combat.killermove.effect.DarkErosionEffect;
import com.reverendinsanity.core.combat.killermove.effect.GoldenDreamEffect;
import com.reverendinsanity.core.combat.killermove.effect.MyriadIllusionEffect;
import com.reverendinsanity.core.combat.killermove.effect.MyriadSwordsReturnEffect;
import com.reverendinsanity.core.combat.killermove.effect.HeavenBladeSkyEffect;
import com.reverendinsanity.core.combat.killermove.effect.StarFallRainEffect;
import com.reverendinsanity.core.combat.killermove.effect.ReverseFateEffect;
import com.reverendinsanity.core.combat.killermove.effect.ExecuteNoMercyEffect;
import com.reverendinsanity.core.combat.killermove.effect.MyriadChangesEffect;
import com.reverendinsanity.core.combat.killermove.effect.ArmyFormationAssaultEffect;
import com.reverendinsanity.core.combat.killermove.effect.HeavenlyMelodyDestructionEffect;
import com.reverendinsanity.core.combat.killermove.effect.BoneForestEffect;
import com.reverendinsanity.core.combat.killermove.effect.SkyEagleDiveEffect;
import com.reverendinsanity.core.combat.killermove.effect.ProfoundQiExplosionEffect;
import com.reverendinsanity.core.combat.killermove.effect.PrimordialChaosEffect;
import com.reverendinsanity.core.combat.killermove.effect.SpaceSunderEffect;
import com.reverendinsanity.core.combat.killermove.effect.TimeParadoxEffect;
import com.reverendinsanity.core.combat.killermove.effect.SoulCharmStormEffect;
import com.reverendinsanity.core.combat.killermove.effect.OmniscienceEffect;
import com.reverendinsanity.core.combat.killermove.effect.VoidCollapseEffect;
import com.reverendinsanity.core.combat.killermove.effect.HeavenEarthSealEffect;
import com.reverendinsanity.core.combat.killermove.effect.HeavenWrathEffect;
import com.reverendinsanity.core.combat.killermove.effect.SupremeJudgmentEffect;
import com.reverendinsanity.core.combat.killermove.effect.ShadowAnnihilationEffect;
import com.reverendinsanity.core.combat.killermove.effect.CloudCalamityEffect;
import com.reverendinsanity.core.combat.killermove.effect.GrandFormationSealEffect;
import com.reverendinsanity.core.combat.killermove.effect.HeavenRefineDestroyEffect;
import com.reverendinsanity.core.combat.killermove.effect.ImmortalPillDestroyEffect;
import com.reverendinsanity.core.combat.killermove.effect.MyriadPaintEffect;
import com.reverendinsanity.core.combat.killermove.effect.HeavenStealEffect;
import com.reverendinsanity.core.combat.killermove.effect.HeavenInfoEffect;
import com.reverendinsanity.core.combat.killermove.effect.HumanSovereignEffect;
import com.reverendinsanity.core.combat.killermove.effect.SoulEnslaveEffect;
import com.reverendinsanity.core.combat.killermove.effect.DrunkenFrenzyEffect;
import com.reverendinsanity.core.combat.killermove.effect.BeastPhantomEffect;
import com.reverendinsanity.core.combat.killermove.effect.HairArmorEffect;
import com.reverendinsanity.core.combat.killermove.effect.IceBladeStormEffect;
import com.reverendinsanity.core.combat.killermove.effect.PrimordialLightFistEffect;
import com.reverendinsanity.core.combat.killermove.effect.FiveFingerFistSwordEffect;
import com.reverendinsanity.core.combat.killermove.effect.StealSkyChangeSunEffect;
import com.reverendinsanity.core.combat.killermove.effect.WhiteBoneChariotEffect;
import com.reverendinsanity.core.combat.killermove.effect.MyriadSelfEffect;
import com.reverendinsanity.core.combat.killermove.effect.BatWingReturnEffect;
import com.reverendinsanity.core.combat.killermove.effect.ThreeHeartsSoulEffect;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 杀招效果注册表：三级查找（精确杀招 → 流派默认 → 全局默认）
public class MoveEffectRegistry {

    private static final Map<ResourceLocation, MoveEffect> BY_MOVE = new HashMap<>();
    private static final Map<DaoPath, MoveEffect> BY_PATH = new HashMap<>();
    private static final MoveEffect DEFAULT_EFFECT = (player, aperture, move, calculatedDamage) -> {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB searchBox = new AABB(eye, eye.add(look.scale(5.0))).inflate(1.5);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, searchBox,
            e -> e != player && e.isAlive()
        );
        if (!targets.isEmpty()) {
            LivingEntity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (LivingEntity target : targets) {
                double dist = target.distanceToSqr(player);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = target;
                }
            }
            if (closest != null) {
                closest.hurt(player.damageSources().magic(), calculatedDamage);
            }
        }
    };

    public static void registerForMove(ResourceLocation moveId, MoveEffect effect) {
        BY_MOVE.put(moveId, effect);
    }

    public static void registerForPath(DaoPath path, MoveEffect effect) {
        BY_PATH.put(path, effect);
    }

    public static boolean hasEffect(ResourceLocation moveId) {
        return BY_MOVE.containsKey(moveId);
    }

    public static MoveEffect resolve(KillerMove move) {
        MoveEffect e = BY_MOVE.get(move.id());
        if (e != null) return e;
        e = BY_PATH.get(move.primaryPath());
        if (e != null) return e;
        return DEFAULT_EFFECT;
    }

    public static void registerDefaults() {
        registerForMove(KillerMoveRegistry.id("moon_blade_storm"), new MoonBladeStormEffect());
        registerForMove(KillerMoveRegistry.id("bronze_wall"), new BronzeWallEffect());
        registerForMove(KillerMoveRegistry.id("shadow_strike"), new ShadowStrikeEffect());
        registerForMove(KillerMoveRegistry.id("gold_iron_bastion"), new GoldIronBastionEffect());
        registerForMove(KillerMoveRegistry.id("moonscar_chain_slash"), new MoonscarChainSlashEffect());
        registerForMove(KillerMoveRegistry.id("silver_moon_tempest"), new SilverMoonTempestEffect());
        registerForMove(KillerMoveRegistry.id("blood_rain_skyward"), new BloodRainEffect());
        registerForMove(KillerMoveRegistry.id("poison_mist_binding"), new PoisonMistEffect());
        registerForMove(KillerMoveRegistry.id("savage_bull_smash"), new SavageBullSmashEffect());
        registerForMove(KillerMoveRegistry.id("frost_domain"), new FrostDomainEffect());
        registerForMove(KillerMoveRegistry.id("blazing_heaven"), new BlazingHeavenEffect());
        registerForMove(KillerMoveRegistry.id("stone_prison"), new StonePrisonEffect());
        registerForMove(KillerMoveRegistry.id("raging_gale"), new RagingGaleEffect());
        registerForMove(KillerMoveRegistry.id("thunder_judgment"), new ThunderJudgmentEffect());
        registerForMove(KillerMoveRegistry.id("tide_fury"), new TideFuryEffect());
        registerForMove(KillerMoveRegistry.id("soul_annihilation"), new SoulAnnihilationEffect());
        registerForMove(KillerMoveRegistry.id("sacred_light_judgment"), new SacredLightJudgmentEffect());
        registerForMove(KillerMoveRegistry.id("dark_erosion"), new DarkErosionEffect());
        registerForMove(KillerMoveRegistry.id("golden_dream"), new GoldenDreamEffect());
        registerForMove(KillerMoveRegistry.id("myriad_illusion"), new MyriadIllusionEffect());
        registerForMove(KillerMoveRegistry.id("myriad_swords_return"), new MyriadSwordsReturnEffect());
        registerForMove(KillerMoveRegistry.id("heaven_blade_sky_split"), new HeavenBladeSkyEffect());
        registerForMove(KillerMoveRegistry.id("star_fall_rain"), new StarFallRainEffect());
        registerForMove(KillerMoveRegistry.id("reverse_fate"), new ReverseFateEffect());
        registerForMove(KillerMoveRegistry.id("execute_no_mercy"), new ExecuteNoMercyEffect());
        registerForMove(KillerMoveRegistry.id("myriad_changes_return"), new MyriadChangesEffect());
        registerForMove(KillerMoveRegistry.id("army_formation_assault"), new ArmyFormationAssaultEffect());
        registerForMove(KillerMoveRegistry.id("heavenly_melody_destruction"), new HeavenlyMelodyDestructionEffect());
        registerForMove(KillerMoveRegistry.id("bone_forest"), new BoneForestEffect());
        registerForMove(KillerMoveRegistry.id("sky_eagle_dive"), new SkyEagleDiveEffect());
        registerForMove(KillerMoveRegistry.id("profound_qi_explosion"), new ProfoundQiExplosionEffect());
        registerForMove(KillerMoveRegistry.id("primordial_chaos"), new PrimordialChaosEffect());
        registerForMove(KillerMoveRegistry.id("space_sunder"), new SpaceSunderEffect());
        registerForMove(KillerMoveRegistry.id("time_paradox"), new TimeParadoxEffect());
        registerForMove(KillerMoveRegistry.id("soul_charm_storm"), new SoulCharmStormEffect());
        registerForMove(KillerMoveRegistry.id("omniscience"), new OmniscienceEffect());
        registerForMove(KillerMoveRegistry.id("void_collapse"), new VoidCollapseEffect());
        registerForMove(KillerMoveRegistry.id("heaven_earth_seal"), new HeavenEarthSealEffect());
        registerForMove(KillerMoveRegistry.id("heaven_wrath"), new HeavenWrathEffect());
        registerForMove(KillerMoveRegistry.id("supreme_judgment"), new SupremeJudgmentEffect());
        registerForMove(KillerMoveRegistry.id("shadow_annihilation"), new ShadowAnnihilationEffect());
        registerForMove(KillerMoveRegistry.id("cloud_calamity"), new CloudCalamityEffect());
        registerForMove(KillerMoveRegistry.id("grand_formation_seal"), new GrandFormationSealEffect());
        registerForMove(KillerMoveRegistry.id("heaven_refine_destroy"), new HeavenRefineDestroyEffect());
        registerForMove(KillerMoveRegistry.id("immortal_pill_destroy"), new ImmortalPillDestroyEffect());
        registerForMove(KillerMoveRegistry.id("myriad_paint_storm"), new MyriadPaintEffect());
        registerForMove(KillerMoveRegistry.id("heaven_steal_annihilation"), new HeavenStealEffect());
        registerForMove(KillerMoveRegistry.id("heaven_info_blast"), new HeavenInfoEffect());
        registerForMove(KillerMoveRegistry.id("human_sovereign"), new HumanSovereignEffect());
        registerForMove(KillerMoveRegistry.id("soul_enslave"), new SoulEnslaveEffect());
        registerForMove(KillerMoveRegistry.id("drunken_frenzy"), new DrunkenFrenzyEffect());

        // 原著标志性杀招 - 使用组件效果系统
        registerForMove(KillerMoveRegistry.id("beast_phantom"), new BeastPhantomEffect());
        registerForMove(KillerMoveRegistry.id("hair_armor"), new HairArmorEffect());
        registerForMove(KillerMoveRegistry.id("ice_blade_storm"), new IceBladeStormEffect());
        registerForMove(KillerMoveRegistry.id("primordial_light_fist"), new PrimordialLightFistEffect());
        registerForMove(KillerMoveRegistry.id("five_finger_fist_sword"), new FiveFingerFistSwordEffect());
        registerForMove(KillerMoveRegistry.id("steal_sky_change_sun"), new StealSkyChangeSunEffect());
        registerForMove(KillerMoveRegistry.id("white_bone_chariot"), new WhiteBoneChariotEffect());
        registerForMove(KillerMoveRegistry.id("myriad_self"), new MyriadSelfEffect());
        registerForMove(KillerMoveRegistry.id("bat_wing_return"), new BatWingReturnEffect());
        registerForMove(KillerMoveRegistry.id("three_hearts_soul"), new ThreeHeartsSoulEffect());
    }
}
