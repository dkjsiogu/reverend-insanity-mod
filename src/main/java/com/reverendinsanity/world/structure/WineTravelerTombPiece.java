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

// 花酒行者遗藏结构件：主墓室、入口通道、侧室
public class WineTravelerTombPiece extends StructurePiece {

    private static final int TOTAL_W = 27;
    private static final int TOTAL_H = 10;
    private static final int TOTAL_D = 25;

    public WineTravelerTombPiece(BlockPos center, RandomSource random) {
        super(ModStructures.WINE_TRAVELER_TOMB_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public WineTravelerTombPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.WINE_TRAVELER_TOMB_PIECE.get(), tag);
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

        int mainRoomX = baseX + (TOTAL_W - 15) / 2;
        int mainRoomZ = baseZ + (TOTAL_D - 15) / 2;

        generateMainChamber(level, random, chunkBB, mainRoomX, baseY, mainRoomZ);

        int corridorX = mainRoomX + (15 - 3) / 2;
        int corridorZ = mainRoomZ - 8;
        generateEntrance(level, random, chunkBB, corridorX, baseY, corridorZ);

        int sideRoomX = mainRoomX + 15;
        int sideRoomZ = mainRoomZ + (15 - 8) / 2;
        generateSideRoom(level, random, chunkBB, sideRoomX, baseY, sideRoomZ);
    }

    private void generateMainChamber(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
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
                    boolean isWall = isWallX || isWallZ;

                    if (isFloor) {
                        level.setBlock(bp, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, randomCeiling(random), 2);
                        if (!isWall && random.nextFloat() < 0.08f) {
                            BlockPos drip = bp.below();
                            if (chunkBB.isInside(drip)) {
                                level.setBlock(drip, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                                    .setValue(BlockStateProperties.DRIPSTONE_THICKNESS,
                                        net.minecraft.world.level.block.state.properties.DripstoneThickness.TIP)
                                    .setValue(BlockStateProperties.VERTICAL_DIRECTION, Direction.DOWN), 2);
                            }
                        }
                    } else if (isWall) {
                        level.setBlock(bp, randomWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + w / 2;
        int cz = oz + d / 2;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos platform = new BlockPos(cx + dx, oy + 1, cz + dz);
                placeIfInside(level, chunkBB, platform, Blocks.STONE_BRICKS.defaultBlockState());
            }
        }

        BlockPos chestPos = new BlockPos(cx, oy + 2, cz);
        if (chunkBB.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> rareLoot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/wine_traveler_tomb"));
                chestEntity.setLootTable(rareLoot, random.nextLong());
            }
        }

        int[][] wineJarPositions = {
            {ox + 2, oy + 1, oz + 2},
            {ox + w - 3, oy + 1, oz + 2},
            {ox + 2, oy + 1, oz + d - 3},
            {ox + w - 3, oy + 1, oz + d - 3}
        };
        for (int[] jp : wineJarPositions) {
            BlockPos jarPos = new BlockPos(jp[0], jp[1], jp[2]);
            placeIfInside(level, chunkBB, jarPos, ModBlocks.WINE_JAR.get().defaultBlockState());
        }

