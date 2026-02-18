package com.reverendinsanity.world.structure;

import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

// 传承秘境结构件：程序化生成多房间地下城
public class InheritanceGroundPiece extends StructurePiece {

    private static final int TOTAL_W = 15;
    private static final int TOTAL_H = 10;
    private static final int TOTAL_D = 70;

    public InheritanceGroundPiece(BlockPos center, RandomSource random) {
        super(ModStructures.INHERITANCE_GROUND_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public InheritanceGroundPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.INHERITANCE_GROUND_PIECE.get(), tag);
    }

    private static BoundingBox makeBB(BlockPos center) {
        return new BoundingBox(
            center.getX() - TOTAL_W / 2, center.getY(), center.getZ(),
            center.getX() + TOTAL_W / 2, center.getY() + TOTAL_H, center.getZ() + TOTAL_D
        );
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource random,
                            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos pos) {

        BoundingBox bb = this.boundingBox;
        int baseX = bb.minX();
        int baseY = bb.minY();
        int baseZ = bb.minZ();

        generateEntrance(level, random, chunkBB, baseX, baseY, baseZ);
        generateCorridor(level, random, chunkBB, baseX, baseY, baseZ + 11, 15);
        generateTrialRoom(level, random, chunkBB, baseX, baseY, baseZ + 26);
        generateCorridor(level, random, chunkBB, baseX, baseY, baseZ + 41, 8);
        generateVault(level, random, chunkBB, baseX, baseY, baseZ + 49);
        generateSecretRoom(level, random, chunkBB, baseX, baseY, baseZ + 60);
    }

