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

// 古月山寨结构件：学堂、蛊室、居民小屋、月兰花园
public class ClanSettlementPiece extends StructurePiece {

    private static final int TOTAL_W = 40;
    private static final int TOTAL_H = 12;
    private static final int TOTAL_D = 40;

    public ClanSettlementPiece(BlockPos center, RandomSource random) {
        super(ModStructures.CLAN_SETTLEMENT_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public ClanSettlementPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.CLAN_SETTLEMENT_PIECE.get(), tag);
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
        generateCourtyard(level, random, chunkBB, bx + 15, by, bz + 15);
        generateAcademy(level, random, chunkBB, bx + 14, by, bz + 2);
        generateGuRoom(level, random, chunkBB, bx + 30, by, bz + 16);
        generateHouse(level, random, chunkBB, bx + 5, by, bz + 32);
        generateHouse(level, random, chunkBB, bx + 25, by, bz + 32);
        generateGarden(level, random, chunkBB, bx + 2, by, bz + 16);
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
    }

    private void generateCourtyard(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                   int ox, int oy, int oz) {
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                place(level, Blocks.STONE_BRICKS.defaultBlockState(), ox + x, oy - 1, oz + z, chunkBB);
            }
        }

        for (int i = 0; i < 10; i++) {
            place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), ox + 4, oy, oz + i, chunkBB);
            place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), ox + 5, oy, oz + i, chunkBB);
            place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), ox + i, oy, oz + 4, chunkBB);
            place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), ox + i, oy, oz + 5, chunkBB);
        }

        int cx = ox + 4;
        int cz = oz + 4;
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean isCenter = x == 1 && z == 1;
                if (isCenter) {
                    for (int depth = 0; depth < 3; depth++) {
                        place(level, Blocks.WATER.defaultBlockState(), cx + x, oy - 1 - depth, cz + z, chunkBB);
                    }
                } else {
                    place(level, Blocks.STONE_BRICKS.defaultBlockState(), cx + x, oy, cz + z, chunkBB);
                }
            }
        }

        place(level, Blocks.OAK_FENCE.defaultBlockState(), cx, oy + 1, cz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), cx + 2, oy + 1, cz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), cx, oy + 1, cz + 2, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), cx + 2, oy + 1, cz + 2, chunkBB);
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                place(level, Blocks.OAK_PLANKS.defaultBlockState(), cx + x, oy + 2, cz + z, chunkBB);
            }
        }
    }

    private void generateAcademy(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                 int ox, int oy, int oz) {
        int w = 11, h = 8, d = 11;

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
                    boolean isDoor = z == d - 1 && x >= 4 && x <= 6 && y <= 2;
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
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + 5, oy + 1, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
            .setValue(DoorBlock.FACING, Direction.SOUTH), ox + 5, oy + 2, oz + d - 1, chunkBB);

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                boolean isEdgeX = x == 0 || x == w - 1;
                boolean isEdgeZ = z == 0 || z == d - 1;

                if (isEdgeZ && !isEdgeX) {
                    Direction facing = z == 0 ? Direction.SOUTH : Direction.NORTH;
                    place(level, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else if (isEdgeX && !isEdgeZ) {
                    Direction facing = x == 0 ? Direction.EAST : Direction.WEST;
                    place(level, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else {
                    place(level, Blocks.SPRUCE_PLANKS.defaultBlockState(), ox + x, oy + h - 2, oz + z, chunkBB);
                }
            }
        }

        for (int x = 1; x < w - 1; x++) {
            for (int z = 1; z < d - 1; z++) {
                place(level, Blocks.SPRUCE_PLANKS.defaultBlockState(), ox + x, oy + h - 3, oz + z, chunkBB);
            }
        }

        for (int x = 2; x <= w - 3; x++) {
            place(level, Blocks.BOOKSHELF.defaultBlockState(), ox + x, oy + 1, oz + 1, chunkBB);
            place(level, Blocks.BOOKSHELF.defaultBlockState(), ox + x, oy + 2, oz + 1, chunkBB);
        }

        place(level, Blocks.LECTERN.defaultBlockState(), ox + w / 2, oy + 1, oz + 3, chunkBB);

        BlockPos academyChestPos = new BlockPos(ox + w - 2, oy + 1, oz + 1);
        if (chunkBB.isInside(academyChestPos)) {
            level.setBlock(academyChestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(academyChestPos) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/clan_settlement_academy"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }

        int lightY = oy + h - 4;
        place(level, Blocks.CHAIN.defaultBlockState(), ox + 3, lightY + 1, oz + d / 2, chunkBB);
        place(level, Blocks.LANTERN.defaultBlockState()
            .setValue(BlockStateProperties.HANGING, true), ox + 3, lightY, oz + d / 2, chunkBB);
        place(level, Blocks.CHAIN.defaultBlockState(), ox + w - 4, lightY + 1, oz + d / 2, chunkBB);
        place(level, Blocks.LANTERN.defaultBlockState()
            .setValue(BlockStateProperties.HANGING, true), ox + w - 4, lightY, oz + d / 2, chunkBB);
    }

    private void generateGuRoom(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                int ox, int oy, int oz) {
        int w = 7, h = 6, d = 7;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                place(level, Blocks.SPRUCE_PLANKS.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 1; y < h - 2; y++) {
                for (int z = 0; z < d; z++) {
                    boolean isWallX = x == 0 || x == w - 1;
                    boolean isWallZ = z == 0 || z == d - 1;
                    boolean isWall = isWallX || isWallZ;
                    boolean isDoor = x == 0 && z >= 2 && z <= 4 && y <= 2;

                    if (isDoor) {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else if (isWall) {
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    } else {
                        place(level, Blocks.AIR.defaultBlockState(), ox + x, oy + y, oz + z, chunkBB);
                    }
                }
            }
        }

        place(level, Blocks.SPRUCE_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(DoorBlock.FACING, Direction.WEST), ox, oy + 1, oz + 3, chunkBB);
        place(level, Blocks.SPRUCE_DOOR.defaultBlockState()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
            .setValue(DoorBlock.FACING, Direction.WEST), ox, oy + 2, oz + 3, chunkBB);

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                boolean isEdgeX = x == 0 || x == w - 1;
                boolean isEdgeZ = z == 0 || z == d - 1;

                if (isEdgeZ && !isEdgeX) {
                    Direction facing = z == 0 ? Direction.SOUTH : Direction.NORTH;
                    place(level, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else if (isEdgeX && !isEdgeZ) {
                    Direction facing = x == 0 ? Direction.EAST : Direction.WEST;
                    place(level, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else {
                    place(level, Blocks.SPRUCE_PLANKS.defaultBlockState(), ox + x, oy + h - 2, oz + z, chunkBB);
                }
            }
        }

        for (int x = 1; x < w - 1; x++) {
            for (int z = 1; z < d - 1; z++) {
                place(level, Blocks.SPRUCE_PLANKS.defaultBlockState(), ox + x, oy + h - 3, oz + z, chunkBB);
            }
        }

        place(level, ModBlocks.GU_SHELF.get().defaultBlockState(), ox + w - 2, oy + 1, oz + 1, chunkBB);
        place(level, ModBlocks.GU_SHELF.get().defaultBlockState(), ox + w - 2, oy + 1, oz + d - 2, chunkBB);
        place(level, ModBlocks.GU_SHELF.get().defaultBlockState(), ox + 1, oy + 1, oz + 1, chunkBB);
        place(level, ModBlocks.GU_SHELF.get().defaultBlockState(), ox + 1, oy + 1, oz + d - 2, chunkBB);

        place(level, ModBlocks.REFINEMENT_CAULDRON.get().defaultBlockState(), ox + w / 2, oy + 1, oz + d / 2, chunkBB);

        BlockPos guChestPos = new BlockPos(ox + w - 2, oy + 1, oz + d / 2);
        if (chunkBB.isInside(guChestPos)) {
            level.setBlock(guChestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(guChestPos) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/clan_settlement_gu_room"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }
    }

    private void generateHouse(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                               int ox, int oy, int oz) {
        int w = 5, h = 5, d = 5;

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

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                boolean isEdgeX = x == 0 || x == w - 1;
                boolean isEdgeZ = z == 0 || z == d - 1;

                if (isEdgeZ && !isEdgeX) {
                    Direction facing = z == 0 ? Direction.SOUTH : Direction.NORTH;
                    place(level, Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else if (isEdgeX && !isEdgeZ) {
                    Direction facing = x == 0 ? Direction.EAST : Direction.WEST;
                    place(level, Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, facing)
                        .setValue(StairBlock.HALF, Half.BOTTOM), ox + x, oy + h - 2, oz + z, chunkBB);
                } else {
                    place(level, Blocks.OAK_PLANKS.defaultBlockState(), ox + x, oy + h - 2, oz + z, chunkBB);
                }
            }
        }

        for (int x = 1; x < w - 1; x++) {
            for (int z = 1; z < d - 1; z++) {
                place(level, Blocks.OAK_PLANKS.defaultBlockState(), ox + x, oy + h - 3, oz + z, chunkBB);
            }
        }

        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT),
            ox + 1, oy + 1, oz + d - 2, chunkBB);
        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD),
            ox + 1, oy + 1, oz + d - 3, chunkBB);

        place(level, Blocks.CRAFTING_TABLE.defaultBlockState(), ox + w - 2, oy + 1, oz + d - 2, chunkBB);

        BlockPos houseChestPos = new BlockPos(ox + w - 2, oy + 1, oz + 1);
        if (chunkBB.isInside(houseChestPos)) {
            level.setBlock(houseChestPos, Blocks.CHEST.defaultBlockState(), 2);
        }
    }

    private void generateGarden(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                int ox, int oy, int oz) {
        int w = 7, d = 7;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                boolean isEdge = x == 0 || x == w - 1 || z == 0 || z == d - 1;
                boolean isGate = z == 0 && (x == w / 2);

                if (isGate) {
                    place(level, Blocks.OAK_FENCE_GATE.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), ox + x, oy, oz + z, chunkBB);
                } else if (isEdge) {
                    place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
                }
            }
        }

        for (int x = 1; x < w - 1; x++) {
            for (int z = 1; z < d - 1; z++) {
                if (z == 2 || z == 4) {
                    place(level, Blocks.FARMLAND.defaultBlockState(), ox + x, oy - 1, oz + z, chunkBB);
                    place(level, ModBlocks.MOON_ORCHID.get().defaultBlockState(), ox + x, oy, oz + z, chunkBB);
                } else if (random.nextFloat() < 0.3f) {
                    place(level, ModBlocks.WILD_MOON_ORCHID.get().defaultBlockState(), ox + x, oy, oz + z, chunkBB);
                }
            }
        }
    }

    private void generateLanterns(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int bx, int by, int bz) {
        int[][] lanternPositions = {
            {bx + 14, by, bz + 13},
            {bx + 25, by, bz + 13},
            {bx + 14, by, bz + 26},
            {bx + 25, by, bz + 26},
            {bx + 14, by, bz + 2},
            {bx + 25, by, bz + 2},
            {bx + 5, by, bz + 31},
            {bx + 30, by, bz + 31},
        };

        for (int[] lp : lanternPositions) {
            place(level, Blocks.OAK_FENCE.defaultBlockState(), lp[0], lp[1], lp[2], chunkBB);
            place(level, Blocks.LANTERN.defaultBlockState(), lp[0], lp[1] + 1, lp[2], chunkBB);
        }
    }
}
