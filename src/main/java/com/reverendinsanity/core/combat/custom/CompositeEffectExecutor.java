package com.reverendinsanity.core.combat.custom;

import com.reverendinsanity.core.combat.DotManager;
import com.reverendinsanity.core.combat.FrostManager;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.EssenceGrade;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.PathRealm;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.ReverendInsanity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;

// 自创杀招效果执行器：道行为层叠 + 道反应 + 道叠加
public class CompositeEffectExecutor {

    private static final ResourceLocation SUPPRESS_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "suppress_slow");
    private static final ResourceLocation BLEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "bleed_dot");
    private static final Random RANDOM = new Random();

    public static boolean execute(ServerPlayer player, Aperture aperture, CustomKillerMove move) {
        for (var guId : move.getAllRequiredGu()) {
            boolean found = false;
            for (GuInstance gu : aperture.getStoredGu()) {
                if (gu.getTypeId().equals(guId) && gu.isActive()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        float compatibility = aperture.getPathCompatibility(move.getPrimaryPath());
        float adjustedEssence = move.getEssenceCost() / compatibility;
        float adjustedThoughts = move.getThoughtsCost() / compatibility;

        if (aperture.getCurrentEssence() < adjustedEssence) return false;
        if (aperture.getThoughts() < adjustedThoughts) return false;

        aperture.consumeEssence(adjustedEssence);
        aperture.consumeThoughts(adjustedThoughts);

        EssenceGrade grade = aperture.getEssenceGrade();
        PathRealm pathRealm = aperture.getPathRealm(move.getPrimaryPath());
        float damage = move.getPower() * grade.getEfficiency() * (1.0f + pathRealm.getTier() * 0.15f) * compatibility;

        CompositeEffects effects = collectEffects(move);
        damage *= effects.getDamageMultiplier();

        executeByType(player, move, damage, effects);

        player.swing(player.getUsedItemHand());
        return true;
    }

    public static void executeFromKillerMove(ServerPlayer player, Aperture aperture, KillerMove move, float damage) {
        CompositeEffects effects = collectEffectsFromKillerMove(move);
        damage *= effects.getDamageMultiplier();

        CustomKillerMove wrapper = new CustomKillerMove(
            move.displayName(), move.coreGu(), move.supportGu(),
            move.primaryPath(), move.moveType(),
            move.power(), move.essenceCost(), move.thoughtsCost(),
            move.cooldownTicks(), 1.0f
        );

        executeByType(player, wrapper, damage, effects);
    }

    private static CompositeEffects collectEffects(CustomKillerMove move) {
        CompositeEffects effects = new CompositeEffects();
        effects.addContribution(PathEffectComponent.get(move.getPrimaryPath()), true);

        List<DaoPath> allPaths = new ArrayList<>();
        allPaths.add(move.getPrimaryPath());

        for (var supId : move.getSupportGuIds()) {
            GuType supType = GuRegistry.get(supId);
            if (supType == null) continue;
            effects.addContribution(PathEffectComponent.get(supType.path()), false);
            allPaths.add(supType.path());

            switch (supType.category()) {
                case ATTACK -> effects.damageBonus += 0.15f;
                case DEFENSE -> effects.shieldBonus += 0.1f;
                case MOVEMENT -> effects.rangeBonus += 0.3f;
                case SUPPORT -> effects.costReduction += 0.1f;
                case DETECTION -> effects.rangeBonus += 0.2f;
                default -> {}
            }
        }

        effects.reactions.addAll(PathReactionRegistry.findReactions(allPaths));
        Map<DaoPath, Integer> counts = new EnumMap<>(DaoPath.class);
        for (DaoPath p : allPaths) counts.merge(p, 1, Integer::sum);
        effects.stackEffects.addAll(PathStackingRule.check(counts));

        return effects;
    }

    private static CompositeEffects collectEffectsFromKillerMove(KillerMove move) {
        CompositeEffects effects = new CompositeEffects();
        effects.addContribution(PathEffectComponent.get(move.primaryPath()), true);

        List<DaoPath> allPaths = new ArrayList<>();
        allPaths.add(move.primaryPath());

        for (var supId : move.supportGu()) {
            GuType supType = GuRegistry.get(supId);
            if (supType == null) continue;
            effects.addContribution(PathEffectComponent.get(supType.path()), false);
            allPaths.add(supType.path());
        }

        effects.reactions.addAll(PathReactionRegistry.findReactions(allPaths));
        Map<DaoPath, Integer> counts = new EnumMap<>(DaoPath.class);
        for (DaoPath p : allPaths) counts.merge(p, 1, Integer::sum);
        effects.stackEffects.addAll(PathStackingRule.check(counts));

        return effects;
    }

    private static void executeByType(ServerPlayer player, CustomKillerMove move, float damage, CompositeEffects effects) {
        switch (move.getMoveType()) {
            case ATTACK -> executeAttack(player, move, damage, effects);
            case DEFENSE -> executeDefense(player, move, damage, effects);
            case MOVEMENT -> executeMovement(player, move, damage, effects);
            case HEAL -> executeHeal(player, move, damage, effects);
            case BUFF -> executeBuff(player, move, damage, effects);
            case CONTROL -> executeControl(player, move, damage, effects);
            default -> executeAttack(player, move, damage, effects);
        }
    }

    private static void executeAttack(ServerPlayer player, CustomKillerMove move, float damage, CompositeEffects effects) {
        float range = (5f + effects.rangeBonus * 3f);
        Vec3 look = player.getLookAngle();
        Vec3 center = player.position().add(look.scale(range * 0.5));

        ServerLevel level = player.serverLevel();
        AABB area = new AABB(center.x - range, center.y - 2, center.z - range,
            center.x + range, center.y + 3, center.z + range);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && player.distanceTo(e) <= range);

        float perTarget = targets.size() > 3 ? damage / (targets.size() * 0.5f) : damage;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), perTarget);
            applyTargetEffects(target, player, effects, perTarget);
        }
        applySelfEffects(player, effects, damage);
        spawnProjectiles(player, effects, damage);
        spawnAreaEffects(player, level, effects);
        applyReactionEffects(player, targets, effects, damage);
        applyStackingEffects(player, targets, effects, damage);

        VfxType vfx = effects.getPrimaryVfx(VfxType.SLASH_ARC);
        int color = effects.getBlendedColor();
        VfxHelper.spawn(player, vfx,
            player.getX(), player.getY() + 1, player.getZ(),
            (float)look.x, (float)look.y, (float)look.z,
            color, 3f + effects.rangeBonus * 0.3f, 20);

        level.playSound(null, player.blockPosition(),
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    private static void executeDefense(ServerPlayer player, CustomKillerMove move, float damage, CompositeEffects effects) {
        float shieldTotal = damage * (0.5f + effects.shieldBonus);
        player.setAbsorptionAmount(player.getAbsorptionAmount() + shieldTotal);

        ServerLevel level = player.serverLevel();
        float range = 3f + effects.rangeBonus * 2f;
        AABB area = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity e : targets) {
            Vec3 knockback = e.position().subtract(player.position()).normalize().scale(1.5);
            e.push(knockback.x, 0.3, knockback.z);
            e.hurtMarked = true;
            applyTargetEffects(e, player, effects, damage * 0.2f);
        }
        applySelfEffects(player, effects, damage);
        spawnAreaEffects(player, level, effects);
        applyReactionEffects(player, targets, effects, damage);
        applyStackingEffects(player, targets, effects, damage);

        int color = effects.getBlendedColor();
        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 1, player.getZ(),
            0, 1, 0, color, 2.5f, 30);

        player.level().playSound(null, player.blockPosition(),
            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void executeMovement(ServerPlayer player, CustomKillerMove move, float damage, CompositeEffects effects) {
        Vec3 look = player.getLookAngle();
        float dashDistance = 6f + effects.rangeBonus * 3f;
        Vec3 dashTarget = player.position().add(look.scale(dashDistance));
        player.teleportTo(dashTarget.x, dashTarget.y, dashTarget.z);

        ServerLevel level = player.serverLevel();
        AABB trail = player.getBoundingBox().inflate(2 + effects.rangeBonus);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, trail, e -> e != player && e.isAlive());
        for (LivingEntity e : targets) {
            e.hurt(player.damageSources().playerAttack(player), damage * 0.6f);
            applyTargetEffects(e, player, effects, damage * 0.6f);
        }
        applySelfEffects(player, effects, damage);
        applyReactionEffects(player, targets, effects, damage);
        applyStackingEffects(player, targets, effects, damage);

        VfxType vfx = effects.getPrimaryVfx(VfxType.IMPACT_BURST);
        int color = effects.getBlendedColor();
        VfxHelper.spawn(player, vfx,
            player.getX(), player.getY() + 1, player.getZ(),
            (float)look.x, (float)look.y, (float)look.z,
            color, 2f, 15);

        level.playSound(null, player.blockPosition(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeHeal(ServerPlayer player, CustomKillerMove move, float damage, CompositeEffects effects) {
        float healAmount = damage * 0.4f;
        player.heal(healAmount);
        applySelfEffects(player, effects, damage);
        applyReactionEffects(player, Collections.emptyList(), effects, damage);
        applyStackingEffects(player, Collections.emptyList(), effects, damage);

        int color = effects.getBlendedColor();
        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY() + 1, player.getZ(),
            0, 1, 0, color, 2f, 25);

        player.level().playSound(null, player.blockPosition(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    private static void executeBuff(ServerPlayer player, CustomKillerMove move, float damage, CompositeEffects effects) {
        Aperture aperture = player.getData(ModAttachments.GU_MASTER_DATA.get()).getAperture();
        float essenceRestore = aperture.getMaxEssence() * 0.1f * (1f + effects.damageBonus * 0.5f);
        float thoughtRestore = aperture.getMaxThoughts() * 0.1f;
        aperture.regenerateEssence(essenceRestore);
        aperture.regenerateThoughts(thoughtRestore);

        applySelfEffects(player, effects, damage);

        ServerLevel level = player.serverLevel();
        spawnAreaEffects(player, level, effects);
        applyReactionEffects(player, Collections.emptyList(), effects, damage);
        applyStackingEffects(player, Collections.emptyList(), effects, damage);

        int color = effects.getBlendedColor();
        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 1, player.getZ(),
            0, 1, 0, color, 1.5f, 30);

        player.level().playSound(null, player.blockPosition(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void executeControl(ServerPlayer player, CustomKillerMove move, float damage, CompositeEffects effects) {
        float range = 6f + effects.rangeBonus * 2f;
        ServerLevel level = player.serverLevel();
        AABB area = player.getBoundingBox().inflate(range);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
        for (LivingEntity e : targets) {
            e.hurt(player.damageSources().playerAttack(player), damage * 0.3f);
            e.setDeltaMovement(Vec3.ZERO);
            e.hurtMarked = true;
            applyTargetEffects(e, player, effects, damage * 0.3f);
        }
        applySelfEffects(player, effects, damage);
        spawnAreaEffects(player, level, effects);
        applyReactionEffects(player, targets, effects, damage);
        applyStackingEffects(player, targets, effects, damage);

        int color = effects.getBlendedColor();
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 1, player.getZ(),
            0, 1, 0, color, range * 0.5f, 30);

        level.playSound(null, player.blockPosition(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8f, 1.5f);
    }

    // === 道行为效果应用 ===

    private static void applyTargetEffects(LivingEntity target, ServerPlayer player,
                                            CompositeEffects effects, float damage) {
        for (var entry : effects.contributions) {
            PathEffectComponent.EffectContribution c = entry.contribution;
            float scale = entry.isPrimary ? 1.0f : 0.6f;

            switch (c.targetEffect()) {
                case NONE -> {}
                case FREEZE -> FrostManager.applyFreeze(target, (int)(c.targetParam() * scale));
                case SLOW -> FrostManager.applySlow(target, (int)(c.targetParam() * scale), 0.5 * scale);
                case IGNITE -> target.igniteForTicks((int)(c.targetParam() * scale));
                case POISON_DOT -> applyPoisonDot(target, player, damage * 0.15f * scale, (int)(c.targetParam() * scale));
                case CHAIN_BOUNCE -> chainToNearby(player, target, damage * 0.4f * scale, (int)c.targetParam());
                case KNOCKBACK -> {
                    Vec3 dir = target.position().subtract(player.position()).normalize().scale(c.targetParam() * scale);
                    target.push(dir.x, 0.3 * scale, dir.z);
                    target.hurtMarked = true;
                }
                case BLEED -> applyBleedDot(target, player, damage * 0.1f * scale, (int)(c.targetParam() * scale));
                case ARMOR_PIERCE -> target.hurt(player.damageSources().magic(), damage * c.targetParam() * scale);
                case ENTANGLE -> {
                    FrostManager.applyFreeze(target, (int)(c.targetParam() * scale * 0.5f));
                    applyPoisonDot(target, player, damage * 0.08f * scale, (int)(c.targetParam() * scale));
                }
                case TRUE_DAMAGE -> target.hurt(player.damageSources().magic(), damage * c.targetParam() * scale);
                case GRAVITY_PULL -> {
                    Vec3 pull = player.position().subtract(target.position()).normalize().scale(c.targetParam() * scale * 0.3);
                    target.push(pull.x, pull.y * 0.5, pull.z);
                    target.hurtMarked = true;
                }
                case BLIND -> applyBlindness(target, (int)(c.targetParam() * scale));
                case WITHER -> applyWitherDot(target, player, damage * 0.1f * scale, (int)(c.targetParam() * scale));
                case CONFUSE -> applyConfusion(target, (int)(c.targetParam() * scale));
                case EXECUTION -> {
                    if (target.getHealth() / target.getMaxHealth() < c.targetParam()) {
                        target.hurt(player.damageSources().magic(), target.getHealth() + 10f);
                    }
                }
                case SUPPRESS -> applySuppression(target, (int)(c.targetParam() * scale));
                case DISPEL -> dispelBuffs(target);
                case AGGRO_REDIRECT -> redirectAggro(target, player);
                case SUMMON_STRIKE -> {
                    target.hurt(player.damageSources().magic(), damage * 0.3f * scale);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1, target.getZ(),
                            5, 0.3, 0.3, 0.3, 0.05);
                    }
                }
            }
        }
    }

    private static void applySelfEffects(ServerPlayer player, CompositeEffects effects, float damage) {
        float totalHeal = 0;
        float totalShield = 0;
        boolean hasEssenceRecover = false;
        float essenceRecoverTotal = 0;

        for (var entry : effects.contributions) {
            PathEffectComponent.EffectContribution c = entry.contribution;
            float scale = entry.isPrimary ? 1.0f : 0.5f;

            switch (c.selfEffect()) {
                case NONE -> {}
                case LIFESTEAL -> totalHeal += damage * c.selfParam() * scale;
                case SELF_DAMAGE -> player.hurt(player.damageSources().magic(), player.getMaxHealth() * 0.08f * scale);
                case SPEED_BOOST -> {
                    player.push(player.getLookAngle().x * 0.3 * scale, 0.1 * scale, player.getLookAngle().z * 0.3 * scale);
                    player.hurtMarked = true;
                }
                case SHIELD -> totalShield += damage * c.selfParam() * scale;
                case ESSENCE_RECOVER -> {
                    hasEssenceRecover = true;
                    essenceRecoverTotal += c.selfParam() * scale;
                }
                case INVISIBILITY -> {}
                case HEAL -> totalHeal += damage * c.selfParam() * scale;
                case THORNS -> totalShield += damage * 0.15f * scale;
            }
        }

        if (totalHeal > 0) player.heal(totalHeal);
        if (totalShield > 0) player.setAbsorptionAmount(player.getAbsorptionAmount() + totalShield);
        if (hasEssenceRecover) {
            Aperture ap = player.getData(ModAttachments.GU_MASTER_DATA.get()).getAperture();
            ap.regenerateEssence(ap.getMaxEssence() * essenceRecoverTotal);
        }
    }

    private static void spawnProjectiles(ServerPlayer player, CompositeEffects effects, float damage) {
        for (var entry : effects.contributions) {
            PathEffectComponent.EffectContribution c = entry.contribution;
            if (c.projectile() == null) continue;

            float scale = entry.isPrimary ? 1.0f : 0.6f;
            Vec3 look = player.getLookAngle();
            Vec3 eye = player.getEyePosition();
            ServerLevel level = player.serverLevel();
            float projDamage = damage * 0.5f * scale;

            AABB searchBox = new AABB(eye, eye.add(look.scale(12.0))).inflate(3);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive());

            if (targets.isEmpty()) continue;

            LivingEntity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (LivingEntity t : targets) {
                double d = t.distanceToSqr(player);
                if (d < closestDist) { closestDist = d; closest = t; }
            }

            if (closest != null) {
                closest.hurt(player.damageSources().magic(), projDamage);

                VfxType projVfx = switch (c.projectile()) {
                    case MOON_BLADE -> VfxType.SLASH_ARC;
                    case BLOOD_BOLT, FIRE_BOLT, ICE_BOLT -> VfxType.ENERGY_BEAM;
                    case GOLD_BEAM -> VfxType.ENERGY_BEAM;
                    case STAR_FALL -> VfxType.GLOW_BURST;
                };
                VfxHelper.spawn(player, projVfx,
                    player.getX(), player.getEyeY(), player.getZ(),
                    (float)look.x, (float)look.y, (float)look.z,
                    c.vfxColor(), 1.5f * scale, 15);
            }
        }
    }

    private static void spawnAreaEffects(ServerPlayer player, ServerLevel level, CompositeEffects effects) {
        for (var entry : effects.contributions) {
            PathEffectComponent.EffectContribution c = entry.contribution;
            if (c.areaEffect() == null) continue;

            float scale = entry.isPrimary ? 1.0f : 0.5f;
            float radius = 4f * scale;
            AABB aoe = player.getBoundingBox().inflate(radius);

            switch (c.areaEffect()) {
                case POISON_CLOUD -> {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applyPoisonDot(e, player, 1.5f * scale, (int)(c.areaParam() * scale));
                    }
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, player.getX(), player.getY() + 1, player.getZ(),
                        20, radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case FROST_FIELD -> {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        FrostManager.applySlow(e, (int)(c.areaParam() * scale * 0.5f), 0.3 * scale);
                    }
                    level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(),
                        25, radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case FIRE_FIELD -> {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        e.igniteForTicks((int)(c.areaParam() * scale * 0.5f));
                    }
                    level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(),
                        25, radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case FORMATION_FIELD -> {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player)) {
                        if (e.isAlive() && !(e instanceof Mob mob && mob.getTarget() == player)) {
                            e.heal(2f * scale);
                        }
                    }
                    player.heal(3f * scale);
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 0.1, player.getZ(),
                        0, 1, 0, c.vfxColor(), radius, 40);
                }
            }
        }
    }

    // === 道反应效果 ===

    private static void applyReactionEffects(ServerPlayer player, List<LivingEntity> targets,
                                              CompositeEffects effects, float damage) {
        for (var reaction : effects.reactions) {
            float power = reaction.power();
            int color = reaction.vfxColor();
            ServerLevel level = player.serverLevel();

            switch (reaction.type()) {
                case VORTEX_FREEZE -> {
                    Vec3 center = player.position().add(player.getLookAngle().scale(4));
                    for (LivingEntity t : targets) {
                        Vec3 pull = center.subtract(t.position()).normalize().scale(1.5);
                        t.push(pull.x, 0.2, pull.z);
                        t.hurtMarked = true;
                        FrostManager.applyFreeze(t, (int)(80 * power));
                    }
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        center.x, center.y + 1, center.z, 0, 1, 0, color, 4f, 30);
                }
                case GIANT_STRIKE -> {
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.5f * power);
                        Vec3 kb = t.position().subtract(player.position()).normalize().scale(2.0 * power);
                        t.push(kb.x, 0.5, kb.z);
                        t.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX() + player.getLookAngle().x * 3, player.getY() + 2, player.getZ() + player.getLookAngle().z * 3,
                        0, -1, 0, color, 5f, 20);
                }
                case PIERCING_STRIKE -> {
                    Vec3 look = player.getLookAngle();
                    Vec3 eye = player.getEyePosition();
                    AABB line = new AABB(eye, eye.add(look.scale(10))).inflate(0.8);
                    List<LivingEntity> lineTargets = level.getEntitiesOfClass(LivingEntity.class, line,
                        e -> e != player && e.isAlive());
                    for (LivingEntity t : lineTargets) {
                        t.hurt(player.damageSources().magic(), damage * 0.4f * power);
                    }
                    VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
                        player.getX(), player.getEyeY(), player.getZ(),
                        (float)look.x, (float)look.y, (float)look.z, color, 2f, 15);
                }
                case SOUL_FIRE -> {
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().magic(), damage * 0.2f * power);
                        t.igniteForTicks((int)(60 * power));
                    }
                }
                case ICE_FIRE_SHOCK -> {
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().magic(), damage * 0.3f * power);
                        FrostManager.applyFreeze(t, 30);
                        t.igniteForTicks(40);
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 4f, 15);
                }
                case BLOOD_RAIN -> {
                    float radius = 6f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        float bleedDmg = damage * 0.15f * power;
                        e.hurt(player.damageSources().magic(), bleedDmg);
                        player.heal(bleedDmg * 0.3f);
                    }
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        player.getX(), player.getY() + 3, player.getZ(), 30, radius * 0.5, 2, radius * 0.5, 0.05);
                }
                case DARK_SOUL_DRAIN -> {
                    for (LivingEntity t : targets) {
                        float trueDmg = damage * 0.25f * power;
                        t.hurt(player.damageSources().magic(), trueDmg);
                        applyWitherDot(t, player, trueDmg * 0.3f, 80);
                        player.heal(trueDmg * 0.2f);
                    }
                }
                case MIND_DOMINATE -> {
                    List<Mob> mobs = new ArrayList<>();
                    for (LivingEntity t : targets) {
                        if (t instanceof Mob mob) mobs.add(mob);
                    }
                    if (mobs.size() >= 2) {
                        for (int i = 0; i < mobs.size(); i++) {
                            Mob mob = mobs.get(i);
                            Mob otherTarget = mobs.get((i + 1) % mobs.size());
                            mob.setTarget(otherTarget);
                        }
                    } else {
                        for (LivingEntity t : targets) {
                            if (t instanceof Mob mob) mob.setTarget(null);
                            t.setDeltaMovement(Vec3.ZERO);
                            t.hurtMarked = true;
                        }
                    }
                }
                case FLIGHT_DASH -> {
                    Vec3 look = player.getLookAngle();
                    float dist = 12f * power;
                    Vec3 dest = player.position().add(look.scale(dist));
                    player.teleportTo(dest.x, dest.y + 1, dest.z);
                    AABB trail = new AABB(player.position(), dest).inflate(2);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, trail, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.4f * power);
                    }
                }
                case EARTH_FORM -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.6f * power);
                    float pullRadius = 6f;
                    AABB pullArea = player.getBoundingBox().inflate(pullRadius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, pullArea, e -> e != player && e.isAlive())) {
                        Vec3 pull = player.position().subtract(e.position()).normalize().scale(0.8);
                        e.push(pull.x, 0.1, pull.z);
                        e.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 0.5, player.getZ(), 0, 1, 0, color, 3f, 40);
                }
                case WIND_FORM -> {
                    player.push(player.getLookAngle().x * 1.5, 0.3, player.getLookAngle().z * 1.5);
                    player.hurtMarked = true;
                    float bladeRadius = 4f;
                    AABB bladeArea = player.getBoundingBox().inflate(bladeRadius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, bladeArea, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.2f * power);
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 2.5f, 30);
                }
                case CLONE_ARMY -> {
                    int clones = 2 + effects.contributions.size();
                    for (LivingEntity t : targets) {
                        float cloneDmg = damage * 0.15f * power;
                        for (int i = 0; i < clones; i++) {
                            t.hurt(player.damageSources().magic(), cloneDmg);
                        }
                        level.sendParticles(ParticleTypes.SOUL,
                            t.getX(), t.getY() + 1, t.getZ(), clones * 3, 1, 0.5, 1, 0.05);
                    }
                }
                case STEAL_BUFF -> {
                    for (LivingEntity t : targets) {
                        float stolen = t.getAbsorptionAmount();
                        t.setAbsorptionAmount(0);
                        t.removeAllEffects();
                        player.heal(stolen * 0.5f + 2f);
                        player.setAbsorptionAmount(player.getAbsorptionAmount() + stolen * 0.3f);
                    }
                }
                case THUNDER_RAIN -> {
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().magic(), damage * 0.3f * power);
                        chainToNearby(player, t, damage * 0.2f * power, 2);
                    }
                    level.sendParticles(ParticleTypes.FIREWORK,
                        player.getX(), player.getY() + 5, player.getZ(), 15, 3, 1, 3, 0.1);
                }
                case BONE_BLADE -> {
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.25f * power);
                    }
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.15f);
                }
                case DREAM_ILLUSION -> {
                    for (LivingEntity t : targets) {
                        applyConfusion(t, (int)(100 * power));
                        if (t instanceof Mob mob) mob.setTarget(null);
                    }
                    level.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1, player.getZ(), 30, 3, 1, 3, 0.5);
                }
                case POISON_FIRE -> {
                    for (LivingEntity t : targets) {
                        applyPoisonDot(t, player, damage * 0.12f * power, 100);
                        t.igniteForTicks((int)(80 * power));
                    }
                    float spreadRadius = 4f;
                    AABB spread = player.getBoundingBox().inflate(spreadRadius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, spread, e -> e != player && e.isAlive() && !targets.contains(e))) {
                        applyPoisonDot(e, player, damage * 0.06f * power, 60);
                    }
                }
                case WATER_ICE_SEAL -> {
                    for (LivingEntity t : targets) {
                        FrostManager.applyFreeze(t, 160);
                        FrostManager.applySlow(t, 200, 0.7);
                    }
                }
                case HEAVEN_RULE -> {
                    for (LivingEntity t : targets) {
                        chainToNearby(player, t, damage * 0.3f * power, 3);
                        dispelBuffs(t);
                    }
                }
                case CORROSIVE_BURST -> {
                    float radius = 8f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        float corrosion = e.getMaxHealth() * 0.15f * power;
                        e.hurt(player.damageSources().magic(), corrosion);
                        applyWitherDot(e, player, corrosion * 0.2f, 60);
                    }
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, radius * 0.8f, 25);
                    level.sendParticles(ParticleTypes.SQUID_INK,
                        player.getX(), player.getY() + 2, player.getZ(), 50, radius * 0.4, 2, radius * 0.4, 0.01);
                }
                case STORM_FUSION -> {
                    float radius = 6f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    Vec3 center = player.position();
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        Vec3 pull = center.subtract(e.position()).normalize().scale(1.2);
                        e.push(pull.x, 0.3, pull.z);
                        e.hurtMarked = true;
                        e.hurt(player.damageSources().magic(), damage * 0.2f * power);
                        chainToNearby(player, e, damage * 0.15f * power, 2);
                    }
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 5f, 20);
                }
                case BLOOD_FLAME -> {
                    for (LivingEntity t : targets) {
                        float hpScaledDmg = t.getHealth() * 0.12f * power;
                        t.hurt(player.damageSources().magic(), hpScaledDmg);
                        t.igniteForTicks((int)(100 * power));
                        applyBleedDot(t, player, hpScaledDmg * 0.15f, 80);
                    }
                    player.hurt(player.damageSources().magic(), player.getMaxHealth() * 0.05f);
                }
                case SOUL_RADIANCE -> {
                    for (LivingEntity t : targets) {
                        float trueDmg = t.getMaxHealth() * 0.1f * power;
                        t.hurt(player.damageSources().magic(), trueDmg);
                        dispelBuffs(t);
                        applyBlindness(t, 60);
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 2, player.getZ(), 0, 1, 0, color, 6f, 20);
                }
                case CRYSTALLIZE -> {
                    for (LivingEntity t : targets) {
                        FrostManager.applyFreeze(t, (int)(200 * power));
                        t.setDeltaMovement(Vec3.ZERO);
                        t.hurtMarked = true;
                        applySuppression(t, (int)(200 * power));
                    }
                    level.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1, player.getZ(), 20, 3, 1, 3, 0.01);
                }
                case BEAST_SHIFT -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.8f * power);
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.6f * power);
                        Vec3 kb = t.position().subtract(player.position()).normalize().scale(2.5 * power);
                        t.push(kb.x, 0.5, kb.z);
                        t.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 4f, 60);
                }
                case FATE_MANIPULATION -> {
                    for (LivingEntity t : targets) {
                        applyConfusion(t, (int)(80 * power));
                        FrostManager.applySlow(t, (int)(80 * power), 0.4);
                        if (t instanceof Mob mob && RANDOM.nextFloat() < 0.3f * power) {
                            mob.setTarget(null);
                        }
                    }
                    player.heal(damage * 0.15f * power);
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        player.getX(), player.getY() + 2, player.getZ(), 0, 1, 0, color, 5f, 25);
                }
                case MIND_SOUL_SYNERGY -> {
                    for (LivingEntity t : targets) {
                        float trueDmg = t.getMaxHealth() * 0.12f * power;
                        t.hurt(player.damageSources().magic(), trueDmg);
                        applyConfusion(t, (int)(100 * power));
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 2, player.getZ(), 0, 1, 0, color, 4f, 20);
                }
                case STAR_FIRE_ESCAPE -> {
                    Vec3 look = player.getLookAngle();
                    float dist = 10f * power;
                    Vec3 dest = player.position().add(look.scale(dist));
                    AABB trail = new AABB(player.position(), dest).inflate(2);
                    player.teleportTo(dest.x, dest.y, dest.z);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, trail, e -> e != player && e.isAlive())) {
                        e.igniteForTicks((int)(80 * power));
                        e.hurt(player.damageSources().magic(), damage * 0.3f * power);
                    }
                    VfxHelper.spawn(player, VfxType.IMPACT_BURST,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (float)look.x, (float)look.y, (float)look.z, color, 2.5f, 15);
                }
                case WOOD_BLOOD_BLOOM -> {
                    for (LivingEntity t : targets) {
                        applyBleedDot(t, player, damage * 0.15f * power, 100);
                    }
                    float spreadRadius = 5f;
                    AABB spread = player.getBoundingBox().inflate(spreadRadius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, spread, e -> e != player && e.isAlive() && !targets.contains(e))) {
                        applyBleedDot(e, player, damage * 0.08f * power, 60);
                    }
                    level.sendParticles(ParticleTypes.CHERRY_LEAVES,
                        player.getX(), player.getY() + 2, player.getZ(), 40, spreadRadius * 0.4, 1, spreadRadius * 0.4, 0.05);
                }
                case SOUND_BLADE -> {
                    Vec3 look = player.getLookAngle();
                    Vec3 eye = player.getEyePosition();
                    AABB line = new AABB(eye, eye.add(look.scale(12))).inflate(1.2);
                    for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, line, e -> e != player && e.isAlive())) {
                        t.hurt(player.damageSources().magic(), damage * 0.35f * power);
                        applySuppression(t, (int)(40 * power));
                    }
                    VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
                        player.getX(), player.getEyeY(), player.getZ(),
                        (float)look.x, (float)look.y, (float)look.z, color, 2f, 12);
                }
                case PHASE_SHIFT -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.7f * power);
                    player.push(player.getLookAngle().x * 0.8, 0.2, player.getLookAngle().z * 0.8);
                    player.hurtMarked = true;
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.5f * power);
                        Vec3 kb = t.position().subtract(player.position()).normalize().scale(2.0 * power);
                        t.push(kb.x, 0.4, kb.z);
                        t.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 4f, 50);
                }
                case SOUL_DEVOUR -> {
                    for (LivingEntity t : targets) {
                        float devourDmg = t.getMaxHealth() * 0.12f * power;
                        t.hurt(player.damageSources().magic(), devourDmg);
                        player.heal(devourDmg * 0.5f);
                        dispelBuffs(t);
                        float stolenShield = t.getAbsorptionAmount();
                        t.setAbsorptionAmount(0);
                        player.setAbsorptionAmount(player.getAbsorptionAmount() + stolenShield * 0.3f);
                    }
                    level.sendParticles(ParticleTypes.SOUL,
                        player.getX(), player.getY() + 1, player.getZ(), 15, 2, 1, 2, 0.05);
                }
                case MURAL_PRISON -> {
                    for (LivingEntity t : targets) {
                        FrostManager.applyFreeze(t, (int)(160 * power));
                        applySuppression(t, (int)(160 * power));
                        t.setDeltaMovement(Vec3.ZERO);
                        t.hurtMarked = true;
                        t.hurt(player.damageSources().magic(), damage * 0.15f * power);
                    }
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 5f, 40);
                }
                case FORCED_BANISH -> {
                    for (LivingEntity t : targets) {
                        Vec3 banish = t.position().subtract(player.position()).normalize().scale(12.0 * power);
                        t.teleportTo(t.getX() + banish.x, t.getY(), t.getZ() + banish.z);
                        t.hurt(player.damageSources().magic(), damage * 0.2f * power);
                        FrostManager.applySlow(t, (int)(60 * power), 0.5);
                    }
                    VfxHelper.spawn(player, VfxType.IMPACT_BURST,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 3f, 15);
                }
                case AUTO_CONSTRUCT -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.8f * power);
                    float radius = 5f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        Vec3 kb = e.position().subtract(player.position()).normalize().scale(2.0);
                        e.push(kb.x, 0.4, kb.z);
                        e.hurtMarked = true;
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.15f * power);
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 0.5, player.getZ(), 0, 1, 0, color, radius * 0.8f, 50);
                }
                case SOUL_REAP -> {
                    for (LivingEntity t : targets) {
                        float soulDmg = t.getMaxHealth() * 0.15f * power;
                        t.hurt(player.damageSources().magic(), soulDmg);
                        if (t.getHealth() / t.getMaxHealth() < 0.3f) {
                            t.hurt(player.damageSources().magic(), t.getHealth() + 10f);
                        }
                        player.heal(soulDmg * 0.3f);
                    }
                    level.sendParticles(ParticleTypes.SOUL,
                        player.getX(), player.getY() + 1, player.getZ(), 20, 2, 1, 2, 0.05);
                }
                case SHADOW_KILL -> {
                    for (LivingEntity t : targets) {
                        float assassinDmg = damage * 0.6f * power;
                        t.hurt(player.damageSources().magic(), assassinDmg);
                        applyBlindness(t, (int)(60 * power));
                        applySuppression(t, (int)(40 * power));
                    }
                    VfxHelper.spawn(player, VfxType.SHADOW_FADE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 3f, 15);
                }
                case SHADOW_MERGE -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.5f * power);
                    player.push(player.getLookAngle().x * 0.8, 0.2, player.getLookAngle().z * 0.8);
                    player.hurtMarked = true;
                    for (LivingEntity t : targets) {
                        applyBlindness(t, (int)(80 * power));
                    }
                    VfxHelper.spawn(player, VfxType.SHADOW_FADE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 3f, 40);
                }
                case LIGHT_SHADOW_SPLIT -> {
                    int clones = 2 + (int)(power);
                    for (LivingEntity t : targets) {
                        for (int i = 0; i < clones; i++) {
                            t.hurt(player.damageSources().magic(), damage * 0.12f * power);
                        }
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 2, player.getZ(), 0, 1, 0, color, 5f, 20);
                    level.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1, player.getZ(), clones * 8, 3, 1, 3, 0.1);
                }
                case MOON_SOUL -> {
                    for (LivingEntity t : targets) {
                        float trueDmg = t.getMaxHealth() * 0.1f * power;
                        t.hurt(player.damageSources().magic(), trueDmg);
                        applyConfusion(t, (int)(80 * power));
                    }
                    player.heal(damage * 0.15f * power);
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 3, player.getZ(), 0, -1, 0, color, 4f, 25);
                }
                case MOONLIGHT_DOMAIN -> {
                    float radius = 7f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        FrostManager.applySlow(e, (int)(100 * power), 0.4);
                        e.hurt(player.damageSources().magic(), damage * 0.15f * power);
                    }
                    player.heal(damage * 0.1f * power);
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 0.5, player.getZ(), 0, 1, 0, color, radius * 0.8f, 50);
                }
                case VOID_COLLAPSE -> {
                    float radius = 6f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    Vec3 center = player.position().add(player.getLookAngle().scale(4));
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        Vec3 pull = center.subtract(e.position()).normalize().scale(2.0 * power);
                        e.push(pull.x, 0.3, pull.z);
                        e.hurtMarked = true;
                        float voidDmg = e.getMaxHealth() * 0.12f * power;
                        e.hurt(player.damageSources().magic(), voidDmg);
                        dispelBuffs(e);
                    }
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        center.x, center.y + 1, center.z, 0, 1, 0, color, 5f, 25);
                }
                case CHARM_ILLUSION -> {
                    for (LivingEntity t : targets) {
                        applyConfusion(t, (int)(120 * power));
                        if (t instanceof Mob mob) {
                            mob.setTarget(null);
                            FrostManager.applySlow(t, (int)(80 * power), 0.5);
                        }
                    }
                    level.sendParticles(ParticleTypes.HEART,
                        player.getX(), player.getY() + 2, player.getZ(), 15, 3, 1, 3, 0.1);
                }
                case BATTLE_FORMATION -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.6f * power);
                    float radius = 5f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.2f * power);
                        Vec3 kb = e.position().subtract(player.position()).normalize().scale(1.5);
                        e.push(kb.x, 0.3, kb.z);
                        e.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 0.5, player.getZ(), 0, 1, 0, color, radius * 0.7f, 50);
                }
                case SKY_CHARGE -> {
                    Vec3 look = player.getLookAngle();
                    float dist = 15f * power;
                    Vec3 dest = player.position().add(look.scale(dist)).add(0, 3, 0);
                    player.teleportTo(dest.x, dest.y, dest.z);
                    AABB trail = new AABB(player.position(), dest).inflate(2);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, trail, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.5f * power);
                        Vec3 kb = e.position().subtract(player.position()).normalize().scale(2.0);
                        e.push(kb.x, 0.5, kb.z);
                        e.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.IMPACT_BURST,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (float)look.x, (float)look.y, (float)look.z, color, 3f, 15);
                }
                case CLOUD_SEA -> {
                    float radius = 7f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        FrostManager.applySlow(e, (int)(100 * power), 0.5);
                        applyBlindness(e, (int)(60 * power));
                    }
                    player.heal(damage * 0.1f * power);
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.3f * power);
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 0.5, player.getZ(), 0, 1, 0, color, radius * 0.7f, 50);
                }
                case YIN_YANG_THUNDER -> {
                    for (LivingEntity t : targets) {
                        float yinDmg = damage * 0.25f * power;
                        float yangDmg = damage * 0.25f * power;
                        t.hurt(player.damageSources().magic(), yinDmg);
                        t.hurt(player.damageSources().playerAttack(player), yangDmg);
                        chainToNearby(player, t, yinDmg * 0.5f, 2);
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 3, player.getZ(), 0, -1, 0, color, 5f, 20);
                }
                case VOID_SEAL -> {
                    for (LivingEntity t : targets) {
                        FrostManager.applyFreeze(t, (int)(200 * power));
                        applySuppression(t, (int)(200 * power));
                        dispelBuffs(t);
                        t.setDeltaMovement(Vec3.ZERO);
                        t.hurtMarked = true;
                        t.hurt(player.damageSources().magic(), damage * 0.2f * power);
                    }
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, color, 5f, 40);
                }
            }
        }
    }

    // === 道叠加效果 ===

    private static void applyStackingEffects(ServerPlayer player, List<LivingEntity> targets,
                                              CompositeEffects effects, float damage) {
        for (var stack : effects.stackEffects) {
            float mult = stack.multiplier();
            ServerLevel level = player.serverLevel();

            switch (stack.effect()) {
                case BEAST_PHANTOM -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.5f * mult);
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.4f * mult);
                        Vec3 kb = t.position().subtract(player.position()).normalize().scale(2.5);
                        t.push(kb.x, 0.5, kb.z);
                        t.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 1, player.getZ(),
                        0, 1, 0, 0xFFCC4422, 5f * mult / 1.8f, 40);
                }
                case ENHANCED_SHIELD -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.3f * mult);
                }
                case ICE_DOMAIN -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        FrostManager.applyFreeze(e, (int)(120 * mult / 2f));
                    }
                    level.sendParticles(ParticleTypes.SNOWFLAKE,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(40 * mult / 2f), radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case FIRE_DOMAIN -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        e.igniteForTicks((int)(120 * mult / 2f));
                        e.hurt(player.damageSources().magic(), damage * 0.1f * mult);
                    }
                    level.sendParticles(ParticleTypes.FLAME,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(40 * mult / 2f), radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case BLOOD_FRENZY -> {
                    for (LivingEntity t : targets) {
                        float bleedDmg = damage * 0.2f * mult;
                        t.hurt(player.damageSources().magic(), bleedDmg);
                        player.heal(bleedDmg * 0.4f);
                    }
                    player.hurt(player.damageSources().magic(), player.getMaxHealth() * 0.1f);
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        player.getX(), player.getY() + 1, player.getZ(), 15, 1, 0.5, 1, 0.02);
                }
                case SOUL_SHATTER -> {
                    float radius = 5f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().magic(), damage * 0.3f * mult);
                    }
                    level.sendParticles(ParticleTypes.SOUL,
                        player.getX(), player.getY() + 1, player.getZ(), 20, radius * 0.4, 1, radius * 0.4, 0.05);
                }
                case BONE_CONSTRUCT -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.4f * mult);
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.15f * mult);
                    }
                }
                case STEAL_POWER -> {
                    for (LivingEntity t : targets) {
                        t.setAbsorptionAmount(0);
                        t.removeAllEffects();
                        player.heal(3f * mult);
                    }
                }
                case DARK_DOMAIN -> {
                    float radius = 5f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        float corrosionDmg = e.getMaxHealth() * 0.08f * mult;
                        e.hurt(player.damageSources().magic(), corrosionDmg);
                        player.heal(corrosionDmg * 0.15f);
                    }
                    level.sendParticles(ParticleTypes.SQUID_INK,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(30 * mult / 2f), radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case LIGHTNING_STORM -> {
                    int strikes = (int)(3 * mult / 2f);
                    for (LivingEntity t : targets) {
                        for (int i = 0; i < strikes; i++) {
                            chainToNearby(player, t, damage * 0.2f, 2);
                        }
                    }
                }
                case WIND_STORM -> {
                    float radius = 6f;
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    Vec3 center = player.position();
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        Vec3 pull = center.subtract(e.position()).normalize().scale(1.0);
                        e.push(pull.x, 0.3, pull.z);
                        e.hurtMarked = true;
                    }
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(2), e -> e != player && e.isAlive())) {
                        Vec3 push = e.position().subtract(center).normalize().scale(3.0 * mult);
                        e.push(push.x, 0.5, push.z);
                        e.hurtMarked = true;
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.2f * mult);
                    }
                }
                case SWORD_RAIN -> {
                    int swords = 3 + (int)(mult);
                    for (LivingEntity t : targets) {
                        for (int i = 0; i < swords; i++) {
                            t.hurt(player.damageSources().magic(), damage * 0.1f);
                        }
                    }
                    VfxHelper.spawn(player, VfxType.SLASH_ARC,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (float)player.getLookAngle().x, (float)player.getLookAngle().y, (float)player.getLookAngle().z,
                        0xFFCCCCEE, 4f, 20);
                }
                case POISON_MIASMA -> {
                    float radius = 8f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applyPoisonDot(e, player, damage * 0.12f * mult, (int)(120 * mult));
                    }
                    level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(30 * mult), radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case DREAM_REALM -> {
                    for (LivingEntity t : targets) {
                        applyConfusion(t, (int)(120 * mult));
                        if (t instanceof Mob mob) {
                            List<Mob> nearby = new ArrayList<>();
                            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                                    mob.getBoundingBox().inflate(8), e -> e != player && e != mob && e instanceof Mob && e.isAlive())) {
                                nearby.add((Mob) e);
                            }
                            if (!nearby.isEmpty()) {
                                mob.setTarget(nearby.get(RANDOM.nextInt(nearby.size())));
                            }
                        }
                    }
                }
                case STAR_PHANTOM -> {
                    int phantoms = 3 + (int)mult;
                    for (LivingEntity t : targets) {
                        for (int i = 0; i < phantoms; i++) {
                            t.hurt(player.damageSources().magic(), damage * 0.08f * mult);
                        }
                        level.sendParticles(ParticleTypes.END_ROD,
                            t.getX(), t.getY() + 1, t.getZ(), phantoms * 5, 2, 1, 2, 0.1);
                    }
                }
                case EARTH_FORTRESS -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.5f * mult);
                    float radius = 5f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.2f * mult);
                        Vec3 kb = e.position().subtract(player.position()).normalize().scale(1.5);
                        e.push(kb.x, 0.5, kb.z);
                        e.hurtMarked = true;
                    }
                }
                case TIDAL_SURGE -> {
                    Vec3 look = player.getLookAngle();
                    float range = 8f * (mult / 2f);
                    AABB wave = new AABB(player.getEyePosition(), player.getEyePosition().add(look.scale(range))).inflate(3);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, wave, e -> e != player && e.isAlive())) {
                        Vec3 push = look.scale(2.5 * mult);
                        e.push(push.x, 0.4, push.z);
                        e.hurtMarked = true;
                        e.hurt(player.damageSources().magic(), damage * 0.15f * mult);
                        FrostManager.applySlow(e, (int)(80 * mult), 0.6);
                    }
                    level.sendParticles(ParticleTypes.SPLASH,
                        player.getX() + look.x * range * 0.5, player.getY() + 1, player.getZ() + look.z * range * 0.5,
                        (int)(40 * mult), range * 0.3, 1, range * 0.3, 0.1);
                }
                case LIGHT_JUDGMENT -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        float hpDmg = e.getMaxHealth() * 0.10f * mult;
                        e.hurt(player.damageSources().magic(), hpDmg);
                        applyBlindness(e, (int)(60 * mult));
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 3, player.getZ(), 0, -1, 0, 0xFFFFEE88, 6f * mult / 2f, 25);
                }
                case SOUND_SUPPRESS -> {
                    float radius = 7f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applySuppression(e, (int)(120 * mult));
                        dispelBuffs(e);
                        e.setDeltaMovement(Vec3.ZERO);
                        e.hurtMarked = true;
                    }
                    level.sendParticles(ParticleTypes.NOTE,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (int)(20 * mult), radius * 0.4, 1, radius * 0.4, 0.01);
                }
                case TRANSFORMATION_EVOLVE -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.3f * mult);
                    player.push(player.getLookAngle().x * 0.5, 0.2, player.getLookAngle().z * 0.5);
                    player.hurtMarked = true;
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.25f * mult);
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, 0xFF88AACC, 3f, 40);
                }
                case NULLIFY -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applySuppression(e, (int)(160 * mult));
                        dispelBuffs(e);
                        e.setDeltaMovement(Vec3.ZERO);
                        e.hurtMarked = true;
                        e.hurt(player.damageSources().magic(), damage * 0.1f * mult);
                    }
                    level.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (int)(25 * mult), radius * 0.4, 1, radius * 0.4, 0.01);
                }
                case MIND_SCATTER -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    List<Mob> mobs = new ArrayList<>();
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applyConfusion(e, (int)(150 * mult));
                        if (e instanceof Mob mob) mobs.add(mob);
                    }
                    if (mobs.size() >= 2) {
                        for (int i = 0; i < mobs.size(); i++) {
                            mobs.get(i).setTarget(mobs.get((i + 1) % mobs.size()));
                        }
                    }
                    level.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (int)(30 * mult), radius * 0.4, 1, radius * 0.4, 0.3);
                }
                case CLOUD_RING -> {
                    float absorbed = damage * 0.4f * mult;
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + absorbed);
                    float radius = 5f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    float counterDmg = absorbed * 0.6f;
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().magic(), counterDmg / Math.max(1, targets.size()));
                        Vec3 kb = e.position().subtract(player.position()).normalize().scale(1.0);
                        e.push(kb.x, 0.2, kb.z);
                        e.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, 0xFFDDEEFF, 3.5f * mult / 2f, 35);
                }
                case METAL_CRUSH -> {
                    for (LivingEntity t : targets) {
                        float armorPierce = damage * 0.4f * mult;
                        t.hurt(player.damageSources().magic(), armorPierce);
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.2f * mult);
                        Vec3 kb = t.position().subtract(player.position()).normalize().scale(1.5);
                        t.push(kb.x, 0.3, kb.z);
                        t.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.IMPACT_BURST,
                        player.getX() + player.getLookAngle().x * 3, player.getY() + 1, player.getZ() + player.getLookAngle().z * 3,
                        0, -1, 0, 0xFFCCBB44, 4f * mult / 2f, 15);
                }
                case SEAL_POWER -> {
                    float radius = 5f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        FrostManager.applyFreeze(e, (int)(180 * mult));
                        applySuppression(e, (int)(180 * mult));
                        e.setDeltaMovement(Vec3.ZERO);
                        e.hurtMarked = true;
                    }
                    level.sendParticles(ParticleTypes.WITCH,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(20 * mult), radius * 0.4, 1, radius * 0.4, 0.02);
                }
                case BLADE_STORM -> {
                    int slashes = 4 + (int)(mult);
                    for (LivingEntity t : targets) {
                        for (int i = 0; i < slashes; i++) {
                            t.hurt(player.damageSources().playerAttack(player), damage * 0.08f);
                        }
                    }
                    VfxHelper.spawn(player, VfxType.SLASH_ARC,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        (float)player.getLookAngle().x, (float)player.getLookAngle().y, (float)player.getLookAngle().z,
                        0xFFBBCCDD, 3.5f, 18);
                }
                case QI_SURGE -> {
                    float radius = 7f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    Vec3 center = player.position();
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().magic(), damage * 0.2f * mult);
                        Vec3 push = e.position().subtract(center).normalize().scale(2.0 * mult);
                        e.push(push.x, 0.5, push.z);
                        e.hurtMarked = true;
                    }
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.2f * mult);
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, 0xFF88CCDD, radius * 0.6f, 25);
                }
                case PEOPLES_WILL -> {
                    float radius = 8f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        if (e instanceof Mob mob && mob.getTarget() == player) {
                            applyConfusion(e, (int)(100 * mult));
                            FrostManager.applySlow(e, (int)(80 * mult), 0.4);
                        } else {
                            e.heal(4f * mult);
                        }
                    }
                    player.heal(damage * 0.15f * mult);
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.2f * mult);
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 2, player.getZ(), 0, 1, 0, 0xFFFFDD88, 5f * mult / 2f, 30);
                }
                case KILL_DOMAIN -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        float killDmg = damage * 0.3f * mult;
                        e.hurt(player.damageSources().magic(), killDmg);
                        if (e.getHealth() / e.getMaxHealth() < 0.25f) {
                            e.hurt(player.damageSources().magic(), e.getHealth() + 10f);
                        }
                    }
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(25 * mult), radius * 0.4, 1, radius * 0.4, 0.05);
                }
                case SHADOW_DOMAIN -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applyBlindness(e, (int)(100 * mult));
                        e.hurt(player.damageSources().magic(), damage * 0.15f * mult);
                    }
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.2f * mult);
                    level.sendParticles(ParticleTypes.SQUID_INK,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(30 * mult), radius * 0.5, 1, radius * 0.5, 0.02);
                }
                case YIN_YANG_REVERSAL -> {
                    for (LivingEntity t : targets) {
                        float currentHp = t.getHealth();
                        float maxHp = t.getMaxHealth();
                        float reversalDmg = (maxHp - currentHp) * 0.3f * mult;
                        t.hurt(player.damageSources().magic(), reversalDmg);
                        dispelBuffs(t);
                    }
                    float playerMissing = player.getMaxHealth() - player.getHealth();
                    player.heal(playerMissing * 0.2f * mult);
                    level.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (int)(20 * mult), 2, 1, 2, 0.05);
                }
                case MOON_DOMAIN -> {
                    float radius = 7f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        FrostManager.applySlow(e, (int)(100 * mult), 0.5);
                        e.hurt(player.damageSources().magic(), damage * 0.12f * mult);
                    }
                    player.heal(damage * 0.1f * mult);
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 0.5, player.getZ(), 0, 1, 0, 0xFFBBCCFF, radius * 0.7f, 50);
                }
                case VOID_PHASE -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.6f * mult);
                    for (LivingEntity t : targets) {
                        float voidDmg = t.getMaxHealth() * 0.08f * mult;
                        t.hurt(player.damageSources().magic(), voidDmg);
                        dispelBuffs(t);
                    }
                    player.push(player.getLookAngle().x * 0.5, 0.2, player.getLookAngle().z * 0.5);
                    player.hurtMarked = true;
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        player.getX(), player.getY() + 1, player.getZ(),
                        (int)(20 * mult), 1, 1, 1, 0.05);
                }
                case CHARM_AURA -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applyConfusion(e, (int)(120 * mult));
                        if (e instanceof Mob mob) {
                            mob.setTarget(null);
                        }
                        FrostManager.applySlow(e, (int)(100 * mult), 0.6);
                    }
                    level.sendParticles(ParticleTypes.HEART,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (int)(15 * mult), radius * 0.4, 1, radius * 0.4, 0.05);
                }
                case WAR_SPIRIT -> {
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.4f * mult);
                    for (LivingEntity t : targets) {
                        t.hurt(player.damageSources().playerAttack(player), damage * 0.3f * mult);
                        Vec3 kb = t.position().subtract(player.position()).normalize().scale(1.5);
                        t.push(kb.x, 0.3, kb.z);
                        t.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.AURA_RING,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, 0xFF886644, 3.5f * mult / 2f, 40);
                }
                case SKY_SOVEREIGNTY -> {
                    Vec3 look = player.getLookAngle();
                    float range = 10f * (mult / 2f);
                    player.push(look.x * 1.5, 0.8, look.z * 1.5);
                    player.hurtMarked = true;
                    AABB wave = new AABB(player.getEyePosition(), player.getEyePosition().add(look.scale(range))).inflate(3);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, wave, e -> e != player && e.isAlive())) {
                        e.hurt(player.damageSources().playerAttack(player), damage * 0.2f * mult);
                        Vec3 push = look.scale(2.0 * mult);
                        e.push(push.x, 0.5, push.z);
                        e.hurtMarked = true;
                    }
                    VfxHelper.spawn(player, VfxType.IMPACT_BURST,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (float)look.x, (float)look.y, (float)look.z, 0xFF88DDCC, 3f * mult / 2f, 15);
                }
                case ILLUSION_REALM -> {
                    float radius = 6f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        applyConfusion(e, (int)(150 * mult));
                        applyBlindness(e, (int)(80 * mult));
                        if (e instanceof Mob mob) {
                            List<Mob> nearby = new ArrayList<>();
                            for (LivingEntity n : level.getEntitiesOfClass(LivingEntity.class,
                                    mob.getBoundingBox().inflate(8), n -> n != player && n != mob && n instanceof Mob && n.isAlive())) {
                                nearby.add((Mob) n);
                            }
                            if (!nearby.isEmpty()) mob.setTarget(nearby.get(RANDOM.nextInt(nearby.size())));
                        }
                    }
                    level.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (int)(40 * mult), radius * 0.4, 1, radius * 0.4, 0.5);
                }
                case SPACE_WARP -> {
                    float radius = 5f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    Vec3 center = player.position();
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        Vec3 pull = center.subtract(e.position()).normalize().scale(2.0 * mult);
                        e.push(pull.x, 0.3, pull.z);
                        e.hurtMarked = true;
                        e.hurt(player.damageSources().magic(), damage * 0.15f * mult);
                    }
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(2), e -> e != player && e.isAlive())) {
                        Vec3 push = e.position().subtract(center).normalize().scale(4.0 * mult);
                        e.push(push.x, 0.6, push.z);
                        e.hurtMarked = true;
                        e.hurt(player.damageSources().magic(), damage * 0.2f * mult);
                    }
                    VfxHelper.spawn(player, VfxType.PULSE_WAVE,
                        player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, 0xFF6644AA, radius * 0.6f, 20);
                }
                case PUPPET_ARMY -> {
                    int puppets = 3 + (int)mult;
                    for (LivingEntity t : targets) {
                        for (int i = 0; i < puppets; i++) {
                            t.hurt(player.damageSources().magic(), damage * 0.06f * mult);
                        }
                        if (t instanceof Mob mob) {
                            List<Mob> nearby = new ArrayList<>();
                            for (LivingEntity n : level.getEntitiesOfClass(LivingEntity.class,
                                    mob.getBoundingBox().inflate(8), n -> n != player && n != mob && n instanceof Mob && n.isAlive())) {
                                nearby.add((Mob) n);
                            }
                            if (!nearby.isEmpty()) mob.setTarget(nearby.get(RANDOM.nextInt(nearby.size())));
                        }
                    }
                    level.sendParticles(ParticleTypes.SOUL,
                        player.getX(), player.getY() + 1, player.getZ(),
                        puppets * 5, 2, 1, 2, 0.05);
                }
                case HEAVEN_WRATH -> {
                    float radius = 8f * (mult / 2f);
                    AABB aoe = player.getBoundingBox().inflate(radius);
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != player && e.isAlive())) {
                        float heavenDmg = e.getMaxHealth() * 0.1f * mult;
                        e.hurt(player.damageSources().magic(), heavenDmg);
                        chainToNearby(player, e, heavenDmg * 0.3f, 2);
                        dispelBuffs(e);
                    }
                    VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getY() + 4, player.getZ(), 0, -1, 0, 0xFFFFEE88, 6f * mult / 2f, 25);
                }
                case FORTUNE_SHIFT -> {
                    for (LivingEntity t : targets) {
                        if (RANDOM.nextFloat() < 0.3f * mult) {
                            t.hurt(player.damageSources().magic(), t.getMaxHealth() * 0.2f);
                        } else {
                            t.hurt(player.damageSources().magic(), damage * 0.15f * mult);
                        }
                        FrostManager.applySlow(t, (int)(60 * mult), 0.3);
                    }
                    player.heal(damage * 0.1f * mult);
                    player.setAbsorptionAmount(player.getAbsorptionAmount() + damage * 0.15f * mult);
                    level.sendParticles(ParticleTypes.COMPOSTER,
                        player.getX(), player.getY() + 2, player.getZ(),
                        (int)(20 * mult), 2, 1, 2, 0.1);
                }
            }
        }
    }

    // === 辅助效果实现 ===

    private static void chainToNearby(ServerPlayer player, LivingEntity origin, float damage, int bounces) {
        ServerLevel level = player.serverLevel();
        LivingEntity current = origin;
        List<LivingEntity> hit = new ArrayList<>();
        hit.add(origin);

        for (int i = 0; i < bounces; i++) {
            AABB searchBox = current.getBoundingBox().inflate(6);
            LivingEntity next = null;
            double bestDist = Double.MAX_VALUE;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, searchBox,
                    e -> e != player && e.isAlive() && !hit.contains(e))) {
                double d = e.distanceToSqr(current);
                if (d < bestDist) { bestDist = d; next = e; }
            }
            if (next == null) break;
            float chainDmg = damage * (1f - i * 0.2f);
            next.hurt(player.damageSources().magic(), chainDmg);
            hit.add(next);

            VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
                current.getX(), current.getEyeY(), current.getZ(),
                (float)(next.getX() - current.getX()),
                (float)(next.getEyeY() - current.getEyeY()),
                (float)(next.getZ() - current.getZ()),
                0xFFFFFF44, 0.5f, 8);
            current = next;
        }
    }

    private static void applyPoisonDot(LivingEntity target, ServerPlayer source, float damagePerTick, int durationTicks) {
        DotManager.applyPoison(target, source, damagePerTick, Math.max(20, durationTicks));
    }

    private static void applyBleedDot(LivingEntity target, ServerPlayer source, float damagePerTick, int durationTicks) {
        DotManager.applyBleed(target, source, damagePerTick, Math.max(20, durationTicks));
    }

    private static void applyWitherDot(LivingEntity target, ServerPlayer source, float damagePerTick, int durationTicks) {
        DotManager.applyWither(target, source, damagePerTick, Math.max(20, durationTicks));
    }

    private static void applyBlindness(LivingEntity target, int durationTicks) {
        if (target.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getEyeY(), target.getZ(),
                10, 0.5, 0.5, 0.5, 0.01);
        }
        FrostManager.applySlow(target, Math.min(durationTicks, 60), 0.3);
    }

    private static void applyConfusion(LivingEntity target, int durationTicks) {
        if (target instanceof Mob mob) {
            mob.setTarget(null);
        }
        double rx = (RANDOM.nextDouble() - 0.5) * 2;
        double rz = (RANDOM.nextDouble() - 0.5) * 2;
        target.push(rx, 0.1, rz);
        target.hurtMarked = true;
        if (target.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getEyeY(), target.getZ(),
                10, 0.5, 0.5, 0.5, 0.5);
        }
    }

    private static void applySuppression(LivingEntity target, int durationTicks) {
        FrostManager.applySlow(target, durationTicks, 0.7);
        AttributeInstance attackSpeed = target.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(SUPPRESS_MOD);
            attackSpeed.addTransientModifier(new AttributeModifier(SUPPRESS_MOD,
                -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void dispelBuffs(LivingEntity target) {
        target.setAbsorptionAmount(0);
        target.removeAllEffects();
    }

    private static void redirectAggro(LivingEntity target, ServerPlayer player) {
        if (target instanceof Mob mob) {
            ServerLevel level = player.serverLevel();
            AABB area = mob.getBoundingBox().inflate(12);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e != mob && e.isAlive() && e instanceof Mob);
            if (!nearby.isEmpty()) {
                LivingEntity redirect = nearby.get(RANDOM.nextInt(nearby.size()));
                mob.setTarget(redirect);
            }
        }
    }

    // === 组合效果容器 ===

    static class CompositeEffects {
        final List<ContributionEntry> contributions = new ArrayList<>();
        final List<PathReactionRegistry.ReactionEffect> reactions = new ArrayList<>();
        final List<PathStackingRule.StackThreshold> stackEffects = new ArrayList<>();
        float damageBonus = 0;
        float shieldBonus = 0;
        float rangeBonus = 0;
        float costReduction = 0;

        void addContribution(PathEffectComponent.EffectContribution c, boolean isPrimary) {
            contributions.add(new ContributionEntry(c, isPrimary));
        }

        float getDamageMultiplier() {
            return 1.0f + damageBonus;
        }

        int getBlendedColor() {
            List<Integer> allColors = new ArrayList<>();
            for (var entry : contributions) {
                allColors.add(entry.contribution.vfxColor());
            }
            for (var reaction : reactions) {
                allColors.add(reaction.vfxColor());
            }
            if (allColors.isEmpty()) return 0xFF888888;
            int[] arr = new int[allColors.size()];
            for (int i = 0; i < allColors.size(); i++) arr[i] = allColors.get(i);
            return PathEffectComponent.blendColors(arr);
        }

        VfxType getPrimaryVfx(VfxType fallback) {
            for (var reaction : reactions) {
                if (reaction.vfxOverride() != null) return reaction.vfxOverride();
            }
            for (var entry : contributions) {
                if (entry.isPrimary && entry.contribution.vfxOverride() != null) {
                    return entry.contribution.vfxOverride();
                }
            }
            return fallback;
        }
    }

    record ContributionEntry(PathEffectComponent.EffectContribution contribution, boolean isPrimary) {}
}
