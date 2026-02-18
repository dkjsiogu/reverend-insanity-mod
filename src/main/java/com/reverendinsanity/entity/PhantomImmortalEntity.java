package com.reverendinsanity.entity;

import com.reverendinsanity.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

// 幻影蛊仙：红莲魔尊"前有古人"杀招召唤的历史蛊仙幻影，临时战斗盟友
public class PhantomImmortalEntity extends PathfinderMob {

    public enum ImmortalType {
        QI_MASTER, SWORD_SAINT, SOUL_REAPER, STAR_SAGE, BLOOD_DEMON
    }

    private UUID ownerUUID;
    private ImmortalType immortalType = ImmortalType.QI_MASTER;
    private int lifeTicks = 0;
    private static final int MAX_LIFE = 200;

    @SuppressWarnings("unchecked")
    public PhantomImmortalEntity(EntityType<?> type, Level level) {
        super((EntityType<? extends PathfinderMob>) type, level);
    }

    public PhantomImmortalEntity(Level level, Entity owner, ImmortalType type) {
        super(ModEntities.PHANTOM_IMMORTAL.get(), level);
        this.ownerUUID = owner.getUUID();
        this.immortalType = type;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.ATTACK_DAMAGE, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.40)
            .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
            target -> target instanceof Player p && !p.getUUID().equals(ownerUUID)));
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (lifeTicks > MAX_LIFE) {
            spawnDissipateEffect();
            this.discard();
            return;
        }

        if (!(this.level() instanceof ServerLevel sl)) return;

        if (lifeTicks == 1) {
            spawnEntranceEffect(sl);
        }

        if (lifeTicks % 10 == 0) {
            spawnAmbientParticles(sl);
        }

        if (lifeTicks % 20 == 0) {
            specialAttack(sl);
        }
    }

    private void spawnEntranceEffect(ServerLevel sl) {
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.0, getZ(), 1, 0, 0, 0, 0);
        switch (immortalType) {
            case QI_MASTER -> sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 30, 1.0, 1.5, 1.0, 0.05);
            case SWORD_SAINT -> sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.0, getZ(), 30, 1.0, 1.5, 1.0, 0.3);
            case SOUL_REAPER -> sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.0, getZ(), 30, 1.0, 1.5, 1.0, 0.05);
            case STAR_SAGE -> sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 30, 1.0, 1.5, 1.0, 0.05);
            case BLOOD_DEMON -> sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, getX(), getY() + 1.0, getZ(), 30, 1.0, 1.5, 1.0, 0.1);
        }
        sl.playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 0.8f);
    }

    private void spawnAmbientParticles(ServerLevel sl) {
        switch (immortalType) {
            case QI_MASTER -> sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 5, 0.3, 0.5, 0.3, 0.01);
            case SWORD_SAINT -> sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.0, getZ(), 5, 0.3, 0.5, 0.3, 0.1);
            case SOUL_REAPER -> sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.0, getZ(), 5, 0.3, 0.5, 0.3, 0.01);
            case STAR_SAGE -> sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 5, 0.3, 0.5, 0.3, 0.01);
            case BLOOD_DEMON -> sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, getX(), getY() + 1.0, getZ(), 5, 0.3, 0.5, 0.3, 0.03);
        }
    }

    private void specialAttack(ServerLevel sl) {
        LivingEntity target = getTarget();

        switch (immortalType) {
            case QI_MASTER -> {
                if (target == null || !target.isAlive()) return;
                List<LivingEntity> nearby = sl.getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(6.0), e -> e != this && isValidTarget(e));
                for (LivingEntity e : nearby) {
                    e.hurt(damageSources().mobAttack(this), 5.0f);
                }
                if (!nearby.isEmpty()) {
                    sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 20, 3.0, 1.0, 3.0, 0.02);
                }
            }
            case SWORD_SAINT -> {
                if (target != null && target.isAlive() && distanceTo(target) < 5.0) {
                    target.hurt(damageSources().mobAttack(this), 12.0f);
                    sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.5, 0.5, 0.3);
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1.0, getZ(), 3, 0.5, 0.3, 0.5, 0.0);
                }
            }
            case SOUL_REAPER -> {
                if (target != null && target.isAlive() && distanceTo(target) < 8.0) {
                    target.hurt(damageSources().magic(), 8.0f);
                    heal(4.0f);
                    sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.5, 0.5, 0.5, 0.02);
                }
            }
            case STAR_SAGE -> {
                if (target != null && target.isAlive()) {
                    GoldBeamEntity beam = new GoldBeamEntity(sl, this, 10.0f);
                    double dx = target.getX() - getX();
                    double dy = target.getEyeY() - getEyeY();
                    double dz = target.getZ() - getZ();
                    beam.shoot(dx, dy, dz, 1.5f, 1.0f);
                    sl.addFreshEntity(beam);
                }
            }
            case BLOOD_DEMON -> {
                if (target != null && target.isAlive() && distanceTo(target) < 6.0) {
                    setHealth(getHealth() - 5.0f);
                    float trueDmg = target.getMaxHealth() * 0.05f;
                    target.hurt(damageSources().magic(), trueDmg);
                    sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
                }
            }
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity instanceof PhantomImmortalEntity) return false;
        if (entity instanceof Player p && ownerUUID != null && p.getUUID().equals(ownerUUID)) return false;
        if (entity instanceof Monster) return true;
        if (entity instanceof Player) return true;
        return false;
    }

    private void spawnDissipateEffect() {
        if (!(level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 40, 0.8, 1.2, 0.8, 0.03);
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 20, 0.5, 1.0, 0.5, 0.5);
        sl.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2f, 0.6f);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    public ImmortalType getImmortalType() {
        return immortalType;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putString("ImmortalType", immortalType.name());
        tag.putInt("LifeTicks", lifeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        if (tag.contains("ImmortalType")) {
            try { immortalType = ImmortalType.valueOf(tag.getString("ImmortalType")); }
            catch (Exception e) { immortalType = ImmortalType.QI_MASTER; }
        }
        lifeTicks = tag.getInt("LifeTicks");
    }
}
