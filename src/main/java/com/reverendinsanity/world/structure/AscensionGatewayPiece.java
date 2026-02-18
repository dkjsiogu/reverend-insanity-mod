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

// 登天台结构件：圆形平台、中央柱、避雷针、周围废墟
public class AscensionGatewayPiece extends StructurePiece {

    private static final int TOTAL_W = 30;
    private static final int TOTAL_H = 16;
    private static final int TOTAL_D = 30;
    private static final int RADIUS = 12;

    public AscensionGatewayPiece(BlockPos center, RandomSource random) {
        super(ModStructures.ASCENSION_GATEWAY_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public AscensionGatewayPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.ASCENSION_GATEWAY_PIECE.get(), tag);
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

        int cx = bx + TOTAL_W / 2;
        int cz = bz + TOTAL_D / 2;

        clearArea(level, chunkBB, bx, by, bz);
        generatePlatform(level, random, chunkBB, cx, by, cz);
        generateCentralPillar(level, random, chunkBB, cx, by, cz);
        generateCardinalPillars(level, random, chunkBB, cx, by, cz);
        generateRuins(level, random, chunkBB, cx, by, cz);
        generateLootChest(level, random, chunkBB, cx, by, cz);
    }

    private void place(WorldGenLevel level, BlockState state, int x, int y, int z, BoundingBox bb) {
        if (bb.isInside(x, y, z)) {
            level.setBlock(new BlockPos(x, y, z), state, 2);
        }
    }

    private void clearArea(WorldGenLevel level, BoundingBox chunkBB, int bx, int by, int bz) {
        for (int x = 0; x < TOTAL_W; x++) {
            for (int z = 0; z < TOTAL_D; z++) {
                for (int y = 0; y < TOTAL_H; y++) {
                    place(level, Blocks.AIR.defaultBlockState(), bx + x, by + y, bz + z, chunkBB);
                }
            }
        }
    }

    private void generatePlatform(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                  int cx, int by, int cz) {
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > RADIUS) continue;

                BlockState platformBlock;
                if (dist <= 3) {
                    platformBlock = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
                } else if (dist <= 6) {
                    platformBlock = random.nextFloat() < 0.7f
                        ? Blocks.END_STONE_BRICKS.defaultBlockState()
                        : Blocks.STONE_BRICKS.defaultBlockState();
                } else if (dist <= 9) {
                    platformBlock = random.nextFloat() < 0.5f
                        ? Blocks.STONE_BRICKS.defaultBlockState()
                        : Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                } else {
                    platformBlock = random.nextFloat() < 0.4f
                        ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                        : Blocks.COBBLESTONE.defaultBlockState();
                }
                place(level, platformBlock, cx + dx, by - 1, cz + dz, chunkBB);

