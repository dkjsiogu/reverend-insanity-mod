package com.reverendinsanity.world.dimension;

import com.reverendinsanity.core.aperture.BlessedLandGrade;
import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 仙窍入侵事件：周期性生成克制道路的敌对生物入侵福地
public class ApertureInvasionManager {

    private static final int INVASION_INTERVAL = 12000;
    private static final int MIN_SPAWN_DIST = 20;
    private static final int MAX_SPAWN_DIST = 40;

    private static final Map<UUID, Integer> invasionTimers = new ConcurrentHashMap<>();

    public static void tickInvasion(ServerPlayer player) {
        UUID id = player.getUUID();
        int timer = invasionTimers.getOrDefault(id, 0) + 1;

        if (timer < INVASION_INTERVAL) {
            invasionTimers.put(id, timer);
            return;
        }

        invasionTimers.put(id, 0);

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImmortalAperture ap = data.getImmortalAperture();
        if (!ap.isFormed()) return;

        ApertureChunkGenerator.IslandData island = ApertureChunkGenerator.findIsland(
                (int) player.getX(), (int) player.getZ());
        if (island == null) return;

        ServerLevel level = (ServerLevel) player.level();
        BlessedLandGrade grade = ap.getGrade();
        int count = getInvasionCount(grade, level.getRandom());

        player.sendSystemMessage(Component.literal("\u00a74\u00a7l你的福地正在遭受入侵！"));
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 0.8f);

        DaoPath counterPath = getCounterPath(island.primaryPath);
        EntityType<?> invaderType = getInvaderType(counterPath, level.getRandom());

        for (int i = 0; i < count; i++) {
            BlockPos pos = findInvasionSpawnPos(player, level);
            if (pos == null) continue;

            Entity entity = invaderType.create(level);
            if (entity != null) {
                entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                        level.getRandom().nextFloat() * 360f, 0f);
                level.addFreshEntity(entity);
            }
        }
    }

    private static int getInvasionCount(BlessedLandGrade grade, RandomSource rand) {
        return switch (grade) {
            case LOWER -> 3 + rand.nextInt(3);
            case MIDDLE -> 5 + rand.nextInt(4);
            case UPPER -> 8 + rand.nextInt(5);
            case SUPREME -> 12 + rand.nextInt(7);
        };
    }

    private static DaoPath getCounterPath(DaoPath path) {
        if (path == null) return null;
        return switch (path) {
            case FIRE -> DaoPath.ICE;
            case ICE -> DaoPath.FIRE;
            case WATER -> DaoPath.LIGHTNING;
            case LIGHTNING -> DaoPath.WATER;
            case EARTH -> DaoPath.WIND;
            case WIND -> DaoPath.EARTH;
            case LIGHT -> DaoPath.DARK;
            case DARK -> DaoPath.LIGHT;
            case SOUL -> DaoPath.BLOOD;
            case BLOOD -> DaoPath.SOUL;
            case WOOD -> DaoPath.FIRE;
            case METAL -> DaoPath.FIRE;
            case MOON -> DaoPath.DARK;
            case POISON -> DaoPath.LIGHT;
            default -> null;
        };
    }

    private static EntityType<?> getInvaderType(DaoPath counterPath, RandomSource rand) {
        if (counterPath == null) return selectGenericInvader(rand);
        return switch (counterPath) {
            case FIRE -> rand.nextBoolean() ? EntityType.BLAZE : EntityType.MAGMA_CUBE;
            case ICE -> EntityType.STRAY;
            case WATER -> EntityType.DROWNED;
            case LIGHTNING -> EntityType.PHANTOM;
            case EARTH -> EntityType.HUSK;
            case WIND -> EntityType.BREEZE;
            case LIGHT -> EntityType.SKELETON;
            case DARK -> rand.nextBoolean() ? EntityType.ENDERMAN : EntityType.PHANTOM;
            case SOUL -> rand.nextBoolean() ? EntityType.VEX : EntityType.PHANTOM;
            case BLOOD -> EntityType.HOGLIN;
            default -> selectGenericInvader(rand);
        };
    }

    private static EntityType<?> selectGenericInvader(RandomSource rand) {
        return switch (rand.nextInt(3)) {
            case 0 -> EntityType.ZOMBIE;
            case 1 -> EntityType.SKELETON;
            default -> EntityType.PILLAGER;
        };
    }

    private static BlockPos findInvasionSpawnPos(ServerPlayer player, ServerLevel level) {
        RandomSource rand = level.getRandom();
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = MIN_SPAWN_DIST + rand.nextDouble() * (MAX_SPAWN_DIST - MIN_SPAWN_DIST);
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > 0 && y < 256) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).isAir()) {
                    return pos;
                }
            }
        }
        return null;
    }

    public static void clearPlayer(UUID playerId) {
        invasionTimers.remove(playerId);
    }
}
