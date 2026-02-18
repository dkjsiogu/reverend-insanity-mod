package com.reverendinsanity.world.structure;

import com.reverendinsanity.registry.ModBlocks;
import com.reverendinsanity.registry.ModStructures;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

// 月兰花海洞穴结构件：大型地下溶洞，月兰花田、地下河、七彩钟乳石
public class MoonOrchidCavePiece extends StructurePiece {

    private static final int W = 60;
    private static final int H = 25;
    private static final int D = 60;

    public MoonOrchidCavePiece(BlockPos center, RandomSource random) {
        super(ModStructures.MOON_ORCHID_CAVE_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public MoonOrchidCavePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.MOON_ORCHID_CAVE_PIECE.get(), tag);
    }

    private static BoundingBox makeBB(BlockPos center) {
        return new BoundingBox(
            center.getX() - W / 2, center.getY(), center.getZ() - D / 2,
            center.getX() + W / 2, center.getY() + H, center.getZ() + D / 2);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
    }

    private void place(WorldGenLevel level, BlockState state, int x, int y, int z, BoundingBox bb) {
        if (bb.isInside(x, y, z)) {
            level.setBlock(new BlockPos(x, y, z), state, 2);
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource random,
                            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos pos) {
        int bx = this.boundingBox.minX();
        int by = this.boundingBox.minY();
        int bz = this.boundingBox.minZ();

        carveMainCavern(level, random, chunkBB, bx, by, bz);
        generateRiver(level, random, chunkBB, bx, by, bz);
        generateFlowerFields(level, random, chunkBB, bx, by, bz);
        generateStalactites(level, random, chunkBB, bx, by, bz);
        generateSpiritSpring(level, random, chunkBB, bx + W / 2, by, bz + D / 2);
        generateTreasure(level, random, chunkBB, bx, by, bz);
    }

    private void carveMainCavern(WorldGenLevel level, RandomSource random, BoundingBox bb,
                                  int bx, int by, int bz) {
        double centerX = bx + 30.0;
        double centerY = by + 12.0;
        double centerZ = bz + 30.0;
        double rx = 28.0;
        double ry = 11.0;
        double rz = 28.0;

        for (int x = bx; x <= bx + W; x++) {
            for (int y = by; y <= by + H; y++) {
                for (int z = bz; z <= bz + D; z++) {
                    if (!bb.isInside(x, y, z)) continue;

                    double dx = (x - centerX) / rx;
                    double dy = (y - centerY) / ry;
                    double dz = (z - centerZ) / rz;
                    double dist = dx * dx + dy * dy + dz * dz;

                    if (dist <= 1.0) {
                        if (y == by) {
                            if (random.nextFloat() < 0.1f) {
                                place(level, Blocks.AIR.defaultBlockState(), x, y, z, bb);
                            } else {
                                place(level, Blocks.STONE.defaultBlockState(), x, y, z, bb);
                            }
                        } else {
                            place(level, Blocks.AIR.defaultBlockState(), x, y, z, bb);
                        }
                    }
                }
            }
        }
    }

    private boolean isInsideEllipsoid(int x, int y, int z, int bx, int by, int bz) {
        double centerX = bx + 30.0;
        double centerY = by + 12.0;
        double centerZ = bz + 30.0;
        double dx = (x - centerX) / 28.0;
        double dy = (y - centerY) / 11.0;
        double dz = (z - centerZ) / 28.0;
        return dx * dx + dy * dy + dz * dz <= 1.0;
    }

    private int getRiverCenterX(int z, int bx, int bz) {
        int centerX = bx + W / 2;
        return centerX + (int) (8 * Math.sin((z - bz) * 0.12));
    }

    private boolean isNearRiver(int x, int z, int bx, int bz) {
        int riverX = getRiverCenterX(z, bx, bz);
        return Math.abs(x - riverX) <= 3;
    }

    private void generateRiver(WorldGenLevel level, RandomSource random, BoundingBox bb,
                                int bx, int by, int bz) {
        for (int z = bz + 5; z <= bz + D - 5; z++) {
            int riverX = getRiverCenterX(z, bx, bz);
            int riverWidth = 2 + (random.nextInt(2));

            for (int x = riverX - riverWidth; x <= riverX + riverWidth; x++) {
                if (!isInsideEllipsoid(x, by, z, bx, by, bz)) continue;

                place(level, Blocks.WATER.defaultBlockState(), x, by, z, bb);
                place(level, Blocks.AIR.defaultBlockState(), x, by + 1, z, bb);

                if (by - 1 >= this.boundingBox.minY()) {
                    place(level, Blocks.STONE.defaultBlockState(), x, by - 1, z, bb);
                }

                if (Math.abs(x - riverX) == riverWidth) {
                    if (random.nextFloat() < 0.4f) {
                        place(level, Blocks.GLOW_LICHEN.defaultBlockState(), x, by + 1, z, bb);
                    }
                }
            }
        }
    }

    private void generateFlowerFields(WorldGenLevel level, RandomSource random, BoundingBox bb,
                                       int bx, int by, int bz) {
        BlockState farmland = Blocks.FARMLAND.defaultBlockState();
        BlockState moonOrchid = ModBlocks.MOON_ORCHID.get().defaultBlockState();
        BlockState wildMoonOrchid = ModBlocks.WILD_MOON_ORCHID.get().defaultBlockState();
        BlockState naiveMushroom = ModBlocks.NAIVE_MUSHROOM.get().defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();

        for (int x = bx + 2; x <= bx + W - 2; x++) {
            for (int z = bz + 2; z <= bz + D - 2; z++) {
                if (!bb.isInside(x, by, z)) continue;
                if (!isInsideEllipsoid(x, by, z, bx, by, bz)) continue;
                if (isNearRiver(x, z, bx, bz)) continue;

                BlockPos floorPos = new BlockPos(x, by, z);
                BlockPos abovePos = new BlockPos(x, by + 1, z);

                BlockState floorState = level.getBlockState(floorPos);
                BlockState aboveState = level.getBlockState(abovePos);

                if (!floorState.is(Blocks.STONE) && !floorState.is(Blocks.DEEPSLATE)) continue;
                if (!aboveState.isAir()) continue;

                float roll = random.nextFloat();

                if (roll < 0.40f) {
                    place(level, farmland, x, by, z, bb);
                    place(level, moonOrchid, x, by + 1, z, bb);
                } else if (roll < 0.65f) {
                    place(level, dirt, x, by, z, bb);
                    place(level, wildMoonOrchid, x, by + 1, z, bb);
                } else if (roll < 0.70f) {
                    place(level, dirt, x, by, z, bb);
                    place(level, naiveMushroom, x, by + 1, z, bb);
                }
            }
        }
    }

    private void generateStalactites(WorldGenLevel level, RandomSource random, BoundingBox bb,
                                      int bx, int by, int bz) {
        BlockState stalactite = ModBlocks.RAINBOW_STALACTITE.get().defaultBlockState();

        for (int x = bx + 3; x <= bx + W - 3; x++) {
            for (int z = bz + 3; z <= bz + D - 3; z++) {
                for (int y = by + H - 3; y <= by + H; y++) {
                    if (!bb.isInside(x, y, z)) continue;
                    if (!isInsideEllipsoid(x, y, z, bx, by, bz)) continue;

                    BlockPos abovePos = new BlockPos(x, y + 1, z);
                    BlockPos currentPos = new BlockPos(x, y, z);

                    boolean aboveIsSolid = !bb.isInside(x, y + 1, z)
                        || !isInsideEllipsoid(x, y + 1, z, bx, by, bz);

                    if (!aboveIsSolid) continue;
                    if (!level.getBlockState(currentPos).isAir()) continue;

                    if (random.nextFloat() < 0.15f) {
                        int length = 1 + random.nextInt(3);
                        for (int dy = 0; dy < length; dy++) {
                            int placeY = y - dy;
                            if (placeY <= by + 1) break;
                            BlockPos stalPos = new BlockPos(x, placeY, z);
                            if (bb.isInside(x, placeY, z) && level.getBlockState(stalPos).isAir()) {
                                place(level, stalactite, x, placeY, z, bb);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateSpiritSpring(WorldGenLevel level, RandomSource random, BoundingBox bb,
                                       int cx, int by, int cz) {
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState seaLantern = Blocks.SEA_LANTERN.defaultBlockState();
        BlockState spiritSpring = ModBlocks.SPIRIT_SPRING.get().defaultBlockState();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                place(level, glowstone, cx + dx, by, cz + dz, bb);
            }
        }

        place(level, spiritSpring, cx, by + 1, cz, bb);

        place(level, seaLantern, cx - 2, by, cz - 2, bb);
        place(level, seaLantern, cx + 2, by, cz - 2, bb);
        place(level, seaLantern, cx - 2, by, cz + 2, bb);
        place(level, seaLantern, cx + 2, by, cz + 2, bb);
    }

    private void generateTreasure(WorldGenLevel level, RandomSource random, BoundingBox bb,
                                   int bx, int by, int bz) {
        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTable =
            ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/moon_orchid_cave"));

        int chestX = bx + W / 2;

        int[][] chestPositions = {
            {chestX, by + 1, bz + 5},
            {chestX, by + 1, bz + D - 5}
        };

        for (int[] cp : chestPositions) {
            int x = cp[0];
            int y = cp[1];
            int z = cp[2];
            place(level, Blocks.CHEST.defaultBlockState(), x, y, z, bb);
            if (bb.isInside(x, y, z)) {
                if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof ChestBlockEntity chest) {
                    chest.setLootTable(lootTable, random.nextLong());
                }
            }
        }
    }
}
