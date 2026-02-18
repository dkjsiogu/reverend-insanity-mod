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

// 金光投射物：一气金光虫催动产生的穿透光束
public class GoldBeamEntity extends ThrowableItemProjectile {

    private float damage = 6.0f;
    private int life = 0;
    private int pierceCount = 0;
    private static final int MAX_PIERCE = 3;

    public GoldBeamEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public GoldBeamEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.GOLD_BEAM.get(), owner, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GOLD_LIGHT_WORM.get();
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        if (life > 40) {
            this.discard();
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.getX(), this.getY(), this.getZ(),
                3, 0.05, 0.05, 0.05, 0.02
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity) {
            result.getEntity().hurt(this.damageSources().magic(), damage);
            pierceCount++;
            if (pierceCount >= MAX_PIERCE) {
                this.discard();
            }
        }
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
