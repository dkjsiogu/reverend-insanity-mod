package com.reverendinsanity.world.dimension;

import com.reverendinsanity.core.aperture.BlessedLandGrade;
import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

import java.util.List;

// 仙窍维度生物生成：根据岛屿道路主题周期性生成原版生物
public class ApertureSpawnManager {

    private static final int SPAWN_INTERVAL = 200;
    private static final int MAX_NEARBY_ENTITIES = 20;
    private static final int MIN_SPAWN_DIST = 16;
    private static final int MAX_SPAWN_DIST = 32;

    private static int tickCounter = 0;

    public static void tickSpawning(ServerPlayer player) {
        tickCounter++;
        if (tickCounter % SPAWN_INTERVAL != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImmortalAperture ap = data.getImmortalAperture();
        if (!ap.isFormed()) return;

        ApertureChunkGenerator.IslandData island = ApertureChunkGenerator.findIsland(
                (int) player.getX(), (int) player.getZ());
        if (island == null) return;

        AABB checkBox = player.getBoundingBox().inflate(MAX_SPAWN_DIST);
        List<Entity> nearby = level.getEntities(player, checkBox);
        if (nearby.size() >= MAX_NEARBY_ENTITIES) return;

        BlessedLandGrade grade = ap.getGrade();
        int spawnCount = 1 + level.getRandom().nextInt(2 + grade.ordinal());
        boolean preferFriendly = grade.ordinal() >= BlessedLandGrade.UPPER.ordinal();

        for (int i = 0; i < spawnCount; i++) {
            EntityType<?> type = selectEntityType(island.primaryPath, preferFriendly, level.getRandom());
            if (type == null) continue;

            BlockPos spawnPos = findSpawnPos(player, level);
            if (spawnPos == null) continue;

            Entity entity = type.create(level);
            if (entity != null) {
                entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                        level.getRandom().nextFloat() * 360f, 0f);
                level.addFreshEntity(entity);
            }
        }
    }

    private static EntityType<?> selectEntityType(DaoPath path, boolean preferFriendly, RandomSource rand) {
        if (path == null) return selectDefault(rand);

        if (preferFriendly) {
            EntityType<?> friendly = selectFriendly(path, rand);
            if (friendly != null && rand.nextFloat() < 0.7f) return friendly;
        }

        return switch (path) {
            case FIRE -> rand.nextBoolean() ? EntityType.BLAZE : EntityType.MAGMA_CUBE;
            case ICE -> rand.nextBoolean() ? EntityType.STRAY : EntityType.SNOW_GOLEM;
            case WATER -> rand.nextBoolean() ? EntityType.DROWNED : EntityType.SQUID;
            case EARTH -> EntityType.IRON_GOLEM;
            case WOOD -> rand.nextBoolean() ? EntityType.FOX : EntityType.BEE;
            case LIGHTNING -> EntityType.PHANTOM;
            case POISON -> rand.nextBoolean() ? EntityType.SPIDER : EntityType.CAVE_SPIDER;
            case SOUL -> rand.nextBoolean() ? EntityType.PHANTOM : EntityType.VEX;
            case BLOOD -> EntityType.HOGLIN;
            case MOON -> EntityType.ENDERMAN;
            case DARK, SHADOW -> rand.nextBoolean() ? EntityType.ENDERMITE : EntityType.PHANTOM;
            case LIGHT -> EntityType.ALLAY;
            case WIND -> EntityType.BREEZE;
            case STAR -> EntityType.BLAZE;
            case BONE -> EntityType.SKELETON;
            default -> selectDefault(rand);
        };
    }

    private static EntityType<?> selectFriendly(DaoPath path, RandomSource rand) {
        return switch (path) {
            case FIRE -> EntityType.STRIDER;
            case ICE -> EntityType.SNOW_GOLEM;
            case WATER -> EntityType.SQUID;
            case EARTH -> EntityType.IRON_GOLEM;
            case WOOD -> rand.nextBoolean() ? EntityType.FOX : EntityType.BEE;
            case LIGHT -> EntityType.ALLAY;
            case MOON -> EntityType.CAT;
            default -> null;
        };
    }

    private static EntityType<?> selectDefault(RandomSource rand) {
        return switch (rand.nextInt(4)) {
            case 0 -> EntityType.COW;
            case 1 -> EntityType.SHEEP;
            case 2 -> EntityType.CHICKEN;
            default -> EntityType.RABBIT;
        };
    }

    private static BlockPos findSpawnPos(ServerPlayer player, ServerLevel level) {
        RandomSource rand = level.getRandom();
        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = MIN_SPAWN_DIST + rand.nextDouble() * (MAX_SPAWN_DIST - MIN_SPAWN_DIST);
            int x = (int) (player.getX() + Math.cos(angle) * dist);
            int z = (int) (player.getZ() + Math.sin(angle) * dist);
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (y > 0 && y < 256) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.getBlockState(pos).isAir() && !level.getBlockState(pos.below()).isAir()) {
                    return pos;
                }
            }
        }
        return null;
    }
}
