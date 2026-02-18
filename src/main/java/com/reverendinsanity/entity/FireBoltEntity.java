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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

// 火弹投射物：火种蛊催动的炎弹，命中引燃敌人
public class FireBoltEntity extends ThrowableItemProjectile {

    private float damage = 5.0f;
    private int fireTicks = 100;
    private int life = 0;

    public FireBoltEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public FireBoltEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.FIRE_BOLT.get(), owner, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    public FireBoltEntity(Level level, LivingEntity owner, float damage, int fireTicks) {
        super(ModEntities.FIRE_BOLT.get(), owner, level);
        this.damage = damage;
        this.fireTicks = fireTicks;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FIRE_SEED_GU.get();
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        if (life > 60) {
            this.discard();
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY(), this.getZ(),
                3, 0.1, 0.1, 0.1, 0.02);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().magic(), damage);
            target.igniteForTicks(fireTicks);
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
