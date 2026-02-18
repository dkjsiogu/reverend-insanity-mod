package com.reverendinsanity.world.structure;

import com.reverendinsanity.core.heaven.HeavenType;
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

// 九天碎片遗迹结构件：陨石坑造型，中央发光方块+碎片箱子
public class HeavenFragmentPiece extends StructurePiece {

    private static final int TOTAL_W = 10;
    private static final int TOTAL_H = 6;
    private static final int TOTAL_D = 10;

    private final int heavenIndex;

    public HeavenFragmentPiece(BlockPos center, RandomSource random) {
        super(ModStructures.HEAVEN_FRAGMENT_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
        this.heavenIndex = random.nextInt(HeavenType.values().length);
    }

    public HeavenFragmentPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.HEAVEN_FRAGMENT_PIECE.get(), tag);
        this.heavenIndex = tag.getInt("HeavenIndex");
    }

    private static BoundingBox makeBB(BlockPos center) {
        return new BoundingBox(
            center.getX() - TOTAL_W / 2, center.getY() - 3, center.getZ() - TOTAL_D / 2,
            center.getX() + TOTAL_W / 2, center.getY() + TOTAL_H - 3, center.getZ() + TOTAL_D / 2
        );
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("HeavenIndex", heavenIndex);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource random,
                            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos pos) {

        BoundingBox bb = this.boundingBox;
        int baseX = bb.minX();
        int baseY = bb.minY();
        int baseZ = bb.minZ();

        HeavenType heaven = HeavenType.values()[heavenIndex % HeavenType.values().length];

        generateCrater(level, random, chunkBB, baseX, baseY, baseZ, heaven);
    }

    private void generateCrater(WorldGenLevel level, RandomSource random, BoundingBox chunkBB,
                                 int ox, int oy, int oz, HeavenType heaven) {
        int w = TOTAL_W;
        int d = TOTAL_D;
        int cx = ox + w / 2;
        int cz = oz + d / 2;

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                double dx = x - w / 2.0 + 0.5;
                double dz = z - d / 2.0 + 0.5;
                double dist = Math.sqrt(dx * dx + dz * dz);

                if (dist > w / 2.0 + 0.5) continue;

                int depth;
                if (dist < 1.5) {
                    depth = 3;
                } else if (dist < 3.0) {
                    depth = 2;
                } else if (dist < 4.5) {
                    depth = 1;
                } else {
                    depth = 0;
                }

                for (int y = -depth; y <= 0; y++) {
                    BlockPos bp = new BlockPos(ox + x, oy + 3 + y, oz + z);
                    if (!chunkBB.isInside(bp)) continue;

                    if (y == -depth) {
                        if (dist < 2.0) {
                            placeIfInside(level, chunkBB, bp, getColoredBlock(heaven));
                        } else {
                            placeIfInside(level, chunkBB, bp, randomCraterFloor(random));
                        }
                    } else {
                        placeIfInside(level, chunkBB, bp, Blocks.AIR.defaultBlockState());
                    }
                }

                if (dist > 4.0 && dist < 5.5 && random.nextFloat() < 0.4f) {
                    BlockPos rimPos = new BlockPos(ox + x, oy + 4, oz + z);
                    placeIfInside(level, chunkBB, rimPos, randomRimBlock(random));
                }
            }
        }

        BlockPos glowPos = new BlockPos(cx, oy + 1, cz);
        placeIfInside(level, chunkBB, glowPos, getGlowBlock(heaven));

        BlockPos chestPos = new BlockPos(cx + 1, oy + 1, cz);
        if (chunkBB.isInside(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot =
                    ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/heaven_fragment"));
                chestEntity.setLootTable(loot, random.nextLong());
            }
        }

        for (int i = 0; i < 4; i++) {
            int rx = random.nextIntBetweenInclusive(ox + 2, ox + w - 3);
            int rz = random.nextIntBetweenInclusive(oz + 2, oz + d - 3);
            BlockPos debrisPos = new BlockPos(rx, oy + 2, rz);
            if (chunkBB.isInside(debrisPos) && level.getBlockState(debrisPos).isAir()) {
                placeIfInside(level, chunkBB, debrisPos, Blocks.END_STONE_BRICK_SLAB.defaultBlockState());
            }
        }

        for (int i = 0; i < 3; i++) {
            int rx = random.nextIntBetweenInclusive(ox + 1, ox + w - 2);
            int rz = random.nextIntBetweenInclusive(oz + 1, oz + d - 2);
            BlockPos scatterPos = new BlockPos(rx, oy + 3, rz);
            if (chunkBB.isInside(scatterPos)) {
                float roll = random.nextFloat();
                if (roll < 0.5f) {
                    placeIfInside(level, chunkBB, scatterPos, Blocks.END_STONE_BRICKS.defaultBlockState());
                } else {
                    placeIfInside(level, chunkBB, scatterPos, getColoredGlass(heaven));
                }
            }
        }
    }

    private BlockState getColoredBlock(HeavenType heaven) {
        return switch (heaven) {
            case WHITE -> Blocks.WHITE_CONCRETE.defaultBlockState();
            case RED -> Blocks.RED_CONCRETE.defaultBlockState();
            case ORANGE -> Blocks.ORANGE_CONCRETE.defaultBlockState();
            case YELLOW -> Blocks.YELLOW_CONCRETE.defaultBlockState();
            case GREEN -> Blocks.GREEN_CONCRETE.defaultBlockState();
            case CYAN -> Blocks.CYAN_CONCRETE.defaultBlockState();
            case BLUE -> Blocks.BLUE_CONCRETE.defaultBlockState();
            case PURPLE -> Blocks.PURPLE_CONCRETE.defaultBlockState();
            case BLACK -> Blocks.BLACK_CONCRETE.defaultBlockState();
        };
    }

    private BlockState getColoredGlass(HeavenType heaven) {
        return switch (heaven) {
            case WHITE -> Blocks.WHITE_STAINED_GLASS.defaultBlockState();
            case RED -> Blocks.RED_STAINED_GLASS.defaultBlockState();
            case ORANGE -> Blocks.ORANGE_STAINED_GLASS.defaultBlockState();
            case YELLOW -> Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
            case GREEN -> Blocks.GREEN_STAINED_GLASS.defaultBlockState();
            case CYAN -> Blocks.CYAN_STAINED_GLASS.defaultBlockState();
            case BLUE -> Blocks.BLUE_STAINED_GLASS.defaultBlockState();
            case PURPLE -> Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
            case BLACK -> Blocks.BLACK_STAINED_GLASS.defaultBlockState();
        };
    }

    private BlockState getGlowBlock(HeavenType heaven) {
        return switch (heaven) {
            case WHITE, YELLOW -> Blocks.SEA_LANTERN.defaultBlockState();
            case RED, ORANGE -> Blocks.GLOWSTONE.defaultBlockState();
            case GREEN, CYAN -> Blocks.SEA_LANTERN.defaultBlockState();
            case BLUE, PURPLE -> Blocks.SHROOMLIGHT.defaultBlockState();
            case BLACK -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
        };
    }

    private BlockState randomCraterFloor(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.3f) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (roll < 0.6f) return Blocks.END_STONE_BRICKS.defaultBlockState();
        if (roll < 0.8f) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    private BlockState randomRimBlock(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.4f) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (roll < 0.7f) return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    private void placeIfInside(WorldGenLevel level, BoundingBox chunkBB, BlockPos pos, BlockState state) {
        if (chunkBB.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
