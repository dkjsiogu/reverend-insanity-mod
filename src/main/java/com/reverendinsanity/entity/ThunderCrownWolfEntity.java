package com.reverendinsanity.entity;

import com.reverendinsanity.registry.ModEntities;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import javax.annotation.Nullable;

// 雷冠头狼：Boss级闪电野兽，能召唤真正的雷电和电狼小弟
public class ThunderCrownWolfEntity extends Monster {

    private int lightningCooldown = 0;
    private int summonCooldown = 0;

    public ThunderCrownWolfEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 80.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.MOVEMENT_SPEED, 0.38)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
            .add(Attributes.FOLLOW_RANGE, 24.0)
            .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, LightningWolfEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) return;

        if (lightningCooldown > 0) lightningCooldown--;
        if (summonCooldown > 0) summonCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        if (lightningCooldown <= 0) {
            summonLightning(target);
            lightningCooldown = 100;
        }

        if (summonCooldown <= 0) {
            summonWolves();
            summonCooldown = 200;
        }
    }

    private void summonLightning(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        double ox = (this.random.nextDouble() - 0.5) * 6;
        double oz = (this.random.nextDouble() - 0.5) * 6;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
            bolt.moveTo(target.getX() + ox, target.getY(), target.getZ() + oz);
            serverLevel.addFreshEntity(bolt);
        }
    }

    private void summonWolves() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        int count = 2 + this.random.nextInt(2);
        for (int i = 0; i < count; i++) {
            LightningWolfEntity wolf = ModEntities.LIGHTNING_WOLF.get().create(serverLevel);
            if (wolf == null) continue;
            double ox = (this.random.nextDouble() - 0.5) * 8;
            double oz = (this.random.nextDouble() - 0.5) * 8;
            wolf.moveTo(this.getX() + ox, this.getY(), this.getZ() + oz, this.random.nextFloat() * 360, 0);
            serverLevel.addFreshEntity(wolf);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WOLF_GROWL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected int getBaseExperienceReward() {
        return 30;
    }
}
