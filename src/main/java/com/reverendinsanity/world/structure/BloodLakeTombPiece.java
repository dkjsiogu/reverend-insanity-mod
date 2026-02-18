package com.reverendinsanity.world.structure;

import com.reverendinsanity.registry.ModBlocks;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

// 血湖墓地结构件：主墓室(血池+蛊阵)、入口通道、骨骸侧室
public class BloodLakeTombPiece extends StructurePiece {

    private static final int TOTAL_W = 30;
    private static final int TOTAL_H = 10;
    private static final int TOTAL_D = 28;

    public BloodLakeTombPiece(BlockPos center, RandomSource random) {
        super(ModStructures.BLOOD_LAKE_TOMB_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public BloodLakeTombPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.BLOOD_LAKE_TOMB_PIECE.get(), tag);
    }

    private static BoundingBox makeBB(BlockPos center) {
        return new BoundingBox(
            center.getX() - TOTAL_W / 2, center.getY(), center.getZ() - TOTAL_D / 2,
            center.getX() + TOTAL_W / 2, center.getY() + TOTAL_H, center.getZ() + TOTAL_D / 2
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

        int mainX = baseX + (TOTAL_W - 17) / 2;
        int mainZ = baseZ + (TOTAL_D - 17) / 2;
        generateMainChamber(level, random, chunkBB, mainX, baseY, mainZ);

        int corridorX = mainX + (17 - 3) / 2;
        int corridorZ = mainZ - 8;
        generateEntrance(level, random, chunkBB, corridorX, baseY, corridorZ);

        int sideX = mainX + 17;
        int sideZ = mainZ + (17 - 9) / 2;
        generateBoneRoom(level, random, chunkBB, sideX, baseY, sideZ);

        int side2X = mainX - 9;
        int side2Z = mainZ + (17 - 9) / 2;
        generateRitualRoom(level, random, chunkBB, side2X, baseY, side2Z);
    }

    private void generateMainChamber(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                     int ox, int oy, int oz) {
        int w = 17, h = 9, d = 17;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    BlockPos bp = new BlockPos(ox + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isWall = isWallX || isWallZ;

                    if (isFloor) {
                        level.setBlock(bp, randomBloodFloor(random), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isWall) {
                        level.setBlock(bp, randomBloodWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int poolStartX = ox + 5;
        int poolStartZ = oz + 5;
        for (int px = 0; px < 7; px++) {
            for (int pz = 0; pz < 7; pz++) {
                boolean isPoolEdge = px == 0 || px == 6 || pz == 0 || pz == 6;
                BlockPos poolPos = new BlockPos(poolStartX + px, oy, poolStartZ + pz);
                if (!chunkBB.isInside(poolPos)) continue;

                if (isPoolEdge) {
                    level.setBlock(poolPos, Blocks.RED_NETHER_BRICKS.defaultBlockState(), 2);
                } else {
                    level.setBlock(poolPos, Blocks.RED_CONCRETE.defaultBlockState(), 2);
                    BlockPos abovePool = poolPos.above();
                    if (chunkBB.isInside(abovePool)) {
                        level.setBlock(abovePool, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + w / 2;
        int cz = oz + d / 2;
        placeIfInside(level, chunkBB, new BlockPos(cx, oy + 1, cz),
            Blocks.RED_NETHER_BRICKS.defaultBlockState());
        placeIfInside(level, chunkBB, new BlockPos(cx, oy + 2, cz),
            Blocks.SKELETON_SKULL.defaultBlockState());

        int[][] carpetPattern = {
            {ox + 3, oz + 3}, {ox + 3, oz + d - 4}, {ox + w - 4, oz + 3}, {ox + w - 4, oz + d - 4},
            {ox + 4, oz + 4}, {ox + 4, oz + d - 5}, {ox + w - 5, oz + 4}, {ox + w - 5, oz + d - 5},
            {ox + 3, oz + d / 2}, {ox + w - 4, oz + d / 2},
            {ox + w / 2, oz + 3}, {ox + w / 2, oz + d - 4},
        };
        for (int[] cp : carpetPattern) {
            placeIfInside(level, chunkBB, new BlockPos(cp[0], oy + 1, cp[1]),
                Blocks.RED_CARPET.defaultBlockState());
        }

        int[][] candlePositions = {
            {ox + 2, oz + 2}, {ox + w - 3, oz + 2}, {ox + 2, oz + d - 3}, {ox + w - 3, oz + d - 3},
            {ox + 4, oz + d / 2}, {ox + w - 5, oz + d / 2},
        };
        for (int[] candle : candlePositions) {
            BlockPos candleBase = new BlockPos(candle[0], oy + 1, candle[1]);
            if (chunkBB.isInside(candleBase) && level.getBlockState(candleBase).isAir()) {
                level.setBlock(candleBase, Blocks.RED_CANDLE.defaultBlockState()
                    .setValue(BlockStateProperties.LIT, true)
                    .setValue(BlockStateProperties.CANDLES, random.nextIntBetweenInclusive(1, 4)), 2);
            }
        }

        int[][] lanternPositions = {
            {ox + 1, oy + h - 2, oz + 1},
            {ox + w - 2, oy + h - 2, oz + 1},
            {ox + 1, oy + h - 2, oz + d - 2},
            {ox + w - 2, oy + h - 2, oz + d - 2}
        };
        for (int[] lp : lanternPositions) {
            BlockPos chainPos = new BlockPos(lp[0], lp[1] + 1, lp[2]);
            BlockPos lanternPos = new BlockPos(lp[0], lp[1], lp[2]);
            placeIfInside(level, chunkBB, chainPos, Blocks.CHAIN.defaultBlockState());
            if (chunkBB.isInside(lanternPos)) {
                level.setBlock(lanternPos, Blocks.SOUL_LANTERN.defaultBlockState()
                    .setValue(BlockStateProperties.HANGING, true), 2);
            }
        }

        BlockPos chestPos = new BlockPos(cx + 2, oy + 1, cz - 3);
        if (chunkBB.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/blood_lake_tomb"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }

        for (int i = 0; i < 5; i++) {
            int rx = random.nextIntBetweenInclusive(ox + 2, ox + w - 3);
            int rz = random.nextIntBetweenInclusive(oz + 2, oz + d - 3);
            BlockPos webPos = new BlockPos(rx, oy + h - 2, rz);
            if (chunkBB.isInside(webPos) && level.getBlockState(webPos).isAir()) {
                level.setBlock(webPos, Blocks.COBWEB.defaultBlockState(), 2);
            }
        }
    }

    private void generateEntrance(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int ox, int oy, int oz) {
        int w = 3, h = 3, d = 8;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                for (int y = 0; y < h + 2; y++) {
                    BlockPos bp = new BlockPos(ox + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    int stairStep = d - 1 - z;
                    int floorY = oy + (stairStep < h ? stairStep : 0);

                    boolean isFloor = (oy + y) == floorY;
                    boolean belowFloor = (oy + y) < floorY;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isCeiling = y == h + 1;

                    if (belowFloor) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isFloor && stairStep > 0 && stairStep < d) {
                        level.setBlock(bp, Blocks.NETHER_BRICK_STAIRS.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), 2);
                    } else if (isFloor) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isWallX) {
                        level.setBlock(bp, randomBloodWall(random), 2);
                    } else if ((oy + y) > floorY) {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        placeIfInside(level, chunkBB, new BlockPos(ox + 1, oy + 1, oz),
            Blocks.RED_CANDLE.defaultBlockState()
                .setValue(BlockStateProperties.LIT, true)
                .setValue(BlockStateProperties.CANDLES, 2));
    }

    private void generateBoneRoom(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int ox, int oy, int oz) {
        int w = 9, h = 7, d = 9;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    BlockPos bp = new BlockPos(ox + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isWall = isWallX || isWallZ;
                    boolean isDoorway = x == 0 && z >= 3 && z <= 5 && y >= 1 && y <= 3;

                    if (isDoorway) {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    } else if (isFloor) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isWall) {
                        level.setBlock(bp, randomBloodWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int[][] skullPositions = {
            {ox + 1, oy + 1, oz + 1}, {ox + w - 2, oy + 1, oz + 1},
            {ox + 1, oy + 1, oz + d - 2}, {ox + w - 2, oy + 1, oz + d - 2},
            {ox + w / 2, oy + 1, oz + 1}, {ox + w / 2, oy + 1, oz + d - 2},
        };
        for (int[] sp : skullPositions) {
            placeIfInside(level, chunkBB, new BlockPos(sp[0], sp[1], sp[2]),
                Blocks.SKELETON_SKULL.defaultBlockState());
        }

        int[][] bonePositions = {
            {ox + 2, oy + 1, oz + 1}, {ox + w - 3, oy + 1, oz + 1},
            {ox + 2, oy + 1, oz + d - 2}, {ox + w - 3, oy + 1, oz + d - 2},
        };
        for (int[] bone : bonePositions) {
            placeIfInside(level, chunkBB, new BlockPos(bone[0], bone[1], bone[2]),
                Blocks.BONE_BLOCK.defaultBlockState());
        }

        for (int bx = 1; bx < w - 1; bx++) {
            for (int bz = 1; bz < d - 1; bz++) {
                if (random.nextFloat() < 0.15f) {
                    BlockPos boneFloor = new BlockPos(ox + bx, oy + 1, oz + bz);
                    if (chunkBB.isInside(boneFloor) && level.getBlockState(boneFloor).isAir()) {
                        level.setBlock(boneFloor, Blocks.BONE_BLOCK.defaultBlockState(), 2);
                    }
                }
            }
        }

        placeIfInside(level, chunkBB, new BlockPos(ox + w / 2, oy + 1, oz + d / 2),
            Blocks.RED_CANDLE.defaultBlockState()
                .setValue(BlockStateProperties.LIT, true)
                .setValue(BlockStateProperties.CANDLES, 3));

        BlockPos boneChest = new BlockPos(ox + w / 2, oy + 1, oz + 2);
        if (chunkBB.isInside(boneChest)) {
            level.setBlock(boneChest, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(boneChest) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/blood_lake_tomb_bones"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }
    }

    private void generateRitualRoom(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                    int ox, int oy, int oz) {
        int w = 9, h = 7, d = 9;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    BlockPos bp = new BlockPos(ox + x, oy + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    boolean isFloor = y == 0;
                    boolean isCeiling = y == h - 1;
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isWall = isWallX || isWallZ;
                    boolean isDoorway = x == w - 1 && z >= 3 && z <= 5 && y >= 1 && y <= 3;

                    if (isDoorway) {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    } else if (isFloor) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, Blocks.NETHER_BRICKS.defaultBlockState(), 2);
                    } else if (isWall) {
                        level.setBlock(bp, randomBloodWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + w / 2;
        int cz = oz + d / 2;

        placeIfInside(level, chunkBB, new BlockPos(cx, oy + 1, cz),
            ModBlocks.FORMATION_STONE.get().defaultBlockState());

        int[][] ritualCandles = {
            {cx - 2, cz - 2}, {cx + 2, cz - 2}, {cx - 2, cz + 2}, {cx + 2, cz + 2},
            {cx, cz - 2}, {cx, cz + 2}, {cx - 2, cz}, {cx + 2, cz},
        };
        for (int[] rc : ritualCandles) {
            BlockPos candlePos = new BlockPos(rc[0], oy + 1, rc[1]);
            if (chunkBB.isInside(candlePos) && level.getBlockState(candlePos).isAir()) {
                level.setBlock(candlePos, Blocks.RED_CANDLE.defaultBlockState()
                    .setValue(BlockStateProperties.LIT, true)
                    .setValue(BlockStateProperties.CANDLES, 4), 2);
            }
        }

        int[][] ritualCarpet = {
            {cx - 1, cz - 1}, {cx, cz - 1}, {cx + 1, cz - 1},
            {cx - 1, cz}, {cx + 1, cz},
            {cx - 1, cz + 1}, {cx, cz + 1}, {cx + 1, cz + 1},
        };
        for (int[] rcp : ritualCarpet) {
            placeIfInside(level, chunkBB, new BlockPos(rcp[0], oy + 1, rcp[1]),
                Blocks.RED_CARPET.defaultBlockState());
        }

        placeIfInside(level, chunkBB, new BlockPos(cx, oy + h - 2, cz),
            Blocks.SOUL_LANTERN.defaultBlockState());

        placeIfInside(level, chunkBB, new BlockPos(ox + 1, oy + 1, oz + 1),
            ModBlocks.GU_SHELF.get().defaultBlockState());
        placeIfInside(level, chunkBB, new BlockPos(ox + w - 2, oy + 1, oz + 1),
            ModBlocks.GU_SHELF.get().defaultBlockState());
    }

    private void placeIfInside(WorldGenLevel level, BoundingBox chunkBB, BlockPos pos, BlockState state) {
        if (chunkBB.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private BlockState randomBloodWall(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.35f) return Blocks.NETHER_BRICKS.defaultBlockState();
        if (roll < 0.55f) return Blocks.RED_NETHER_BRICKS.defaultBlockState();
        if (roll < 0.75f) return Blocks.CRACKED_NETHER_BRICKS.defaultBlockState();
        if (roll < 0.90f) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }

    private BlockState randomBloodFloor(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.40f) return Blocks.NETHER_BRICKS.defaultBlockState();
        if (roll < 0.60f) return Blocks.RED_NETHER_BRICKS.defaultBlockState();
        if (roll < 0.80f) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    }
}