    private void generateEntrance(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int ox, int oy, int oz) {
        int w = 11, h = 6, d = 11;
        int offsetX = (TOTAL_W - w) / 2;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    BlockPos bp = new BlockPos(ox + offsetX + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isCorner = (x == 0 || x == w - 1) && (z == 0 || z == d - 1);

                    if (isFloor) {
                        level.setBlock(bp, randomFloor(random), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, randomCeiling(random), 2);
                    } else if (isCorner) {
                        level.setBlock(bp, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                    } else if (isWallX || isWallZ) {
                        level.setBlock(bp, randomWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + offsetX + w / 2;
        int cz = oz + d / 2;
        placeIfInside(level, chunkBB, new BlockPos(cx - 3, oy + 4, cz), Blocks.SOUL_LANTERN.defaultBlockState());
        placeIfInside(level, chunkBB, new BlockPos(cx + 3, oy + 4, cz), Blocks.SOUL_LANTERN.defaultBlockState());
        placeIfInside(level, chunkBB, new BlockPos(cx, oy + 4, cz - 3), Blocks.SOUL_LANTERN.defaultBlockState());
        placeIfInside(level, chunkBB, new BlockPos(cx, oy + 4, cz + 3), Blocks.SOUL_LANTERN.defaultBlockState());

        for (int step = 0; step < 3; step++) {
            for (int sx = -2; sx <= 2; sx++) {
                BlockPos stairPos = new BlockPos(cx + sx, oy + step, oz - 1 - step);
                if (chunkBB.isInside(stairPos)) {
                    level.setBlock(stairPos, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState(), 2);
                }
                for (int sy = step + 1; sy < h; sy++) {
                    BlockPos airAbove = new BlockPos(cx + sx, oy + sy, oz - 1 - step);
                    placeIfInside(level, chunkBB, airAbove, Blocks.AIR.defaultBlockState());
                }
            }
        }

        scatterDecor(level, random, chunkBB, ox + offsetX + 1, oy + 1, oz + 1, w - 2, d - 2, 6);
    }

    private void generateCorridor(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int ox, int oy, int oz, int length) {
        int w = 5, h = 4;
        int offsetX = (TOTAL_W - w) / 2;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < length; z++) {
                    BlockPos bp = new BlockPos(ox + offsetX + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;

                    if (isFloor) {
                        level.setBlock(bp, randomFloor(random), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, randomCeiling(random), 2);
                    } else if (isWallX) {
                        level.setBlock(bp, randomWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + offsetX + w / 2;
        for (int z = 2; z < length - 2; z += 4) {
            BlockPos chainPos = new BlockPos(cx, oy + h - 1, oz + z);
            BlockPos lanternPos = new BlockPos(cx, oy + h - 2, oz + z);
            if (chunkBB.isInside(chainPos)) {
                level.setBlock(chainPos, Blocks.CHAIN.defaultBlockState(), 2);
            }
            if (chunkBB.isInside(lanternPos)) {
                level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState()
                    .setValue(BlockStateProperties.HANGING, true), 2);
            }
        }

        int trapZ = oz + length / 2;
        BlockPos tntPos = new BlockPos(cx, oy - 1, trapZ);
        if (chunkBB.isInside(tntPos)) {
            level.setBlock(tntPos, Blocks.TNT.defaultBlockState(), 2);
        }
        BlockPos tripwireHook1 = new BlockPos(ox + offsetX + 1, oy + 1, trapZ);
        BlockPos tripwireHook2 = new BlockPos(ox + offsetX + w - 2, oy + 1, trapZ);
        if (chunkBB.isInside(tripwireHook1)) {
            level.setBlock(tripwireHook1, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), 2);
        }
        if (chunkBB.isInside(tripwireHook2)) {
            level.setBlock(tripwireHook2, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST), 2);
        }
        for (int tx = ox + offsetX + 2; tx <= ox + offsetX + w - 3; tx++) {
            BlockPos wire = new BlockPos(tx, oy + 1, trapZ);
            if (chunkBB.isInside(wire)) {
                level.setBlock(wire, Blocks.TRIPWIRE.defaultBlockState(), 2);
            }
        }
    }

    private void generateTrialRoom(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                   int ox, int oy, int oz) {
        int w = 15, h = 8, d = 15;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    BlockPos bp = new BlockPos(ox + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isCorner = (x <= 1 || x >= w - 2) && (z <= 1 || z >= d - 2);

                    if (isFloor) {
                        level.setBlock(bp, randomFloor(random), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, randomCeiling(random), 2);
                    } else if (isCorner && !isFloor && !isCeiling) {
                        level.setBlock(bp, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                    } else if (isWallX || isWallZ) {
                        level.setBlock(bp, randomWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + w / 2;
        int cz = oz + d / 2;

        int[][] redstoneLampPos = {
            {cx - 4, oy + h - 2, cz - 4}, {cx + 4, oy + h - 2, cz - 4},
            {cx - 4, oy + h - 2, cz + 4}, {cx + 4, oy + h - 2, cz + 4}
        };
        for (int[] lp : redstoneLampPos) {
            BlockPos lampPos = new BlockPos(lp[0], lp[1], lp[2]);
            if (chunkBB.isInside(lampPos)) {
                level.setBlock(lampPos, Blocks.REDSTONE_LAMP.defaultBlockState()
                    .setValue(BlockStateProperties.LIT, true), 2);
            }
            BlockPos redstoneBelow = new BlockPos(lp[0], lp[1] + 1, lp[2]);
            if (chunkBB.isInside(redstoneBelow)) {
                level.setBlock(redstoneBelow, Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
            }
        }

        BlockPos spawner1 = new BlockPos(cx - 3, oy + 1, cz);
        BlockPos spawner2 = new BlockPos(cx + 3, oy + 1, cz);
        placeSpawner(level, random, chunkBB, spawner1);
        placeSpawner(level, random, chunkBB, spawner2);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos altarBase = new BlockPos(cx + dx, oy, cz + dz);
                placeIfInside(level, chunkBB, altarBase, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                BlockPos altarTop = new BlockPos(cx + dx, oy + 1, cz + dz);
                if (dx == 0 && dz == 0) {
                    placeIfInside(level, chunkBB, altarTop, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    placeIfInside(level, chunkBB, new BlockPos(cx, oy + 2, cz),
                        Blocks.SOUL_LANTERN.defaultBlockState());
                }
            }
        }

        scatterDecor(level, random, chunkBB, ox + 2, oy + 1, oz + 2, w - 4, d - 4, 10);
    }

    private void generateVault(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                               int ox, int oy, int oz) {
        int w = 11, h = 6, d = 11;
        int offsetX = (TOTAL_W - w) / 2;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    BlockPos bp = new BlockPos(ox + offsetX + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isCorner = (x == 0 || x == w - 1) && (z == 0 || z == d - 1);

                    if (isFloor) {
                        level.setBlock(bp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, randomCeiling(random), 2);
                    } else if (isCorner) {
                        level.setBlock(bp, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                    } else if (isWallX || isWallZ) {
                        level.setBlock(bp, randomWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTable =
            ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/inheritance_ground"));

        int cx = ox + offsetX + w / 2;
        int cz = oz + d / 2;

        int[][] chestPositions = {
            {ox + offsetX + 2, oy + 1, oz + 2},
            {ox + offsetX + w - 3, oy + 1, oz + 2},
            {ox + offsetX + 2, oy + 1, oz + d - 3},
            {ox + offsetX + w - 3, oy + 1, oz + d - 3}
        };
        for (int[] cp : chestPositions) {
            BlockPos chestPos = new BlockPos(cp[0], cp[1], cp[2]);
            if (chunkBB.isInside(chestPos)) {
                level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
                if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                    chestEntity.setLootTable(lootTable, random.nextLong());
                }
            }
        }

        placeIfInside(level, chunkBB, new BlockPos(cx, oy + h - 2, cz),
            Blocks.SOUL_LANTERN.defaultBlockState());

        scatterDecor(level, random, chunkBB, ox + offsetX + 1, oy + 1, oz + 1, w - 2, d - 2, 8);
    }

    private void generateSecretRoom(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                    int ox, int oy, int oz) {
        int w = 7, h = 5, d = 7;
        int offsetX = (TOTAL_W - w) / 2;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    BlockPos bp = new BlockPos(ox + offsetX + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;

                    if (isFloor) {
                        level.setBlock(bp, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, Blocks.DEEPSLATE_TILES.defaultBlockState(), 2);
                    } else if (isWallX || isWallZ) {
                        level.setBlock(bp, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + offsetX + w / 2;
        int cz = oz + d / 2;

        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> rareLoot =
            ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/inheritance_ground_rare"));

        BlockPos chestPos = new BlockPos(cx, oy + 1, cz);
        if (chunkBB.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                chestEntity.setLootTable(rareLoot, random.nextLong());
            }
        }

        placeIfInside(level, chunkBB, new BlockPos(cx, oy + h - 2, cz),
            Blocks.SOUL_LANTERN.defaultBlockState());

        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                placeIfInside(level, chunkBB, new BlockPos(cx + dx, oy + 1, cz + dz),
                    Blocks.AMETHYST_CLUSTER.defaultBlockState());
            }
        }
    }

    private void placeSpawner(WorldGenLevel level, RandomSource random, BoundingBox chunkBB, BlockPos pos) {
        if (!chunkBB.isInside(pos)) return;
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawnerEntity) {
            spawnerEntity.setEntityId(
                net.minecraft.world.entity.EntityType.byString("reverend_insanity:gu_master")
                    .orElse(net.minecraft.world.entity.EntityType.ZOMBIE),
                random
            );
        }
    }

    private void placeIfInside(WorldGenLevel level, BoundingBox chunkBB, BlockPos pos, BlockState state) {
        if (chunkBB.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void scatterDecor(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                              int startX, int y, int startZ, int rangeX, int rangeZ, int count) {
        for (int i = 0; i < count; i++) {
            int rx = startX + random.nextInt(rangeX);
            int rz = startZ + random.nextInt(rangeZ);
            BlockPos dp = new BlockPos(rx, y, rz);
            if (!chunkBB.isInside(dp) || !level.getBlockState(dp).isAir()) continue;

            float roll = random.nextFloat();
            if (roll < 0.3f) {
                level.setBlock(dp, Blocks.CANDLE.defaultBlockState(), 2);
            } else if (roll < 0.5f) {
                level.setBlock(dp, Blocks.SCULK.defaultBlockState(), 2);
            } else if (roll < 0.7f) {
                level.setBlock(dp, Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
            } else {
                level.setBlock(dp, Blocks.SOUL_LANTERN.defaultBlockState(), 2);
            }
        }
    }

    private BlockState randomFloor(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.4f) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        if (roll < 0.7f) return Blocks.DEEPSLATE_TILES.defaultBlockState();
        return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    }

    private BlockState randomCeiling(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.6f) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
    }

    private BlockState randomWall(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.5f) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        if (roll < 0.7f) return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        if (roll < 0.85f) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }
}
