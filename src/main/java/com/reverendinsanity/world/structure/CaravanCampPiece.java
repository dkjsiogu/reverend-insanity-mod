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

// 商队营地结构件：帐篷、篝火、交易摊位、散落货物
public class CaravanCampPiece extends StructurePiece {

    private static final int TOTAL_W = 22;
    private static final int TOTAL_H = 8;
    private static final int TOTAL_D = 20;

    public CaravanCampPiece(BlockPos center, RandomSource random) {
        super(ModStructures.CARAVAN_CAMP_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public CaravanCampPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.CARAVAN_CAMP_PIECE.get(), tag);
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
        generateFence(level, random, chunkBB, bx, by, bz);
        generateMainTent(level, random, chunkBB, bx + 2, by, bz + 2);
        generateSmallTent(level, random, chunkBB, bx + 14, by, bz + 2);
        generateCampfire(level, random, chunkBB, bx + TOTAL_W / 2, by, bz + TOTAL_D / 2);
        generateTradingStall(level, random, chunkBB, bx + 2, by, bz + 13);
        generateScatteredGoods(level, random, chunkBB, bx, by, bz);
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
                for (int y = 0; y < TOTAL_H; y++) {
                    place(level, Blocks.AIR.defaultBlockState(), bx + x, by + y, bz + z, chunkBB);
                }
                if (random.nextFloat() < 0.3f) {
                    place(level, Blocks.DIRT_PATH.defaultBlockState(), bx + x, by - 1, bz + z, chunkBB);
                }
            }
        }
    }

    private void generateFence(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                               int bx, int by, int bz) {
        for (int x = 0; x < TOTAL_W; x++) {
            for (int z = 0; z < TOTAL_D; z++) {
                boolean isEdge = x == 0 || x == TOTAL_W - 1 || z == 0 || z == TOTAL_D - 1;
                boolean isGate = z == 0 && x >= TOTAL_W / 2 - 1 && x <= TOTAL_W / 2 + 1;

                if (isGate) {
                    if (x == TOTAL_W / 2) {
                        place(level, Blocks.OAK_FENCE_GATE.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), bx + x, by, bz + z, chunkBB);
                    }
                } else if (isEdge) {
                    if (random.nextFloat() < 0.85f) {
                        place(level, Blocks.OAK_FENCE.defaultBlockState(), bx + x, by, bz + z, chunkBB);
                    }
                }
            }
        }
    }

    private void generateMainTent(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int ox, int oy, int oz) {
        int w = 7, d = 7;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                place(level, Blocks.WHITE_CARPET.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
            }
        }

        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox, oy + 1, oz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w - 1, oy + 1, oz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox, oy + 1, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w - 1, oy + 1, oz + d - 1, chunkBB);

        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w / 2, oy + 1, oz + d / 2, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w / 2, oy + 2, oz + d / 2, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w / 2, oy + 3, oz + d / 2, chunkBB);

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                int distX = Math.min(x, w - 1 - x);
                int distZ = Math.min(z, d - 1 - z);
                int minDist = Math.min(distX, distZ);
                int roofY = oy + 2 + Math.min(minDist, 2);

                BlockState woolColor;
                if ((x + z) % 3 == 0) {
                    woolColor = Blocks.RED_WOOL.defaultBlockState();
                } else if ((x + z) % 3 == 1) {
                    woolColor = Blocks.ORANGE_WOOL.defaultBlockState();
                } else {
                    woolColor = Blocks.YELLOW_WOOL.defaultBlockState();
                }

                place(level, woolColor, ox + x, roofY, oz + z, chunkBB);
            }
        }

        BlockPos tentChest = new BlockPos(ox + 1, oy + 1, oz + 1);
        if (chunkBB.isInside(tentChest)) {
            level.setBlock(tentChest, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(tentChest) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/caravan_camp"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }

        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT),
            ox + w - 2, oy + 1, oz + d - 2, chunkBB);
        place(level, Blocks.RED_BED.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.BED_PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD),
            ox + w - 2, oy + 1, oz + d - 3, chunkBB);
    }

    private void generateSmallTent(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                   int ox, int oy, int oz) {
        int w = 5, d = 5;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                place(level, Blocks.BROWN_CARPET.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
            }
        }

        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox, oy + 1, oz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w - 1, oy + 1, oz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox, oy + 1, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w - 1, oy + 1, oz + d - 1, chunkBB);

        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w / 2, oy + 1, oz + d / 2, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w / 2, oy + 2, oz + d / 2, chunkBB);

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                int distX = Math.min(x, w - 1 - x);
                int distZ = Math.min(z, d - 1 - z);
                int minDist = Math.min(distX, distZ);
                int roofY = oy + 2 + Math.min(minDist, 1);

                place(level, Blocks.BROWN_WOOL.defaultBlockState(), ox + x, roofY, oz + z, chunkBB);
            }
        }

        place(level, Blocks.BARREL.defaultBlockState(), ox + 1, oy + 1, oz + 1, chunkBB);
        place(level, Blocks.BARREL.defaultBlockState(), ox + w - 2, oy + 1, oz + 1, chunkBB);
    }

    private void generateCampfire(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int cx, int cy, int cz) {
        place(level, Blocks.CAMPFIRE.defaultBlockState()
            .setValue(BlockStateProperties.LIT, true), cx, cy, cz, chunkBB);

        int[][] seatPositions = {
            {cx - 2, cz}, {cx + 2, cz}, {cx, cz - 2}, {cx, cz + 2},
        };
        Direction[] facings = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};
        for (int i = 0; i < seatPositions.length; i++) {
            place(level, Blocks.OAK_LOG.defaultBlockState(), seatPositions[i][0], cy, seatPositions[i][1], chunkBB);
        }
    }

    private void generateTradingStall(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                      int ox, int oy, int oz) {
        int w = 7, d = 4;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                place(level, Blocks.OAK_PLANKS.defaultBlockState(), ox + x, oy, oz + z, chunkBB);
            }
        }

        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox, oy + 1, oz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w - 1, oy + 1, oz, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox, oy + 1, oz + d - 1, chunkBB);
        place(level, Blocks.OAK_FENCE.defaultBlockState(), ox + w - 1, oy + 1, oz + d - 1, chunkBB);

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                BlockState woolColor = (x + z) % 2 == 0
                    ? Blocks.BLUE_WOOL.defaultBlockState()
                    : Blocks.WHITE_WOOL.defaultBlockState();
                place(level, woolColor, ox + x, oy + 2, oz + z, chunkBB);
            }
        }

        place(level, Blocks.CRAFTING_TABLE.defaultBlockState(), ox + 1, oy + 1, oz + 1, chunkBB);

        BlockPos stallChest = new BlockPos(ox + 3, oy + 1, oz + 1);
        if (chunkBB.isInside(stallChest)) {
            level.setBlock(stallChest, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(stallChest) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/caravan_camp_stall"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }

        place(level, ModBlocks.WINE_JAR.get().defaultBlockState(), ox + 5, oy + 1, oz + 1, chunkBB);
        place(level, ModBlocks.GU_SHELF.get().defaultBlockState(), ox + w - 2, oy + 1, oz + d - 2, chunkBB);
    }

    private void generateScatteredGoods(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                        int bx, int by, int bz) {
        for (int i = 0; i < 6; i++) {
            int rx = random.nextIntBetweenInclusive(bx + 2, bx + TOTAL_W - 3);
            int rz = random.nextIntBetweenInclusive(bz + 2, bz + TOTAL_D - 3);
            BlockPos goodsPos = new BlockPos(rx, by, rz);
            if (chunkBB.isInside(goodsPos) && level.getBlockState(goodsPos).isAir()) {
                float roll = random.nextFloat();
                if (roll < 0.3f) {
                    level.setBlock(goodsPos, Blocks.BARREL.defaultBlockState(), 2);
                } else if (roll < 0.5f) {
                    level.setBlock(goodsPos, ModBlocks.WINE_JAR.get().defaultBlockState(), 2);
                } else if (roll < 0.7f) {
                    level.setBlock(goodsPos, Blocks.HAY_BLOCK.defaultBlockState(), 2);
                } else {
                    level.setBlock(goodsPos, Blocks.FLOWER_POT.defaultBlockState(), 2);
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            int rx = random.nextIntBetweenInclusive(bx + 1, bx + TOTAL_W - 2);
            int rz = random.nextIntBetweenInclusive(bz + 1, bz + TOTAL_D - 2);
            BlockPos torchPos = new BlockPos(rx, by, rz);
            if (chunkBB.isInside(torchPos) && level.getBlockState(torchPos).isAir()) {
                level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 2);
            }
        }
    }
}
