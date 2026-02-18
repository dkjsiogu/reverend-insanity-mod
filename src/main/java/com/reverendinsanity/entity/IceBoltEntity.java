package com.reverendinsanity.entity;

import com.reverendinsanity.core.combat.FrostManager;
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

// 冰弹投射物：寒冰蛊催动的冰晶飞弹，命中减速敌人
public class IceBoltEntity extends ThrowableItemProjectile {

    private float damage = 4.0f;
    private int life = 0;

    public IceBoltEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public IceBoltEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.ICE_BOLT.get(), owner, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.COLD_ICE_GU.get();
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
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY(), this.getZ(),
                2, 0.1, 0.1, 0.1, 0.01);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().magic(), damage);
            FrostManager.applySlow(target, 60, 0.6);
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
