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

// 月刃投射物：月光蛊催动产生的攻击实体
public class MoonBladeEntity extends ThrowableItemProjectile {

    private float damage = 4.0f;
    private int life = 0;

    public MoonBladeEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public MoonBladeEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.MOON_BLADE.get(), owner, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MOONLIGHT_GU.get();
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
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY(), this.getZ(),
                2, 0.1, 0.1, 0.1, 0.01
            );
        }
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
