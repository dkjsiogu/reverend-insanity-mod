package com.reverendinsanity.entity;

import com.reverendinsanity.registry.ModEntities;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 血弹投射物：血蛊催动的自追踪血弹
public class BloodBoltEntity extends ThrowableItemProjectile {

    private float damage = 6.0f;
    private int life = 0;
    private static final double HOMING_RANGE = 12.0;
    private static final double TURN_SPEED = 0.15;

    public BloodBoltEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public BloodBoltEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.BLOOD_BOLT.get(), owner, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BLOOD_GU.get();
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        if (life > 80) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            LivingEntity target = findNearestTarget();
            if (target != null) {
                Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0)
                    .subtract(this.position()).normalize();
                Vec3 currentVel = this.getDeltaMovement();
                double speed = currentVel.length();
                if (speed < 0.1) speed = 0.8;
                Vec3 newVel = currentVel.normalize().lerp(toTarget, TURN_SPEED).normalize().scale(speed);
                this.setDeltaMovement(newVel);
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
                this.getX(), this.getY(), this.getZ(),
                3, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private LivingEntity findNearestTarget() {
        AABB searchBox = this.getBoundingBox().inflate(HOMING_RANGE);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox,
            e -> e != this.getOwner() && e.isAlive());
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity target : targets) {
            double dist = target.distanceToSqr(this);
            if (dist < closestDist) {
                closestDist = dist;
                closest = target;
            }
        }
        return closest;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity) {
            result.getEntity().hurt(this.damageSources().magic(), damage);
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }
}