        int[][] lanternPositions = {
            {ox + 3, oy + h - 2, oz + 3},
            {ox + w - 4, oy + h - 2, oz + 3},
            {ox + 3, oy + h - 2, oz + d - 4},
            {ox + w - 4, oy + h - 2, oz + d - 4}
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

        for (int i = 0; i < 6; i++) {
            int rx = random.nextIntBetweenInclusive(ox + 2, ox + w - 3);
            int rz = random.nextIntBetweenInclusive(oz + 2, oz + d - 3);
            BlockPos webPos = new BlockPos(rx, oy + h - 2, rz);
            if (chunkBB.isInside(webPos) && level.getBlockState(webPos).isAir()) {
                level.setBlock(webPos, Blocks.COBWEB.defaultBlockState(), 2);
            }
        }

        for (int i = 0; i < 8; i++) {
            int rx = random.nextIntBetweenInclusive(ox + 1, ox + w - 2);
            int rz = random.nextIntBetweenInclusive(oz + 1, oz + d - 2);
            BlockPos mossPos = new BlockPos(rx, oy, rz);
            if (chunkBB.isInside(mossPos)) {
                if (random.nextFloat() < 0.5f) {
                    level.setBlock(mossPos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            int rx = random.nextIntBetweenInclusive(ox + 2, ox + w - 3);
            int rz = random.nextIntBetweenInclusive(oz + 2, oz + d - 3);
            BlockPos carpetPos = new BlockPos(rx, oy + 1, rz);
            if (chunkBB.isInside(carpetPos) && level.getBlockState(carpetPos).isAir()) {
                level.setBlock(carpetPos, Blocks.MOSS_CARPET.defaultBlockState(), 2);
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
                        level.setBlock(bp, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                    } else if (isFloor && stairStep > 0 && stairStep < d) {
                        level.setBlock(bp, Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), 2);
                    } else if (isFloor) {
                        level.setBlock(bp, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, Blocks.STONE_BRICKS.defaultBlockState(), 2);
                    } else if (isWallX) {
                        level.setBlock(bp, randomWall(random), 2);
                    } else if ((oy + y) > floorY) {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        BlockPos doorPos1 = new BlockPos(ox + 1, oy + 1, oz);
        BlockPos doorPos2 = new BlockPos(ox + 1, oy + 2, oz);
        if (chunkBB.isInside(doorPos1)) {
            level.setBlock(doorPos1, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                .setValue(BlockStateProperties.OPEN, true), 2);
        }
        if (chunkBB.isInside(doorPos2)) {
            level.setBlock(doorPos2, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                .setValue(BlockStateProperties.OPEN, true), 2);
        }
    }

    private void generateSideRoom(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int ox, int oy, int oz) {
        int w = 8, h = 6, d = 8;

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
                    boolean isDoorway = x == 0 && z >= 2 && z <= 4 && y >= 1 && y <= 3;

                    if (isDoorway) {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    } else if (isFloor) {
                        level.setBlock(bp, Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                    } else if (isCeiling) {
                        level.setBlock(bp, randomCeiling(random), 2);
                    } else if (isWall) {
                        level.setBlock(bp, randomWall(random), 2);
                    } else {
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cx = ox + w / 2;
        int cz = oz + d / 2;

        placeIfInside(level, chunkBB, new BlockPos(cx, oy + 1, cz - 1), Blocks.BREWING_STAND.defaultBlockState());
        placeIfInside(level, chunkBB, new BlockPos(cx - 1, oy + 1, cz - 1), Blocks.CRAFTING_TABLE.defaultBlockState());

        for (int by = 0; by < 3; by++) {
            placeIfInside(level, chunkBB, new BlockPos(ox + w - 2, oy + 1 + by, oz + 1), Blocks.BOOKSHELF.defaultBlockState());
            placeIfInside(level, chunkBB, new BlockPos(ox + w - 2, oy + 1 + by, oz + 2), Blocks.BOOKSHELF.defaultBlockState());
        }

        BlockPos sideChestPos = new BlockPos(cx + 1, oy + 1, cz + 1);
        if (chunkBB.isInside(sideChestPos)) {
            level.setBlock(sideChestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(sideChestPos) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> sideLoot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/wine_traveler_tomb_side"));
                chestEntity.setLootTable(sideLoot, random.nextLong());
            }
        }

        placeIfInside(level, chunkBB, new BlockPos(cx, oy + h - 2, cz),
            Blocks.SOUL_LANTERN.defaultBlockState());

        for (int i = 0; i < 4; i++) {
            int rx = random.nextIntBetweenInclusive(ox + 1, ox + w - 2);
            int rz = random.nextIntBetweenInclusive(oz + 1, oz + d - 2);
            BlockPos dp = new BlockPos(rx, oy + 1, rz);
            if (chunkBB.isInside(dp) && level.getBlockState(dp).isAir()) {
                float roll = random.nextFloat();
                if (roll < 0.4f) {
                    level.setBlock(dp, Blocks.CANDLE.defaultBlockState(), 2);
                } else if (roll < 0.7f) {
                    level.setBlock(dp, Blocks.COBWEB.defaultBlockState(), 2);
                } else {
                    level.setBlock(dp, Blocks.MOSS_CARPET.defaultBlockState(), 2);
                }
            }
        }
    }

    private void placeIfInside(WorldGenLevel level, BoundingBox chunkBB, BlockPos pos, BlockState state) {
        if (chunkBB.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private BlockState randomWall(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.45f) return Blocks.STONE_BRICKS.defaultBlockState();
        if (roll < 0.65f) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (roll < 0.85f) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    private BlockState randomCeiling(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.5f) return Blocks.STONE_BRICKS.defaultBlockState();
        if (roll < 0.75f) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    }
}