                if (dist > 8 && dist <= RADIUS && random.nextFloat() < 0.15f) {
                    place(level, Blocks.COBBLESTONE.defaultBlockState(), cx + dx, by, cz + dz, chunkBB);
                }
            }
        }

        for (int step = 1; step <= 2; step++) {
            int stepRadius = 3 + step * 2;
            for (int dx = -stepRadius; dx <= stepRadius; dx++) {
                for (int dz = -stepRadius; dz <= stepRadius; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= stepRadius && dist > stepRadius - 1.5) {
                        place(level, Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, getStairFacing(dx, dz)),
                            cx + dx, by - step, cz + dz, chunkBB);
                    } else if (dist <= stepRadius - 1.5) {
                        place(level, Blocks.STONE_BRICKS.defaultBlockState(), cx + dx, by - step, cz + dz, chunkBB);
                    }
                }
            }
        }

        int innerRing = 5;
        for (int dx = -innerRing; dx <= innerRing; dx++) {
            for (int dz = -innerRing; dz <= innerRing; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist >= innerRing - 0.5 && dist <= innerRing + 0.5) {
                    place(level, Blocks.END_STONE_BRICKS.defaultBlockState(), cx + dx, by, cz + dz, chunkBB);
                }
            }
        }
    }

    private void generateCentralPillar(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                       int cx, int by, int cz) {
        for (int y = 0; y <= 8; y++) {
            BlockState pillarBlock;
            if (y < 3) {
                pillarBlock = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            } else if (y < 6) {
                pillarBlock = Blocks.OBSIDIAN.defaultBlockState();
            } else {
                pillarBlock = Blocks.END_STONE_BRICKS.defaultBlockState();
            }
            place(level, pillarBlock, cx, by + y, cz, chunkBB);
        }

        place(level, Blocks.LIGHTNING_ROD.defaultBlockState(), cx, by + 9, cz, chunkBB);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (Math.abs(dx) + Math.abs(dz) <= 1) {
                    place(level, Blocks.END_STONE_BRICKS.defaultBlockState(), cx + dx, by + 7, cz + dz, chunkBB);
                }
            }
        }

        int[][] cornerLights = {{cx - 1, cz - 1}, {cx + 1, cz - 1}, {cx - 1, cz + 1}, {cx + 1, cz + 1}};
        for (int[] cl : cornerLights) {
            place(level, Blocks.END_STONE_BRICKS.defaultBlockState(), cl[0], by + 6, cl[1], chunkBB);
            place(level, Blocks.SOUL_LANTERN.defaultBlockState(), cl[0], by + 7, cl[1], chunkBB);
        }

        place(level, ModBlocks.FORMATION_STONE.get().defaultBlockState(), cx - 1, by, cz, chunkBB);
        place(level, ModBlocks.FORMATION_STONE.get().defaultBlockState(), cx + 1, by, cz, chunkBB);
        place(level, ModBlocks.FORMATION_STONE.get().defaultBlockState(), cx, by, cz - 1, chunkBB);
        place(level, ModBlocks.FORMATION_STONE.get().defaultBlockState(), cx, by, cz + 1, chunkBB);
    }

    private void generateCardinalPillars(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                         int cx, int by, int cz) {
        int pillarDist = 8;
        int[][] cardinals = {
            {cx, cz - pillarDist}, {cx, cz + pillarDist},
            {cx - pillarDist, cz}, {cx + pillarDist, cz}
        };

        for (int[] card : cardinals) {
            int px = card[0];
            int pz = card[1];

            int pillarHeight = random.nextIntBetweenInclusive(3, 5);
            for (int y = 0; y < pillarHeight; y++) {
                BlockState block = y < 2
                    ? Blocks.STONE_BRICKS.defaultBlockState()
                    : (random.nextFloat() < 0.5f
                        ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                        : Blocks.STONE_BRICKS.defaultBlockState());
                place(level, block, px, by + y, pz, chunkBB);
            }

            if (random.nextFloat() < 0.6f) {
                place(level, Blocks.LIGHTNING_ROD.defaultBlockState(), px, by + pillarHeight, pz, chunkBB);
            } else {
                place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), px, by + pillarHeight, pz, chunkBB);
            }
        }

        int diagDist = 6;
        int[][] diagonals = {
            {cx - diagDist, cz - diagDist}, {cx + diagDist, cz - diagDist},
            {cx - diagDist, cz + diagDist}, {cx + diagDist, cz + diagDist}
        };
        for (int[] diag : diagonals) {
            int pillarHeight = random.nextIntBetweenInclusive(2, 4);
            for (int y = 0; y < pillarHeight; y++) {
                place(level, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), diag[0], by + y, diag[1], chunkBB);
            }
        }
    }

    private void generateRuins(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                               int cx, int by, int cz) {
        for (int i = 0; i < 20; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 9 + random.nextDouble() * 4;
            int rx = cx + (int) (Math.cos(angle) * dist);
            int rz = cz + (int) (Math.sin(angle) * dist);

            if (!chunkBB.isInside(rx, by, rz)) continue;

            float roll = random.nextFloat();
            if (roll < 0.35f) {
                place(level, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), rx, by, rz, chunkBB);
                if (random.nextFloat() < 0.3f) {
                    place(level, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), rx, by + 1, rz, chunkBB);
                }
            } else if (roll < 0.55f) {
                place(level, Blocks.COBBLESTONE.defaultBlockState(), rx, by, rz, chunkBB);
            } else if (roll < 0.70f) {
                place(level, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), rx, by, rz, chunkBB);
            } else if (roll < 0.80f) {
                place(level, Blocks.STONE_BRICK_SLAB.defaultBlockState(), rx, by, rz, chunkBB);
            } else {
                place(level, Blocks.COBBLESTONE_SLAB.defaultBlockState(), rx, by, rz, chunkBB);
            }
        }

        for (int i = 0; i < 4; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 7 + random.nextDouble() * 5;
            int rx = cx + (int) (Math.cos(angle) * dist);
            int rz = cz + (int) (Math.sin(angle) * dist);

            if (chunkBB.isInside(rx, by, rz)) {
                place(level, Blocks.SOUL_TORCH.defaultBlockState(), rx, by, rz, chunkBB);
            }
        }
    }

    private void generateLootChest(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                   int cx, int by, int cz) {
        BlockPos chestPos = new BlockPos(cx + 2, by, cz + 2);
        if (chunkBB.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/ascension_gateway"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }
    }

    private Direction getStairFacing(int dx, int dz) {
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.WEST : Direction.EAST;
        } else {
            return dz > 0 ? Direction.NORTH : Direction.SOUTH;
        }
    }
}
