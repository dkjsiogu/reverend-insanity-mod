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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

// 蛊师学堂结构件：主讲堂、练功房、宿舍、院墙
public class GuAcademyPiece extends StructurePiece {

    private static final int TOTAL_W = 38;
    private static final int TOTAL_H = 12;
    private static final int TOTAL_D = 32;

    public GuAcademyPiece(BlockPos center, RandomSource random) {
        super(ModStructures.GU_ACADEMY_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public GuAcademyPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.GU_ACADEMY_PIECE.get(), tag);
    }

    private static BoundingBox makeBB(BlockPos center) {
        return new BoundingBox(
            center.getX() - TOTAL_W / 2, center.getY() - 1, center.getZ() - TOTAL_D / 2,
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
        int bx = this.boundingBox.minX();
        int by = this.boundingBox.minY() + 1;
        int bz = this.boundingBox.minZ();

        generateFoundation(level, random, chunkBB, bx, by, bz);
        generateWalls(level, random, chunkBB, bx, by, bz);
        generateLectureHall(level, random, chunkBB, bx + 4, by, bz + 4);
        generateTrainingRoom(level, random, chunkBB, bx + 22, by, bz + 4);
        generateDormitory(level, random, chunkBB, bx + 4, by, bz + 20);
        generateDormitory(level, random, chunkBB, bx + 16, by, bz + 20);
        generateCourtyard(level, random, chunkBB, bx + 28, by, bz + 20);
        generateLanterns(level, random, chunkBB, bx, by, bz);
    }

    private void place(WorldGenLevel level, BlockState state, int x, int y, int z, BoundingBox bb) {
        if (bb.isInside(x, y, z)) {
            level.setBlock(new BlockPos(x, y, z), state, 2);
        }
    }

    private void generateFoundation(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                    int bx, int by, int bz) {
        for (int x = 0; x < TOTAL_W; x++) {
            for (int z = 0; z < TOTAL_D; z++) {
                place(level, Blocks.COBBLESTONE.defaultBlockState(), bx + x, by - 1, bz + z, chunkBB);
                for (int y = 0; y < TOTAL_H; y++) {
                    place(level, Blocks.AIR.defaultBlockState(), bx + x, by + y, bz + z, chunkBB);
                }
            }
        }

        for (int x = 0; x < TOTAL_W; x++) {
            for (int z = 0; z < TOTAL_D; z++) {
                boolean isPath = (z >= 2 && z <= 3 && x >= TOTAL_W / 2 - 1 && x <= TOTAL_W / 2 + 1) ||
                                 (x >= TOTAL_W / 2 - 1 && x <= TOTAL_W / 2 + 1 && z >= 2 && z <= TOTAL_D - 3);
                if (isPath) {
                    place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), bx + x, by, bz + z, chunkBB);
                }
            }
        }
    }

    private void generateWalls(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                               int bx, int by, int bz) {
        for (int x = 0; x < TOTAL_W; x++) {
            for (int z = 0; z < TOTAL_D; z++) {
                boolean isEdge = x == 0 || x == TOTAL_W - 1 || z == 0 || z == TOTAL_D - 1;
                boolean isGate = z == 0 && x >= TOTAL_W / 2 - 2 && x <= TOTAL_W / 2 + 2;

                if (isGate) {
                    if (x == TOTAL_W / 2 - 2 || x == TOTAL_W / 2 + 2) {
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), bx + x, by, bz + z, chunkBB);
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), bx + x, by + 1, bz + z, chunkBB);
                        place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), bx + x, by + 2, bz + z, chunkBB);
                    }
                } else if (isEdge) {
                    place(level, Blocks.STONE_BRICK_WALL.defaultBlockState(), bx + x, by, bz + z, chunkBB);
                    if ((x + z) % 6 == 0) {
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), bx + x, by, bz + z, chunkBB);
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), bx + x, by + 1, bz + z, chunkBB);
                    }
                }
            }
        }
    }

    private void generateLectureHall(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                     int ox, int oy, int oz) {
        int w = 14, h = 8, d = 12;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                place(level, Blocks.OAK_PLANKS.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 1; y < h - 2; y++) {
                for (int z = 0; z < d; z++) {
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isCorner = isWallX && isWallZ;
                    boolean isWall = isWallX || isWallZ;
                    boolean isDoor = z == d - 1 && x >= 5 && x <= 8 && y <= 2;
                    boolean isWindow = !isCorner && isWall && y >= 2 && y <= 3 && !isWallZ;

                    if (isDoor) {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else if (isCorner) {
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else if (isWindow) {
                        place(level, Blocks.GLASS_PANE.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else if (isWall) {
                        if (y == 1) {
                            place(level, Blocks.COBBLESTONE.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                        } else {
                            place(level, Blocks.OAK_PLANKS.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                        }
                    } else {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    }
                }
            }
        }

        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + 6, oy + 1, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + 6, oy + 2, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + 7, oy + 1, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + 7, oy + 2, oz + d - 1, chunkBB);

        generateRoof(level, chunkBB, ox, oy, oz, w, h, d, Blocks.SPRUCE_STAIRS.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState());

        for (int x = 2; x <= w - 3; x++) {
            place(level, Blocks.BOOKSHELF.defaultBlockState(), ox + x, oy + 1, oz + 1, chunkBB);
            place(level, Blocks.BOOKSHELF.defaultBlockState(), ox + x, oy + 2, oz + 1, chunkBB);
            if (x % 3 == 0) {
                place(level, Blocks.BOOKSHELF.defaultBlockState(), ox + x, oy + 3, oz + 1, chunkBB);
            }
        }

        place(level, Blocks.LECTERN.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), ox + w / 2, oy + 1, oz + 4, chunkBB);

        for (int row = 0; row < 3; row++) {
            for (int seat = 2; seat < w - 2; seat += 2) {
                place(level, Blocks.OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM), ox + seat, oy + 1, oz + 6 + row * 2, chunkBB);
            }
        }

        BlockPos hallChest = new BlockPos(ox + w - 2, oy + 1, oz + 1);
        if (chunkBB.isInside(hallChest)) {
            level.setBlock(hallChest, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(hallChest) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/gu_academy_hall"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }

        int lightY = oy + h - 4;
        place(level, Blocks.CHAIN.defaultBlockState(), ox + 4, lightY + 1, oz + d / 2, chunkBB);
        place(level, Blocks.LANTERN.defaultBlockState()
            .setValue(BlockStateProperties.HANGING, true), ox + 4, lightY, oz + d / 2, chunkBB);
        place(level, Blocks.CHAIN.defaultBlockState(), ox + w - 5, lightY + 1, oz + d / 2, chunkBB);
        place(level, Blocks.LANTERN.defaultBlockState()
            .setValue(BlockStateProperties.HANGING, true), ox + w - 5, lightY, oz + d / 2, chunkBB);
    }

    private void generateTrainingRoom(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                      int ox, int oy, int oz) {
        int w = 12, h = 7, d = 12;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                place(level, Blocks.COBBLESTONE.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 1; y < h - 2; y++) {
                for (int z = 0; z < d; z++) {
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isCorner = isWallX && isWallZ;
                    boolean isWall = isWallX || isWallZ;
                    boolean isDoor = z == d - 1 && x >= w / 2 - 1 && x <= w / 2 + 1 && y <= 2;

                    if (isDoor) {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else if (isCorner) {
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else if (isWall) {
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    }
                }
            }
        }

        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + w / 2, oy + 1, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + w / 2, oy + 2, oz + d - 1, chunkBB);

        generateRoof(level, chunkBB, ox, oy, oz, w, h, d, Blocks.SPRUCE_STAIRS.defaultBlockState(),
            Blocks.SPRUCE_PLANKS.defaultBlockState());

        int[][] targetPositions = {
            {ox + 2, oy + 2, oz + 1}, {ox + 5, oy + 2, oz + 1}, {ox + 8, oy + 2, oz + 1},
        };
        for (int[] tp : targetPositions) {
            place(level, Blocks.HAY_BLOCK.defaultBlockState(), tp[0], tp[1], tp[2], chunkBB);
            place(level, Blocks.HAY_BLOCK.defaultBlockState(), tp[0], tp[1] + 1, tp[2], chunkBB);
            place(level, Blocks.TARGET.defaultBlockState(), tp[0], tp[1] + 2, tp[2], chunkBB);
        }

        place(level, Blocks.HAY_BLOCK.defaultBlockState(), ox + w - 3, oy + 1, oz + 2, chunkBB);
        place(level, Blocks.HAY_BLOCK.defaultBlockState(), ox + w - 3, oy + 2, oz + 2, chunkBB);
        place(level, Blocks.CARVED_PUMPKIN.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), ox + w - 3, oy + 3, oz + 2, chunkBB);
        place(level, Blocks.HAY_BLOCK.defaultBlockState(), ox + w - 3, oy + 1, oz + d - 3, chunkBB);
        place(level, Blocks.HAY_BLOCK.defaultBlockState(), ox + w - 3, oy + 2, oz + d - 3, chunkBB);
        place(level, Blocks.CARVED_PUMPKIN.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), ox + w - 3, oy + 3, oz + d - 3, chunkBB);

        place(level, ModBlocks.FORMATION_STONE.get().defaultBlockState(), ox + w / 2, oy + 1, oz + d / 2, chunkBB);

        BlockPos trainingChest = new BlockPos(ox + 1, oy + 1, oz + d - 2);
        if (chunkBB.isInside(trainingChest)) {
            level.setBlock(trainingChest, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(trainingChest) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/gu_academy_training"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }

        int lightY = oy + h - 4;
        place(level, Blocks.CHAIN.defaultBlockState(), ox + w / 2, lightY + 1, oz + d / 2, chunkBB);
        place(level, Blocks.LANTERN.defaultBlockState()
            .setValue(BlockStateProperties.HANGING, true), ox + w / 2, lightY, oz + d / 2, chunkBB);
    }

    private void generateDormitory(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                   int ox, int oy, int oz) {
        int w = 8, h = 6, d = 8;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                place(level, Blocks.OAK_PLANKS.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 1; y < h - 2; y++) {
                for (int z = 0; z < d; z++) {
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isWall = isWallX || isWallZ;
                    boolean isDoor = z == 0 && x == w / 2 && y <= 2;

                    if (isDoor) {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else if (isWall) {
                        if (y == 1) {
                            place(level, Blocks.COBBLESTONE.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                        } else {
                            place(level, Blocks.OAK_PLANKS.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                        }
                    } else {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    }
                }
            }
        }

        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(DoorBlock.FACING, Direction.NORTH), ox + w / 2, oy + 1, oz, chunkBB);
        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
            .setValue(DoorBlock.FACING, Direction.NORTH), ox + w / 2, oy + 2, oz, chunkBB);

        generateRoof(level, chunkBB, ox, oy, oz, w, h, d, Blocks.OAK_STAIRS.defaultBlockState(),
            Blocks.OAK_PLANKS.defaultBlockState());

        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT),
            ox + 1, oy + 1, oz + d - 2, chunkBB);
        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD),
            ox + 1, oy + 1, oz + d - 3, chunkBB);

        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT),
            ox + w - 2, oy + 1, oz + d - 2, chunkBB);
        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD),
            ox + w - 2, oy + 1, oz + d - 3, chunkBB);

        place(level, Blocks.CRAFTING_TABLE.defaultBlockState(), ox + w / 2, oy + 1, oz + 1, chunkBB);

        BlockPos dormChest = new BlockPos(ox + 1, oy + 1, oz + 1);
        if (chunkBB.isInside(dormChest)) {
            level.setBlock(dormChest, Blocks.CHEST.defaultBlockState(), 2);
        }

        place(level, Blocks.LANTERN.defaultBlockState(), ox + w / 2, oy + 1, oz + d / 2, chunkBB);
    }

    private void generateCourtyard(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                   int ox, int oy, int oz) {
        int w = 8, d = 8;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                boolean isEdge = x == 0 || x == w - 1 || z == 0 || z == d - 1;
                if (isEdge) {
                    place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
                }
            }
        }

        place(level, ModBlocks.GU_SHELF.get().defaultBlockState(), ox + w / 2, oy + 1, oz + 2, chunkBB);
        place(level, ModBlocks.GU_SHELF.get().defaultBlockState(), ox + w / 2, oy + 1, oz + d - 3, chunkBB);

        for (int x = 2; x < w - 2; x++) {
            for (int z = 2; z < d - 2; z++) {
                if (random.nextFloat() < 0.2f) {
                    place(level, Blocks.GRASS_BLOCK.defaultBlockState(), ox + x, oy - 1, oz + z, chunkBB);
                    if (random.nextFloat() < 0.5f) {
                        place(level, Blocks.SHORT_GRASS.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
                    }
                }
            }
        }
    }

    private void generateRoof(WorldGenLevel level, BoundingBox chunkBB,
                              int ox, int oy, int oz, int w, int h, int d,
                              BlockState stairState, BlockState plankState) {
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                boolean isEdgeX = x == 0 || x == w - 1;
                boolean isEdgeZ = z == 0 || z == d - 1;

                if (isEdgeZ && !isEdgeX) {
                    Direction facing = z == 0 ? Direction.SOUTH : Direction.NORTH;
                    place(level, stairState
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else if (isEdgeX && !isEdgeZ) {
                    Direction facing = x == 0 ? Direction.EAST : Direction.WEST;
                    place(level, stairState
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else {
                    place(level, plankState, ox + x, oy + h - 2, oz + z, chunkBB);
                }
            }
        }

        for (int x = 1; x < w - 1; x++) {
            for (int z = 1; z < d - 1; z++) {
                place(level, plankState, ox + x, oy + h - 3, oz + z, chunkBB);
            }
        }
    }

    private void generateLanterns(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int bx, int by, int bz) {
        int[][] lanternPositions = {
            {bx + 3, by, bz + 2},
            {bx + TOTAL_W - 4, by, bz + 2},
            {bx + 3, by, bz + TOTAL_D - 3},
            {bx + TOTAL_W - 4, by, bz + TOTAL_D - 3},
            {bx + TOTAL_W / 2, by, bz + 2},
            {bx + TOTAL_W / 2, by, bz + TOTAL_D - 3},
        };

        for (int[] lp : lanternPositions) {
            place(level, Blocks.OAK_FENCE.defaultBlockState(), lp[0], lp[1], lp[2], chunkBB);
            place(level, Blocks.LANTERN.defaultBlockState(), lp[0], lp[1] + 1, lp[2], chunkBB);
        }
    }
}
