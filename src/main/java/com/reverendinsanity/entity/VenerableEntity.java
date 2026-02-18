package com.reverendinsanity.entity;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModEntities;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// 十大九转尊者Boss实体：蛊真人世界观中最强存在，各具独特战斗AI
public class VenerableEntity extends Monster {

    private static final EntityDataAccessor<String> DATA_TYPE =
        SynchedEntityData.defineId(VenerableEntity.class, EntityDataSerializers.STRING);

    private static final ResourceLocation VEN_HP_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_hp");
    private static final ResourceLocation VEN_ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_atk");
    private static final ResourceLocation VEN_ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_armor");
    private static final ResourceLocation VEN_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_speed");
    private static final ResourceLocation PHASE_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_phase_speed");
    private static final ResourceLocation LAW_SLOW_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_law_slow");
    private static final ResourceLocation FORMATION_ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_formation_armor");
    private static final ResourceLocation SAVAGE_BOOST_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_savage_boost");
    private static final ResourceLocation SAVAGE_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_savage_speed");
    private static final ResourceLocation KUANGMAN_ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_kuangman_atk");
    private static final ResourceLocation KUANGMAN_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "venerable_kuangman_speed");

    private static final List<Item> RARE_GU_DROPS = List.of(
        ModItems.SILVER_MOON_GU.get(), ModItems.WHITE_JADE_GU.get(),
        ModItems.HEAVENS_EYE_GU.get(), ModItems.GOLD_LIGHT_WORM.get(),
        ModItems.FOUR_FLAVORS_LIQUOR_WORM.get(), ModItems.IRON_BONE_GU.get(),
        ModItems.ENSLAVE_SNAKE_GU.get(), ModItems.GIANT_STRENGTH_GU.get()
    );

    private final ServerBossEvent bossEvent = new ServerBossEvent(
        Component.literal("尊者"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private VenerableType venerableType = VenerableType.YUAN_SHI;
    private int abilityCooldown = 80;
    private int currentPhase = 1;
    private int phaseSpeedBoostTicks = 0;

    private boolean hasUsedCicada = false;
    private float savedHP = -1;
    private int savedHPTimer = 0;

    private int yuanLianRegenTick = 0;
    private boolean yuanLianLotusReviveUsed1 = false;
    private boolean yuanLianLotusReviveUsed2 = false;
    private boolean yuanLianLotusReviveUsed3 = false;
    private int leTuHitCount = 0;
    private boolean leTuAggro = false;
    private int invisTicks = 0;

    private final List<ResourceLocation> equippedGu = new ArrayList<>();
    private final List<KillerMove> availableMoves = new ArrayList<>();
    private int killerMoveCooldown = 0;

    private int lotusSporesTicks = 0;
    private double lotusSporesX = 0;
    private double lotusSporesY = 0;
    private double lotusSporesZ = 0;

    private int lawFreezeTicks = 0;
    private int timeSlowTicks = 0;

    private int comboStep = 0;
    private int enslavedCount = 0;
    private int meleeComboCount = 0;
    private int visibleTimer = 0;
    private boolean cicadaEnraged = false;

    private int yuanShiQiWallTicks = 0;
    private int triQiComboStep = 0;

    private final List<double[]> starTraps = new ArrayList<>();
    private int starTrapCooldown = 0;
    private int starNeedleCooldown = 0;

    private int juYangFortuneFieldTicks = 0;

    private int leTuDomainTicks = 0;
    private int soulBeastCount = 0;
    private int leTuMercyTicks = 0;
    private boolean leTuFormationActive = false;
    private int leTuFormationTicks = 0;

    private int wuJiMadnessLayers = 0;
    private int wuJiLawMarkTicks = 0;
    private LivingEntity wuJiLawTarget = null;
    private boolean wuJiImmortalUsed1 = false;
    private boolean wuJiImmortalUsed2 = false;

    private enum KuangManForm { HUMAN, ICE_PHOENIX, LIGHTNING_WOLF, GIANT }
    private KuangManForm currentForm = KuangManForm.HUMAN;
    private int formDurationTicks = 0;
    private int formCooldownTicks = 0;

    private int daoTianStealCount = 0;

    private int youHunGazeCooldown = 0;
    private boolean youHunSoulSplitUsed = false;
    private int youHunSoulBeastCount = 0;

    private int hongLianTimeFreezeTicks = 0;
    private int predecessorCooldown = 0;

    private int absoluteDefenseTicks = 0;
    private boolean absoluteDefenseUsed = false;

    public VenerableEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.bossEvent.setVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 1500.0)
            .add(Attributes.ATTACK_DAMAGE, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.ARMOR, 20.0)
            .add(Attributes.ARMOR_TOUGHNESS, 10.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
            .add(Attributes.FOLLOW_RANGE, 50.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, VenerableType.YUAN_SHI.name());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public void setVenerableType(VenerableType type) {
        this.venerableType = type;
        this.entityData.set(DATA_TYPE, type.name());
        this.bossEvent.setName(Component.literal(type.displayNameCN));
        this.bossEvent.setColor(type.isDemon() ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.BLUE);
        applyVenerableStats(type);
        selectVenerableEquipment(type);
        matchVenerableMoves();
    }

    public VenerableType getVenerableType() {
        return this.venerableType;
    }

    public String getVenerableTypeName() {
        return this.entityData.get(DATA_TYPE);
    }

    private void applyVenerableStats(VenerableType type) {
        applyMod(Attributes.MAX_HEALTH, VEN_HP_MOD, type.maxHealth - 1500.0);
        applyMod(Attributes.ATTACK_DAMAGE, VEN_ATK_MOD, type.attackDamage - 30.0);
        applyMod(Attributes.ARMOR, VEN_ARMOR_MOD, type.armor - 20.0);
        applyMod(Attributes.MOVEMENT_SPEED, VEN_SPEED_MOD, type.moveSpeed - 0.35);
        this.setHealth(this.getMaxHealth());
    }

    private void applyMod(Holder<Attribute> attr, ResourceLocation id, double value) {
        AttributeInstance inst = this.getAttribute(attr);
        if (inst != null) {
            inst.removeModifier(id);
            if (Math.abs(value) > 0.001) {
                inst.addTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    // ======================== 主循环 ========================

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) return;

        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        tickPhaseSpeed();
        tickInvisibility();
        tickAuraEffects();
        tickLotusSpores();
        tickLawFreeze();
        tickTimeSlow();
        tickLeTuDomain();
        tickLeTuFormation();
        tickLeTuMercy();
        tickWuJiLawMark();
        tickKuangManForm();
        tickYouHunGaze();
        tickJuYangFortuneField();
        tickHongLianTimeFreeze();
        tickStarTraps();
        tickAbsoluteDefense();

        if (predecessorCooldown > 0) predecessorCooldown--;

        if (venerableType == VenerableType.HONG_LIAN) {
            tickSavedHP();
        }

        if (venerableType == VenerableType.DAO_TIAN && !this.isInvisible()) {
            visibleTimer++;
            if (visibleTimer >= 60 && this.getTarget() != null) {
                this.setInvisible(true);
                invisTicks = 80;
                visibleTimer = 0;
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 30, 0.5, 1.0, 0.5, 0.1);
                }
            }
        }

        int newPhase = getHealth() > getMaxHealth() * 0.6f ? 1 :
                       getHealth() > getMaxHealth() * 0.3f ? 2 : 3;
        if (newPhase != currentPhase) {
            onPhaseTransition(currentPhase, newPhase);
            currentPhase = newPhase;
        }

        if (venerableType == VenerableType.YUAN_LIAN) {
            yuanLianRegenTick++;
            float regenRate = currentPhase == 1 ? 0.03f : currentPhase == 2 ? 0.05f : 0.08f;
            if (yuanLianRegenTick >= 60) {
                yuanLianRegenTick = 0;
                this.heal(this.getMaxHealth() * regenRate);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.5, getZ(), 8, 0.5, 0.5, 0.5, 0.02);
                }
            }
        }

        if (venerableType == VenerableType.XING_XIU && this.tickCount % 400 == 0 && this.getTarget() != null) {
            placeStarTrapAtPredictedPosition();
        }

        if (abilityCooldown > 0) {
            abilityCooldown--;
        }
        if (killerMoveCooldown > 0) killerMoveCooldown--;
        if (formCooldownTicks > 0) formCooldownTicks--;
        if (starTrapCooldown > 0) starTrapCooldown--;
        if (starNeedleCooldown > 0) starNeedleCooldown--;
        if (youHunGazeCooldown > 0) youHunGazeCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        if (abilityCooldown > 0) return;

        performVenerableAttack(target);
    }

    private void tickLeTuDomain() {
        if (leTuDomainTicks <= 0) return;
        leTuDomainTicks--;
        if (this.tickCount % 20 != 0) return;
        if (!(this.level() instanceof ServerLevel sl)) return;

        double range = currentPhase == 3 ? 24.0 : 12.0;
        float dmgPerSec = currentPhase == 3 ? 4.0f : 2.0f;
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 0.5, getZ(), 15, range * 0.4, 1.0, range * 0.4, 0.02);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.3, getZ(), 10, range * 0.4, 0.2, range * 0.4, 0.01);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range),
            e -> e != this && !(e instanceof Zombie z && "战魂".equals(z.getName().getString())));
        for (LivingEntity entity : nearby) {
            if (entity instanceof Player) {
                entity.hurt(this.damageSources().magic(), dmgPerSec);
            }
        }
    }

    private void tickLeTuFormation() {
        if (!leTuFormationActive) return;
        if (leTuFormationTicks > 0) {
            leTuFormationTicks--;
        } else {
            leTuFormationActive = false;
        }
    }

    private void tickLeTuMercy() {
        if (leTuMercyTicks > 0) leTuMercyTicks--;
    }

    private void tickWuJiLawMark() {
        if (wuJiLawMarkTicks > 0) {
            wuJiLawMarkTicks--;
            if (wuJiLawTarget != null && (!wuJiLawTarget.isAlive() || wuJiLawMarkTicks == 0)) {
                if (wuJiLawTarget instanceof Player player) {
                    AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speed != null) speed.removeModifier(LAW_SLOW_MOD);
                    AttributeInstance atkSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
                    if (atkSpeed != null) atkSpeed.removeModifier(LAW_SLOW_MOD);
                }
                wuJiLawTarget = null;
            }
        }
    }

    private void tickKuangManForm() {
        if (currentForm == KuangManForm.HUMAN) return;
        if (formDurationTicks > 0) {
            formDurationTicks--;
            if (formDurationTicks == 0) {
                revertToHumanForm();
            }
        }
    }

    private void tickYouHunGaze() {
        if (venerableType != VenerableType.YOU_HUN) return;
        if (youHunGazeCooldown <= 0 && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double dist = this.distanceTo(target);
            if (dist < 12.0) {
                Vec3 look = this.getLookAngle();
                Vec3 toTarget = target.position().subtract(this.position()).normalize();
                double dot = look.dot(toTarget);
                if (dot > 0.7) {
                    target.setDeltaMovement(target.getDeltaMovement().multiply(0.05, 0.5, 0.05));
                    target.hurtMarked = true;
                    youHunGazeCooldown = 200;
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 30, 0.5, 1.0, 0.5, 0.03);
                        this.level().playSound(null, target.blockPosition(), SoundEvents.WARDEN_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.2f);
                    }
                }
            }
        }
    }

    private void tickJuYangFortuneField() {
        if (juYangFortuneFieldTicks <= 0) return;
        juYangFortuneFieldTicks--;
        if (this.tickCount % 20 != 0) return;
        if (!(this.level() instanceof ServerLevel sl)) return;

        sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.3, getZ(), 15, 4.0, 0.2, 4.0, 0.005);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.5, getZ(), 8, 4.0, 0.3, 4.0, 0.01);
    }

    private void tickHongLianTimeFreeze() {
        if (hongLianTimeFreezeTicks <= 0) return;
        hongLianTimeFreezeTicks--;
        if (this.tickCount % 5 != 0) return;
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
        }
    }

    private void tickStarTraps() {
        if (starTraps.isEmpty()) return;
        LivingEntity target = this.getTarget();
        if (target == null) return;

        var it = starTraps.iterator();
        while (it.hasNext()) {
            double[] pos = it.next();
            double dx = target.getX() - pos[0];
            double dz = target.getZ() - pos[2];
            if (dx * dx + dz * dz < 9.0) {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLASH, pos[0], pos[1] + 1.0, pos[2], 3, 0.5, 0.5, 0.5, 0.0);
                    sl.sendParticles(ParticleTypes.END_ROD, pos[0], pos[1] + 0.5, pos[2], 40, 2.0, 2.0, 2.0, 0.1);
                    this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5f, 1.0f);
                }
                target.hurt(this.damageSources().magic(), target.getMaxHealth() * 0.08f);
                it.remove();
            }
        }
    }

    private void performVenerableAttack(LivingEntity target) {
        int baseCooldown = switch (currentPhase) {
            case 2 -> 30 + random.nextInt(20);
            case 3 -> 20 + random.nextInt(15);
            default -> 50 + random.nextInt(30);
        };

        if (cicadaEnraged) {
            baseCooldown = baseCooldown / 2;
        }

        double distance = this.distanceTo(target);

        if (killerMoveCooldown <= 0 && !availableMoves.isEmpty()) {
            float moveChance = switch (currentPhase) {
                case 2 -> 0.30f;
                case 3 -> 0.45f;
                default -> 0.15f;
            };
            if (this.random.nextFloat() < moveChance) {
                executeVenerableKillerMove(target);
                return;
            }
        }

        switch (venerableType) {
            case YUAN_SHI -> aiYuanShi(target, baseCooldown, distance);
            case XING_XIU -> aiXingXiu(target, baseCooldown, distance);
            case YUAN_LIAN -> aiYuanLian(target, baseCooldown, distance);
            case WU_JI -> aiWuJi(target, baseCooldown, distance);
            case KUANG_MAN -> aiKuangMan(target, baseCooldown, distance);
            case DAO_TIAN -> aiDaoTian(target, baseCooldown, distance);
            case JU_YANG -> aiJuYang(target, baseCooldown, distance);
            case YOU_HUN -> aiYouHun(target, baseCooldown, distance);
            case LE_TU -> aiLeTu(target, baseCooldown, distance);
            case HONG_LIAN -> aiHongLian(target, baseCooldown, distance);
        }
    }

    // ======================== 元始仙尊 - 气道+奴道 - 至高霸主 ========================

    private void aiYuanShi(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3 && triQiComboStep == 0) {
            triQiHumanQi(target);
            triQiComboStep = 1;
            abilityCooldown = 15;
            return;
        }
        if (currentPhase == 3 && triQiComboStep == 1) {
            triQiEarthQi(target);
            triQiComboStep = 2;
            abilityCooldown = 15;
            return;
        }
        if (currentPhase == 3 && triQiComboStep == 2) {
            triQiHeavenQi(target);
            triQiComboStep = 0;
            abilityCooldown = baseCooldown;
            return;
        }

        if (distance > 10) {
            int count = currentPhase >= 2 ? 7 : 5;
            qiBarrage(target, count);
            abilityCooldown = baseCooldown;
        } else if (distance > 5) {
            if (enslavedCount < 3) {
                enslaveNearby();
                abilityCooldown = baseCooldown + 20;
            } else {
                qiSuppression();
                abilityCooldown = baseCooldown;
            }
        } else {
            qiSuppression();
            abilityCooldown = baseCooldown;
        }
    }

    private void triQiHumanQi(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 60, 5.0, 2.0, 5.0, 0.05);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.5, getZ(), 30, 5.0, 1.0, 5.0, 0.02);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.6f);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), entity.getMaxHealth() * 0.08f);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3, 1.0, 0.3));
            entity.hurtMarked = true;
        }
        for (LivingEntity entity : nearby) {
            if (entity instanceof ServerPlayer sp) {
                VfxHelper.spawn(sp, VfxType.DOME_FIELD, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFF0F0FF, 2.0f, 30);
            }
        }
    }

    private void triQiEarthQi(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double tx = target.getX();
        double tz = target.getZ();
        sl.sendParticles(ParticleTypes.CLOUD, tx, target.getY() + 0.3, tz, 40, 3.0, 0.3, 3.0, 0.02);
        sl.sendParticles(ParticleTypes.CRIT, tx, target.getY() + 0.5, tz, 20, 3.0, 0.5, 3.0, 0.1);
        this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5f, 0.7f);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), entity.getMaxHealth() * 0.10f);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.8, 0));
            entity.hurtMarked = true;
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.PULSE_WAVE, tx, target.getY() + 1, tz, 0, 1, 0, 0xFF8B4513, 2.0f, 30);
        }
    }

    private void triQiHeavenQi(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double tx = target.getX();
        double ty = target.getY();
        double tz = target.getZ();
        sl.sendParticles(ParticleTypes.END_ROD, tx, ty + 8.0, tz, 60, 1.0, 4.0, 1.0, 0.1);
        sl.sendParticles(ParticleTypes.FLASH, tx, ty + 2.0, tz, 5, 1.0, 1.0, 1.0, 0.0);
        sl.sendParticles(ParticleTypes.CLOUD, tx, ty + 1.0, tz, 80, 6.0, 3.0, 6.0, 0.08);
        this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.5f, 0.3f);
        this.level().playSound(null, target.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.4f);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), entity.getMaxHealth() * 0.15f);
            double knockDx = entity.getX() - tx;
            double knockDz = entity.getZ() - tz;
            double dist = Math.sqrt(knockDx * knockDx + knockDz * knockDz);
            if (dist > 0.001) {
                entity.knockback(3.0, -knockDx / dist, -knockDz / dist);
            }
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SKY_STRIKE, tx, ty, tz, 0, 1, 0, 0xFFFFD700, 2.5f, 40);
            VfxHelper.spawn(sp, VfxType.PULSE_WAVE, tx, ty + 1, tz, 0, 1, 0, 0xFFF0F0FF, 2.0f, 30);
        }
    }

    private void qiSuppression() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 60, 4.0, 2.0, 4.0, 0.02);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.5, getZ(), 30, 4.0, 0.5, 4.0, 0.01);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.5f, 0.5f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), 3.0f);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3, 1.0, 0.3));
            entity.hurtMarked = true;
        }
        for (LivingEntity entity : nearby) {
            if (entity instanceof ServerPlayer sp) {
                VfxHelper.spawn(sp, VfxType.AURA_RING, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFF0F0FF, 2.0f, 30);
            }
        }
    }

    private void enslaveNearby() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 2.0, getZ(), 40, 6.0, 2.0, 6.0, 0.03);
        this.level().playSound(null, blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2.0f, 0.7f);

        LivingEntity playerTarget = this.getTarget();
        if (playerTarget == null) return;

        enslavedCount = 0;
        List<Mob> nearby = this.level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(16.0),
            e -> e != this);
        for (Mob mob : nearby) {
            mob.setTarget(playerTarget);
            enslavedCount++;
        }
    }

    private void qiBarrage(LivingEntity target, int count) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.5f, 1.2f);

        for (int i = 0; i < count; i++) {
            double spread = (i - count / 2.0) * 1.5;
            double hitX = target.getX() + spread * 0.5 + (random.nextDouble() - 0.5);
            double hitZ = target.getZ() + spread * 0.5 + (random.nextDouble() - 0.5);

            sl.sendParticles(ParticleTypes.CLOUD, getX(), getEyeY(), getZ(), 5, 0.3, 0.3, 0.3, 0.05);
            double steps = 8;
            double dx = (hitX - getX()) / steps;
            double dz = (hitZ - getZ()) / steps;
            for (int p = 0; p < steps; p++) {
                sl.sendParticles(ParticleTypes.CLOUD,
                    getX() + dx * p, getEyeY() + (random.nextDouble() - 0.5) * 0.5, getZ() + dz * p,
                    2, 0.1, 0.1, 0.1, 0.01);
            }
        }

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
            target.getBoundingBox().inflate(3.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), 12.0f);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.3, 0));
            entity.hurtMarked = true;
        }
        if (target instanceof ServerPlayer sp) {
            double dirX = target.getX() - getX();
            double dirY = target.getEyeY() - getEyeY();
            double dirZ = target.getZ() - getZ();
            VfxHelper.spawn(sp, VfxType.ENERGY_BEAM, getX(), getEyeY(), getZ(), (float) dirX, (float) dirY, (float) dirZ, 0xFFF0F0FF, 1.5f, 20);
        }
    }

    // ======================== 星宿仙尊 - 智道+星道 - 算计大师 ========================

    private void aiXingXiu(LivingEntity target, int baseCooldown, double distance) {
        if (distance < 8) {
            wisdomDodge();
            abilityCooldown = 10;
            return;
        }

        if (currentPhase == 3) {
            stellarAnnihilation(target);
            abilityCooldown = baseCooldown + 20;
            return;
        }

        if (random.nextFloat() < 0.3f && starNeedleCooldown <= 0) {
            starNeedlePinning(target);
            starNeedleCooldown = 80;
            abilityCooldown = baseCooldown / 2;
            return;
        }

        if (random.nextFloat() < 0.35f) {
            heavenlyPrediction(target);
            abilityCooldown = baseCooldown;
            return;
        }

        int count = currentPhase >= 2 ? 5 : 3;
        starProjectiles(target, count);
        abilityCooldown = baseCooldown;
    }

    private void starProjectiles(LivingEntity target, int count) {
        this.level().playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 2.0f, 1.5f);
        for (int i = 0; i < count; i++) {
            GoldBeamEntity beam = new GoldBeamEntity(this.level(), this, 10.0f);
            beam.setPos(getX(), getEyeY() - 0.1, getZ());
            double bx = target.getX() + (random.nextDouble() - 0.5) * 3;
            double by = target.getEyeY() - getEyeY();
            double bz = target.getZ() + (random.nextDouble() - 0.5) * 3;
            beam.shoot(bx, by, bz, 2.5f, 1.5f);
            this.level().addFreshEntity(beam);
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF6699FF, 1.5f, 20);
        }
    }

    private void heavenlyPrediction(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        placeStarTrapAtPredictedPosition();
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 2.5, getZ(), 40, 2.0, 2.0, 2.0, 0.08);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 3.0, getZ(), 15, 1.0, 0.5, 1.0, 0.02);
        this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 1.8f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.DOME_FIELD, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF6699FF, 2.0f, 30);
        }

        for (double[] trap : starTraps) {
            double dist = Math.sqrt(Math.pow(target.getX() - trap[0], 2) + Math.pow(target.getZ() - trap[2], 2));
            if (dist < 3.0) {
                sl.sendParticles(ParticleTypes.FLASH, trap[0], trap[1] + 1.0, trap[2], 3, 0.5, 0.5, 0.5, 0.0);
                sl.sendParticles(ParticleTypes.END_ROD, trap[0], trap[1], trap[2], 30, 1.5, 2.0, 1.5, 0.08);
                target.hurt(this.damageSources().magic(), target.getMaxHealth() * 0.08f);
                target.setDeltaMovement(target.getDeltaMovement().multiply(0.1, 0.5, 0.1));
                target.hurtMarked = true;
                this.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 2.0f, 0.5f);
            }
        }
    }

    private void starNeedlePinning(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.level().playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 2.0f, 2.0f);

        GoldBeamEntity needle = new GoldBeamEntity(this.level(), this, 15.0f);
        needle.setPos(getX(), getEyeY() - 0.1, getZ());
        double bx = target.getX() - getX();
        double by = target.getEyeY() - getEyeY();
        double bz = target.getZ() - getZ();
        needle.shoot(bx, by, bz, 3.5f, 0.5f);
        this.level().addFreshEntity(needle);

        sl.sendParticles(ParticleTypes.END_ROD, getX(), getEyeY(), getZ(), 15, 0.2, 0.2, 0.2, 0.05);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.ENERGY_BEAM, getX(), getEyeY(), getZ(), (float) bx, (float) by, (float) bz, 0xFF6699FF, 1.5f, 20);
        }
    }

    private void placeStarTrapAtPredictedPosition() {
        if (starTraps.size() >= 3) return;
        LivingEntity target = this.getTarget();
        if (target == null) return;
        Vec3 velocity = target.getDeltaMovement();
        double predX = target.getX() + velocity.x * 40;
        double predZ = target.getZ() + velocity.z * 40;
        starTraps.add(new double[]{predX, target.getY(), predZ});
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, predX, target.getY() + 0.2, predZ, 8, 0.3, 0.1, 0.3, 0.01);
        }
    }

    private void stellarAnnihilation(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 3.0, getZ(), 8, 2.0, 2.0, 2.0, 0.0);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 120, 10.0, 4.0, 10.0, 0.1);
        sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 40, 5.0, 2.0, 5.0, 0.05);
        this.level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.5f, 0.5f);
        this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 0.3f);

        for (double[] trap : starTraps) {
            sl.sendParticles(ParticleTypes.FLASH, trap[0], trap[1] + 1.0, trap[2], 3, 0.5, 0.5, 0.5, 0.0);
            sl.sendParticles(ParticleTypes.END_ROD, trap[0], trap[1], trap[2], 40, 2.0, 2.0, 2.0, 0.1);
            List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().move(trap[0] - target.getX(), trap[1] - target.getY(), trap[2] - target.getZ()).inflate(4.0),
                e -> e != this);
            for (LivingEntity entity : nearby) {
                entity.hurt(this.damageSources().magic(), entity.getMaxHealth() * 0.10f);
            }
        }
        starTraps.clear();

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            float damage = entity.getMaxHealth() * 0.15f;
            entity.hurt(this.damageSources().magic(), damage);
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SKY_STRIKE, getX(), getY(), getZ(), 0, 1, 0, 0xFF6699FF, 2.5f, 40);
            VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF6699FF, 2.0f, 30);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (venerableType == VenerableType.XING_XIU) {
            float dodgeChance = currentPhase >= 2 ? 0.40f : 0.30f;
            if (random.nextFloat() < dodgeChance && source.getEntity() instanceof LivingEntity) {
                wisdomDodge();
                return false;
            }
        }
        if (venerableType == VenerableType.JU_YANG) {
            if (random.nextFloat() < 0.30f && source.getEntity() instanceof LivingEntity) {
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 15, 0.5, 1.0, 0.5, 0.05);
                }
                return false;
            }
        }
        if (venerableType == VenerableType.LE_TU && absoluteDefenseTicks > 0) {
            return false;
        }
        if (venerableType == VenerableType.LE_TU && !leTuAggro && source.getEntity() instanceof Player) {
            leTuHitCount++;
            if (leTuHitCount >= 3) {
                leTuAggro = true;
            }
        }
        if (venerableType == VenerableType.LE_TU && leTuFormationActive) {
            amount *= 0.70f;
        }
        if (venerableType == VenerableType.LE_TU && currentPhase == 3 && !absoluteDefenseUsed
            && this.getHealth() - amount < this.getMaxHealth() * 0.15f && this.getHealth() > this.getMaxHealth() * 0.15f) {
            absoluteDefenseUsed = true;
            absoluteDefenseTicks = 60;
            this.heal(this.getMaxHealth() * 0.05f);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 5, 1.0, 1.0, 1.0, 0.0);
                sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 60, 3.0, 3.0, 3.0, 0.05);
            }
            this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.5f, 0.5f);
            if (source.getEntity() instanceof ServerPlayer sp) {
                VfxHelper.spawn(sp, VfxType.DOME_FIELD, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFFFFFFF, 3.0f, 60);
            }
            return false;
        }
        if (venerableType == VenerableType.YUAN_SHI && random.nextFloat() < 0.35f) {
            amount *= 0.50f;
            yuanShiQiWallTicks = 100;
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 30, 2.0, 2.0, 2.0, 0.02);
            }
        }
        if (venerableType == VenerableType.WU_JI) {
            if ((currentPhase == 1 && !wuJiImmortalUsed1 && this.getHealth() - amount <= 0) ||
                (currentPhase == 2 && !wuJiImmortalUsed2 && this.getHealth() - amount <= 0)) {
                if (currentPhase == 1) wuJiImmortalUsed1 = true;
                else wuJiImmortalUsed2 = true;
                this.setHealth(this.getMaxHealth() * 0.20f);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.0, getZ(), 60, 3.0, 3.0, 3.0, 0.1);
                    this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.5f);
                }
                return false;
            }
        }

        if (venerableType == VenerableType.HONG_LIAN && !hasUsedCicada && (this.getHealth() - amount) <= 0) {
            super.hurt(source, 0);
            springAutumnCicada();
            return false;
        }

        boolean result = super.hurt(source, amount);

        if (result && venerableType == VenerableType.JU_YANG && currentPhase >= 2 && random.nextFloat() < 0.20f) {
            if (source.getEntity() instanceof LivingEntity attacker) {
                List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(8.0), e -> e != this && e != attacker);
                if (!nearby.isEmpty()) {
                    LivingEntity deflectTarget = nearby.get(random.nextInt(nearby.size()));
                    deflectTarget.hurt(this.damageSources().magic(), amount * 0.5f);
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.END_ROD, deflectTarget.getX(), deflectTarget.getY() + 1.0, deflectTarget.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
                    }
                }
            }
        }

        if (result && venerableType == VenerableType.YUAN_LIAN) {
            boolean canRevive = (currentPhase == 1 && !yuanLianLotusReviveUsed1) ||
                                (currentPhase == 2 && !yuanLianLotusReviveUsed2) ||
                                (currentPhase == 3 && !yuanLianLotusReviveUsed3);
            if (canRevive && this.getHealth() < this.getMaxHealth() * 0.10f) {
                if (currentPhase == 1) yuanLianLotusReviveUsed1 = true;
                else if (currentPhase == 2) yuanLianLotusReviveUsed2 = true;
                else yuanLianLotusReviveUsed3 = true;
                this.heal(this.getMaxHealth() * 0.30f);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.0, getZ(), 40, 2.0, 2.0, 2.0, 0.05);
                    sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 2.0, getZ(), 10, 1.0, 1.0, 1.0, 0.05);
                    this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 1.5f);
                }
            }
        }

        return result;
    }

    private void wisdomDodge() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 20, 0.5, 1.0, 0.5, 0.1);

        double angle;
        double dist;
        LivingEntity target = this.getTarget();
        if (venerableType == VenerableType.XING_XIU && target != null) {
            double dx = getX() - target.getX();
            double dz = getZ() - target.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.001) {
                angle = Math.atan2(dz, dx) + (random.nextDouble() - 0.5) * 0.6;
            } else {
                angle = random.nextDouble() * Math.PI * 2;
            }
            dist = 8.0 + random.nextDouble() * 4.0;
        } else {
            angle = random.nextDouble() * Math.PI * 2;
            dist = 2.0 + random.nextDouble() * 2.0;
        }

        double newX = getX() + Math.cos(angle) * dist;
        double newZ = getZ() + Math.sin(angle) * dist;
        this.teleportTo(newX, getY(), newZ);
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 20, 0.5, 1.0, 0.5, 0.1);
        this.level().playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5f, 1.2f);
    }

    // ======================== 元莲仙尊 - 木道 - 不死之身 ========================

    private void aiYuanLian(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3 && lotusSporesTicks <= 0) {
            genesisLotusBloom();
            abilityCooldown = baseCooldown + 40;
            return;
        }

        if (distance <= 4) {
            vineEntangle(target);
            abilityCooldown = baseCooldown;
            return;
        }

        if (random.nextFloat() < 0.4f) {
            dissipateEssenceRain(target);
            abilityCooldown = baseCooldown;
        } else {
            vineWhip(target);
            abilityCooldown = baseCooldown;
        }
    }

    private void vineWhip(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.level().playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.5f, 0.8f);

        for (int i = 0; i < 3; i++) {
            double hitX = target.getX() + (random.nextDouble() - 0.5) * 2;
            double hitZ = target.getZ() + (random.nextDouble() - 0.5) * 2;
            double steps = 10;
            double dx = (hitX - getX()) / steps;
            double dz = (hitZ - getZ()) / steps;
            for (int p = 0; p < steps; p++) {
                sl.sendParticles(ParticleTypes.COMPOSTER,
                    getX() + dx * p, getEyeY() - 0.3 + Math.sin(p * 0.5) * 0.4, getZ() + dz * p,
                    2, 0.1, 0.1, 0.1, 0.01);
            }
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, hitX, target.getY() + 1.0, hitZ, 8, 0.3, 0.5, 0.3, 0.02);
        }

        target.hurt(this.damageSources().magic(), 8.0f);
        target.setDeltaMovement(target.getDeltaMovement().multiply(0.4, 1.0, 0.4));
        target.hurtMarked = true;
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SLASH_ARC, getX(), getY() + 1, getZ(), (float)(target.getX() - getX()), 0, (float)(target.getZ() - getZ()), 0xFF33CC33, 1.5f, 20);
        }
    }

    private void vineEntangle(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        float damage = 10.0f;
        target.hurt(this.damageSources().magic(), damage);
        this.heal(damage * 0.10f);
        target.setDeltaMovement(target.getDeltaMovement().multiply(0.2, 1.0, 0.2));
        target.hurtMarked = true;

        double pullDx = getX() - target.getX();
        double pullDz = getZ() - target.getZ();
        double pullLen = Math.sqrt(pullDx * pullDx + pullDz * pullDz);
        if (pullLen > 0.001) {
            target.setDeltaMovement(target.getDeltaMovement().add(pullDx / pullLen * 0.5, 0.1, pullDz / pullLen * 0.5));
            target.hurtMarked = true;
        }

        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 0.5, target.getZ(), 20, 1.0, 1.0, 1.0, 0.03);
        sl.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY(), target.getZ(), 15, 0.8, 0.5, 0.8, 0.02);
        this.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0f, 0.5f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.AURA_RING, target.getX(), target.getY() + 1, target.getZ(), 0, 1, 0, 0xFF33CC33, 1.5f, 25);
        }
    }

    private void dissipateEssenceRain(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY() + 3.0, target.getZ(), 40, 3.0, 2.0, 3.0, 0.03);
        sl.sendParticles(ParticleTypes.COMPOSTER, target.getX(), target.getY() + 1.0, target.getZ(), 20, 3.0, 1.0, 3.0, 0.02);
        this.level().playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 0.8f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(6.0),
            e -> e != this && e instanceof Player);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), 8.0f);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3, 1.0, 0.3));
            entity.hurtMarked = true;
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.RIPPLE, target.getX(), target.getY() + 1, target.getZ(), 0, 1, 0, 0xFF33CC33, 2.0f, 25);
        }
    }

    private void deployLotusSpores() {
        lotusSporesTicks = 100;
        lotusSporesX = getX();
        lotusSporesY = getY();
        lotusSporesZ = getZ();
    }

    private void genesisLotusBloom() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.heal(this.getMaxHealth() * 0.50f);
        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 2.0, getZ(), 80, 4.0, 3.0, 4.0, 0.1);
        sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 2.5, getZ(), 20, 2.0, 2.0, 2.0, 0.1);
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 3, 1.0, 1.0, 1.0, 0.0);
        this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.5f, 0.5f);

        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.HEAL_SPIRAL, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF33CC33, 3.0f, 40);
        }

        lotusSporesTicks = 200;
        lotusSporesX = getX();
        lotusSporesY = getY();
        lotusSporesZ = getZ();
    }

    private void tickLotusSpores() {
        if (lotusSporesTicks <= 0) return;
        lotusSporesTicks--;

        if (!(this.level() instanceof ServerLevel sl)) return;
        if (this.tickCount % 20 == 0) {
            sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, lotusSporesX, lotusSporesY + 1.0, lotusSporesZ, 20, 4.0, 1.0, 4.0, 0.02);
            List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(8.0).move(lotusSporesX - getX(), lotusSporesY - getY(), lotusSporesZ - getZ()),
                e -> e != this);
            for (LivingEntity entity : nearby) {
                entity.hurt(this.damageSources().magic(), 10.0f);
            }
        }
    }

    // ======================== 无极魔尊 - 律道+禁道 - 规则支配 ========================

    private void aiWuJi(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3) {
            absoluteLaw();
            abilityCooldown = baseCooldown + 30;
            return;
        }

        if (random.nextFloat() < 0.35f) {
            madnessSonicWave(target);
            abilityCooldown = baseCooldown;
            return;
        }

        if (wuJiLawMarkTicks <= 0) {
            lawImprison(target);
            abilityCooldown = baseCooldown + 10;
            return;
        }

        if (distance > 8) {
            int count = currentPhase >= 2 ? 5 : 3;
            orderProjectiles(target, count);
            abilityCooldown = baseCooldown;
        } else {
            ruleStrike(target);
            abilityCooldown = baseCooldown;
        }
    }

    private void madnessSonicWave(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 1.0, getZ(), 5, 4.0, 1.0, 4.0, 0.0);
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 0.5, getZ(), 40, 4.0, 1.0, 4.0, 0.05);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.3f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.RIPPLE, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF990099, 2.0f, 25);
        }

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), 6.0f);
            entity.push((random.nextDouble() - 0.5) * 1.5, 0.3, (random.nextDouble() - 0.5) * 1.5);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4, 1.0, 0.4));
            entity.hurtMarked = true;
        }

        if (target instanceof Player) {
            wuJiMadnessLayers++;
            if (wuJiMadnessLayers >= 3) {
                wuJiMadnessLayers = 0;
                target.hurt(this.damageSources().magic(), target.getMaxHealth() * 0.15f);
                target.setDeltaMovement(0, 0, 0);
                target.hurtMarked = true;
                sl.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0, target.getZ(), 3, 0.5, 0.5, 0.5, 0.0);
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY(), target.getZ(), 30, 1.0, 1.5, 1.0, 0.03);
                this.level().playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.0f, 0.3f);
            }
        }
    }

    private void lawImprison(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0, target.getZ(), 60, 1.5, 2.0, 1.5, 0.05);
        sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 0.5, target.getZ(), 20, 1.0, 1.0, 1.0, 0.02);
        this.level().playSound(null, target.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2.0f, 0.5f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.BLACK_HOLE, target.getX(), target.getY() + 1, target.getZ(), 0, 1, 0, 0xFF990099, 2.0f, 40);
        }

        wuJiLawTarget = target;
        wuJiLawMarkTicks = 160;

        if (target instanceof Player player) {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.removeModifier(LAW_SLOW_MOD);
                speed.addTransientModifier(new AttributeModifier(LAW_SLOW_MOD, -0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            AttributeInstance atkSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
            if (atkSpeed != null) {
                atkSpeed.removeModifier(LAW_SLOW_MOD);
                atkSpeed.addTransientModifier(new AttributeModifier(LAW_SLOW_MOD, -0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        }
    }

    private void ruleStrike(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        target.hurt(this.damageSources().magic(), 20.0f);
        if (currentPhase >= 2) {
            target.setAbsorptionAmount(0);
        }
        double knockDx = target.getX() - getX();
        double knockDz = target.getZ() - getZ();
        double dist = Math.sqrt(knockDx * knockDx + knockDz * knockDz);
        if (dist > 0.001) {
            target.knockback(2.0, -knockDx / dist, -knockDz / dist);
        }
        sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0, target.getZ(), 30, 1.0, 1.5, 1.0, 0.2);
        this.level().playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.5f, 0.5f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.IMPACT_BURST, target.getX(), target.getY() + 1, target.getZ(), 0, 1, 0, 0xFF990099, 1.5f, 20);
        }
    }

    private void tickLawFreeze() {
        if (lawFreezeTicks <= 0) return;
        lawFreezeTicks--;
        if (lawFreezeTicks == 0) {
            List<Player> nearby = this.level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(12.0));
            for (Player player : nearby) {
                AttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (moveSpeed != null) moveSpeed.removeModifier(LAW_SLOW_MOD);
                AttributeInstance atkSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
                if (atkSpeed != null) atkSpeed.removeModifier(LAW_SLOW_MOD);
            }
        }
    }

    private void orderProjectiles(LivingEntity target, int count) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        this.level().playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.5f, 0.6f);

        for (int i = 0; i < count; i++) {
            double hitX = target.getX() + (random.nextDouble() - 0.5) * 3;
            double hitZ = target.getZ() + (random.nextDouble() - 0.5) * 3;
            sl.sendParticles(ParticleTypes.ENCHANT, hitX, target.getY() + 2.0, hitZ, 15, 0.3, 1.0, 0.3, 0.05);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, hitX, target.getY() + 0.5, hitZ, 5, 0.2, 0.2, 0.2, 0.01);
        }

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
            target.getBoundingBox().inflate(3.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), 10.0f);
        }
    }

    private void absoluteLaw() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 5, 1.0, 1.0, 1.0, 0.0);
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.0, getZ(), 100, 4.0, 3.0, 4.0, 0.1);
        sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 0.5, getZ(), 50, 4.0, 1.0, 4.0, 0.02);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.5f, 0.3f);
        this.level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0f, 0.5f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
            float damage = entity.getMaxHealth() * 0.20f;
            entity.hurt(this.damageSources().magic(), damage);
        }
        lawFreezeTicks = 60;
        for (LivingEntity entity : nearby) {
            if (entity instanceof ServerPlayer sp) {
                VfxHelper.spawn(sp, VfxType.DOME_FIELD, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF990099, 2.5f, 40);
                VfxHelper.spawn(sp, VfxType.PULSE_WAVE, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF990099, 2.0f, 30);
            }
        }
    }

    // ======================== 狂蛮魔尊 - 力道+变化道 - 形态战士 ========================

    private void aiKuangMan(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3 && currentForm == KuangManForm.HUMAN && formCooldownTicks <= 0) {
            activateAllFormsUltimate();
            abilityCooldown = baseCooldown;
            return;
        }

        if (currentForm == KuangManForm.HUMAN && formCooldownTicks <= 0) {
            int formChoice = random.nextInt(3);
            switch (formChoice) {
                case 0 -> switchToIcePhoenix();
                case 1 -> switchToLightningWolf();
                case 2 -> switchToGiant();
            }
            abilityCooldown = 20;
            return;
        }

        switch (currentForm) {
            case ICE_PHOENIX -> aiKuangManIcePhoenix(target, baseCooldown, distance);
            case LIGHTNING_WOLF -> aiKuangManLightningWolf(target, baseCooldown, distance);
            case GIANT -> aiKuangManGiant(target, baseCooldown, distance);
            default -> {
                if (distance > 6) {
                    savageCharge(target);
                    abilityCooldown = baseCooldown;
                } else {
                    devastatingStrike(target);
                    abilityCooldown = baseCooldown;
                }
            }
        }
    }

    private void switchToIcePhoenix() {
        currentForm = KuangManForm.ICE_PHOENIX;
        formDurationTicks = 600;
        this.setNoGravity(true);
        applyMod(Attributes.MOVEMENT_SPEED, KUANGMAN_SPEED_MOD, 0.10);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1.0, getZ(), 60, 2.0, 2.0, 2.0, 0.1);
            this.level().playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0f, 1.5f);
            LivingEntity target = this.getTarget();
            if (target instanceof ServerPlayer sp) {
                VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF88CCFF, 2.0f, 30);
            }
        }
    }

    private void switchToLightningWolf() {
        currentForm = KuangManForm.LIGHTNING_WOLF;
        formDurationTicks = 600;
        applyMod(Attributes.MOVEMENT_SPEED, KUANGMAN_SPEED_MOD, 0.20);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 1.0, getZ(), 60, 2.0, 2.0, 2.0, 0.1);
            this.level().playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0f, 1.2f);
            LivingEntity target = this.getTarget();
            if (target instanceof ServerPlayer sp) {
                VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF9933FF, 2.0f, 30);
            }
        }
    }

    private void switchToGiant() {
        currentForm = KuangManForm.GIANT;
        formDurationTicks = 400;
        applyMod(Attributes.ATTACK_DAMAGE, KUANGMAN_ATK_MOD, venerableType.attackDamage * 0.80);
        applyMod(Attributes.MOVEMENT_SPEED, KUANGMAN_SPEED_MOD, -0.10);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0, getZ(), 3, 1.0, 1.0, 1.0, 0.0);
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.0, getZ(), 40, 2.0, 2.0, 2.0, 0.2);
            this.level().playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 3.0f, 0.3f);
            LivingEntity target = this.getTarget();
            if (target instanceof ServerPlayer sp) {
                VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFCC0000, 2.0f, 30);
            }
        }
    }

    private void revertToHumanForm() {
        if (currentForm == KuangManForm.ICE_PHOENIX) {
            this.setNoGravity(false);
        }
        currentForm = KuangManForm.HUMAN;
        applyMod(Attributes.ATTACK_DAMAGE, KUANGMAN_ATK_MOD, 0);
        applyMod(Attributes.MOVEMENT_SPEED, KUANGMAN_SPEED_MOD, 0);
        formCooldownTicks = currentPhase == 3 ? 600 : 1200;
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.0, getZ(), 20, 1.0, 1.0, 1.0, 0.1);
        }
    }

    private void activateAllFormsUltimate() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        applyMod(Attributes.ATTACK_DAMAGE, KUANGMAN_ATK_MOD, venerableType.attackDamage * 0.80);
        applyMod(Attributes.MOVEMENT_SPEED, KUANGMAN_SPEED_MOD, 0.20);
        formDurationTicks = 200;
        currentForm = KuangManForm.GIANT;
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 5, 1.0, 1.0, 1.0, 0.0);
        sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 1.0, getZ(), 30, 3.0, 3.0, 3.0, 0.1);
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 1.0, getZ(), 30, 3.0, 3.0, 3.0, 0.1);
        sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0, getZ(), 3, 1.0, 1.0, 1.0, 0.0);
        this.level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 3.0f, 0.3f);
        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.PULSE_WAVE, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFCC0000, 2.5f, 35);
            VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFCC0000, 2.0f, 30);
        }
    }

    private void aiKuangManIcePhoenix(LivingEntity target, int baseCooldown, double distance) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SNOWFLAKE, getX(), getY() + 0.5, getZ(), 5, 1.5, 1.5, 1.5, 0.02);

        if (distance > 6) {
            IceBoltEntity bolt = new IceBoltEntity(this.level(), this, 15.0f);
            bolt.setPos(getX(), getEyeY() - 0.1, getZ());
            double bx = target.getX() - getX();
            double by = target.getEyeY() - getEyeY();
            double bz = target.getZ() - getZ();
            bolt.shoot(bx, by, bz, 2.5f, 1.5f);
            this.level().addFreshEntity(bolt);
            this.level().playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5f, 1.5f);
        } else {
            target.hurt(this.damageSources().magic(), 12.0f);
            target.setDeltaMovement(target.getDeltaMovement().multiply(0.3, 1.0, 0.3));
            target.hurtMarked = true;
            sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.5, 1.0, 0.5, 0.05);
        }
        abilityCooldown = baseCooldown - 10;
    }

    private void aiKuangManLightningWolf(LivingEntity target, int baseCooldown, double distance) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 0.5, getZ(), 5, 1.0, 1.0, 1.0, 0.02);

        if (distance > 3) {
            savageCharge(target);
        }
        target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (random.nextFloat() < 0.3f && this.level() instanceof ServerLevel) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(this.level());
            if (bolt != null) {
                bolt.moveTo(target.getX(), target.getY(), target.getZ());
                bolt.setVisualOnly(false);
                this.level().addFreshEntity(bolt);
            }
        }
        abilityCooldown = baseCooldown / 2;
    }

    private void aiKuangManGiant(LivingEntity target, int baseCooldown, double distance) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.5, getZ(), 5, 1.5, 1.5, 1.5, 0.1);

        sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 8, 3.0, 0.3, 3.0, 0.05);
        this.level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5f, 0.5f);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.8, 0));
            entity.hurtMarked = true;
        }
        abilityCooldown = baseCooldown;
    }

    private void savageCharge(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) return;

        this.setDeltaMovement(dx / len * 2.0, 0.3, dz / len * 2.0);
        this.hurtMarked = true;
        sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0, getZ(), 10, 1.0, 0.5, 1.0, 0.1);
        this.level().playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0f, 0.8f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.IMPACT_BURST, getX(), getY() + 1, getZ(), (float)(dx / len), 0, (float)(dz / len), 0xFFCC0000, 2.0f, 25);
        }

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().mobAttack(this), 20.0f);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.8, 0));
            entity.hurtMarked = true;
        }
    }

    private void devastatingStrike(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        float distance = this.distanceTo(target);
        if (distance > 6.0f) return;

        target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 15.0f);
        target.setDeltaMovement(target.getDeltaMovement().add(0, 0.5, 0));
        target.hurtMarked = true;
        sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.5, 0.5, 0.5, 0.3);
        sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5, target.getZ(), 3, 0.5, 0.5, 0.5, 0.05);
        this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0f, 1.2f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SLASH_ARC, target.getX(), target.getY() + 1, target.getZ(), 0, 1, 0, 0xFFCC0000, 1.5f, 20);
        }
    }

    // ======================== 盗天魔尊 - 偷道+宇道 - 神偷 ========================

    private void aiDaoTian(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3) {
            heavenTheft(target);
            abilityCooldown = baseCooldown + 30;
            return;
        }

        if (distance < 5) {
            spaceBlink();
            abilityCooldown = 10;
            return;
        }

        if (random.nextFloat() < 0.45f) {
            formlessHandSteal(target);
            spaceBlink();
            this.setInvisible(true);
            invisTicks = currentPhase >= 2 ? 40 : 60;
            visibleTimer = 0;
            abilityCooldown = baseCooldown;
            return;
        }

        if (random.nextFloat() < 0.3f) {
            shadowTeleport(target);
            spaceCut(target);
            spaceBlink();
            this.setInvisible(true);
            invisTicks = currentPhase >= 2 ? 40 : 60;
            visibleTimer = 0;
            abilityCooldown = baseCooldown;
            return;
        }

        spaceBlink();
        this.setInvisible(true);
        invisTicks = 60;
        visibleTimer = 0;
        abilityCooldown = baseCooldown / 2;
    }

    private void formlessHandSteal(LivingEntity target) {
        if (!(target instanceof ServerPlayer player)) return;
        if (!(this.level() instanceof ServerLevel sl)) return;

        int fingers = currentPhase == 1 ? 2 : currentPhase == 2 ? 3 : 5;
        FormlessHandEntity hand = new FormlessHandEntity(this.level(), this, player, fingers);
        hand.setPos(this.getX(), this.getY() + 1.5, this.getZ());
        sl.addFreshEntity(hand);

        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.2f, 1.8f);
        player.sendSystemMessage(Component.literal(
            "\u00a79\u00a7l[\u65e0\u76f8\u624b] \u00a7b\u4e00\u53ea\u6de1\u84dd\u8272\u7684\u624b\u4ece\u865a\u7a7a\u4e2d\u4f38\u51fa..."));
    }

    private void spaceBlink() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 20, 0.5, 1.0, 0.5, 0.1);
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = 6.0 + random.nextDouble() * 4.0;
        double newX = getX() + Math.cos(angle) * dist;
        double newZ = getZ() + Math.sin(angle) * dist;
        this.teleportTo(newX, getY(), newZ);
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 20, 0.5, 1.0, 0.5, 0.1);
        this.level().playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.2f);
        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SHADOW_FADE, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF444444, 1.5f, 20);
        }
    }

    private void shadowTeleport(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 30, 0.5, 1.0, 0.5, 0.1);
        double angle = Math.atan2(target.getZ() - getZ(), target.getX() - getX());
        double behindX = target.getX() - Math.cos(angle) * 2.0;
        double behindZ = target.getZ() - Math.sin(angle) * 2.0;
        this.teleportTo(behindX, target.getY(), behindZ);
        sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 30, 0.5, 1.0, 0.5, 0.1);
        this.level().playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5f, 1.0f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SHADOW_FADE, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF444444, 1.5f, 20);
        }
    }

    private void spaceCut(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        float damage = currentPhase >= 2 ? 8.0f : 5.0f;
        target.hurt(this.damageSources().magic(), damage);

        double angle = random.nextDouble() * Math.PI * 2;
        double pushDist = 3.0 + random.nextDouble() * 2.0;
        target.setDeltaMovement(Math.cos(angle) * 0.8, 0.3, Math.sin(angle) * 0.8);
        target.hurtMarked = true;

        double steps = 8;
        double dx = (target.getX() - getX()) / steps;
        double dz = (target.getZ() - getZ()) / steps;
        for (int p = 0; p < steps; p++) {
            sl.sendParticles(ParticleTypes.PORTAL,
                getX() + dx * p, getEyeY(), getZ() + dz * p,
                3, 0.1, 0.2, 0.1, 0.01);
        }
        this.level().playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5f, 1.0f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SLASH_ARC, getX(), getEyeY(), getZ(), (float)(target.getX() - getX()), 0, (float)(target.getZ() - getZ()), 0xFF444444, 1.5f, 20);
        }
    }

    private void heavenTheft(LivingEntity target) {
        if (!(target instanceof ServerPlayer player)) return;
        if (!(this.level() instanceof ServerLevel sl)) return;

        int handCount = 3 + random.nextInt(3);
        for (int i = 0; i < handCount; i++) {
            int fingers = 3 + random.nextInt(3);
            FormlessHandEntity hand = new FormlessHandEntity(this.level(), this, player, fingers);
            double angle = (Math.PI * 2 * i / handCount);
            double dist = 3.0 + random.nextDouble() * 2.0;
            hand.setPos(
                player.getX() + Math.cos(angle) * dist,
                player.getY() + 2.0 + random.nextDouble(),
                player.getZ() + Math.sin(angle) * dist);
            sl.addFreshEntity(hand);
        }

        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.5, player.getZ(),
            80, 2.0, 2.5, 2.0, 0.03);
        this.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 2.0f, 1.5f);
        player.sendSystemMessage(Component.literal(
            "\u00a79\u00a7l[\u5077\u5929\u6362\u65e5] \u00a7b\u65e0\u6570\u6de1\u84dd\u8272\u7684\u624b\u4ece\u865a\u7a7a\u4e2d\u6d8c\u51fa\uff01"));
        VfxHelper.spawn(player, VfxType.BLACK_HOLE, player.getX(), player.getY() + 1, player.getZ(), 0, 1, 0, 0xFF334466, 2.0f, 40);

        for (int blink = 0; blink < 5; blink++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 4.0 + random.nextDouble() * 4.0;
            double px = target.getX() + Math.cos(angle) * dist;
            double pz = target.getZ() + Math.sin(angle) * dist;
            sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 15, 0.3, 0.5, 0.3, 0.1);
            this.teleportTo(px, target.getY(), pz);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, target.getY() + 1.5, pz, 20, 0.5, 1.0, 0.5, 0.02);
        }

        this.setInvisible(true);
        invisTicks = 80;
        visibleTimer = 0;
    }

    private void tickInvisibility() {
        if (invisTicks <= 0) return;
        invisTicks--;
        if (invisTicks == 0) {
            this.setInvisible(false);
            if (venerableType == VenerableType.DAO_TIAN) {
                visibleTimer = 0;
            }
        }
    }

    // ======================== 巨阳仙尊 - 运道+血道 - 金光审判 ========================

    private void aiJuYang(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3) {
            solarJudgment(target);
            giantSunWill(target);
            abilityCooldown = baseCooldown + 10;
            return;
        }

        float critRoll = random.nextFloat();
        float critChance = 0.20f;

        if (random.nextFloat() < 0.3f) {
            giantSunWill(target);
            abilityCooldown = baseCooldown + 15;
            return;
        }

        if (distance < 5) {
            fortuneDodge();
            goldenBeam(target, 3, critRoll < critChance);
            abilityCooldown = baseCooldown;
        } else {
            goldenBeam(target, currentPhase >= 2 ? 7 : 5, critRoll < critChance);
            abilityCooldown = baseCooldown;
        }
    }

    private void goldenBeam(LivingEntity target, int count, boolean crit) {
        this.level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.5f, 1.0f);

        float dmg = crit ? 24.0f : 12.0f;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) return;
        dx /= len;
        dz /= len;

        for (int i = 0; i < count; i++) {
            double spreadAngle = (i - count / 2.0) * 0.12;
            double offX = dx * Math.cos(spreadAngle) - dz * Math.sin(spreadAngle);
            double offZ = dx * Math.sin(spreadAngle) + dz * Math.cos(spreadAngle);

            GoldBeamEntity beam = new GoldBeamEntity(this.level(), this, dmg);
            beam.setPos(getX(), getEyeY() - 0.1, getZ());
            double sdx = target.getX() + offX * 2.0 - getX();
            double sdy = target.getEyeY() - getEyeY();
            double sdz = target.getZ() + offZ * 2.0 - getZ();
            beam.shoot(sdx, sdy, sdz, 2.5f, 1.5f);
            this.level().addFreshEntity(beam);
        }

        if (crit && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 2.0, getZ(), 20, 1.0, 1.0, 1.0, 0.3);
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.ENERGY_BEAM, getX(), getEyeY(), getZ(), (float) dx, 0, (float) dz, 0xFFFF8800, 1.5f, 20);
        }
    }

    private void giantSunWill(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double tx = target.getX();
        double ty = target.getY();
        double tz = target.getZ();

        sl.sendParticles(ParticleTypes.FLASH, tx, ty + 5.0, tz, 5, 1.0, 2.0, 1.0, 0.0);
        sl.sendParticles(ParticleTypes.END_ROD, tx, ty + 3.0, tz, 80, 2.0, 4.0, 2.0, 0.08);
        sl.sendParticles(ParticleTypes.END_ROD, tx, ty + 1.0, tz, 40, 4.0, 0.5, 4.0, 0.02);
        this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0f, 0.5f);
        this.level().playSound(null, target.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 0.5f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
            target.getBoundingBox().inflate(10.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            float damage = entity.getMaxHealth() * 0.20f;
            entity.hurt(this.damageSources().magic(), damage);
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SKY_STRIKE, tx, ty, tz, 0, 1, 0, 0xFFFF8800, 2.0f, 30);
        }
    }

    private void solarJudgment(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SKY_STRIKE, target.getX(), target.getY(), target.getZ(), 0, 1, 0, 0xFFFF8800, 3.0f, 40);
            VfxHelper.spawn(sp, VfxType.PULSE_WAVE, target.getX(), target.getY() + 1, target.getZ(), 0, 1, 0, 0xFFFF8800, 2.5f, 30);
        }

        giantSunWill(target);

        goldenBeam(target, 12, true);

        sl.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 2.0, target.getZ(), 8, 2.0, 2.0, 2.0, 0.0);
        this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0f, 0.3f);
    }

    private void fortuneDodge() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double angle = random.nextDouble() * Math.PI * 2;
        double dist = 4.0 + random.nextDouble() * 3.0;
        double newX = getX() + Math.cos(angle) * dist;
        double newZ = getZ() + Math.sin(angle) * dist;
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 30, 1.0, 1.5, 1.0, 0.1);
        this.teleportTo(newX, getY(), newZ);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 30, 1.0, 1.5, 1.0, 0.1);
        this.level().playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 2.0f, 1.0f);
        LivingEntity target = this.getTarget();
        if (target != null && random.nextFloat() < 0.30f) {
            goldenBeam(target, 3, true);
        }
    }

    // ======================== 幽魂魔尊 - 魂道+影道 - 噬魂者 ========================

    private void aiYouHun(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3 && this.getHealth() < this.getMaxHealth() * 0.20f && !youHunSoulSplitUsed) {
            soulSplit();
            abilityCooldown = baseCooldown;
            return;
        }

        if (currentPhase == 3) {
            soulDevourUltimate(target);
            abilityCooldown = baseCooldown + 20;
            return;
        }

        if (distance <= 4) {
            float damage = 18.0f;
            target.hurt(this.damageSources().magic(), damage);
            this.heal(damage * 0.30f);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 1.0, 0.5, 0.05);
            }
            abilityCooldown = baseCooldown - 5;
            return;
        }

        if (random.nextFloat() < 0.35f) {
            soulSeize(target);
            abilityCooldown = baseCooldown;
            return;
        }

        soulDamage(target);
        abilityCooldown = baseCooldown;
    }

    private void soulSeize(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() - 0.2, target.getZ(), 30, 1.5, 0.3, 1.5, 0.02);
        sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.5, 1.5, 0.5, 0.05);
        this.level().playSound(null, target.blockPosition(), SoundEvents.WARDEN_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.3f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.hurt(this.damageSources().magic(), entity.getMaxHealth() * 0.08f);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3, 1.0, 0.3));
            entity.hurtMarked = true;
        }
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.SHADOW_FADE, target.getX(), target.getY() + 1, target.getZ(), 0, 1, 0, 0xFF330066, 1.5f, 25);
        }
    }

    private void soulDamage(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        float damage = currentPhase >= 2 ? 20.0f : 15.0f;
        target.hurt(this.damageSources().magic(), damage);
        this.heal(damage * 0.30f);
        sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 25, 0.5, 1.0, 0.5, 0.05);
        sl.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1.5, target.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        this.level().playSound(null, target.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.5f, 1.5f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.ENERGY_BEAM, getX(), getEyeY(), getZ(), (float)(target.getX() - getX()), (float)(target.getEyeY() - getEyeY()), (float)(target.getZ() - getZ()), 0xFF330066, 1.5f, 20);
        }
    }

    private void fearAura() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        if (this.tickCount % 40 != 0) return;
        double speedMul = currentPhase >= 2 ? 0.5 : 0.7;
        List<Player> nearby = this.level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(6.0));
        for (Player player : nearby) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(speedMul, 1.0, speedMul));
            player.hurtMarked = true;
        }
    }

    private void spawnSoulBeast(double x, double y, double z) {
        if (youHunSoulBeastCount >= 5) return;
        Zombie soulBeast = EntityType.ZOMBIE.create(this.level());
        if (soulBeast == null) return;
        soulBeast.setPos(x, y, z);
        soulBeast.setCustomName(Component.literal("\u00a75\u9b42\u517d"));
        soulBeast.setGlowingTag(true);
        if (this.getTarget() != null) soulBeast.setTarget(this.getTarget());
        this.level().addFreshEntity(soulBeast);
        youHunSoulBeastCount++;
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 1.0, z, 20, 0.5, 1.0, 0.5, 0.05);
        }
    }

    private void soulSplit() {
        if (youHunSoulSplitUsed) return;
        youHunSoulSplitUsed = true;
        this.heal(this.getMaxHealth() * 0.30f);
        if (!(this.level() instanceof ServerLevel sl)) return;

        for (int i = 0; i < 2; i++) {
            double spawnX = getX() + (random.nextDouble() - 0.5) * 6.0;
            double spawnZ = getZ() + (random.nextDouble() - 0.5) * 6.0;
            Zombie clone = EntityType.ZOMBIE.create(this.level());
            if (clone == null) continue;
            clone.setPos(spawnX, getY(), spawnZ);
            clone.setCustomName(Component.literal("\u00a75\u5e7d\u9b42\u5206\u8eab"));
            clone.setGlowingTag(true);
            if (this.getTarget() != null) clone.setTarget(this.getTarget());
            this.level().addFreshEntity(clone);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, spawnX, getY() + 1.0, spawnZ, 30, 0.5, 1.5, 0.5, 0.05);
        }
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.0, getZ(), 3, 1.0, 1.0, 1.0, 0.0);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.5f);
        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF330066, 2.0f, 30);
        }
    }

    private void soulDevourUltimate(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.0, getZ(), 80, 6.0, 3.0, 6.0, 0.05);
        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.5, getZ(), 40, 6.0, 1.0, 6.0, 0.03);
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 3, 1.0, 1.0, 1.0, 0.0);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.5f, 0.3f);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_DEATH, SoundSource.HOSTILE, 2.0f, 0.5f);

        float totalDamageDealt = 0;
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(10.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            float damage = entity.getMaxHealth() * 0.25f;
            entity.hurt(this.damageSources().magic(), damage);
            totalDamageDealt += damage;
        }
        this.heal(totalDamageDealt * 0.50f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.BLACK_HOLE, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF330066, 2.5f, 40);
        }
    }

    // ======================== 乐土仙尊 - 土道+天道 - 杀招大师 ========================

    private void aiLeTu(LivingEntity target, int baseCooldown, double distance) {
        if (!leTuAggro) {
            abilityCooldown = 40;
            return;
        }

        if (target instanceof Player player && player.getHealth() < player.getMaxHealth() * 0.20f && leTuMercyTicks <= 0) {
            leTuMercyTicks = 200;
            abilityCooldown = 100;
            player.sendSystemMessage(Component.literal("\u00a7b\u00a7l[\u4e50\u571f\u4ed9\u5c0a] \u00a77\u4f60\u8fd8\u6709\u4e00\u7ebf\u751f\u673a..."));
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 2.0, getZ(), 10, 1.0, 1.0, 1.0, 0.05);
            }
            return;
        }

        if (leTuMercyTicks > 0) {
            abilityCooldown = 20;
            return;
        }

        if (currentPhase == 3) {
            if (leTuDomainTicks <= 0) {
                activateReincarnationBattlefield(true);
            }
            leTuFormationDefense();
            if (distance <= 6) {
                devastatingStrike(target);
            }
            abilityCooldown = baseCooldown;
            return;
        }

        if (leTuDomainTicks <= 0 && currentPhase >= 1) {
            activateReincarnationBattlefield(false);
            abilityCooldown = baseCooldown + 20;
            return;
        }

        if (random.nextFloat() < 0.3f && !leTuFormationActive) {
            leTuFormationDefense();
            abilityCooldown = baseCooldown;
            return;
        }

        if (distance <= 6) {
            devastatingStrike(target);
            abilityCooldown = baseCooldown;
        } else {
            abilityCooldown = baseCooldown / 2;
        }
    }

    private void activateReincarnationBattlefield(boolean enhanced) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        double range = enhanced ? 24.0 : 12.0;
        leTuDomainTicks = enhanced ? 400 : 200;
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.0, getZ(), 60, range * 0.4, 2.0, range * 0.4, 0.05);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.5, getZ(), 40, range * 0.4, 0.3, range * 0.4, 0.01);
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 3, 1.0, 1.0, 1.0, 0.0);
        this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 0.5f);
        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.DOME_FIELD, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF88DDFF, 2.5f, 40);
        }
    }

    private void leTuFormationDefense() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        leTuFormationActive = true;
        leTuFormationTicks = 200;
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.5, getZ(), 40, 3.0, 0.3, 3.0, 0.01);
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.0, getZ(), 30, 3.0, 1.5, 3.0, 0.05);
        this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5f, 1.0f);
        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.AURA_RING, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFF88DDFF, 2.0f, 30);
        }
    }

    public void onNearbyEntityDeath(double x, double y, double z) {
        if (venerableType == VenerableType.LE_TU && leTuDomainTicks > 0) {
            spawnWarSoul(x, y, z);
        }
        if (venerableType == VenerableType.YOU_HUN) {
            spawnSoulBeast(x, y, z);
        }
    }

    private void spawnWarSoul(double x, double y, double z) {
        if (soulBeastCount >= 5) return;
        Zombie warSoul = EntityType.ZOMBIE.create(this.level());
        if (warSoul == null) return;
        warSoul.setPos(x, y, z);
        warSoul.setCustomName(Component.literal("\u00a75\u6218\u9b42"));
        warSoul.setGlowingTag(true);
        if (this.getTarget() != null) warSoul.setTarget(this.getTarget());
        this.level().addFreshEntity(warSoul);
        soulBeastCount++;
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 1.0, z, 20, 0.5, 1.0, 0.5, 0.05);
        }
    }

    // ======================== 红莲魔尊 - 宙道+宇道 - 时间主宰 ========================

    private void aiHongLian(LivingEntity target, int baseCooldown, double distance) {
        if (currentPhase == 3) {
            timeFreeze();
            abilityCooldown = baseCooldown + 30;
            return;
        }

        if (random.nextFloat() < 0.30f && target instanceof LivingEntity) {
            fateInterrupt(target);
        }

        if (distance < 4) {
            timeSlow();
            temporalBurst(target);
            abilityCooldown = baseCooldown;
            return;
        }

        if (distance > 8) {
            temporalBurst(target);
            abilityCooldown = baseCooldown;
        } else {
            timeSlow();
            abilityCooldown = baseCooldown / 2;
        }

        if (random.nextFloat() < 0.25f) {
            hpReversal();
        }

        if (currentPhase >= 2 && cicadaEnraged) {
            applyMod(Attributes.MOVEMENT_SPEED, PHASE_SPEED_MOD, 0.08);
        }
    }

    private void fateInterrupt(LivingEntity target) {
        target.setDeltaMovement(0, 0, 0);
        target.hurtMarked = true;
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 1.0, 0.5, 0.03);
            this.level().playSound(null, target.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 1.5f, 0.5f);
        }
    }

    private void timeFreeze() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        hongLianTimeFreezeTicks = 100;
        sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 8, 2.0, 2.0, 2.0, 0.0);
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 100, 6.0, 3.0, 6.0, 0.05);
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 0.5, getZ(), 60, 6.0, 1.0, 6.0, 0.02);
        this.level().playSound(null, blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 3.0f, 0.1f);
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0f, 0.3f);
        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.DOME_FIELD, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFFF0033, 2.5f, 40);
        }
    }

    private void timeSlow() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 60, 4.0, 2.0, 4.0, 0.02);
        sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 0.5, getZ(), 30, 4.0, 1.0, 4.0, 0.01);
        this.level().playSound(null, blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 2.0f, 0.3f);
        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.RIPPLE, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFFF0033, 2.0f, 25);
        }

        double speedMul = cicadaEnraged ? 0.08 : 0.15;
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(speedMul, 0.5, speedMul));
            entity.hurtMarked = true;
        }
        timeSlowTicks = 100;
    }

    private void tickTimeSlow() {
        if (timeSlowTicks <= 0) return;
        timeSlowTicks--;

        if (this.tickCount % 10 != 0) return;
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4, 0.8, 0.4));
            entity.hurtMarked = true;
        }
    }

    private void temporalBurst(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) return;
        dx /= len;
        dz /= len;

        float burstDamage = cicadaEnraged ? 22.5f : 15.0f;
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0), e -> e != this);
        for (LivingEntity entity : nearby) {
            double ex = entity.getX() - getX();
            double ez = entity.getZ() - getZ();
            double elen = Math.sqrt(ex * ex + ez * ez);
            if (elen > 0.001) {
                double dot = (ex / elen) * dx + (ez / elen) * dz;
                double coneWidth = currentPhase >= 2 ? 0.3 : 0.5;
                if (dot > coneWidth) {
                    entity.hurt(this.damageSources().magic(), burstDamage);
                    entity.knockback(1.5, -dx, -dz);
                }
            }
        }

        sl.sendParticles(ParticleTypes.END_ROD, getX() + dx * 3, getY() + 1.0, getZ() + dz * 3, 30, 1.5, 1.0, 1.5, 0.05);
        sl.sendParticles(ParticleTypes.CRIT, getX() + dx * 3, getY() + 1.0, getZ() + dz * 3, 15, 1.0, 0.5, 1.0, 0.2);
        this.level().playSound(null, blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 2.0f, 0.5f);
        if (target instanceof ServerPlayer sp) {
            VfxHelper.spawn(sp, VfxType.PULSE_WAVE, getX(), getY() + 1, getZ(), (float) dx, 0, (float) dz, 0xFFFF0033, 2.0f, 25);
        }
    }

    private void hpReversal() {
        if (savedHP > 0 && savedHP > this.getHealth()) {
            float restored = savedHP;
            this.setHealth(Math.min(restored, this.getMaxHealth()));
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.5, getZ(), 50, 2.0, 2.0, 2.0, 0.1);
                sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 3, 1.0, 1.0, 1.0, 0.0);
            }
            this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 0.5f);
            savedHP = -1;
        }
    }

    private void tickSavedHP() {
        savedHPTimer++;
        if (savedHPTimer >= 200) {
            savedHP = this.getHealth();
            savedHPTimer = 0;
        }
    }

    private void springAutumnCicada() {
        hasUsedCicada = true;
        this.setHealth(this.getMaxHealth());

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0, getZ(), 10, 2.0, 2.0, 2.0, 0.0);
            sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 150, 8.0, 4.0, 8.0, 0.15);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 0.5, getZ(), 60, 5.0, 2.0, 5.0, 0.08);
            sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 1.5, getZ(), 80, 4.0, 3.0, 4.0, 0.1);
        }
        this.level().playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.0f, 0.3f);
        this.level().playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.5f, 0.3f);
        this.level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.5f, 0.5f);
        List<ServerPlayer> viewers = this.level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(50.0));
        for (ServerPlayer sp : viewers) {
            VfxHelper.spawn(sp, VfxType.GLOW_BURST, getX(), getY() + 1, getZ(), 0, 1, 0, 0xFFFF0033, 3.0f, 40);
        }

        currentPhase = 1;
        abilityCooldown = 60;
        cicadaEnraged = true;
    }

    // ======================== 乐土绝对防御 ========================

    private void tickAbsoluteDefense() {
        if (absoluteDefenseTicks > 0) {
            absoluteDefenseTicks--;
            if (this.tickCount % 10 == 0 && this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 15, 2.0, 2.0, 2.0, 0.02);
            }
        }
    }

    // ======================== 杀招差异化执行 ========================

    private void executeVenerableKillerMove(LivingEntity target) {
        KillerMove move = availableMoves.get(this.random.nextInt(availableMoves.size()));
        if (!(this.level() instanceof ServerLevel sl)) return;

        float power = move.power();
        float hpPercent = power / 150f;
        float damage = target.getMaxHealth() * hpPercent;
        damage = Math.min(damage, 60.0f);
        damage = Math.max(damage, 10.0f);

        if (cicadaEnraged) {
            damage *= 1.5f;
        }

        DaoPath path = move.primaryPath();
        ParticleOptions particle = getPathParticle(path);

        sl.sendParticles(particle, this.getX(), this.getY() + 1.5, this.getZ(), 80, 4.0, 3.0, 4.0, 0.1);
        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 8, 3.0, 1.5, 3.0, 0.08);

        switch (move.moveType()) {
            case ATTACK, ULTIMATE -> {
                target.hurt(this.damageSources().magic(), damage);
                List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(6.0), e -> e != this && e != target);
                float splashDmg = damage * 0.5f;
                for (LivingEntity e : nearby) {
                    e.hurt(this.damageSources().magic(), splashDmg);
                }
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 40, 1.5, 1.5, 1.5, 0.4);
            }
            case DEFENSE, BUFF -> {
                this.heal(this.getMaxHealth() * 0.20f);
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 2.0, this.getZ(), 20, 1.5, 1.5, 1.5, 0.1);
                target.hurt(this.damageSources().magic(), damage * 0.6f);
            }
            case CONTROL, DEBUFF -> {
                target.hurt(this.damageSources().magic(), damage * 0.7f);
                target.setDeltaMovement(0, 0, 0);
                target.hurtMarked = true;
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 40, 2.0, 1.5, 2.0, 0.03);
            }
            case MOVEMENT -> {
                double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                double behindX = target.getX() - Math.cos(angle) * 2.5;
                double behindZ = target.getZ() - Math.sin(angle) * 2.5;
                sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.5, 1.5, 0.5, 0.15);
                this.teleportTo(behindX, target.getY(), behindZ);
                sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.5, 1.5, 0.5, 0.15);
                target.hurt(this.damageSources().mobAttack(this), damage * 1.8f);
            }
            default -> target.hurt(this.damageSources().magic(), damage);
        }

        applyVenerableKillerMoveBonus(target, damage, sl);

        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0f, 0.8f);
        killerMoveCooldown = move.cooldownTicks() / 5;
        abilityCooldown = 20;
    }

    private void applyVenerableKillerMoveBonus(LivingEntity target, float damage, ServerLevel sl) {
        switch (venerableType) {
            case YUAN_SHI -> {
                List<LivingEntity> mobs = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0),
                    e -> e != this && !(e instanceof Player) && e instanceof net.minecraft.world.entity.Mob);
                for (LivingEntity entity : mobs) {
                    if (entity instanceof net.minecraft.world.entity.Mob mob && this.getTarget() != null) {
                        mob.setTarget(this.getTarget());
                    }
                }
            }
            case XING_XIU -> {
                double angle = random.nextDouble() * Math.PI * 2;
                double dist = 12.0 + random.nextDouble() * 4.0;
                double newX = target.getX() + Math.cos(angle) * dist;
                double newZ = target.getZ() + Math.sin(angle) * dist;
                sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 30, 0.5, 1.0, 0.5, 0.1);
                this.teleportTo(newX, getY(), newZ);
                sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 30, 0.5, 1.0, 0.5, 0.1);
            }
            case YUAN_LIAN -> {
                this.heal(this.getMaxHealth() * 0.20f);
                sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 2.0, getZ(), 10, 1.0, 1.0, 1.0, 0.05);
            }
            case WU_JI -> {
                target.setDeltaMovement(0, 0, 0);
                target.hurtMarked = true;
                lawFreezeTicks = 60;
                List<Player> nearby = this.level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(8.0));
                for (Player player : nearby) {
                    AttributeInstance atkSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
                    if (atkSpeed != null) {
                        atkSpeed.removeModifier(LAW_SLOW_MOD);
                        atkSpeed.addTransientModifier(new AttributeModifier(LAW_SLOW_MOD, -0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    }
                }
                sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0, target.getZ(), 40, 1.0, 2.0, 1.0, 0.1);
            }
            case KUANG_MAN -> {
                double knockDx = target.getX() - getX();
                double knockDz = target.getZ() - getZ();
                double dist = Math.sqrt(knockDx * knockDx + knockDz * knockDz);
                if (dist > 0.001) {
                    target.knockback(4.0, -knockDx / dist, -knockDz / dist);
                }
                target.setDeltaMovement(target.getDeltaMovement().add(0, 1.0, 0));
                target.hurtMarked = true;
            }
            case DAO_TIAN -> {
                this.setInvisible(true);
                invisTicks = 40;
                visibleTimer = 0;
                double behindAngle = Math.atan2(target.getZ() - getZ(), target.getX() - getX());
                double bx = target.getX() - Math.cos(behindAngle) * 2.0;
                double bz = target.getZ() - Math.sin(behindAngle) * 2.0;
                sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 20, 0.5, 1.0, 0.5, 0.1);
                this.teleportTo(bx, target.getY(), bz);
            }
            case JU_YANG -> {
                if (target instanceof ServerPlayer sp) {
                    VfxHelper.spawn(sp, VfxType.SKY_STRIKE, target.getX(), target.getY(), target.getZ(), 0, 1, 0, 0xFFFF8800, 2.0f, 30);
                }
                target.hurt(this.damageSources().magic(), target.getMaxHealth() * 0.10f);
            }
            case YOU_HUN -> {
                this.heal(damage * 0.50f);
                sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.5, getZ(), 20, 1.0, 1.0, 1.0, 0.05);
            }
            case LE_TU -> {
                List<LivingEntity> allies = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(10.0),
                    e -> e != this && !(e instanceof Player));
                for (LivingEntity ally : allies) {
                    ally.heal(ally.getMaxHealth() * 0.10f);
                }
                this.heal(this.getMaxHealth() * 0.10f);
                sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 2.0, getZ(), 15, 5.0, 1.0, 5.0, 0.03);
            }
            case HONG_LIAN -> {
                timeSlowTicks = 60;
                List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(8.0), e -> e != this);
                for (LivingEntity entity : nearby) {
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.1, 0.5, 0.1));
                    entity.hurtMarked = true;
                }
                sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0, getZ(), 40, 4.0, 2.0, 4.0, 0.02);
                this.level().playSound(null, blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 2.0f, 0.2f);
            }
        }
    }

    private ParticleOptions getPathParticle(DaoPath path) {
        if (path == null) return ParticleTypes.END_ROD;
        return switch (path) {
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case SOUL, DARK -> ParticleTypes.SCULK_SOUL;
            case LIGHT, STAR -> ParticleTypes.END_ROD;
            case BLOOD -> ParticleTypes.DAMAGE_INDICATOR;
            case POISON -> ParticleTypes.ITEM_SLIME;
            case WIND, FLIGHT -> ParticleTypes.CLOUD;
            case WATER -> ParticleTypes.SPLASH;
            case WOOD -> ParticleTypes.HAPPY_VILLAGER;
            case TIME, SPACE -> ParticleTypes.PORTAL;
            case STRENGTH, EARTH -> ParticleTypes.CRIT;
            case SHADOW -> ParticleTypes.SMOKE;
            default -> ParticleTypes.ENCHANT;
        };
    }

    // ======================== 蛊虫装备与杀招系统 ========================

    private void selectVenerableEquipment(VenerableType type) {
        equippedGu.clear();
        switch (type) {
            case YUAN_SHI -> {
                equippedGu.add(GuRegistry.id("true_qi_gu"));
                equippedGu.add(GuRegistry.id("qi_shield_gu"));
                equippedGu.add(GuRegistry.id("profound_qi_gu"));
                equippedGu.add(GuRegistry.id("enslave_worm_gu"));
                equippedGu.add(GuRegistry.id("enslave_shield_gu"));
                equippedGu.add(GuRegistry.id("feast_gu"));
                equippedGu.add(GuRegistry.id("enslave_snake_gu"));
            }
            case XING_XIU -> {
                equippedGu.add(GuRegistry.id("thought_gu"));
                equippedGu.add(GuRegistry.id("mind_guard_gu"));
                equippedGu.add(GuRegistry.id("heavens_eye_gu"));
                equippedGu.add(GuRegistry.id("starlight_gu"));
                equippedGu.add(GuRegistry.id("star_shield_gu"));
                equippedGu.add(GuRegistry.id("star_fall_gu"));
            }
            case YUAN_LIAN -> {
                equippedGu.add(GuRegistry.id("vitality_grass_gu"));
                equippedGu.add(GuRegistry.id("vitality_leaf_gu"));
                equippedGu.add(GuRegistry.id("self_heal_gu"));
                equippedGu.add(GuRegistry.id("flesh_bone_gu"));
                equippedGu.add(GuRegistry.id("stealth_scales_gu"));
                equippedGu.add(GuRegistry.id("moonlight_gu"));
                equippedGu.add(GuRegistry.id("white_boar_gu"));
            }
            case WU_JI -> {
                equippedGu.add(GuRegistry.id("rule_gu"));
                equippedGu.add(GuRegistry.id("order_gu"));
                equippedGu.add(GuRegistry.id("supreme_law_gu"));
                equippedGu.add(GuRegistry.id("seal_gu"));
                equippedGu.add(GuRegistry.id("restriction_gu"));
                equippedGu.add(GuRegistry.id("heaven_seal_gu"));
            }
            case KUANG_MAN -> {
                equippedGu.add(GuRegistry.id("bear_strength_gu"));
                equippedGu.add(GuRegistry.id("savage_bull_gu"));
                equippedGu.add(GuRegistry.id("taishan_gu"));
                equippedGu.add(GuRegistry.id("giant_strength_gu"));
                equippedGu.add(GuRegistry.id("morph_gu"));
                equippedGu.add(GuRegistry.id("heaven_change_gu"));
                equippedGu.add(GuRegistry.id("shrink_ground_gu"));
                equippedGu.add(GuRegistry.id("enslave_snake_gu"));
            }
            case DAO_TIAN -> {
                equippedGu.add(GuRegistry.id("steal_qi_gu"));
                equippedGu.add(GuRegistry.id("steal_hide_gu"));
                equippedGu.add(GuRegistry.id("heaven_steal_gu"));
                equippedGu.add(GuRegistry.id("displacement_gu"));
                equippedGu.add(GuRegistry.id("warp_gu"));
                equippedGu.add(GuRegistry.id("space_barrier_gu"));
            }
            case JU_YANG -> {
                equippedGu.add(GuRegistry.id("lucky_gu"));
                equippedGu.add(GuRegistry.id("misfortune_ward_gu"));
                equippedGu.add(GuRegistry.id("heavens_secret_gu"));
                equippedGu.add(GuRegistry.id("blood_spear_gu"));
                equippedGu.add(GuRegistry.id("blood_sacrifice_gu"));
                equippedGu.add(GuRegistry.id("blood_shield_gu"));
                equippedGu.add(GuRegistry.id("gold_light_worm"));
            }
            case YOU_HUN -> {
                equippedGu.add(GuRegistry.id("soul_search_gu"));
                equippedGu.add(GuRegistry.id("soul_shield_gu"));
                equippedGu.add(GuRegistry.id("soul_crush_gu"));
                equippedGu.add(GuRegistry.id("shadow_cloak_gu"));
                equippedGu.add(GuRegistry.id("shadow_blade_gu"));
                equippedGu.add(GuRegistry.id("shadow_step_gu"));
            }
            case LE_TU -> {
                equippedGu.add(GuRegistry.id("earth_wall_gu"));
                equippedGu.add(GuRegistry.id("earth_spike_gu"));
                equippedGu.add(GuRegistry.id("mountain_guard_gu"));
                equippedGu.add(GuRegistry.id("heaven_will_gu"));
                equippedGu.add(GuRegistry.id("heaven_seal_gu"));
                equippedGu.add(GuRegistry.id("heaven_decree_gu"));
            }
            case HONG_LIAN -> {
                equippedGu.add(GuRegistry.id("time_decel_gu"));
                equippedGu.add(GuRegistry.id("time_shield_gu"));
                equippedGu.add(GuRegistry.id("time_reversal_gu"));
                equippedGu.add(GuRegistry.id("spring_autumn_cicada"));
                equippedGu.add(GuRegistry.id("warp_gu"));
                equippedGu.add(GuRegistry.id("space_barrier_gu"));
            }
        }
    }

    private void matchVenerableMoves() {
        availableMoves.clear();
        DaoPath primaryPath = venerableType.getPrimaryPath();
        DaoPath secondaryPath = venerableType.getSecondaryPath();

        for (KillerMove move : KillerMoveRegistry.getAll()) {
            if (move.primaryPath() != primaryPath && move.primaryPath() != secondaryPath) continue;
            boolean hasAllGu = true;
            for (ResourceLocation reqGu : move.getAllRequiredGu()) {
                if (!equippedGu.contains(reqGu)) {
                    hasAllGu = false;
                    break;
                }
            }
            if (hasAllGu) {
                availableMoves.add(move);
            }
        }

        if (availableMoves.isEmpty()) {
            for (KillerMove move : KillerMoveRegistry.getAll()) {
                if (move.primaryPath() == primaryPath || move.primaryPath() == secondaryPath) {
                    availableMoves.add(move);
                    if (availableMoves.size() >= 3) break;
                }
            }
        }
    }

    // ======================== 通用系统 ========================

    private void onPhaseTransition(int oldPhase, int newPhase) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        if (oldPhase == 1 && newPhase == 2) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 50, 2.0, 2.0, 2.0, 0.05);
            this.level().playSound(null, blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 2.0f, 1.0f);
            AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(PHASE_SPEED_MOD);
                speedAttr.addTransientModifier(new AttributeModifier(PHASE_SPEED_MOD, 0.06, AttributeModifier.Operation.ADD_VALUE));
            }
            phaseSpeedBoostTicks = 600;

            if (venerableType == VenerableType.KUANG_MAN) {
                applyMod(Attributes.ATTACK_DAMAGE, SAVAGE_BOOST_MOD, venerableType.attackDamage * 0.20);
                applyMod(Attributes.MOVEMENT_SPEED, SAVAGE_SPEED_MOD, 0.04);
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.0, getZ(), 2, 1.0, 1.0, 1.0, 0.0);
                this.level().playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.5f, 0.5f);
            }

            if (venerableType == VenerableType.XING_XIU) {
                comboStep = 0;
            }
        } else if (oldPhase == 2 && newPhase == 3) {
            sl.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.5, getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0, getZ(), 10, 3.0, 2.0, 3.0, 0.1);
            this.level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.5f, 0.5f);

            comboStep = 0;

            if (venerableType == VenerableType.KUANG_MAN) {
                applyMod(Attributes.ATTACK_DAMAGE, SAVAGE_BOOST_MOD, venerableType.attackDamage * 0.40);
                applyMod(Attributes.MOVEMENT_SPEED, SAVAGE_SPEED_MOD, 0.08);
            }
        }
    }

    private void tickPhaseSpeed() {
        if (phaseSpeedBoostTicks > 0) {
            phaseSpeedBoostTicks--;
            if (phaseSpeedBoostTicks == 0) {
                AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr != null) speedAttr.removeModifier(PHASE_SPEED_MOD);
            }
        }
    }

    private void tickAuraEffects() {
        if (this.tickCount % 40 != 0) return;
        if (!(this.level() instanceof ServerLevel sl)) return;

        switch (venerableType) {
            case YUAN_SHI -> sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.5, getZ(), 10, 1.5, 1.5, 1.5, 0.02);
            case XING_XIU -> sl.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0, getZ(), 12, 2.0, 2.0, 2.0, 0.01);
            case YUAN_LIAN -> sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.0, getZ(), 8, 1.5, 1.0, 1.5, 0.02);
            case WU_JI -> sl.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 2.0, getZ(), 15, 1.5, 2.0, 1.5, 0.05);
            case KUANG_MAN -> sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.0, getZ(), 10, 1.0, 1.0, 1.0, 0.1);
            case DAO_TIAN -> sl.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.0, getZ(), 12, 1.0, 1.5, 1.0, 0.05);
            case JU_YANG -> sl.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.5, getZ(), 10, 1.0, 1.0, 1.0, 0.02);
            case YOU_HUN -> sl.sendParticles(ParticleTypes.SCULK_SOUL, getX(), getY() + 1.0, getZ(), 10, 1.5, 1.5, 1.5, 0.02);
            case LE_TU -> sl.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.5, getZ(), 8, 1.5, 1.0, 1.5, 0.01);
            case HONG_LIAN -> sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 10, 1.0, 1.5, 1.0, 0.02);
        }
    }

    // ======================== 数据存储 ========================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("VenerableType", venerableType.name());
        tag.putInt("CurrentPhase", currentPhase);
        tag.putBoolean("HasUsedCicada", hasUsedCicada);
        tag.putFloat("SavedHP", savedHP);
        tag.putBoolean("LeTuAggro", leTuAggro);
        tag.putInt("LeTuHitCount", leTuHitCount);
        tag.putBoolean("CicadaEnraged", cicadaEnraged);
        tag.putString("KuangManForm", currentForm.name());
        tag.putInt("FormDuration", formDurationTicks);
        tag.putInt("FormCooldown", formCooldownTicks);
        tag.putBoolean("WuJiImmortal1", wuJiImmortalUsed1);
        tag.putBoolean("WuJiImmortal2", wuJiImmortalUsed2);
        tag.putBoolean("YouHunSoulSplit", youHunSoulSplitUsed);
        tag.putBoolean("LotusRevive1", yuanLianLotusReviveUsed1);
        tag.putBoolean("LotusRevive2", yuanLianLotusReviveUsed2);
        tag.putBoolean("LotusRevive3", yuanLianLotusReviveUsed3);
        ListTag guList = new ListTag();
        for (ResourceLocation guId : equippedGu) {
            guList.add(StringTag.valueOf(guId.toString()));
        }
        tag.put("EquippedGu", guList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("VenerableType")) {
            VenerableType type = VenerableType.fromName(tag.getString("VenerableType"));
            setVenerableType(type);
        }
        if (tag.contains("CurrentPhase")) currentPhase = tag.getInt("CurrentPhase");
        if (tag.contains("HasUsedCicada")) hasUsedCicada = tag.getBoolean("HasUsedCicada");
        if (tag.contains("SavedHP")) savedHP = tag.getFloat("SavedHP");
        if (tag.contains("LeTuAggro")) leTuAggro = tag.getBoolean("LeTuAggro");
        if (tag.contains("LeTuHitCount")) leTuHitCount = tag.getInt("LeTuHitCount");
        if (tag.contains("CicadaEnraged")) cicadaEnraged = tag.getBoolean("CicadaEnraged");
        if (tag.contains("KuangManForm")) {
            try { currentForm = KuangManForm.valueOf(tag.getString("KuangManForm")); } catch (Exception e) { currentForm = KuangManForm.HUMAN; }
        }
        if (tag.contains("FormDuration")) formDurationTicks = tag.getInt("FormDuration");
        if (tag.contains("FormCooldown")) formCooldownTicks = tag.getInt("FormCooldown");
        if (tag.contains("WuJiImmortal1")) wuJiImmortalUsed1 = tag.getBoolean("WuJiImmortal1");
        if (tag.contains("WuJiImmortal2")) wuJiImmortalUsed2 = tag.getBoolean("WuJiImmortal2");
        if (tag.contains("YouHunSoulSplit")) youHunSoulSplitUsed = tag.getBoolean("YouHunSoulSplit");
        if (tag.contains("LotusRevive1")) yuanLianLotusReviveUsed1 = tag.getBoolean("LotusRevive1");
        if (tag.contains("LotusRevive2")) yuanLianLotusReviveUsed2 = tag.getBoolean("LotusRevive2");
        if (tag.contains("LotusRevive3")) yuanLianLotusReviveUsed3 = tag.getBoolean("LotusRevive3");
        if (tag.contains("EquippedGu")) {
            equippedGu.clear();
            ListTag guList = tag.getList("EquippedGu", Tag.TAG_STRING);
            for (int i = 0; i < guList.size(); i++) {
                equippedGu.add(ResourceLocation.parse(guList.getString(i)));
            }
            matchVenerableMoves();
        }
    }

    // ======================== Boss事件 ========================

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

    // ======================== 掉落 ========================

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        this.spawnAtLocation(new ItemStack(ModItems.BREAKTHROUGH_STONE.get(), 3));
        this.spawnAtLocation(new ItemStack(ModItems.PRIMEVAL_STONE.get(), 10 + random.nextInt(11)));
        this.spawnAtLocation(new ItemStack(ModItems.LORE_SCROLL.get(), 2));

        for (int i = 0; i < 3; i++) {
            Item drop = RARE_GU_DROPS.get(random.nextInt(RARE_GU_DROPS.size()));
            this.spawnAtLocation(new ItemStack(drop));
        }
    }

    @Override
    protected int getBaseExperienceReward() {
        return 200;
    }

    // ======================== 音效 ========================

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
