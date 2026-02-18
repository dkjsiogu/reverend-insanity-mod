package com.reverendinsanity.entity;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.combat.DotManager;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.KillerMoveRegistry;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.core.faction.Faction;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModEntities;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// NPC蛊师Boss：道路分配+蛊虫装备+杀招序列+多阶段战斗的蛊修敌对实体
public class GuMasterEntity extends Monster {

    private static final EntityDataAccessor<Integer> GU_RANK =
        SynchedEntityData.defineId(GuMasterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> PRIMARY_PATH =
        SynchedEntityData.defineId(GuMasterEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SECONDARY_PATH =
        SynchedEntityData.defineId(GuMasterEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_FACTION =
        SynchedEntityData.defineId(GuMasterEntity.class, EntityDataSerializers.INT);

    private static final ResourceLocation SLOW_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_master_slow");
    private static final ResourceLocation PHASE_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_master_phase_speed");
    private static final ResourceLocation PATH_ATK_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_master_path_atk");
    private static final ResourceLocation PATH_ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_master_path_armor");
    private static final ResourceLocation PATH_SPEED_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_master_path_speed");
    private static final ResourceLocation PATH_HP_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_master_path_hp");
    private static final ResourceLocation PATH_TOUGH_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_master_path_tough");

    private static final Set<DaoPath> NON_COMBAT_PATHS = Set.of(
        DaoPath.REFINEMENT, DaoPath.PILL, DaoPath.INFORMATION, DaoPath.FOOD, DaoPath.PAINT
    );

    private static final Set<DaoPath> MELEE_PATHS = Set.of(
        DaoPath.STRENGTH, DaoPath.METAL, DaoPath.BONE, DaoPath.EARTH
    );
    private static final Set<DaoPath> RANGED_PATHS = Set.of(
        DaoPath.MOON, DaoPath.STAR, DaoPath.LIGHT, DaoPath.FIRE, DaoPath.ICE, DaoPath.LIGHTNING
    );
    private static final Set<DaoPath> CONTROL_PATHS = Set.of(
        DaoPath.SOUL, DaoPath.DARK, DaoPath.SHADOW, DaoPath.ILLUSION, DaoPath.DREAM, DaoPath.CHARM
    );
    private static final Set<DaoPath> SUPPORT_PATHS = Set.of(
        DaoPath.BLOOD, DaoPath.POISON, DaoPath.WOOD, DaoPath.WATER
    );
    private static final Set<DaoPath> RUSH_PATHS = Set.of(
        DaoPath.WIND, DaoPath.FLIGHT, DaoPath.SWORD, DaoPath.BLADE
    );

    private static final Set<DaoPath> RIGHTEOUS_PATHS = Set.of(
        DaoPath.LIGHT, DaoPath.HUMAN, DaoPath.RULE, DaoPath.HEAVEN, DaoPath.FORMATION, DaoPath.PILL, DaoPath.WOOD, DaoPath.WATER
    );
    private static final Set<DaoPath> DEMONIC_PATHS = Set.of(
        DaoPath.BLOOD, DaoPath.KILL, DaoPath.DARK, DaoPath.POISON, DaoPath.SOUL, DaoPath.ENSLAVE, DaoPath.BONE, DaoPath.STEAL
    );

    private int abilityCooldown = 60;
    private int killerMoveCooldown = 0;
    private int currentPhase = 1;
    private boolean hasHealed = false;
    private int slowTargetTicks = 0;
    private LivingEntity slowedTarget = null;
    private int phaseSpeedBoostTicks = 0;

    private ServerBossEvent bossEvent = null;

    private final List<ResourceLocation> equippedGu = new ArrayList<>();
    private final List<KillerMove> availableMoves = new ArrayList<>();

    public GuMasterEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.MOVEMENT_SPEED, 0.36)
            .add(Attributes.ARMOR, 10.0)
            .add(Attributes.ARMOR_TOUGHNESS, 0.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
            .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GU_RANK, 1);
        builder.define(PRIMARY_PATH, "");
        builder.define(SECONDARY_PATH, "");
        builder.define(DATA_FACTION, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);

        float roll = this.random.nextFloat();
        int rank;
        if (roll < 0.60f) {
            rank = 1;
        } else if (roll < 0.90f) {
            rank = 2;
        } else {
            rank = 3;
        }
        setGuRank(rank);

        List<DaoPath> combatPaths = new ArrayList<>();
        for (DaoPath p : DaoPath.values()) {
            if (!NON_COMBAT_PATHS.contains(p)) combatPaths.add(p);
        }

        DaoPath primary = combatPaths.get(this.random.nextInt(combatPaths.size()));
        setPrimaryPath(primary.name());

        if (RIGHTEOUS_PATHS.contains(primary)) {
            setFaction(Faction.RIGHTEOUS);
        } else if (DEMONIC_PATHS.contains(primary)) {
            setFaction(Faction.DEMONIC);
        } else {
            setFaction(Faction.INDEPENDENT);
        }

        if (rank >= 2 && (rank == 3 || this.random.nextFloat() < 0.5f)) {
            combatPaths.remove(primary);
            DaoPath secondary = combatPaths.get(this.random.nextInt(combatPaths.size()));
            setSecondaryPath(secondary.name());
        }

        applyRankAttributes(rank);
        applyPathAttributes(primary);
        selectGuEquipment(rank, primary);
        matchKillerMoves(rank);

        if (rank == 3) {
            initBossEvent();
        }

        return spawnData;
    }

    private void selectGuEquipment(int rank, DaoPath primary) {
        equippedGu.clear();
        List<GuType> pathGu = GuRegistry.getByPath(primary);
        List<GuType> usable = pathGu.stream()
            .filter(g -> g.rank() <= rank)
            .collect(Collectors.toList());

        int guCount = switch (rank) {
            case 1 -> 2 + random.nextInt(2);
            case 2 -> 3 + random.nextInt(2);
            case 3 -> 4 + random.nextInt(3);
            default -> 2;
        };

        List<GuType> selected = new ArrayList<>();
        List<GuType> pool = new ArrayList<>(usable);
        for (int i = 0; i < guCount && !pool.isEmpty(); i++) {
            GuType pick = pool.remove(random.nextInt(pool.size()));
            selected.add(pick);
            equippedGu.add(pick.id());
        }

        DaoPath secondary = getSecondaryDaoPath();
        if (secondary != null && rank >= 2) {
            List<GuType> secGu = GuRegistry.getByPath(secondary).stream()
                .filter(g -> g.rank() <= rank)
                .collect(Collectors.toList());
            int secCount = 1 + random.nextInt(2);
            List<GuType> secPool = new ArrayList<>(secGu);
            for (int i = 0; i < secCount && !secPool.isEmpty(); i++) {
                GuType pick = secPool.remove(random.nextInt(secPool.size()));
                equippedGu.add(pick.id());
            }
        }
    }

    private void matchKillerMoves(int rank) {
        availableMoves.clear();
        DaoPath primary = getPrimaryDaoPath();
        if (primary == null) return;

        for (KillerMove move : KillerMoveRegistry.getAll()) {
            if (move.minRank() > rank) continue;
            if (move.primaryPath() != primary) continue;
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

        if (availableMoves.isEmpty() && rank >= 2) {
            for (KillerMove move : KillerMoveRegistry.getAll()) {
                if (move.minRank() > rank) continue;
                if (move.primaryPath() != primary) continue;
                availableMoves.add(move);
                if (availableMoves.size() >= 2) break;
            }
        }
    }

    private void initBossEvent() {
        DaoPath path = getPrimaryDaoPath();
        String pathName = path != null ? path.getDisplayName() : "\u65e0\u540d";
        Faction faction = getFaction();
        BossEvent.BossBarColor barColor = switch (faction) {
            case RIGHTEOUS -> BossEvent.BossBarColor.BLUE;
            case DEMONIC -> BossEvent.BossBarColor.RED;
            case INDEPENDENT -> BossEvent.BossBarColor.YELLOW;
        };
        bossEvent = new ServerBossEvent(
            Component.literal("[" + faction.getDisplayName() + "] \u86ca\u5e08\u00b7" + pathName),
            barColor, BossEvent.BossBarOverlay.PROGRESS
        );
        bossEvent.setVisible(true);
    }

    private void applyRankAttributes(int rank) {
        double hp, atk, speed, armor, knockback;
        switch (rank) {
            case 2 -> { hp = 60; atk = 7; speed = 0.33; armor = 6; knockback = 0.2; }
            case 3 -> { hp = 120; atk = 14; speed = 0.36; armor = 12; knockback = 0.5; }
            default -> { hp = 30; atk = 4; speed = 0.3; armor = 2; knockback = 0.0; }
        }
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        this.setHealth((float) hp);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(atk);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        this.getAttribute(Attributes.ARMOR).setBaseValue(armor);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(knockback);
    }

    private void applyPathAttributes(DaoPath path) {
        if (path == DaoPath.STRENGTH) {
            addTransientMod(Attributes.ATTACK_DAMAGE, PATH_ATK_MOD, 3.0, AttributeModifier.Operation.ADD_VALUE);
            addTransientMod(Attributes.ARMOR, PATH_ARMOR_MOD, 2.0, AttributeModifier.Operation.ADD_VALUE);
        } else if (path == DaoPath.METAL) {
            addTransientMod(Attributes.ARMOR, PATH_ARMOR_MOD, 4.0, AttributeModifier.Operation.ADD_VALUE);
            addTransientMod(Attributes.ARMOR_TOUGHNESS, PATH_TOUGH_MOD, 3.0, AttributeModifier.Operation.ADD_VALUE);
        } else if (path == DaoPath.WIND || path == DaoPath.FLIGHT) {
            addTransientMod(Attributes.MOVEMENT_SPEED, PATH_SPEED_MOD, 0.04, AttributeModifier.Operation.ADD_VALUE);
        } else if (path == DaoPath.BLOOD) {
            addTransientMod(Attributes.MAX_HEALTH, PATH_HP_MOD, 20.0, AttributeModifier.Operation.ADD_VALUE);
            this.setHealth(this.getMaxHealth());
        } else if (path == DaoPath.DARK || path == DaoPath.SHADOW) {
            addTransientMod(Attributes.MOVEMENT_SPEED, PATH_SPEED_MOD, 0.02, AttributeModifier.Operation.ADD_VALUE);
        } else if (path == DaoPath.EARTH || path == DaoPath.BONE) {
            addTransientMod(Attributes.ARMOR, PATH_ARMOR_MOD, 3.0, AttributeModifier.Operation.ADD_VALUE);
        } else if (path == DaoPath.SWORD || path == DaoPath.BLADE || path == DaoPath.KILL) {
            addTransientMod(Attributes.ATTACK_DAMAGE, PATH_ATK_MOD, 2.0, AttributeModifier.Operation.ADD_VALUE);
        } else if (path == DaoPath.FIRE || path == DaoPath.LIGHTNING) {
            addTransientMod(Attributes.ATTACK_DAMAGE, PATH_ATK_MOD, 1.5, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    private void addTransientMod(Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id, double value, AttributeModifier.Operation op) {
        AttributeInstance inst = this.getAttribute(attr);
        if (inst != null) {
            inst.removeModifier(id);
            inst.addTransientModifier(new AttributeModifier(id, value, op));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) return;

        if (bossEvent != null) {
            bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }

        tickSlowTimer();
        tickPhaseSpeedBoost();

        int newPhase = getHealth() > getMaxHealth() * 0.6f ? 1 :
                       getHealth() > getMaxHealth() * 0.3f ? 2 : 3;
        if (newPhase != currentPhase) {
            onPhaseTransition(currentPhase, newPhase);
            currentPhase = newPhase;
        }

        if (this.tickCount % 40 == 0 && this.level() instanceof ServerLevel sl) {
            spawnPathAuraParticles(sl);
        }

        if (abilityCooldown > 0) abilityCooldown--;
        if (killerMoveCooldown > 0) killerMoveCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        if (abilityCooldown > 0) return;

        if (currentPhase >= 2 && killerMoveCooldown <= 0 && !availableMoves.isEmpty()) {
            float moveChance = currentPhase == 3 ? 0.35f : 0.20f;
            if (this.random.nextFloat() < moveChance) {
                executeKillerMove(target);
                return;
            }
        }

        DaoPath path = getPrimaryDaoPath();
        if (path == null) {
            performGenericAttack(target);
            return;
        }

        CombatArchetype archetype = getArchetype(path);
        int baseCooldown = switch (currentPhase) {
            case 2 -> 25 + random.nextInt(15);
            case 3 -> 15 + random.nextInt(10);
            default -> 40 + random.nextInt(20);
        };

        boolean used = switch (archetype) {
            case MELEE -> performMeleeAttack(target, baseCooldown);
            case RANGED -> performRangedAttack(target, baseCooldown);
            case CONTROL -> performControlAttack(target, baseCooldown);
            case SUPPORT -> performSupportAttack(target, baseCooldown);
            case RUSH -> performRushAttack(target, baseCooldown);
            default -> performGenericAttack(target);
        };

        if (!used) {
            abilityCooldown = 20;
        }
    }

    private void executeKillerMove(LivingEntity target) {
        KillerMove move = availableMoves.get(this.random.nextInt(availableMoves.size()));
        if (!(this.level() instanceof ServerLevel sl)) return;

        float power = move.power();
        float hpPercent = power / 200f;
        float damage = target.getMaxHealth() * hpPercent;
        damage = Math.min(damage, 40.0f);
        damage = Math.max(damage, 6.0f);

        DaoPath path = move.primaryPath();
        ParticleOptions particle = getPathParticle(path);

        sl.sendParticles(particle, this.getX(), this.getY() + 1.5, this.getZ(), 60, 3.0, 2.0, 3.0, 0.08);
        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 5, 2.0, 1.0, 2.0, 0.05);

        switch (move.moveType()) {
            case ATTACK, ULTIMATE -> {
                target.hurt(this.damageSources().magic(), damage);
                List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(4.0), e -> e != this && e != target);
                float splashDmg = damage * 0.4f;
                for (LivingEntity e : nearby) {
                    e.hurt(this.damageSources().magic(), splashDmg);
                }
                if (path == DaoPath.FIRE || path == DaoPath.LIGHTNING) {
                    target.igniteForTicks(80);
                }
                sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 25, 1.0, 1.0, 1.0, 0.3);
            }
            case DEFENSE, BUFF -> {
                this.heal(this.getMaxHealth() * 0.15f);
                sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 2.0, this.getZ(), 15, 1.0, 1.0, 1.0, 0.1);
                target.hurt(this.damageSources().magic(), damage * 0.5f);
            }
            case CONTROL, DEBUFF -> {
                target.hurt(this.damageSources().magic(), damage * 0.6f);
                applySlow(target, -0.08, 120);
                target.setDeltaMovement(0, 0.5, 0);
                target.hurtMarked = true;
                sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 30, 1.5, 1.0, 1.5, 0.02);
            }
            case MOVEMENT -> {
                double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                double behindX = target.getX() - Math.cos(angle) * 2.0;
                double behindZ = target.getZ() - Math.sin(angle) * 2.0;
                sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.1);
                this.teleportTo(behindX, target.getY(), behindZ);
                sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.1);
                target.hurt(this.damageSources().mobAttack(this), damage * 1.5f);
            }
            default -> {
                target.hurt(this.damageSources().magic(), damage);
            }
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.5f, 1.0f);

        killerMoveCooldown = move.cooldownTicks() / 4;
        abilityCooldown = 30;
    }

    private void onPhaseTransition(int oldPhase, int newPhase) {
        if (!(this.level() instanceof ServerLevel sl)) return;

        if (newPhase == 2) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 40, 2.0, 2.0, 2.0, 0.05);
            this.level().playSound(null, this.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 1.2f);
            applyPhaseSpeedBoost(0.04, 400);
        } else if (newPhase == 3) {
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 8, 2.0, 1.5, 2.0, 0.1);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 60, 3.0, 2.0, 3.0, 0.05);
            this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.0f, 0.6f);
            applyPhaseSpeedBoost(0.06, 600);
        }
    }

    private void applyPhaseSpeedBoost(double amount, int ticks) {
        AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(PHASE_SPEED_MOD);
            speedAttr.addTransientModifier(new AttributeModifier(PHASE_SPEED_MOD, amount, AttributeModifier.Operation.ADD_VALUE));
        }
        phaseSpeedBoostTicks = ticks;
    }

    private void tickPhaseSpeedBoost() {
        if (phaseSpeedBoostTicks > 0) {
            phaseSpeedBoostTicks--;
            if (phaseSpeedBoostTicks == 0) {
                AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr != null) speedAttr.removeModifier(PHASE_SPEED_MOD);
            }
        }
    }

    private void tickSlowTimer() {
        if (slowTargetTicks > 0) {
            slowTargetTicks--;
            if (slowTargetTicks == 0 && slowedTarget != null) {
                AttributeInstance speedAttr = slowedTarget.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speedAttr != null) speedAttr.removeModifier(SLOW_MOD);
                slowedTarget = null;
            }
        }
    }

    private void applySlow(LivingEntity target, double amount, int ticks) {
        if (slowedTarget != null && slowedTarget != target) {
            AttributeInstance oldSpeed = slowedTarget.getAttribute(Attributes.MOVEMENT_SPEED);
            if (oldSpeed != null) oldSpeed.removeModifier(SLOW_MOD);
        }
        AttributeInstance speedAttr = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SLOW_MOD);
            speedAttr.addTransientModifier(new AttributeModifier(SLOW_MOD, amount, AttributeModifier.Operation.ADD_VALUE));
        }
        slowedTarget = target;
        slowTargetTicks = ticks;
    }

    // ==================== 近战型道路 ====================
    private boolean performMeleeAttack(LivingEntity target, int baseCooldown) {
        float roll = this.random.nextFloat();
        if (currentPhase == 1) {
            if (roll < 0.35f) {
                heavyStrike(target);
                abilityCooldown = baseCooldown;
                return true;
            }
        } else if (currentPhase == 2) {
            if (roll < 0.30f) {
                heavyStrike(target);
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.55f) {
                earthquakeWave();
                abilityCooldown = baseCooldown + 10;
                return true;
            }
        } else {
            if (roll < 0.25f) {
                heavyStrike(target);
                abilityCooldown = baseCooldown - 5;
                return true;
            } else if (roll < 0.50f) {
                earthquakeWave();
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.75f) {
                berserkerCombo(target);
                abilityCooldown = baseCooldown + 5;
                return true;
            }
        }
        return false;
    }

    private void heavyStrike(LivingEntity target) {
        if (this.distanceTo(target) > 4.0) return;
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.8f;
        target.hurt(this.damageSources().mobAttack(this), dmg);
        target.knockback(2.0, this.getX() - target.getX(), this.getZ() - target.getZ());
        target.hurtMarked = true;
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 0.5, 0.5, 0.2);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0f, 0.6f);
    }

    private void earthquakeWave() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5, this.getZ(), 12, 3.0, 0.5, 3.0, 0.05);
        sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY() + 0.2, this.getZ(), 30, 4.0, 0.3, 4.0, 0.1);
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5f, 0.5f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0), e -> e != this);
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().mobAttack(this), 8.0f);
            e.setDeltaMovement(e.getDeltaMovement().add(0, 0.6, 0));
            e.hurtMarked = true;
        }
    }

    private void berserkerCombo(LivingEntity target) {
        if (this.distanceTo(target) > 4.0) return;
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (int i = 0; i < 3; i++) {
            target.hurt(this.damageSources().mobAttack(this), dmg * 0.7f);
        }
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2f, 0.8f);
    }

    // ==================== 远程型道路 ====================
    private boolean performRangedAttack(LivingEntity target, int baseCooldown) {
        float roll = this.random.nextFloat();
        if (currentPhase == 1) {
            if (roll < 0.40f) {
                shootProjectile(target);
                abilityCooldown = baseCooldown;
                return true;
            }
        } else if (currentPhase == 2) {
            if (roll < 0.30f) {
                shootProjectile(target);
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.55f) {
                fanBarrage(target);
                abilityCooldown = baseCooldown + 5;
                return true;
            }
        } else {
            if (roll < 0.25f) {
                shootProjectile(target);
                abilityCooldown = baseCooldown - 5;
                return true;
            } else if (roll < 0.50f) {
                fanBarrage(target);
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.75f) {
                areaBombardment();
                abilityCooldown = baseCooldown + 10;
                return true;
            }
        }
        return false;
    }

    private void shootProjectile(LivingEntity target) {
        DaoPath path = getPrimaryDaoPath();
        boolean useMoonBlade = (path == DaoPath.MOON || path == DaoPath.ICE || path == DaoPath.STAR);

        if (useMoonBlade) {
            MoonBladeEntity blade = new MoonBladeEntity(ModEntities.MOON_BLADE.get(), this.level());
            blade.setOwner(this);
            blade.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            blade.shoot(dx, dy, dz, 1.8f, 4.0f);
            this.level().addFreshEntity(blade);
        } else {
            GoldBeamEntity beam = new GoldBeamEntity(ModEntities.GOLD_BEAM.get(), this.level());
            beam.setOwner(this);
            beam.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            beam.shoot(dx, dy, dz, 2.2f, 3.0f);
            this.level().addFreshEntity(beam);
        }
    }

    private void fanBarrage(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) return;
        dx /= len;
        dz /= len;

        int count = currentPhase == 3 ? 5 : 3;
        double[][] offsets = new double[count][];
        offsets[0] = new double[]{0, 0};
        offsets[1] = new double[]{dz * 0.3, -dx * 0.3};
        offsets[2] = new double[]{-dz * 0.3, dx * 0.3};
        if (count >= 5) {
            offsets[3] = new double[]{dz * 0.6, -dx * 0.6};
            offsets[4] = new double[]{-dz * 0.6, dx * 0.6};
        }

        DaoPath path = getPrimaryDaoPath();
        boolean useMoonBlade = (path == DaoPath.MOON || path == DaoPath.ICE || path == DaoPath.STAR);

        for (double[] off : offsets) {
            double sdx = target.getX() + off[0] * 3.0 - this.getX();
            double sdy = target.getEyeY() - this.getEyeY();
            double sdz = target.getZ() + off[1] * 3.0 - this.getZ();

            if (useMoonBlade) {
                MoonBladeEntity blade = new MoonBladeEntity(ModEntities.MOON_BLADE.get(), this.level());
                blade.setOwner(this);
                blade.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
                blade.shoot(sdx, sdy, sdz, 1.8f, 3.0f);
                this.level().addFreshEntity(blade);
            } else {
                GoldBeamEntity beam = new GoldBeamEntity(ModEntities.GOLD_BEAM.get(), this.level());
                beam.setOwner(this);
                beam.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
                beam.shoot(sdx, sdy, sdz, 2.2f, 2.0f);
                this.level().addFreshEntity(beam);
            }
        }
    }

    private void areaBombardment() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        DaoPath path = getPrimaryDaoPath();
        ParticleOptions particle = getPathParticle(path);

        sl.sendParticles(particle, this.getX(), this.getY() + 2.0, this.getZ(), 50, 5.0, 2.0, 5.0, 0.05);
        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 6, 3.0, 1.0, 3.0, 0.1);
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5f, 0.7f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0), e -> e != this);
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().magic(), 10.0f);
            if (path == DaoPath.FIRE) e.igniteForTicks(60);
        }
    }

    // ==================== 控制型道路 ====================
    private boolean performControlAttack(LivingEntity target, int baseCooldown) {
        float roll = this.random.nextFloat();
        if (currentPhase == 1) {
            if (roll < 0.35f) {
                singleSlow(target);
                abilityCooldown = baseCooldown;
                return true;
            }
        } else if (currentPhase == 2) {
            if (roll < 0.25f) {
                singleSlow(target);
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.50f) {
                teleportBehindTarget(target);
                abilityCooldown = baseCooldown + 5;
                return true;
            }
        } else {
            if (roll < 0.20f) {
                singleSlow(target);
                abilityCooldown = baseCooldown - 5;
                return true;
            } else if (roll < 0.40f) {
                teleportBehindTarget(target);
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.65f) {
                soulShockwave();
                abilityCooldown = baseCooldown + 5;
                return true;
            }
        }
        return false;
    }

    private void singleSlow(LivingEntity target) {
        applySlow(target, -0.05, 100);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SCULK_SOUL, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
        }
        this.level().playSound(null, target.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    private void teleportBehindTarget(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.1);

        double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
        double behindX = target.getX() - Math.cos(angle) * 2.0;
        double behindZ = target.getZ() - Math.sin(angle) * 2.0;
        this.teleportTo(behindX, target.getY(), behindZ);

        sl.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.5, 1.0, 0.5, 0.1);
        target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.3f);
    }

    private void soulShockwave() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 1.0, this.getZ(), 40, 4.0, 2.0, 4.0, 0.02);
        sl.sendParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.0, this.getZ(), 3, 2.0, 0.5, 2.0, 0.0);
        this.level().playSound(null, this.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.5f, 0.8f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0), e -> e != this);
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().magic(), 10.0f);
            e.setDeltaMovement(e.getDeltaMovement().add(0, 0.5, 0));
            e.hurtMarked = true;
        }
    }

    // ==================== 辅助型道路 ====================
    private boolean performSupportAttack(LivingEntity target, int baseCooldown) {
        float roll = this.random.nextFloat();
        if (currentPhase == 1) {
            if (roll < 0.30f && !hasHealed && this.getHealth() < this.getMaxHealth() * 0.8f) {
                selfHeal();
                abilityCooldown = baseCooldown + 20;
                return true;
            }
        } else if (currentPhase == 2) {
            if (roll < 0.25f && this.getHealth() < this.getMaxHealth() * 0.6f) {
                selfHeal();
                hasHealed = true;
                abilityCooldown = baseCooldown + 20;
                return true;
            } else if (roll < 0.50f) {
                dotAttack(target);
                abilityCooldown = baseCooldown;
                return true;
            }
        } else {
            if (roll < 0.20f) {
                dotAttack(target);
                abilityCooldown = baseCooldown - 5;
                return true;
            } else if (roll < 0.40f && this.getHealth() < this.getMaxHealth() * 0.4f) {
                selfHeal();
                abilityCooldown = baseCooldown + 10;
                return true;
            } else if (roll < 0.60f) {
                desperateExplosion();
                abilityCooldown = baseCooldown + 15;
                return true;
            }
        }
        return false;
    }

    private void selfHeal() {
        float healAmount = this.getMaxHealth() * 0.1f;
        this.heal(healAmount);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.5, this.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    private void dotAttack(LivingEntity target) {
        DaoPath path = getPrimaryDaoPath();
        if (path == DaoPath.POISON) {
            DotManager.applyPoison(target, this, 2.0f, 100);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ITEM_SLIME, target.getX(), target.getY() + 1.0, target.getZ(), 20, 1.0, 0.5, 1.0, 0.05);
            }
        } else if (path == DaoPath.BLOOD) {
            DotManager.applyBleed(target, this, 2.5f, 80);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 0.5, 0.5, 0.1);
            }
        } else {
            DotManager.applyPoison(target, this, 1.5f, 80);
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.5, 0.5, 0.5, 0.05);
            }
        }
        this.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_BREATH, SoundSource.HOSTILE, 1.0f, 0.5f);
    }

    private void desperateExplosion() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 10, 3.0, 1.5, 3.0, 0.1);
        sl.sendParticles(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 1.0, this.getZ(), 40, 4.0, 2.0, 4.0, 0.1);
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.5f, 0.6f);

        this.hurt(this.damageSources().magic(), this.getMaxHealth() * 0.1f);

        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0), e -> e != this);
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().magic(), 12.0f);
            double dxx = e.getX() - this.getX();
            double dzz = e.getZ() - this.getZ();
            double dist = Math.sqrt(dxx * dxx + dzz * dzz);
            if (dist > 0.001) {
                e.knockback(1.5, -dxx / dist, -dzz / dist);
            }
        }
    }

    // ==================== 速攻型道路 ====================
    private boolean performRushAttack(LivingEntity target, int baseCooldown) {
        float roll = this.random.nextFloat();
        if (currentPhase == 1) {
            if (roll < 0.35f) {
                dashStrike(target);
                abilityCooldown = baseCooldown;
                return true;
            }
        } else if (currentPhase == 2) {
            if (roll < 0.30f) {
                dashStrike(target);
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.55f) {
                multiSlash(target);
                abilityCooldown = baseCooldown + 5;
                return true;
            }
        } else {
            if (roll < 0.25f) {
                dashStrike(target);
                abilityCooldown = baseCooldown - 5;
                return true;
            } else if (roll < 0.50f) {
                multiSlash(target);
                abilityCooldown = baseCooldown;
                return true;
            } else if (roll < 0.70f) {
                windBladeRanged(target);
                abilityCooldown = baseCooldown + 5;
                return true;
            }
        }
        return false;
    }

    private void dashStrike(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.5, this.getZ(), 15, 0.3, 0.5, 0.3, 0.1);

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 1.0) {
            double tpX = target.getX() - (dx / dist) * 1.5;
            double tpZ = target.getZ() - (dz / dist) * 1.5;
            this.teleportTo(tpX, target.getY(), tpZ);
        }

        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
        target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.5f);
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2f, 1.0f);
    }

    private void multiSlash(LivingEntity target) {
        if (this.distanceTo(target) > 4.0) {
            dashStrike(target);
            return;
        }
        float dmg = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        int hits = currentPhase == 3 ? 3 : 2;
        for (int i = 0; i < hits; i++) {
            target.hurt(this.damageSources().mobAttack(this), dmg * 0.6f);
        }
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.0, target.getZ(), hits * 3, 0.5, 0.5, 0.5, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    private void windBladeRanged(LivingEntity target) {
        MoonBladeEntity blade = new MoonBladeEntity(ModEntities.MOON_BLADE.get(), this.level());
        blade.setOwner(this);
        blade.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        blade.shoot(dx, dy, dz, 2.5f, 2.0f);
        this.level().addFreshEntity(blade);

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getEyeY(), this.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
        }
    }

    // ==================== 通用攻击 ====================
    private boolean performGenericAttack(LivingEntity target) {
        float roll = this.random.nextFloat();
        if (roll < 0.25f) {
            shootProjectile(target);
            abilityCooldown = 40 + random.nextInt(20);
            return true;
        } else if (roll < 0.40f) {
            earthquakeWave();
            abilityCooldown = 50 + random.nextInt(20);
            return true;
        }
        return false;
    }

    // ==================== 粒子光环 ====================
    private void spawnPathAuraParticles(ServerLevel sl) {
        DaoPath path = getPrimaryDaoPath();
        if (path == null) return;
        ParticleOptions particle = getPathParticle(path);
        int count = getGuRank() >= 3 ? 3 : 1;
        sl.sendParticles(particle, this.getX(), this.getEyeY() + 0.5, this.getZ(), count, 0.6, 0.6, 0.6, 0.005);
    }

    private ParticleOptions getPathParticle(DaoPath path) {
        if (path == null) return ParticleTypes.WITCH;
        return switch (path) {
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case MOON -> ParticleTypes.END_ROD;
            case SOUL -> ParticleTypes.SCULK_SOUL;
            case BLOOD -> ParticleTypes.CRIMSON_SPORE;
            case POISON -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case DARK, SHADOW -> ParticleTypes.SMOKE;
            case LIGHT -> ParticleTypes.WAX_ON;
            case WIND, FLIGHT -> ParticleTypes.CLOUD;
            case STRENGTH, EARTH -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
            case METAL -> ParticleTypes.CRIT;
            case WATER -> ParticleTypes.DRIPPING_WATER;
            case STAR -> ParticleTypes.END_ROD;
            case SWORD, BLADE -> ParticleTypes.SWEEP_ATTACK;
            case DREAM, ILLUSION -> ParticleTypes.ENCHANT;
            case CHARM -> ParticleTypes.HEART;
            case BONE -> ParticleTypes.ASH;
            case WOOD -> ParticleTypes.COMPOSTER;
            default -> ParticleTypes.WITCH;
        };
    }

    // ==================== 掉落（基于实际装备蛊虫） ====================
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int rank = getGuRank();

        if (!equippedGu.isEmpty()) {
            int dropCount = 1 + (rank >= 3 ? 1 : 0);
            List<ResourceLocation> pool = new ArrayList<>(equippedGu);
            for (int i = 0; i < dropCount && !pool.isEmpty(); i++) {
                ResourceLocation guId = pool.remove(this.random.nextInt(pool.size()));
                Item item = BuiltInRegistries.ITEM.get(guId);
                if (item != net.minecraft.world.item.Items.AIR) {
                    this.spawnAtLocation(new ItemStack(item));
                }
            }
        }

        if (this.random.nextFloat() < 0.50f) {
            int count = rank + this.random.nextInt(3);
            this.spawnAtLocation(new ItemStack(ModItems.PRIMEVAL_STONE.get(), count));
        }

        if (rank == 3 && this.random.nextFloat() < 0.10f) {
            this.spawnAtLocation(new ItemStack(ModItems.BREAKTHROUGH_STONE.get()));
        }
    }

    // ==================== Boss条管理 ====================
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (bossEvent != null) bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (bossEvent != null) bossEvent.removePlayer(player);
    }

    // ==================== 数据存储 ====================
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GuRank", getGuRank());
        tag.putString("PrimaryPath", this.entityData.get(PRIMARY_PATH));
        tag.putString("SecondaryPath", this.entityData.get(SECONDARY_PATH));
        tag.putInt("Faction", getFaction().ordinal());

        ListTag guList = new ListTag();
        for (ResourceLocation id : equippedGu) {
            guList.add(StringTag.valueOf(id.toString()));
        }
        tag.put("EquippedGu", guList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GuRank")) {
            int rank = tag.getInt("GuRank");
            setGuRank(rank);
            applyRankAttributes(rank);
        }
        if (tag.contains("PrimaryPath")) {
            setPrimaryPath(tag.getString("PrimaryPath"));
            DaoPath path = getPrimaryDaoPath();
            if (path != null) applyPathAttributes(path);
        }
        if (tag.contains("SecondaryPath")) {
            setSecondaryPath(tag.getString("SecondaryPath"));
        }
        if (tag.contains("Faction")) {
            int fIdx = tag.getInt("Faction");
            Faction[] factions = Faction.values();
            setFaction(fIdx >= 0 && fIdx < factions.length ? factions[fIdx] : Faction.INDEPENDENT);
        }

        equippedGu.clear();
        if (tag.contains("EquippedGu", Tag.TAG_LIST)) {
            ListTag guList = tag.getList("EquippedGu", Tag.TAG_STRING);
            for (int i = 0; i < guList.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(guList.getString(i));
                if (id != null) equippedGu.add(id);
            }
        }

        matchKillerMoves(getGuRank());

        if (getGuRank() == 3 && bossEvent == null) {
            initBossEvent();
        }
    }

    @Override
    protected int getBaseExperienceReward() {
        return 5 + getGuRank() * 5;
    }

    // ==================== 音效 ====================
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    public Component getDisplayName() {
        Faction faction = getFaction();
        Component factionTag = Component.literal("[" + faction.getDisplayName() + "] ").withColor(faction.getColor());
        if (getGuRank() >= 3) {
            return factionTag.copy().append(Component.literal("\u86ca\u5e08\u00b7???"));
        }
        return factionTag.copy().append(Component.literal("\u86ca\u5e08"));
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    // ==================== 工具方法 ====================
    public int getGuRank() {
        return this.entityData.get(GU_RANK);
    }

    public void setGuRank(int rank) {
        this.entityData.set(GU_RANK, rank);
    }

    public Faction getFaction() {
        int idx = this.entityData.get(DATA_FACTION);
        Faction[] factions = Faction.values();
        return idx >= 0 && idx < factions.length ? factions[idx] : Faction.INDEPENDENT;
    }

    public void setFaction(Faction faction) {
        this.entityData.set(DATA_FACTION, faction.ordinal());
    }

    public String getPrimaryPathName() {
        return this.entityData.get(PRIMARY_PATH);
    }

    public void setPrimaryPath(String path) {
        this.entityData.set(PRIMARY_PATH, path);
    }

    public String getSecondaryPathName() {
        return this.entityData.get(SECONDARY_PATH);
    }

    public void setSecondaryPath(String path) {
        this.entityData.set(SECONDARY_PATH, path);
    }

    @Nullable
    public DaoPath getPrimaryDaoPath() {
        String name = getPrimaryPathName();
        if (name == null || name.isEmpty()) return null;
        try {
            return DaoPath.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public DaoPath getSecondaryDaoPath() {
        String name = getSecondaryPathName();
        if (name == null || name.isEmpty()) return null;
        try {
            return DaoPath.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<ResourceLocation> getEquippedGu() {
        return List.copyOf(equippedGu);
    }

    public List<KillerMove> getAvailableMoves() {
        return List.copyOf(availableMoves);
    }

    private CombatArchetype getArchetype(DaoPath path) {
        if (MELEE_PATHS.contains(path)) return CombatArchetype.MELEE;
        if (RANGED_PATHS.contains(path)) return CombatArchetype.RANGED;
        if (CONTROL_PATHS.contains(path)) return CombatArchetype.CONTROL;
        if (SUPPORT_PATHS.contains(path)) return CombatArchetype.SUPPORT;
        if (RUSH_PATHS.contains(path)) return CombatArchetype.RUSH;
        return CombatArchetype.GENERIC;
    }

    private enum CombatArchetype {
        MELEE, RANGED, CONTROL, SUPPORT, RUSH, GENERIC
    }
}
