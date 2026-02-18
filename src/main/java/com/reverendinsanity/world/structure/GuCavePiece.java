package com.reverendinsanity.world.structure;

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
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

// 蛊窟结构件：生成地下洞窟房间
public class GuCavePiece extends StructurePiece {

    private static final int ROOM_W = 15;
    private static final int ROOM_H = 8;
    private static final int ROOM_D = 15;

    public GuCavePiece(BlockPos center, RandomSource random) {
        super(ModStructures.GU_CAVE_PIECE.get(), 0, makeBB(center));
        this.setOrientation(null);
    }

    public GuCavePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructures.GU_CAVE_PIECE.get(), tag);
    }

    private static BoundingBox makeBB(BlockPos center) {
        return new BoundingBox(
            center.getX() - ROOM_W / 2, center.getY(), center.getZ() - ROOM_D / 2,
            center.getX() + ROOM_W / 2, center.getY() + ROOM_H, center.getZ() + ROOM_D / 2
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

        BlockState deepslate = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState crackedDeep = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        BlockState deepTile = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState mossyStone = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState();
        BlockState chest = Blocks.CHEST.defaultBlockState();
        BlockState spawner = Blocks.SPAWNER.defaultBlockState();

        for (int x = bb.minX(); x <= bb.maxX(); x++) {
            for (int y = bb.minY(); y <= bb.maxY(); y++) {
                for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
                    if (!chunkBB.isInside(x, y, z)) continue;
                    BlockPos bp = new BlockPos(x, y, z);

                    boolean isFloor = y == bb.minY();
                    boolean isCeiling = y == bb.maxY();
                    boolean isWallX = x == bb.minX() || x == bb.maxX();
                    boolean isWallZ = z == bb.minZ() || z == bb.maxZ();
                    boolean isWall = isWallX || isWallZ;

                    if (isFloor) {
                        BlockState floorBlock = random.nextFloat() < 0.3f ? deepTile : deepslate;
                        level.setBlock(bp, floorBlock, 2);
                    } else if (isCeiling) {
                        BlockState ceilBlock = random.nextFloat() < 0.2f ? crackedDeep : deepslate;
                        level.setBlock(bp, ceilBlock, 2);
                    } else if (isWall) {
                        if (random.nextFloat() < 0.15f) {
                            level.setBlock(bp, mossyStone, 2);
                        } else if (random.nextFloat() < 0.1f) {
                            level.setBlock(bp, crackedDeep, 2);
                        } else {
                            level.setBlock(bp, deepslate, 2);
                        }
                    } else {
                        level.setBlock(bp, air, 2);
                    }
                }
            }
        }

        int cx = (bb.minX() + bb.maxX()) / 2;
        int cy = bb.minY() + 1;
        int cz = (bb.minZ() + bb.maxZ()) / 2;

        // 四角柱
        for (int[] corner : new int[][]{
            {bb.minX() + 2, bb.minZ() + 2},
            {bb.maxX() - 2, bb.minZ() + 2},
            {bb.minX() + 2, bb.maxZ() - 2},
            {bb.maxX() - 2, bb.maxZ() - 2}
        }) {
            for (int py = bb.minY() + 1; py < bb.maxY(); py++) {
                BlockPos pp = new BlockPos(corner[0], py, corner[1]);
                if (chunkBB.isInside(pp)) {
                    level.setBlock(pp, deepTile, 2);
                }
            }
        }

        // 魂灯
        int[][] lanternPositions = {
            {bb.minX() + 1, bb.minY() + 4, cz},
            {bb.maxX() - 1, bb.minY() + 4, cz},
            {cx, bb.minY() + 4, bb.minZ() + 1},
            {cx, bb.minY() + 4, bb.maxZ() - 1}
        };
        for (int[] lp : lanternPositions) {
            BlockPos bp = new BlockPos(lp[0], lp[1], lp[2]);
            if (chunkBB.isInside(bp)) {
                level.setBlock(bp, soulLantern, 2);
            }
        }

        // 中央刷怪笼
        BlockPos spawnerPos = new BlockPos(cx, cy, cz);
        if (chunkBB.isInside(spawnerPos)) {
            level.setBlock(spawnerPos.below(), deepTile, 2);
            level.setBlock(spawnerPos, spawner, 2);
            if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawnerEntity) {
                spawnerEntity.setEntityId(
                    net.minecraft.world.entity.EntityType.byString("reverend_insanity:wild_gu")
                        .orElse(net.minecraft.world.entity.EntityType.BAT),
                    random
                );
            }
        }

        // 两个宝箱
        int[][] chestPositions = {
            {bb.minX() + 2, cy, bb.maxZ() - 2},
            {bb.maxX() - 2, cy, bb.minZ() + 2}
        };
        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTable =
            ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("reverend_insanity", "chests/gu_cave"));
        for (int[] cp : chestPositions) {
            BlockPos chestPos = new BlockPos(cp[0], cp[1], cp[2]);
            if (chunkBB.isInside(chestPos)) {
                level.setBlock(chestPos, chest, 2);
                if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                    chestEntity.setLootTable(lootTable, random.nextLong());
                }
            }
        }

        // 中央祭坛台
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos altarPos = new BlockPos(cx + dx, bb.minY(), cz + dz);
                if (chunkBB.isInside(altarPos)) {
                    level.setBlock(altarPos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                if (dx == 0 && dz == 0) {
                    BlockPos above = altarPos.above();
                    if (chunkBB.isInside(above)) {
                        level.setBlock(above, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                    }
                }
            }
        }

        // 散布装饰方块
        for (int i = 0; i < 8; i++) {
            int rx = random.nextIntBetweenInclusive(bb.minX() + 2, bb.maxX() - 2);
            int rz = random.nextIntBetweenInclusive(bb.minZ() + 2, bb.maxZ() - 2);
            BlockPos dp = new BlockPos(rx, cy, rz);
            if (chunkBB.isInside(dp) && level.getBlockState(dp).isAir()) {
                if (random.nextFloat() < 0.5f) {
                    level.setBlock(dp, Blocks.CANDLE.defaultBlockState(), 2);
                } else {
                    level.setBlock(dp, Blocks.SCULK.defaultBlockState(), 2);
                }
            }
        }
    }
}
