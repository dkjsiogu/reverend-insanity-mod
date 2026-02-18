package com.reverendinsanity.core.inheritance;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 传承秘境试炼系统：在传承之地触发波次战斗，通关获得稀有道痕和蛊虫奖励
public class InheritanceTrialManager {

    private static final Map<UUID, TrialState> activeTrials = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private static final int COOLDOWN_TICKS = 12000;
    private static final int WAVE_INTERVAL = 100;
    private static final int MAX_WAVES = 3;

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        TrialState state = activeTrials.get(uuid);

        if (state == null) {
            tryStartTrial(player);
            return;
        }

        tickActiveTrial(player, state);
    }

    private static void tryStartTrial(ServerPlayer player) {
        if (!player.isShiftKeyDown()) return;
        if (player.getDeltaMovement().horizontalDistanceSqr() > 0.001) return;

        UUID uuid = player.getUUID();
        Long cd = cooldowns.get(uuid);
        if (cd != null && player.tickCount < cd) return;

        if (!isInInheritanceGround(player)) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;
        if (aperture.getCurrentEssence() < aperture.getMaxEssence() * 0.3f) {
            player.displayClientMessage(
                    Component.literal("真元不足，无法触发传承试炼").withStyle(ChatFormatting.RED), true);
            return;
        }

        TrialState state = new TrialState();
        state.startTick = player.tickCount;
        state.wave = 0;
        state.waveStartTick = player.tickCount;
        state.center = player.blockPosition();
        state.spawnedMobs = new ArrayList<>();
        activeTrials.put(uuid, state);

        aperture.consumeEssence(aperture.getMaxEssence() * 0.2f);

        player.displayClientMessage(
                Component.literal("【传承试炼】古蛊仙的意志苏醒...试炼开始！")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.6f, 0.3f);

        spawnWave(player, state);
    }

    private static void tickActiveTrial(ServerPlayer player, TrialState state) {
        if (!isInInheritanceGround(player)) {
            failTrial(player, "离开了传承之地，试炼终止！");
            return;
        }

        state.spawnedMobs.removeIf(mob -> !mob.isAlive());

        if (state.spawnedMobs.isEmpty()) {
            state.wave++;
            if (state.wave >= MAX_WAVES) {
                completeTrial(player, state);
                return;
            }

            int waitTicks = player.tickCount - state.waveStartTick;
            if (waitTicks < WAVE_INTERVAL) {
                if (waitTicks == 0) {
                    player.displayClientMessage(
                            Component.literal("第" + state.wave + "波清除！准备下一波...")
                                    .withStyle(ChatFormatting.YELLOW), true);
                }
                return;
            }

            state.waveStartTick = player.tickCount;
            spawnWave(player, state);
        }
    }

    private static void spawnWave(ServerPlayer player, TrialState state) {
        ServerLevel level = player.serverLevel();
        int wave = state.wave;
        int count = 3 + wave * 2;

        player.displayClientMessage(
                Component.literal("【第" + (wave + 1) + "波】" + count + "个守卫现身！")
                        .withStyle(ChatFormatting.RED), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 0.8f, 1.0f + wave * 0.2f);

        for (int i = 0; i < count; i++) {
            Mob mob = createTrialMob(level, wave);
            if (mob == null) continue;

            double angle = (2 * Math.PI / count) * i;
            double spawnX = state.center.getX() + Math.cos(angle) * 4;
            double spawnZ = state.center.getZ() + Math.sin(angle) * 4;
            mob.moveTo(spawnX, state.center.getY(), spawnZ, (float)(angle * 180 / Math.PI), 0);
            mob.setTarget(player);
            level.addFreshEntity(mob);
            state.spawnedMobs.add(mob);
        }
    }

    private static Mob createTrialMob(ServerLevel level, int wave) {
        return switch (wave) {
            case 0 -> {
                Zombie zombie = EntityType.ZOMBIE.create(level);
                if (zombie != null) zombie.setPersistenceRequired();
                yield zombie;
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

    private static void completeTrial(ServerPlayer player, TrialState state) {
        activeTrials.remove(player.getUUID());
        cooldowns.put(player.getUUID(), (long)(player.tickCount + COOLDOWN_TICKS));

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        DaoPath[] paths = DaoPath.values();
        DaoPath rewardPath1 = paths[player.getRandom().nextInt(paths.length)];
        DaoPath rewardPath2 = paths[player.getRandom().nextInt(paths.length)];
        int marks1 = 30 + player.getRandom().nextInt(21);
        int marks2 = 20 + player.getRandom().nextInt(16);
        data.addDaoMarks(rewardPath1, marks1);
        data.addDaoMarks(rewardPath2, marks2);

        aperture.regenerateEssence(aperture.getMaxEssence() * 0.5f);

        player.displayClientMessage(
                Component.literal("【传承试炼通关】")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        player.displayClientMessage(
                Component.literal("获得 " + rewardPath1.getDisplayName() + " 道痕+" + marks1
                        + "，" + rewardPath2.getDisplayName() + " 道痕+" + marks2)
                        .withStyle(ChatFormatting.AQUA), false);
        player.displayClientMessage(
                Component.literal("真元恢复50%").withStyle(ChatFormatting.GREEN), false);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);

        AdvancementHelper.grant(player, "complete_inheritance_trial");
    }

    private static void failTrial(ServerPlayer player, String reason) {
        TrialState state = activeTrials.remove(player.getUUID());
        if (state == null) return;

        for (Mob mob : state.spawnedMobs) {
            if (mob.isAlive()) mob.discard();
        }

        cooldowns.put(player.getUUID(), (long)(player.tickCount + COOLDOWN_TICKS / 2));

        player.displayClientMessage(
                Component.literal("【传承试炼失败】" + reason).withStyle(ChatFormatting.RED), false);
    }

    public static void onPlayerDeath(ServerPlayer player) {
        failTrial(player, "死亡，试炼终止。");
    }

    public static void onPlayerLogout(ServerPlayer player) {
        TrialState state = activeTrials.remove(player.getUUID());
        if (state != null) {
            for (Mob mob : state.spawnedMobs) {
                if (mob.isAlive()) mob.discard();
            }
        }
    }

    private static boolean isInInheritanceGround(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        try {
            Structure structure = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .get(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "inheritance_ground"));
            if (structure == null) return false;
            StructureStart start = level.structureManager().getStructureAt(pos, structure);
            return start.isValid();
        } catch (Exception e) {
            return false;
        }
    }

    private static class TrialState {
        int startTick;
        int wave;
        int waveStartTick;
        BlockPos center;
        List<Mob> spawnedMobs;
    }
}
