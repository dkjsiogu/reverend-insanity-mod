package com.reverendinsanity.entity;

import com.reverendinsanity.registry.ModEntities;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import com.reverendinsanity.ReverendInsanity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

// 远古蛊仙残魂：秘境守卫BOSS，拥有强大蛊术的远古存在
public class AncientGuImmortalEntity extends Monster {

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Component.literal("\u8fdc\u53e4\u86ca\u4ed9\u6b8b\u9b42"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private int abilityCooldown = 60;
    private boolean hasHealed = false;
    private int currentPhase = 1;
    private int phaseSpeedBoostTicks = 0;
    private static final ResourceLocation PHASE_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "boss_phase_speed");

    private static final List<Item> BOSS_DROPS = List.of(
        ModItems.SILVER_MOON_GU.get(), ModItems.WHITE_JADE_GU.get(),
        ModItems.GOLD_LIGHT_WORM.get(), ModItems.FOUR_FLAVORS_LIQUOR_WORM.get(),
        ModItems.ENSLAVE_SNAKE_GU.get(), ModItems.IRON_BONE_GU.get()
    );

    public AncientGuImmortalEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 300.0)
            .add(Attributes.ATTACK_DAMAGE, 15.0)
            .add(Attributes.MOVEMENT_SPEED, 0.38)
            .add(Attributes.ARMOR, 15.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
            .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) return;

        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (phaseSpeedBoostTicks > 0) {
            phaseSpeedBoostTicks--;
            if (phaseSpeedBoostTicks == 0) {
                AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr != null) speedAttr.removeModifier(PHASE_SPEED_MOD);
            }
        }

        int newPhase = getHealth() > getMaxHealth() * 0.6f ? 1 :
                       getHealth() > getMaxHealth() * 0.3f ? 2 : 3;
        if (newPhase != currentPhase) {
            onPhaseTransition(currentPhase, newPhase);
            currentPhase = newPhase;
        }

        if (abilityCooldown > 0) {
            abilityCooldown--;
        }

        if (!hasHealed && this.getHealth() < this.getMaxHealth() * 0.3f) {
            hasHealed = true;
            this.heal(50.0f);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.5, this.getZ(), 20, 1.0, 1.0, 1.0, 0.1);
            }
            this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 0.5f);
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        if (abilityCooldown > 0) return;

        float roll = this.random.nextFloat();
        int baseCooldown = switch (currentPhase) {
            case 2 -> 20 + random.nextInt(15);
            case 3 -> 15 + random.nextInt(10);
            default -> 30 + random.nextInt(20);
        };

        if (currentPhase == 1) {
            if (roll < 0.25f) {
                shootMoonBladeSpread(target);
                abilityCooldown = baseCooldown;
            } else if (roll < 0.45f) {
                shootGoldBeamBarrage(target);
                abilityCooldown = baseCooldown;
            } else if (roll < 0.60f) {
                teleportBehindTarget(target);
                abilityCooldown = baseCooldown + 10;
            } else if (roll < 0.80f) {
                aoeShockwave();
                abilityCooldown = baseCooldown + 20;
            } else {
                abilityCooldown = 20;
            }
        } else if (currentPhase == 2) {
            if (roll < 0.20f) {
                shootMoonBladeSpread(target);
                abilityCooldown = baseCooldown;
            } else if (roll < 0.35f) {
                shootGoldBeamBarrage(target);
                abilityCooldown = baseCooldown;
            } else if (roll < 0.50f) {
                teleportBehindTarget(target);
                abilityCooldown = baseCooldown + 5;
            } else if (roll < 0.65f) {
                aoeShockwave();
                abilityCooldown = baseCooldown + 10;
            } else if (roll < 0.80f) {
                soulShockwave();
                abilityCooldown = baseCooldown + 5;
            } else if (roll < 0.90f) {
                summonMinions();
                abilityCooldown = baseCooldown + 20;
            } else {
                abilityCooldown = 15;
            }
        } else {
            if (roll < 0.25f) {
                shootMoonBladeSpread(target);
                abilityCooldown = baseCooldown;
            } else if (roll < 0.40f) {
                shootGoldBeamBarrage(target);
                abilityCooldown = baseCooldown;
            } else if (roll < 0.55f) {
                teleportBehindTarget(target);
                abilityCooldown = baseCooldown;
            } else if (roll < 0.70f) {
                aoeShockwave();
                abilityCooldown = baseCooldown + 5;
            } else if (roll < 0.85f) {
                soulShockwave();
                abilityCooldown = baseCooldown;
            } else {
                ancientFormation();
                abilityCooldown = baseCooldown + 10;
            }
        }
    }

    private void onPhaseTransition(int oldPhase, int newPhase) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        if (oldPhase == 1 && newPhase == 2) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 50, 2.0, 2.0, 2.0, 0.05);
            this.level().playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 1.0f);
            AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(PHASE_SPEED_MOD);
                speedAttr.addTransientModifier(new AttributeModifier(PHASE_SPEED_MOD, 0.06, AttributeModifier.Operation.ADD_VALUE));
            }
            phaseSpeedBoostTicks = 600;
        } else if (oldPhase == 2 && newPhase == 3) {
            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.5, this.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 10, 3.0, 2.0, 3.0, 0.1);
            this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.5f, 0.5f);
        }
    }

    private void soulShockwave() {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 1.0, this.getZ(), 40, 4.0, 2.0, 4.0, 0.02);
            sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.0, this.getZ(), 5, 2.0, 0.5, 2.0, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.7f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), 12.0f);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.6, 0));
            entity.hurtMarked = true;
        }
    }

    private void summonMinions() {
        if (!(this.level() instanceof ServerLevel sl)) return;

        for (int i = 0; i < 2; i++) {
            double spawnX = this.getX() + (this.random.nextDouble() - 0.5) * 12.0;
            double spawnZ = this.getZ() + (this.random.nextDouble() - 0.5) * 12.0;
            double spawnY = this.getY();

            Zombie zombie = EntityType.ZOMBIE.create(this.level());
            if (zombie == null) continue;
            zombie.setPos(spawnX, spawnY, spawnZ);
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            zombie.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
            zombie.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
            if (this.getTarget() != null) {
                zombie.setTarget(this.getTarget());
            }
            this.level().addFreshEntity(zombie);

            sl.sendParticles(ParticleTypes.SMOKE, spawnX, spawnY + 1.0, spawnZ, 20, 0.5, 1.0, 0.5, 0.05);
        }
    }

    private void ancientFormation() {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.5, this.getZ(), 60, 5.0, 3.0, 5.0, 0.05);
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 0.5, this.getZ(), 40, 5.0, 1.0, 5.0, 0.02);
            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 2.0, this.getZ(), 3, 1.0, 1.0, 1.0, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.5f, 0.3f);
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0f, 0.5f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), 15.0f);
            double dx = entity.getX() - this.getX();
            double dz = entity.getZ() - this.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.001) {
                entity.knockback(2.0, -dx / dist, -dz / dist);
            }
        }
    }

    private void shootMoonBladeSpread(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) return;
        dx /= len;
        dz /= len;

        double[][] offsets = {{0, 0}, {dz * 0.3, -dx * 0.3}, {-dz * 0.3, dx * 0.3}, {dz * 0.6, -dx * 0.6}, {-dz * 0.6, dx * 0.6}};
        for (double[] off : offsets) {
            MoonBladeEntity blade = new MoonBladeEntity(ModEntities.MOON_BLADE.get(), this.level());
            blade.setOwner(this);
            blade.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
            double sdx = target.getX() + off[0] * 3.0 - this.getX();
            double sdy = target.getEyeY() - this.getEyeY();
            double sdz = target.getZ() + off[1] * 3.0 - this.getZ();
            blade.shoot(sdx, sdy, sdz, 2.0f, 2.0f);
            this.level().addFreshEntity(blade);
        }
    }

    private void shootGoldBeamBarrage(LivingEntity target) {
        for (int i = 0; i < 3; i++) {
            GoldBeamEntity beam = new GoldBeamEntity(ModEntities.GOLD_BEAM.get(), this.level());
            beam.setOwner(this);
            beam.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
            double bx = target.getX() + (this.random.nextDouble() - 0.5) * 2;
            double by = target.getEyeY() - this.getEyeY();
            double bz = target.getZ() + (this.random.nextDouble() - 0.5) * 2;
            beam.shoot(bx, by, bz, 2.5f, 2.0f);
            this.level().addFreshEntity(beam);
        }
    }

    private void teleportBehindTarget(LivingEntity target) {
        double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
        double behindX = target.getX() - Math.cos(angle) * 2.0;
        double behindZ = target.getZ() - Math.sin(angle) * 2.0;

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.1);
        }
        this.teleportTo(behindX, target.getY(), behindZ);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.1);
        }
    }

    private void aoeShockwave() {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 20, 2.0, 1.0, 2.0, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5f, 0.8f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().mobAttack(this), 10.0f);
            double knockX = entity.getX() - this.getX();
            double knockZ = entity.getZ() - this.getZ();
            double dist = Math.sqrt(knockX * knockX + knockZ * knockZ);
            if (dist > 0.001) {
                entity.knockback(1.5f, -knockX / dist, -knockZ / dist);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        for (int i = 0; i < 2; i++) {
            Item drop = BOSS_DROPS.get(this.random.nextInt(BOSS_DROPS.size()));
            this.spawnAtLocation(new ItemStack(drop));
        }

        this.spawnAtLocation(new ItemStack(ModItems.PRIMEVAL_STONE.get(), 5 + this.random.nextInt(6)));
        this.spawnAtLocation(new ItemStack(ModItems.BREAKTHROUGH_STONE.get()));
        this.spawnAtLocation(new ItemStack(ModItems.LORE_SCROLL.get()));
    }

    @Override
    protected int getBaseExperienceReward() {
        return 50;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }
}
