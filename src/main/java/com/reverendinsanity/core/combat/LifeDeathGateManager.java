package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.heavenwill.HeavenWillManager;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 生死门天地秘境系统：多阶段探索试炼，死路→公平之地→生路
public class LifeDeathGateManager {

    public enum GatePhase {
        NONE, DEATH_PATH, FAIRNESS_GROUND, LIFE_PATH
    }

    private static final Map<UUID, GateSession> sessions = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();

    private static final ResourceLocation GATE_ATTACK = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "life_death_gate_atk");
    private static final ResourceLocation GATE_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "life_death_gate_spd");
    private static final ResourceLocation GATE_ARMOR = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "life_death_gate_armor");
    private static final ResourceLocation GATE_SLOW = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "life_death_gate_slow");

    private static final int COOLDOWN_TICKS = 12000;
    private static final int ESSENCE_COST_PERCENT = 30;
    private static final int DEATH_PATH_WAVE_INTERVAL = 200;
    private static final int FAIRNESS_GROUND_TIMEOUT = 600;
    private static final int LIFE_PATH_DURATION = 200;
    private static final float DEATH_PATH_DOT_PERCENT = 0.005f;
    private static final int DOT_INTERVAL = 20;

    private static final DustParticleOptions DEATH_PARTICLE = new DustParticleOptions(new Vector3f(0.2f, 0.0f, 0.1f), 1.5f);
    private static final DustParticleOptions SOUL_PARTICLE = new DustParticleOptions(new Vector3f(0.5f, 0.3f, 0.8f), 1.2f);
    private static final DustParticleOptions LIFE_PARTICLE = new DustParticleOptions(new Vector3f(0.2f, 1.0f, 0.4f), 1.5f);
    private static final DustParticleOptions GOLD_PARTICLE = new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.2f), 2.0f);

    public static boolean openGate(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Integer cd = cooldowns.get(uuid);
        if (cd != null && cd > 0) {
            player.displayClientMessage(Component.literal("生死门冷却中...").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (sessions.containsKey(uuid)) return false;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        boolean hasSoulPathGu = false;
        for (GuInstance gu : aperture.getStoredGu()) {
            if (gu.isAlive() && gu.getType() != null && gu.getType().path() == DaoPath.SOUL) {
                hasSoulPathGu = true;
                break;
            }
        }
        if (!hasSoulPathGu) {
            player.displayClientMessage(
                    Component.literal("需要至少一只魂道蛊虫才能踏入生死门").withStyle(ChatFormatting.RED), true);
            return false;
        }

        float essenceCost = aperture.getMaxEssence() * (ESSENCE_COST_PERCENT / 100f);
        if (aperture.getCurrentEssence() < essenceCost) {
            player.displayClientMessage(
                    Component.literal("真元不足，无法开启生死门").withStyle(ChatFormatting.RED), true);
            return false;
        }
        aperture.consumeEssence(essenceCost);

        ServerLevel level = player.serverLevel();

        GateSession session = new GateSession();
        session.phase = GatePhase.DEATH_PATH;
        session.ticksInPhase = 0;
        session.wavesCleared = 0;
        session.totalWaves = 3 + aperture.getRank().ordinal();
        session.originalPos = player.blockPosition();
        session.spawnedMobs = new ArrayList<>();
        session.waveSpawned = false;
        session.choiceMade = false;
        sessions.put(uuid, session);

        applyDeathPathDebuff(player);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8f, 0.3f);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                80, 1.0, 2.0, 1.0, 0.05);
        level.sendParticles(DEATH_PARTICLE,
                player.getX(), player.getY(), player.getZ(),
                40, 2.0, 0.5, 2.0, 0.02);

        player.displayClientMessage(
                Component.literal("").append(
                        Component.literal("【生死门开启】").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
                ).append(
                        Component.literal("你踏入了死路...忧患之气扑面而来").withStyle(ChatFormatting.GRAY)
                ), false);

        HeavenWillManager.addAttention(player, 8f);
        return true;
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();

        Integer cd = cooldowns.get(uuid);
        if (cd != null) {
            if (cd <= 0) cooldowns.remove(uuid);
            else cooldowns.put(uuid, cd - 1);
        }

        GateSession session = sessions.get(uuid);
        if (session == null) return;

        session.ticksInPhase++;

        switch (session.phase) {
            case DEATH_PATH -> tickDeathPath(player, session);
            case FAIRNESS_GROUND -> tickFairnessGround(player, session);
            case LIFE_PATH -> tickLifePath(player, session);
            case NONE -> {
                if (session.buffTicksRemaining > 0) {
                    session.buffTicksRemaining--;
                    if (session.buffTicksRemaining <= 0) {
                        removeAllModifiers(player);
                        sessions.remove(uuid);
                        player.displayClientMessage(
                                Component.literal("生死门增益消散").withStyle(ChatFormatting.GRAY), true);
                    }
                } else {
                    sessions.remove(uuid);
                }
            }
        }
    }

    private static void tickDeathPath(ServerPlayer player, GateSession session) {
        ServerLevel level = player.serverLevel();

        if (session.ticksInPhase % DOT_INTERVAL == 0) {
            float dot = player.getMaxHealth() * DEATH_PATH_DOT_PERCENT;
            if (player.getHealth() > dot + 1.0f) {
                player.hurt(player.damageSources().magic(), dot);
            }
        }

        if (session.ticksInPhase % 10 == 0) {
            level.sendParticles(DEATH_PARTICLE,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    5, 3.0, 1.5, 3.0, 0.01);
            level.sendParticles(SOUL_PARTICLE,
                    player.getX() + (player.getRandom().nextFloat() - 0.5) * 6,
                    player.getY() + player.getRandom().nextFloat() * 2,
                    player.getZ() + (player.getRandom().nextFloat() - 0.5) * 6,
                    3, 0.2, 0.5, 0.2, 0.02);
        }

        if (!session.waveSpawned) {
            spawnDeathWave(player, session);
            session.waveSpawned = true;
        }

        session.spawnedMobs.removeIf(mob -> !mob.isAlive());

        if (session.waveSpawned && session.spawnedMobs.isEmpty()) {
            session.wavesCleared++;
            if (session.wavesCleared >= session.totalWaves) {
                enterFairnessGround(player, session);
                return;
            }

            player.displayClientMessage(
                    Component.literal("【死路】第" + session.wavesCleared + "波魂兽消散...还剩"
                            + (session.totalWaves - session.wavesCleared) + "波")
                            .withStyle(ChatFormatting.DARK_PURPLE), false);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SKELETON_HURT, SoundSource.HOSTILE, 0.6f, 0.5f);

            session.waveSpawned = false;
            session.ticksInPhase = -(DEATH_PATH_WAVE_INTERVAL);
        }
    }

    private static void spawnDeathWave(ServerPlayer player, GateSession session) {
        ServerLevel level = player.serverLevel();
        int wave = session.wavesCleared;
        int count = 3 + wave * 2;

        player.displayClientMessage(
                Component.literal("【死路·第" + (wave + 1) + "波】" + count + "只魂兽从黑暗中涌出！")
                        .withStyle(ChatFormatting.DARK_RED), false);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 0.5f, 0.4f + wave * 0.15f);

        for (int i = 0; i < count; i++) {
            Mob mob = createSoulBeast(level, wave);
            if (mob == null) continue;

            double angle = (2 * Math.PI / count) * i;
            double radius = 5 + wave;
            double spawnX = player.getX() + Math.cos(angle) * radius;
            double spawnZ = player.getZ() + Math.sin(angle) * radius;
            mob.moveTo(spawnX, player.getY(), spawnZ, (float)(angle * 180 / Math.PI), 0);
            mob.setTarget(player);
            mob.setGlowingTag(true);
            mob.setPersistenceRequired();
            level.addFreshEntity(mob);
            session.spawnedMobs.add(mob);

            level.sendParticles(SOUL_PARTICLE,
                    spawnX, player.getY() + 1, spawnZ,
                    10, 0.3, 0.5, 0.3, 0.05);
        }
    }

    private static Mob createSoulBeast(ServerLevel level, int wave) {
        return switch (wave % 3) {
            case 0 -> {
                Phantom phantom = EntityType.PHANTOM.create(level);
                if (phantom != null) phantom.setPersistenceRequired();
                yield phantom;
            }
            case 1 -> {
                Skeleton skeleton = EntityType.SKELETON.create(level);
                if (skeleton != null) skeleton.setPersistenceRequired();
                yield skeleton;
            }
            default -> {
                WitherSkeleton ws = EntityType.WITHER_SKELETON.create(level);
                if (ws != null) ws.setPersistenceRequired();
                yield ws;
            }
        };
    }

    private static void enterFairnessGround(ServerPlayer player, GateSession session) {
        session.phase = GatePhase.FAIRNESS_GROUND;
        session.ticksInPhase = 0;
        session.choiceMade = false;

        removeDeathPathDebuff(player);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);
        level.sendParticles(GOLD_PARTICLE,
                player.getX(), player.getY() + 1, player.getZ(),
                60, 2.0, 2.0, 2.0, 0.05);
        level.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 2, player.getZ(),
                30, 1.0, 1.5, 1.0, 0.08);

        player.setHealth(Math.min(player.getHealth() + player.getMaxHealth() * 0.3f, player.getMaxHealth()));

        player.displayClientMessage(
                Component.literal("").append(
                        Component.literal("【公平之地】").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                ).append(
                        Component.literal("生死是世间最大的公平。").withStyle(ChatFormatting.YELLOW)
                ), false);
        player.displayClientMessage(
                Component.literal("").append(
                        Component.literal("  蹲下").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                ).append(
                        Component.literal(" = 踏入生路（丰厚奖励，但仍有考验）").withStyle(ChatFormatting.GREEN)
                ), false);
        player.displayClientMessage(
                Component.literal("").append(
                        Component.literal("  等待").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)
                ).append(
                        Component.literal(" = 安全离开（少量奖励）").withStyle(ChatFormatting.GRAY)
                ), false);
    }

    private static void tickFairnessGround(ServerPlayer player, GateSession session) {
        ServerLevel level = player.serverLevel();

        if (session.ticksInPhase % 20 == 0) {
            level.sendParticles(GOLD_PARTICLE,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    3, 1.5, 1.0, 1.5, 0.01);
        }

        if (!session.choiceMade && player.isShiftKeyDown()) {
            session.choiceMade = true;
            enterLifePath(player, session);
            return;
        }

        if (session.ticksInPhase >= FAIRNESS_GROUND_TIMEOUT) {
            exitWithMinorReward(player, session);
        }
    }

    private static void enterLifePath(ServerPlayer player, GateSession session) {
        session.phase = GatePhase.LIFE_PATH;
        session.ticksInPhase = 0;

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.8f);
        level.sendParticles(LIFE_PARTICLE,
                player.getX(), player.getY(), player.getZ(),
                40, 2.0, 2.0, 2.0, 0.05);

        player.displayClientMessage(
                Component.literal("【生路】").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        .append(Component.literal("你踏上了归途，生之力量涌入体内...").withStyle(ChatFormatting.GREEN)),
                false);

        applyLifePathBuff(player);
    }

    private static void tickLifePath(ServerPlayer player, GateSession session) {
        ServerLevel level = player.serverLevel();

        if (session.ticksInPhase % 20 == 0) {
            level.sendParticles(LIFE_PARTICLE,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    5, 1.5, 1.0, 1.5, 0.02);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1, player.getZ(),
                    3, 1.0, 0.5, 1.0, 0.1);
        }

        if (session.ticksInPhase % 40 == 0) {
            float heal = player.getMaxHealth() * 0.05f;
            player.heal(heal);
        }

        if (session.ticksInPhase >= LIFE_PATH_DURATION) {
            completeLifePath(player, session);
        }
    }

    private static void completeLifePath(ServerPlayer player, GateSession session) {
        sessions.remove(player.getUUID());
        cooldowns.put(player.getUUID(), COOLDOWN_TICKS);

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        ServerLevel level = player.serverLevel();

        player.setHealth(player.getMaxHealth());
        aperture.regenerateEssence(aperture.getMaxEssence());
        aperture.regenerateThoughts(aperture.getMaxThoughts());

        DaoPath[] paths = DaoPath.values();
        DaoPath soulReward = DaoPath.SOUL;
        DaoPath randomReward = paths[player.getRandom().nextInt(paths.length)];
        int soulMarks = 40 + player.getRandom().nextInt(31);
        int randomMarks = 25 + player.getRandom().nextInt(21);
        data.addDaoMarks(soulReward, soulMarks);
        data.addDaoMarks(randomReward, randomMarks);

        boolean gotSpecialGu = player.getRandom().nextFloat() < 0.15f;
        String specialGuName = "";
        if (gotSpecialGu) {
            specialGuName = player.getRandom().nextBoolean() ? "生蛊" : "死蛊";
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 1.0, 2.0, 1.0, 0.3);
        level.sendParticles(LIFE_PARTICLE,
                player.getX(), player.getY(), player.getZ(),
                30, 2.0, 1.0, 2.0, 0.05);

        player.displayClientMessage(
                Component.literal("【生死门通关】").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(Component.literal("你从生路归来！").withStyle(ChatFormatting.GREEN)),
                false);
        player.displayClientMessage(
                Component.literal("  真元/念头完全恢复").withStyle(ChatFormatting.GREEN), false);
        player.displayClientMessage(
                Component.literal("  " + soulReward.getDisplayName() + " 道痕+" + soulMarks
                        + "，" + randomReward.getDisplayName() + " 道痕+" + randomMarks)
                        .withStyle(ChatFormatting.AQUA), false);
        if (gotSpecialGu) {
            player.displayClientMessage(
                    Component.literal("  获得稀有蛊虫：" + specialGuName + "！")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
        }
        player.displayClientMessage(
                Component.literal("  50%攻击/30%速度增益持续20秒").withStyle(ChatFormatting.YELLOW), false);

        removeDeathPathDebuff(player);

        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(GATE_ATTACK);
            atk.addTransientModifier(new AttributeModifier(GATE_ATTACK, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(GATE_SPEED);
            spd.addTransientModifier(new AttributeModifier(GATE_SPEED, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(GATE_ARMOR);
            armor.addTransientModifier(new AttributeModifier(GATE_ARMOR, 8.0, AttributeModifier.Operation.ADD_VALUE));
        }

        GateSession buffSession = new GateSession();
        buffSession.phase = GatePhase.NONE;
        buffSession.ticksInPhase = 0;
        buffSession.buffTicksRemaining = 400;
        sessions.put(player.getUUID(), buffSession);

        HeavenWillManager.addAttention(player, 5f);
    }

    private static void exitWithMinorReward(ServerPlayer player, GateSession session) {
        sessions.remove(player.getUUID());
        cooldowns.put(player.getUUID(), COOLDOWN_TICKS / 2);

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        ServerLevel level = player.serverLevel();

        aperture.regenerateEssence(aperture.getMaxEssence() * 0.3f);
        DaoPath[] paths = DaoPath.values();
        DaoPath reward = paths[player.getRandom().nextInt(paths.length)];
        int marks = 10 + player.getRandom().nextInt(11);
        data.addDaoMarks(reward, marks);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.0f);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 1.0, 1.5, 1.0, 0.05);

        removeDeathPathDebuff(player);

        player.displayClientMessage(
                Component.literal("【安全离开】").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)
                        .append(Component.literal("你选择了安全...").withStyle(ChatFormatting.GRAY)),
                false);
        player.displayClientMessage(
                Component.literal("  真元恢复30%，" + reward.getDisplayName() + " 道痕+" + marks)
                        .withStyle(ChatFormatting.AQUA), false);
    }

    private static void failGate(ServerPlayer player, String reason) {
        GateSession session = sessions.remove(player.getUUID());
        if (session == null) return;

        for (Mob mob : session.spawnedMobs) {
            if (mob.isAlive()) mob.discard();
        }

        cooldowns.put(player.getUUID(), COOLDOWN_TICKS);

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        aperture.consumeEssence(aperture.getMaxEssence() * 0.3f);
        data.consumeLifespan(50);

        removeAllModifiers(player);

        player.displayClientMessage(
                Component.literal("【生死门失败】").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                        .append(Component.literal(reason).withStyle(ChatFormatting.RED)),
                false);
        player.displayClientMessage(
                Component.literal("  损失30%真元，寿元-50").withStyle(ChatFormatting.RED), false);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.6f, 0.2f);
        level.sendParticles(ParticleTypes.SOUL,
                player.getX(), player.getY() + 1, player.getZ(),
                40, 1.5, 1.5, 1.5, 0.1);
    }

    private static void applyDeathPathDebuff(ServerPlayer player) {
        var spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(GATE_SLOW);
            spd.addTransientModifier(new AttributeModifier(GATE_SLOW, -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void removeDeathPathDebuff(ServerPlayer player) {
        var spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) spd.removeModifier(GATE_SLOW);
    }

    private static void applyLifePathBuff(ServerPlayer player) {
        var spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(GATE_SLOW);
            spd.removeModifier(GATE_SPEED);
            spd.addTransientModifier(new AttributeModifier(GATE_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void removeAllModifiers(ServerPlayer player) {
        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.removeModifier(GATE_ATTACK);
        var spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(GATE_SPEED);
            spd.removeModifier(GATE_SLOW);
        }
        var armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(GATE_ARMOR);
    }

    public static void onPlayerDeath(ServerPlayer player) {
        GateSession session = sessions.get(player.getUUID());
        if (session != null && (session.phase == GatePhase.DEATH_PATH || session.phase == GatePhase.FAIRNESS_GROUND || session.phase == GatePhase.LIFE_PATH)) {
            failGate(player, "你在生死门中陨落...");
            return;
        }
        removeAllModifiers(player);
        sessions.remove(player.getUUID());
    }

    public static void onPlayerLogout(ServerPlayer player) {
        GateSession session = sessions.remove(player.getUUID());
        if (session != null) {
            for (Mob mob : session.spawnedMobs) {
                if (mob.isAlive()) mob.discard();
            }
        }
        removeAllModifiers(player);
        cooldowns.remove(player.getUUID());
    }

    public static void clearAll(MinecraftServer server) {
        for (GateSession session : sessions.values()) {
            for (Mob mob : session.spawnedMobs) {
                if (mob.isAlive()) {
                    mob.discard();
                }
            }
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            removeAllModifiers(player);
        }
        sessions.clear();
        cooldowns.clear();
    }

    public static boolean isInGate(ServerPlayer player) {
        GateSession session = sessions.get(player.getUUID());
        return session != null && session.phase != GatePhase.NONE;
    }

    public static GatePhase getPhase(ServerPlayer player) {
        GateSession session = sessions.get(player.getUUID());
        return session != null ? session.phase : GatePhase.NONE;
    }

    private static class GateSession {
        GatePhase phase = GatePhase.NONE;
        int ticksInPhase = 0;
        int wavesCleared = 0;
        int totalWaves = 3;
        BlockPos originalPos;
        List<Mob> spawnedMobs = new ArrayList<>();
        boolean waveSpawned = false;
        boolean choiceMade = false;
        int buffTicksRemaining = 0;
    }
}
