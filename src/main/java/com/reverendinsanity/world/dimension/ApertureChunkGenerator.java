package com.reverendinsanity.world.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// 仙窍维度区块生成器：懒加载圆形岛屿地形，道路主题化地表，支持原著规模的超大半径(1500~8000格)
public class ApertureChunkGenerator extends ChunkGenerator {

    public static final MapCodec<ApertureChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
            ).apply(instance, ApertureChunkGenerator::new)
    );

    private static final int SURFACE_Y = 64;
    private static final int WATER_LEVEL = 57;
    static final int GRID_SPACING = 100000;

    public static class IslandData {
        public final int centerX, centerZ, radius;
        public final DaoPath primaryPath;

        public IslandData(int centerX, int centerZ, int radius, DaoPath primaryPath) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = radius;
            this.primaryPath = primaryPath;
        }
    }

    private static final ConcurrentHashMap<Long, IslandData> ISLANDS = new ConcurrentHashMap<>();

    public static void registerIsland(int centerX, int centerZ, int radius, DaoPath primaryPath) {
        ISLANDS.put(gridKey(centerX, centerZ), new IslandData(centerX, centerZ, radius, primaryPath));
    }

    public static void removeIsland(int centerX, int centerZ) {
        ISLANDS.remove(gridKey(centerX, centerZ));
    }

    public static void clearIslands() {
        ISLANDS.clear();
    }

    private static long gridKey(int x, int z) {
        long gx = Math.round((double) x / GRID_SPACING);
        long gz = Math.round((double) z / GRID_SPACING);
        return (gx << 32) | (gz & 0xFFFFFFFFL);
    }

    static IslandData findIsland(int blockX, int blockZ) {
        long key = gridKey(blockX, blockZ);
        IslandData island = ISLANDS.get(key);
        if (island == null) return null;
        double dx = blockX - island.centerX;
        double dz = blockZ - island.centerZ;
        if (dx * dx + dz * dz > (double) island.radius * island.radius * 1.2) return null;
        return island;
    }

    private static final Block[] DEFAULT_FLOWERS = {
            Blocks.DANDELION, Blocks.POPPY, Blocks.CORNFLOWER, Blocks.ALLIUM,
            Blocks.AZURE_BLUET, Blocks.OXEYE_DAISY, Blocks.LILY_OF_THE_VALLEY
    };

    public ApertureChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
            StructureManager structureManager, ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        IslandData island = findIsland(minX + 8, minZ + 8);
        if (island == null) return CompletableFuture.completedFuture(chunk);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;
                int surfaceY = getSurfaceHeight(worldX, worldZ, island);
                if (surfaceY < 0) continue;

                double dx = worldX - island.centerX;
                double dz = worldZ - island.centerZ;
                double dist = Math.sqrt(dx * dx + dz * dz);
                double distRatio = dist / island.radius;

                pos.set(worldX, 0, worldZ);
                chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);

                for (int y = 1; y <= Math.max(0, surfaceY - 4); y++) {
                    pos.setY(y);
                    BlockState state;
                    if (y < 24) {
                        state = getThemedDeepStone(island.primaryPath);
                        long yHash = posHash(worldX * 31 + y, worldZ * 17 + y);
                        if ((yHash & 0x1FF) < 3) {
                            state = getThemedDeepOre(island.primaryPath, yHash);
                        }
                    } else {
                        state = getThemedShallowStone(island.primaryPath);
                        long yHash = posHash(worldX * 31 + y, worldZ * 17 + y);
                        if ((yHash & 0x1FF) < 3) {
                            state = getThemedShallowOre(island.primaryPath, yHash);
                        }
                    }
                    chunk.setBlockState(pos, state, false);
                }

                for (int y = Math.max(1, surfaceY - 3); y < surfaceY; y++) {
                    pos.setY(y);
                    chunk.setBlockState(pos, getThemedSubsurface(island.primaryPath), false);
                }

                pos.setY(surfaceY);
                if (surfaceY >= WATER_LEVEL) {
                    long surfHash = posHash(worldX * 7, worldZ * 13);
                    chunk.setBlockState(pos, getThemedSurface(island.primaryPath, distRatio, surfHash), false);
                } else if (surfaceY >= WATER_LEVEL - 2) {
                    chunk.setBlockState(pos, getThemedShore(island.primaryPath), false);
                } else {
                    chunk.setBlockState(pos, Blocks.GRAVEL.defaultBlockState(), false);
                }

                if (surfaceY < WATER_LEVEL) {
                    BlockState fluid = getThemedFluid(island.primaryPath);
                    for (int y = surfaceY + 1; y <= WATER_LEVEL; y++) {
                        pos.setY(y);
                        chunk.setBlockState(pos, fluid, false);
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager,
            RandomState randomState, ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        IslandData island = findIsland(minX + 8, minZ + 8);
        if (island == null) return;

        Random rng = new Random(chunk.getPos().toLong() ^ 0x5A7B3C1DL);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;
                int surfaceY = getSurfaceHeight(worldX, worldZ, island);
                if (surfaceY < WATER_LEVEL) continue;

                pos.set(worldX, surfaceY + 1, worldZ);
                if (!chunk.getBlockState(pos).isAir()) continue;

                pos.setY(surfaceY);
                BlockState surface = chunk.getBlockState(pos);
                if (surface.isAir()) continue;

                pos.setY(surfaceY + 1);
                placeThemedVegetation(chunk, pos, island.primaryPath, rng);
            }
        }
    }

    private static void placeThemedVegetation(ChunkAccess chunk, BlockPos.MutableBlockPos pos, DaoPath path, Random rng) {
        if (path == null) {
            float roll = rng.nextFloat();
            if (roll < 0.12f) chunk.setBlockState(pos, Blocks.SHORT_GRASS.defaultBlockState(), false);
            else if (roll < 0.15f) chunk.setBlockState(pos, DEFAULT_FLOWERS[rng.nextInt(DEFAULT_FLOWERS.length)].defaultBlockState(), false);
            return;
        }

        float roll = rng.nextFloat();
        switch (path) {
            case FIRE -> {
                if (roll < 0.04f) chunk.setBlockState(pos, Blocks.FIRE.defaultBlockState(), false);
                else if (roll < 0.06f) chunk.setBlockState(pos, Blocks.DEAD_BUSH.defaultBlockState(), false);
            }
            case ICE -> {
                if (roll < 0.15f) chunk.setBlockState(pos, Blocks.SNOW.defaultBlockState(), false);
            }
            case WATER -> {
                if (roll < 0.10f) chunk.setBlockState(pos, Blocks.SHORT_GRASS.defaultBlockState(), false);
                else if (roll < 0.14f) chunk.setBlockState(pos, Blocks.FERN.defaultBlockState(), false);
            }
            case EARTH -> {
                if (roll < 0.08f) chunk.setBlockState(pos, Blocks.SHORT_GRASS.defaultBlockState(), false);
                else if (roll < 0.12f) chunk.setBlockState(pos, Blocks.FERN.defaultBlockState(), false);
                else if (roll < 0.14f) chunk.setBlockState(pos, Blocks.MOSS_CARPET.defaultBlockState(), false);
            }
            case WOOD -> {
                if (roll < 0.15f) chunk.setBlockState(pos, Blocks.SHORT_GRASS.defaultBlockState(), false);
                else if (roll < 0.22f) chunk.setBlockState(pos, Blocks.FERN.defaultBlockState(), false);
                else if (roll < 0.25f) chunk.setBlockState(pos, Blocks.MOSS_CARPET.defaultBlockState(), false);
            }
            case LIGHTNING -> {
                if (roll < 0.02f) chunk.setBlockState(pos, Blocks.LIGHTNING_ROD.defaultBlockState(), false);
            }
            case METAL -> {
                if (roll < 0.03f) chunk.setBlockState(pos, Blocks.IRON_BARS.defaultBlockState(), false);
            }
            case POISON -> {
                if (roll < 0.06f) chunk.setBlockState(pos, Blocks.RED_MUSHROOM.defaultBlockState(), false);
                else if (roll < 0.10f) chunk.setBlockState(pos, Blocks.BROWN_MUSHROOM.defaultBlockState(), false);
                else if (roll < 0.13f) chunk.setBlockState(pos, Blocks.VINE.defaultBlockState(), false);
            }
            case SOUL -> {
                if (roll < 0.05f) chunk.setBlockState(pos, Blocks.SOUL_TORCH.defaultBlockState(), false);
            }
            case BLOOD -> {
                if (roll < 0.04f) chunk.setBlockState(pos, Blocks.RED_CARPET.defaultBlockState(), false);
                else if (roll < 0.06f) chunk.setBlockState(pos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), false);
            }
            case MOON -> {
                if (roll < 0.05f) chunk.setBlockState(pos, Blocks.AMETHYST_CLUSTER.defaultBlockState(), false);
                else if (roll < 0.08f) chunk.setBlockState(pos, ModBlocks.MOON_ORCHID.get().defaultBlockState(), false);
            }
            case STAR -> {
                if (roll < 0.03f) chunk.setBlockState(pos, Blocks.SEA_LANTERN.defaultBlockState(), false);
            }
            case DARK, SHADOW -> {
                if (roll < 0.03f) chunk.setBlockState(pos, Blocks.SCULK.defaultBlockState(), false);
            }
            case LIGHT -> {
                if (roll < 0.04f) chunk.setBlockState(pos, Blocks.GLOWSTONE.defaultBlockState(), false);
                else if (roll < 0.08f) chunk.setBlockState(pos, Blocks.ALLIUM.defaultBlockState(), false);
            }
            case BONE -> {
                if (roll < 0.03f) chunk.setBlockState(pos, Blocks.SKELETON_SKULL.defaultBlockState(), false);
            }
            case WIND, CLOUD -> {
                if (roll < 0.15f) chunk.setBlockState(pos, Blocks.SHORT_GRASS.defaultBlockState(), false);
                else if (roll < 0.17f) chunk.setBlockState(pos, Blocks.WHITE_BANNER.defaultBlockState(), false);
            }
            case FOOD -> {
                if (roll < 0.05f) chunk.setBlockState(pos, Blocks.WHEAT.defaultBlockState(), false);
                else if (roll < 0.08f) chunk.setBlockState(pos, Blocks.POTATOES.defaultBlockState(), false);
            }
            case VOID -> {
                if (roll < 0.02f) chunk.setBlockState(pos, Blocks.END_ROD.defaultBlockState(), false);
            }
            default -> {
                if (roll < 0.12f) chunk.setBlockState(pos, Blocks.SHORT_GRASS.defaultBlockState(), false);
                else if (roll < 0.15f) chunk.setBlockState(pos, DEFAULT_FLOWERS[rng.nextInt(DEFAULT_FLOWERS.length)].defaultBlockState(), false);
            }
        }
    }

    private static BlockState getThemedSurface(DaoPath path, double distRatio, long hash) {
        if (path == null) return Blocks.GRASS_BLOCK.defaultBlockState();
        double themeChance = 0.7 - distRatio * 0.55;
        if (((hash >> 8) & 0xFF) / 255.0 >= themeChance) return Blocks.GRASS_BLOCK.defaultBlockState();

        return switch (path) {
            case FIRE -> ((hash & 7) == 0) ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.RED_SANDSTONE.defaultBlockState();
            case ICE -> ((hash & 3) == 0) ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
            case WATER -> Blocks.GRASS_BLOCK.defaultBlockState();
            case EARTH -> ((hash & 3) == 0) ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            case WOOD -> ((hash & 7) == 0) ? Blocks.PODZOL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            case LIGHTNING -> ((hash & 3) == 0) ? Blocks.OXIDIZED_COPPER.defaultBlockState() : Blocks.COPPER_BLOCK.defaultBlockState();
            case METAL -> ((hash & 1) == 0) ? Blocks.RAW_IRON_BLOCK.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState();
            case POISON -> ((hash & 3) == 0) ? Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState() : Blocks.MUD.defaultBlockState();
            case SOUL -> ((hash & 7) == 0) ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState();
            case BLOOD -> ((hash & 3) == 0) ? Blocks.RED_CONCRETE.defaultBlockState() : Blocks.RED_SANDSTONE.defaultBlockState();
            case MOON -> ((hash & 3) == 0) ? Blocks.END_STONE.defaultBlockState() : Blocks.CALCITE.defaultBlockState();
            case STAR -> {
                int v = (int) (hash & 3);
                yield v == 0 ? Blocks.PURPUR_BLOCK.defaultBlockState() : v == 1 ? Blocks.END_STONE.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
            }
            case DARK, SHADOW -> ((hash & 1) == 0) ? Blocks.BLACKSTONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
            case LIGHT -> ((hash & 1) == 0) ? Blocks.SMOOTH_QUARTZ.defaultBlockState() : Blocks.WHITE_CONCRETE.defaultBlockState();
            case BONE -> Blocks.BONE_BLOCK.defaultBlockState();
            case WIND, CLOUD -> ((hash & 3) == 0) ? Blocks.SANDSTONE.defaultBlockState() : Blocks.SAND.defaultBlockState();
            case FORMATION -> Blocks.SMOOTH_STONE.defaultBlockState();
            case REFINEMENT -> Blocks.STONE_BRICKS.defaultBlockState();
            case FOOD -> Blocks.FARMLAND.defaultBlockState();
            case VOID -> ((hash & 3) == 0) ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.END_STONE.defaultBlockState();
            default -> Blocks.GRASS_BLOCK.defaultBlockState();
        };
    }

    private static BlockState getThemedSubsurface(DaoPath path) {
        if (path == null) return Blocks.DIRT.defaultBlockState();
        return switch (path) {
            case FIRE -> Blocks.RED_SAND.defaultBlockState();
            case ICE -> Blocks.PACKED_ICE.defaultBlockState();
            case SOUL -> Blocks.SOUL_SAND.defaultBlockState();
            case DARK, SHADOW -> Blocks.DEEPSLATE.defaultBlockState();
            case BONE -> Blocks.BONE_BLOCK.defaultBlockState();
            case METAL -> Blocks.RAW_IRON_BLOCK.defaultBlockState();
            case BLOOD -> Blocks.RED_SANDSTONE.defaultBlockState();
            case VOID -> Blocks.END_STONE.defaultBlockState();
            case STAR -> Blocks.END_STONE.defaultBlockState();
            case MOON -> Blocks.CALCITE.defaultBlockState();
            default -> Blocks.DIRT.defaultBlockState();
        };
    }

    private static BlockState getThemedShore(DaoPath path) {
        if (path == null) return Blocks.SAND.defaultBlockState();
        return switch (path) {
            case FIRE -> Blocks.RED_SAND.defaultBlockState();
            case ICE -> Blocks.SNOW_BLOCK.defaultBlockState();
            case SOUL -> Blocks.SOUL_SOIL.defaultBlockState();
            case DARK, SHADOW -> Blocks.BLACKSTONE.defaultBlockState();
            case BLOOD -> Blocks.RED_SAND.defaultBlockState();
            case POISON -> Blocks.MUD.defaultBlockState();
            default -> Blocks.SAND.defaultBlockState();
        };
    }

    private static BlockState getThemedFluid(DaoPath path) {
        if (path == null) return Blocks.WATER.defaultBlockState();
        return switch (path) {
            case FIRE -> Blocks.LAVA.defaultBlockState();
            default -> Blocks.WATER.defaultBlockState();
        };
    }

    private static BlockState getThemedDeepStone(DaoPath path) {
        if (path == null) return Blocks.DEEPSLATE.defaultBlockState();
        return switch (path) {
            case FIRE -> Blocks.NETHERRACK.defaultBlockState();
            case SOUL -> Blocks.SOUL_SAND.defaultBlockState();
            case VOID, STAR -> Blocks.END_STONE.defaultBlockState();
            case DARK, SHADOW -> Blocks.DEEPSLATE.defaultBlockState();
            default -> Blocks.DEEPSLATE.defaultBlockState();
        };
    }

    private static BlockState getThemedShallowStone(DaoPath path) {
        if (path == null) return Blocks.STONE.defaultBlockState();
        return switch (path) {
            case FIRE -> Blocks.NETHERRACK.defaultBlockState();
            case ICE -> Blocks.PACKED_ICE.defaultBlockState();
            case SOUL -> Blocks.SOUL_SOIL.defaultBlockState();
            case VOID, STAR -> Blocks.END_STONE.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private static BlockState getThemedDeepOre(DaoPath path, long yHash) {
        if (path == null || path == FIRE_PATH || path == SOUL_PATH || path == VOID_PATH || path == STAR_PATH) {
            return switch ((int) ((yHash >> 9) & 3)) {
                case 0 -> Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
                case 1 -> Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
                case 2 -> Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
                default -> ModBlocks.DEEPSLATE_PRIMEVAL_STONE_ORE.get().defaultBlockState();
            };
        }
        return switch ((int) ((yHash >> 9) & 3)) {
            case 0 -> Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
            case 1 -> Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
            case 2 -> Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
            default -> ModBlocks.DEEPSLATE_PRIMEVAL_STONE_ORE.get().defaultBlockState();
        };
    }

    private static BlockState getThemedShallowOre(DaoPath path, long yHash) {
        if (path == DaoPath.METAL) {
            return switch ((int) ((yHash >> 9) & 3)) {
                case 0 -> Blocks.IRON_ORE.defaultBlockState();
                case 1 -> Blocks.GOLD_ORE.defaultBlockState();
                default -> ModBlocks.PRIMEVAL_STONE_ORE.get().defaultBlockState();
            };
        }
        return switch ((int) ((yHash >> 9) & 3)) {
            case 0 -> Blocks.IRON_ORE.defaultBlockState();
            case 1 -> Blocks.GOLD_ORE.defaultBlockState();
            case 2 -> Blocks.COPPER_ORE.defaultBlockState();
            default -> ModBlocks.PRIMEVAL_STONE_ORE.get().defaultBlockState();
        };
    }

    private static final DaoPath FIRE_PATH = DaoPath.FIRE;
    private static final DaoPath SOUL_PATH = DaoPath.SOUL;
    private static final DaoPath VOID_PATH = DaoPath.VOID;
    private static final DaoPath STAR_PATH = DaoPath.STAR;

    static int getSurfaceHeight(int worldX, int worldZ, IslandData island) {
        double dx = worldX - island.centerX;
        double dz = worldZ - island.centerZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist >= island.radius) return -1;

        int edgeWidth = Math.max(32, island.radius / 8);
        double edgeFactor = (dist > island.radius - edgeWidth)
                ? smoothstep((island.radius - dist) / edgeWidth)
                : 1.0;

        double centerFlatten = smoothstep(Math.min(1.0, dist / 48.0));
        double noise = terrainNoise(worldX, worldZ);

        if (island.primaryPath == DaoPath.EARTH) noise *= 1.5;
        if (island.primaryPath == DaoPath.WIND || island.primaryPath == DaoPath.CLOUD) noise *= 0.5;

        double innerHeight = SURFACE_Y + noise * centerFlatten;
        double edgeHeight = WATER_LEVEL - 3;

        return Math.max(1, (int) (edgeHeight + (innerHeight - edgeHeight) * edgeFactor));
    }

    private static double smoothstep(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    private static double terrainNoise(double x, double z) {
        double n = 0;
        n += Math.sin(x * 0.005 + 0.3) * Math.cos(z * 0.005 + 0.7) * 8;
        n += Math.sin(x * 0.013 + z * 0.009) * Math.cos(z * 0.011 - x * 0.007) * 5;
        n += Math.sin(x * 0.031 + 0.5) * Math.cos(z * 0.027 + 0.2) * 2.5;
        n += Math.sin(x * 0.067 + z * 0.059) * 1;
        return n;
    }

    private static long posHash(int a, int b) {
        long h = a * 3129871L ^ b * 116129781L;
        h = h * h * 42317861L + h * 11L;
        return h >> 16;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType,
            LevelHeightAccessor level, RandomState randomState) {
        return SURFACE_Y + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] states = new BlockState[level.getHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        return new NoiseColumn(level.getMinBuildHeight(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("Immortal Aperture");
        IslandData island = findIsland(pos.getX(), pos.getZ());
        if (island != null) {
            double dist = Math.sqrt(Math.pow(pos.getX() - island.centerX, 2) + Math.pow(pos.getZ() - island.centerZ, 2));
            info.add("Island: r=" + island.radius + " dist=" + (int) dist
                    + (island.primaryPath != null ? " path=" + island.primaryPath.name() : ""));
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState,
            BiomeManager biomeManager, StructureManager structureManager,
            ChunkAccess chunk, GenerationStep.Carving carving) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getMinY() { return 0; }

    @Override
    public int getGenDepth() { return 256; }

    @Override
    public int getSeaLevel() { return WATER_LEVEL; }
}
