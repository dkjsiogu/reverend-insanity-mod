package com.reverendinsanity.world.dimension;

import com.reverendinsanity.core.aperture.BlessedLandGrade;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;
import java.util.Random;

// 仙窍地形生成器：在中心featureRadius范围内放置特征方块（传送台/灵泉/资源/道痕区域），基础地形由ChunkGenerator懒加载
public class ApertureTerrainBuilder {

    private static final int SURFACE_Y = 64;
    private static final int DAO_FEATURE_TOP5 = 5;

    public static void generate(ServerLevel level, BlockPos center, BlessedLandGrade grade, Map<DaoPath, Integer> daoMarks) {
        int featureRadius = grade.getFeatureRadius();
        Random random = new Random(center.getX() * 31L + center.getZ());

        placeExitPortal(level, center);
        placeSpiritSpring(level, center);
        placePaths(level, center, featureRadius, random);
        placeScatter(level, center, featureRadius, random);
        placeResources(level, center, featureRadius, grade, random);
        placeVegetation(level, center, featureRadius, grade, random);
        placeDaoFeatures(level, center, featureRadius, daoMarks, random);
    }

    private static void placeExitPortal(ServerLevel level, BlockPos center) {
        int cx = center.getX();
        int cz = center.getZ();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(new BlockPos(cx + dx, SURFACE_Y + 1, cz + dz),
                        Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(cx, SURFACE_Y + 2, cz),
                ModBlocks.APERTURE_EXIT_PORTAL.get().defaultBlockState(), 2);
    }

    private static void placeSpiritSpring(ServerLevel level, BlockPos center) {
        level.setBlock(new BlockPos(center.getX(), SURFACE_Y + 1, center.getZ() - 5),
                ModBlocks.SPIRIT_SPRING.get().defaultBlockState(), 2);
    }

    private static void placePaths(ServerLevel level, BlockPos center, int radius, Random random) {
        int cx = center.getX();
        int cz = center.getZ();
        int pathLen = Math.min(radius - 4, 80);
        if (pathLen < 8) return;

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};

        for (int[] dir : dirs) {
            for (int step = 3; step < pathLen; step++) {
                int px = cx + dir[0] * step;
                int pz = cz + dir[1] * step;
                int wobbleX = random.nextInt(3) - 1;
                int wobbleZ = random.nextInt(3) - 1;
                px += wobbleX;
                pz += wobbleZ;

                BlockPos pathPos = new BlockPos(px, SURFACE_Y, pz);
                if (level.getBlockState(pathPos).is(Blocks.GRASS_BLOCK)) {
                    level.setBlock(pathPos, Blocks.DIRT_PATH.defaultBlockState(), 2);
                }
                if (random.nextInt(4) == 0) {
                    BlockPos side = new BlockPos(px + (random.nextBoolean() ? 1 : -1), SURFACE_Y, pz);
                    if (level.getBlockState(side).is(Blocks.GRASS_BLOCK)) {
                        level.setBlock(side, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void placeScatter(ServerLevel level, BlockPos center, int radius, Random random) {
        int cx = center.getX();
        int cz = center.getZ();
        int inner = radius - 4;
        int count = Math.min(radius, 300);

        for (int i = 0; i < count; i++) {
            int dx = random.nextInt(inner * 2 + 1) - inner;
            int dz = random.nextInt(inner * 2 + 1) - inner;
            if (dx * dx + dz * dz > inner * inner) continue;
            int bx = cx + dx;
            int bz = cz + dz;

            BlockPos pos = new BlockPos(bx, SURFACE_Y, bz);
            if (!level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) continue;
            if (Math.abs(dx) < 3 && Math.abs(dz) < 3) continue;

            if (random.nextInt(3) == 0) {
                level.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(), 2);
            } else if (random.nextInt(5) == 0) {
                level.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 2);
            }

            if (random.nextInt(8) == 0) {
                BlockPos above = pos.above();
                if (level.getBlockState(above).isAir()) {
                    level.setBlock(above, Blocks.COBBLESTONE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void placeResources(ServerLevel level, BlockPos center, int radius, BlessedLandGrade grade, Random random) {
        int cx = center.getX();
        int cz = center.getZ();
        int inner = radius - 4;

        int oreClusters = switch (grade) {
            case LOWER -> 4;
            case MIDDLE -> 10;
            case UPPER -> 20;
            case SUPREME -> 35;
        };

        for (int i = 0; i < oreClusters; i++) {
            int ox = cx + random.nextInt(inner * 2 + 1) - inner;
            int oz = cz + random.nextInt(inner * 2 + 1) - inner;
            int oy = 15 + random.nextInt(40);
            Block ore = (oy < 30)
                    ? ModBlocks.DEEPSLATE_PRIMEVAL_STONE_ORE.get()
                    : ModBlocks.PRIMEVAL_STONE_ORE.get();

            int clusterSize = 2 + random.nextInt(3);
            for (int dx = 0; dx < clusterSize; dx++) {
                for (int dz = 0; dz < clusterSize; dz++) {
                    for (int dy = 0; dy < clusterSize; dy++) {
                        if (random.nextFloat() < 0.6f) {
                            BlockPos orePos = new BlockPos(ox + dx, oy + dy, oz + dz);
                            if (level.getBlockState(orePos).is(Blocks.STONE) ||
                                    level.getBlockState(orePos).is(Blocks.DEEPSLATE)) {
                                level.setBlock(orePos, ore.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }

        int lakeCount = switch (grade) {
            case LOWER -> 1;
            case MIDDLE -> 2;
            case UPPER -> 4;
            case SUPREME -> 6;
        };
        int lakeSize = switch (grade) {
            case LOWER -> 5;
            case MIDDLE -> 8;
            case UPPER -> 10;
            case SUPREME -> 14;
        };

        for (int l = 0; l < lakeCount; l++) {
            int lx = cx + random.nextInt(inner) - inner / 2;
            int lz = cz + random.nextInt(inner) - inner / 2;
            if (Math.abs(lx - cx) < 6 && Math.abs(lz - cz) < 6) {
                lx += (lx >= cx) ? 10 : -10;
            }
            placeLake(level, new BlockPos(lx, SURFACE_Y, lz), lakeSize, random);
        }

        if (grade.ordinal() >= BlessedLandGrade.MIDDLE.ordinal()) {
            int bambooCount = switch (grade) {
                case MIDDLE -> 8;
                case UPPER -> 20;
                case SUPREME -> 35;
                default -> 0;
            };
            for (int i = 0; i < bambooCount; i++) {
                int bx = cx + random.nextInt(inner * 2 + 1) - inner;
                int bz = cz + random.nextInt(inner * 2 + 1) - inner;
                BlockPos pos = new BlockPos(bx, SURFACE_Y + 1, bz);
                if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, ModBlocks.SPEAR_BAMBOO.get().defaultBlockState(), 2);
                }
            }

            int mushroomCount = switch (grade) {
                case MIDDLE -> 5;
                case UPPER -> 12;
                case SUPREME -> 20;
                default -> 0;
            };
            for (int i = 0; i < mushroomCount; i++) {
                int mx = cx + random.nextInt(inner * 2 + 1) - inner;
                int mz = cz + random.nextInt(inner * 2 + 1) - inner;
                BlockPos pos = new BlockPos(mx, SURFACE_Y + 1, mz);
                if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, ModBlocks.NAIVE_MUSHROOM.get().defaultBlockState(), 2);
                }
            }
        }

        if (grade.ordinal() >= BlessedLandGrade.UPPER.ordinal()) {
            int caveX = cx + 20 + random.nextInt(15);
            int caveZ = cz + random.nextInt(10) - 5;
            placeStalactiteCave(level, new BlockPos(caveX, 45, caveZ), random);
        }

        if (grade == BlessedLandGrade.SUPREME) {
            for (int i = 0; i < 8; i++) {
                int sx = cx + random.nextInt(inner) - inner / 2;
                int sz = cz + random.nextInt(inner) - inner / 2;
                if (Math.abs(sx - cx) < 5 && Math.abs(sz - cz) < 5) continue;
                BlockPos pos = new BlockPos(sx, SURFACE_Y + 1, sz);
                if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, ModBlocks.GU_SHELF.get().defaultBlockState(), 2);
                }
            }
        }
    }

    private static void placeLake(ServerLevel level, BlockPos center, int size, Random random) {
        int cx = center.getX();
        int cz = center.getZ();
        int r2 = size * size;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > r2) continue;

                int bx = cx + dx;
                int bz = cz + dz;

                int depth = (d2 < r2 / 3) ? 3 : (d2 < r2 * 2 / 3) ? 2 : 1;

                for (int y = SURFACE_Y; y > SURFACE_Y - depth; y--) {
                    level.setBlock(new BlockPos(bx, y, bz), Blocks.WATER.defaultBlockState(), 2);
                }

                if (d2 >= r2 * 2 / 3 && random.nextFloat() < 0.4f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SAND.defaultBlockState(), 2);
                }

                if (d2 >= r2 - size * 2 && random.nextFloat() < 0.3f) {
                    BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                    if (level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.SHORT_GRASS.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void placeStalactiteCave(ServerLevel level, BlockPos origin, Random random) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx * dx + dz * dz > 20) continue;
                for (int dy = 0; dy <= 5; dy++) {
                    BlockPos pos = new BlockPos(ox + dx, oy + dy, oz + dz);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx * dx + dz * dz > 20) continue;
                if (random.nextFloat() < 0.3f) {
                    BlockPos pos = new BlockPos(ox + dx, oy + 5, oz + dz);
                    level.setBlock(pos, ModBlocks.RAINBOW_STALACTITE.get().defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(ox, oy, oz), Blocks.GLOWSTONE.defaultBlockState(), 2);
    }

    private static void placeVegetation(ServerLevel level, BlockPos center, int radius, BlessedLandGrade grade, Random random) {
        int cx = center.getX();
        int cz = center.getZ();
        int inner = radius - 4;

        int moonOrchidCount = switch (grade) {
            case LOWER -> 8;
            case MIDDLE -> 20;
            case UPPER -> 40;
            case SUPREME -> 60;
        };

        for (int i = 0; i < moonOrchidCount; i++) {
            int ox = cx + random.nextInt(inner * 2 + 1) - inner;
            int oz = cz + random.nextInt(inner * 2 + 1) - inner;
            BlockPos pos = new BlockPos(ox, SURFACE_Y + 1, oz);
            if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(pos).isAir()) {
                level.setBlock(pos, ModBlocks.MOON_ORCHID.get().defaultBlockState(), 2);
            }
        }

        int treeCount = switch (grade) {
            case LOWER -> 5;
            case MIDDLE -> 15;
            case UPPER -> 30;
            case SUPREME -> 50;
        };

        for (int i = 0; i < treeCount; i++) {
            int tx = cx + random.nextInt(inner * 2 + 1) - inner;
            int tz = cz + random.nextInt(inner * 2 + 1) - inner;
            if (Math.abs(tx - cx) < 5 && Math.abs(tz - cz) < 5) continue;
            BlockPos base = new BlockPos(tx, SURFACE_Y + 1, tz);
            if (!level.getBlockState(base.below()).is(Blocks.GRASS_BLOCK)) continue;

            int height = 4 + random.nextInt(3);
            Block logBlock = random.nextBoolean() ? Blocks.OAK_LOG : Blocks.BIRCH_LOG;
            Block leafBlock = (logBlock == Blocks.OAK_LOG) ? Blocks.OAK_LEAVES : Blocks.BIRCH_LEAVES;

            for (int y = 0; y < height; y++) {
                level.setBlock(base.above(y), logBlock.defaultBlockState(), 2);
            }

            int canopyStart = height - 2;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = canopyStart; dy <= height + 1; dy++) {
                        if (Math.abs(dx) == 2 && Math.abs(dz) == 2 && dy > height - 1) continue;
                        if (dy == height + 1 && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) continue;
                        BlockPos leafPos = base.offset(dx, dy, dz);
                        if (level.getBlockState(leafPos).isAir()) {
                            level.setBlock(leafPos, leafBlock.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }

        int flowerCount = Math.min(radius / 2, 200);
        for (int i = 0; i < flowerCount; i++) {
            int fx = cx + random.nextInt(inner * 2 + 1) - inner;
            int fz = cz + random.nextInt(inner * 2 + 1) - inner;
            BlockPos pos = new BlockPos(fx, SURFACE_Y + 1, fz);
            if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(pos).isAir()) {
                Block flower = switch (random.nextInt(6)) {
                    case 0 -> Blocks.DANDELION;
                    case 1 -> Blocks.POPPY;
                    case 2 -> Blocks.CORNFLOWER;
                    case 3 -> Blocks.ALLIUM;
                    case 4 -> Blocks.SHORT_GRASS;
                    default -> Blocks.FERN;
                };
                level.setBlock(pos, flower.defaultBlockState(), 2);
            }
        }
    }

    private static void placeDaoFeatures(ServerLevel level, BlockPos center, int radius, Map<DaoPath, Integer> daoMarks, Random random) {
        if (daoMarks == null || daoMarks.isEmpty()) return;

        List<Map.Entry<DaoPath, Integer>> sorted = daoMarks.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<DaoPath, Integer>comparingByValue().reversed())
                .limit(DAO_FEATURE_TOP5)
                .toList();

        if (sorted.isEmpty()) return;

        int inner = radius - 4;
        int sectorDist = Math.max(15, inner / 2);

        double angleStep = 2 * Math.PI / Math.max(sorted.size(), 1);

        for (int idx = 0; idx < sorted.size(); idx++) {
            DaoPath path = sorted.get(idx).getKey();
            int marks = sorted.get(idx).getValue();

            double angle = angleStep * idx + 0.3;
            int fx = center.getX() + (int) (Math.cos(angle) * sectorDist);
            int fz = center.getZ() + (int) (Math.sin(angle) * sectorDist);

            int strength = Math.min(marks / 50, 5);

            placeDaoZone(level, new BlockPos(fx, SURFACE_Y, fz), path, strength, random);
        }
    }

    private static void placeDaoZone(ServerLevel level, BlockPos origin, DaoPath path, int strength, Random random) {
        switch (path) {
            case FIRE -> placeFireZone(level, origin, strength, random);
            case ICE -> placeIceZone(level, origin, strength, random);
            case WATER -> placeWaterZone(level, origin, strength, random);
            case EARTH -> placeEarthZone(level, origin, strength, random);
            case WOOD -> placeWoodZone(level, origin, strength, random);
            case LIGHTNING -> placeLightningZone(level, origin, strength, random);
            case METAL -> placeMetalZone(level, origin, strength, random);
            case POISON -> placePoisonZone(level, origin, strength, random);
            case SOUL -> placeSoulZone(level, origin, strength, random);
            case BLOOD -> placeBloodZone(level, origin, strength, random);
            case MOON -> placeMoonZone(level, origin, strength, random);
            case STAR -> placeStarZone(level, origin, strength, random);
            case DARK, SHADOW -> placeDarkZone(level, origin, strength, random);
            case LIGHT -> placeLightZone(level, origin, strength, random);
            case BONE -> placeBoneZone(level, origin, strength, random);
            case WIND, CLOUD -> placeWindZone(level, origin, strength, random);
            case FORMATION -> placeFormationZone(level, origin, strength, random);
            case REFINEMENT -> placeRefinementZone(level, origin, strength, random);
            case FOOD -> placeFoodZone(level, origin, strength, random);
            default -> placeDefaultZone(level, origin, strength, random);
        }
    }

    private static void placeFireZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;
                int d2 = dx * dx + dz * dz;

                level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.RED_SANDSTONE.defaultBlockState(), 2);

                if (d2 < (size * size) / 4) {
                    if (random.nextFloat() < 0.3f) {
                        level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                    }
                }

                if (random.nextFloat() < 0.1f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y + 1, bz), Blocks.FIRE.defaultBlockState(), 2);
                }
            }
        }

        int poolSize = 1 + strength / 2;
        for (int dx = -poolSize; dx <= poolSize; dx++) {
            for (int dz = -poolSize; dz <= poolSize; dz++) {
                if (dx * dx + dz * dz <= poolSize * poolSize) {
                    level.setBlock(new BlockPos(ox + dx, SURFACE_Y, oz + dz), Blocks.LAVA.defaultBlockState(), 2);
                }
            }
        }

        for (int i = 0; i < 2 + strength; i++) {
            int dx = random.nextInt(size * 2 + 1) - size;
            int dz = random.nextInt(size * 2 + 1) - size;
            if (dx * dx + dz * dz <= size * size) {
                BlockPos pos = new BlockPos(ox + dx, SURFACE_Y + 1, oz + dz);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void placeIceZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 5 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;
                int d2 = dx * dx + dz * dz;

                level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SNOW_BLOCK.defaultBlockState(), 2);

                if (d2 < (size * size) / 3) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.PACKED_ICE.defaultBlockState(), 2);
                    if (random.nextFloat() < 0.2f) {
                        level.setBlock(new BlockPos(bx, SURFACE_Y + 1, bz), Blocks.ICE.defaultBlockState(), 2);
                    }
                }

                if (d2 < (size * size) / 6 && random.nextFloat() < 0.15f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.BLUE_ICE.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.08f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y + 1, bz), Blocks.POWDER_SNOW.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.15f) {
                    BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                    if (level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.SNOW.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < strength; i++) {
            int px = ox + random.nextInt(size) - size / 2;
            int pz = oz + random.nextInt(size) - size / 2;
            int h = 2 + random.nextInt(3);
            for (int y = 0; y < h; y++) {
                level.setBlock(new BlockPos(px, SURFACE_Y + 1 + y, pz), Blocks.PACKED_ICE.defaultBlockState(), 2);
            }
        }
    }

    private static void placeWaterZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 6 + strength;
        int r2 = size * size;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > r2) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (d2 >= r2 * 3 / 4) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SAND.defaultBlockState(), 2);
                    if (random.nextFloat() < 0.3f) {
                        level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.CLAY.defaultBlockState(), 2);
                    }
                    continue;
                }

                int depth = (d2 < r2 / 4) ? 3 : (d2 < r2 / 2) ? 2 : 1;
                for (int y = SURFACE_Y; y > SURFACE_Y - depth; y--) {
                    level.setBlock(new BlockPos(bx, y, bz), Blocks.WATER.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.05f && d2 < r2 / 2) {
                    BlockPos seagrass = new BlockPos(bx, SURFACE_Y - depth + 1, bz);
                    level.setBlock(seagrass, Blocks.SEAGRASS.defaultBlockState(), 2);
                }
            }
        }

        if (random.nextFloat() < 0.5f) {
            level.setBlock(new BlockPos(ox, SURFACE_Y + 1, oz),
                    ModBlocks.SPIRIT_SPRING.get().defaultBlockState(), 2);
        }
    }

    private static void placeEarthZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 5 + strength;
        int peakHeight = 3 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                double dist = Math.sqrt(d2);
                double ratio = 1.0 - (dist / size);
                ratio = ratio * ratio;
                int hillHeight = (int) (ratio * peakHeight);

                for (int y = 1; y <= hillHeight; y++) {
                    if (y == hillHeight) {
                        level.setBlock(new BlockPos(bx, SURFACE_Y + y, bz), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    } else {
                        level.setBlock(new BlockPos(bx, SURFACE_Y + y, bz), Blocks.DIRT.defaultBlockState(), 2);
                    }
                }

                if (hillHeight > 2 && random.nextFloat() < 0.1f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y + hillHeight, bz), Blocks.STONE.defaultBlockState(), 2);
                }
            }
        }

        if (strength >= 3) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx * dx + dz * dz <= 4) {
                        level.setBlock(new BlockPos(ox + dx, SURFACE_Y + peakHeight, oz + dz),
                                Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void placeWoodZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 5 + strength;
        int treeCount = 5 + strength * 2;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                BlockPos pos = new BlockPos(ox + dx, SURFACE_Y + 1, oz + dz);
                if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(pos).isAir()) {
                    if (random.nextFloat() < 0.3f) {
                        level.setBlock(pos, Blocks.FERN.defaultBlockState(), 2);
                    } else if (random.nextFloat() < 0.1f) {
                        level.setBlock(pos, Blocks.MOSS_CARPET.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < treeCount; i++) {
            int tx = ox + random.nextInt(size * 2 + 1) - size;
            int tz = oz + random.nextInt(size * 2 + 1) - size;
            if ((tx - ox) * (tx - ox) + (tz - oz) * (tz - oz) > size * size) continue;

            BlockPos base = new BlockPos(tx, SURFACE_Y + 1, tz);
            if (!level.getBlockState(base.below()).is(Blocks.GRASS_BLOCK)) continue;

            boolean isSpruce = random.nextFloat() < 0.3f;
            boolean isDarkOak = !isSpruce && random.nextFloat() < 0.2f;
            Block log = isSpruce ? Blocks.SPRUCE_LOG : (isDarkOak ? Blocks.DARK_OAK_LOG : Blocks.OAK_LOG);
            Block leaf = isSpruce ? Blocks.SPRUCE_LEAVES : (isDarkOak ? Blocks.DARK_OAK_LEAVES : Blocks.OAK_LEAVES);

            int height = isSpruce ? 6 + random.nextInt(3) : 4 + random.nextInt(3);
            for (int y = 0; y < height; y++) {
                level.setBlock(base.above(y), log.defaultBlockState(), 2);
            }

            if (isSpruce) {
                for (int y = 2; y <= height; y++) {
                    int canopyR = (y < height - 1) ? 2 : 1;
                    if (y == height) canopyR = 0;
                    for (int dx = -canopyR; dx <= canopyR; dx++) {
                        for (int dz = -canopyR; dz <= canopyR; dz++) {
                            if (Math.abs(dx) == canopyR && Math.abs(dz) == canopyR) continue;
                            BlockPos lp = base.offset(dx, y, dz);
                            if (level.getBlockState(lp).isAir()) {
                                level.setBlock(lp, leaf.defaultBlockState(), 2);
                            }
                        }
                    }
                }
                level.setBlock(base.above(height), leaf.defaultBlockState(), 2);
            } else {
                int canopyStart = height - 2;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        for (int dy = canopyStart; dy <= height + 1; dy++) {
                            if (Math.abs(dx) == 2 && Math.abs(dz) == 2 && dy > height - 1) continue;
                            if (dy == height + 1 && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) continue;
                            BlockPos lp = base.offset(dx, dy, dz);
                            if (level.getBlockState(lp).isAir()) {
                                level.setBlock(lp, leaf.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

            if (random.nextFloat() < 0.3f) {
                int vineY = 2 + random.nextInt(height - 2);
                BlockPos vinePos = base.offset(random.nextBoolean() ? 1 : -1, vineY, 0);
                if (level.getBlockState(vinePos).isAir()) {
                    level.setBlock(vinePos, Blocks.VINE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void placeLightningZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.4f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.OXIDIZED_COPPER.defaultBlockState(), 2);
                } else {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.COPPER_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        for (int i = 0; i < 2 + strength; i++) {
            int rx = ox + random.nextInt(size * 2 + 1) - size;
            int rz = oz + random.nextInt(size * 2 + 1) - size;
            if ((rx - ox) * (rx - ox) + (rz - oz) * (rz - oz) > size * size) continue;
            BlockPos rodBase = new BlockPos(rx, SURFACE_Y + 1, rz);
            int rodHeight = 2 + random.nextInt(3);
            for (int y = 0; y < rodHeight; y++) {
                level.setBlock(rodBase.above(y), Blocks.COPPER_BLOCK.defaultBlockState(), 2);
            }
            level.setBlock(rodBase.above(rodHeight), Blocks.LIGHTNING_ROD.defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(ox, SURFACE_Y + 1, oz), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
    }

    private static void placeMetalZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                for (int y = 20; y <= 55; y++) {
                    BlockPos stonePos = new BlockPos(bx, y, bz);
                    if (!level.getBlockState(stonePos).is(Blocks.STONE)) continue;

                    if (random.nextFloat() < 0.06f) {
                        Block ore = switch (random.nextInt(4)) {
                            case 0 -> Blocks.IRON_ORE;
                            case 1 -> Blocks.GOLD_ORE;
                            case 2 -> Blocks.COPPER_ORE;
                            default -> ModBlocks.PRIMEVAL_STONE_ORE.get();
                        };
                        level.setBlock(stonePos, ore.defaultBlockState(), 2);
                    }
                }

                if (random.nextFloat() < 0.15f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.RAW_IRON_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(ox, SURFACE_Y + 1, oz), Blocks.IRON_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox, SURFACE_Y + 2, oz), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
    }

    private static void placePoisonZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 5 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.5f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.MUD.defaultBlockState(), 2);
                } else {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.08f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.WATER.defaultBlockState(), 2);
                }

                BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                if (level.getBlockState(above).isAir()) {
                    if (random.nextFloat() < 0.15f) {
                        level.setBlock(above, Blocks.LILY_PAD.defaultBlockState(), 2);
                    } else if (random.nextFloat() < 0.1f) {
                        level.setBlock(above, Blocks.VINE.defaultBlockState(), 2);
                    } else if (random.nextFloat() < 0.05f) {
                        level.setBlock(above, Blocks.RED_MUSHROOM.defaultBlockState(), 2);
                    } else if (random.nextFloat() < 0.05f) {
                        level.setBlock(above, Blocks.BROWN_MUSHROOM.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < strength; i++) {
            int mx = ox + random.nextInt(size) - size / 2;
            int mz = oz + random.nextInt(size) - size / 2;
            BlockPos base = new BlockPos(mx, SURFACE_Y + 1, mz);
            int h = 3 + random.nextInt(4);
            for (int y = 0; y < h; y++) {
                level.setBlock(base.above(y), Blocks.MANGROVE_LOG.defaultBlockState(), 2);
                if (y > 1 && random.nextFloat() < 0.5f) {
                    level.setBlock(base.offset(random.nextBoolean() ? 1 : -1, y, 0),
                            Blocks.MANGROVE_LEAVES.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void placeSoulZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SOUL_SAND.defaultBlockState(), 2);

                if (random.nextFloat() < 0.05f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SOUL_SOIL.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.08f) {
                    BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                    if (level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.SOUL_TORCH.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < 1 + strength / 2; i++) {
            int sx = ox + random.nextInt(size) - size / 2;
            int sz = oz + random.nextInt(size) - size / 2;
            level.setBlock(new BlockPos(sx, SURFACE_Y + 1, sz), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }
    }

    private static void placeBloodZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.6f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.RED_SANDSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.RED_CONCRETE.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.1f) {
                    BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                    if (level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.RED_CARPET.defaultBlockState(), 2);
                    }
                }

                if (random.nextFloat() < 0.03f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        for (int i = 0; i < strength; i++) {
            int px = ox + random.nextInt(size) - size / 2;
            int pz = oz + random.nextInt(size) - size / 2;
            level.setBlock(new BlockPos(px, SURFACE_Y + 1, pz), Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
            level.setBlock(new BlockPos(px, SURFACE_Y + 2, pz), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
        }
    }

    private static void placeMoonZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.4f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.END_STONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.CALCITE.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.1f) {
                    int h = 1 + random.nextInt(3);
                    for (int y = 1; y <= h; y++) {
                        level.setBlock(new BlockPos(bx, SURFACE_Y + y, bz),
                                Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                    }
                }

                if (random.nextFloat() < 0.05f) {
                    BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                    if (level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < 2 + strength; i++) {
            int mx = ox + random.nextInt(size) - size / 2;
            int mz = oz + random.nextInt(size) - size / 2;
            BlockPos pos = new BlockPos(mx, SURFACE_Y + 1, mz);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, ModBlocks.MOON_ORCHID.get().defaultBlockState(), 2);
            }
        }
    }

    private static void placeStarZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.3f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.END_STONE.defaultBlockState(), 2);
                } else if (random.nextFloat() < 0.3f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.PURPUR_BLOCK.defaultBlockState(), 2);
                } else {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.OBSIDIAN.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.08f) {
                    int h = 1 + random.nextInt(2);
                    for (int y = 1; y <= h; y++) {
                        level.setBlock(new BlockPos(bx, SURFACE_Y + y, bz),
                                Blocks.AMETHYST_BLOCK.defaultBlockState(), 2);
                    }
                    level.setBlock(new BlockPos(bx, SURFACE_Y + h + 1, bz),
                            Blocks.AMETHYST_CLUSTER.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.06f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y + 1, bz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(ox, SURFACE_Y + 1, oz), Blocks.BEACON.defaultBlockState(), 2);
    }

    private static void placeDarkZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.5f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.BLACKSTONE.defaultBlockState(), 2);
                } else {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.DEEPSLATE.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.05f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.03f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y + 1, bz), Blocks.SCULK.defaultBlockState(), 2);
                }
            }
        }

        for (int i = 0; i < strength; i++) {
            int sx = ox + random.nextInt(size) - size / 2;
            int sz = oz + random.nextInt(size) - size / 2;
            level.setBlock(new BlockPos(sx, SURFACE_Y + 1, sz), Blocks.SCULK_SENSOR.defaultBlockState(), 2);
        }
    }

    private static void placeLightZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.4f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 2);
                } else {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.08f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y + 1, bz), Blocks.GLOWSTONE.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.05f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y + 1, bz), Blocks.SEA_LANTERN.defaultBlockState(), 2);
                }
            }
        }

        level.setBlock(new BlockPos(ox, SURFACE_Y + 1, oz), Blocks.BEACON.defaultBlockState(), 2);
    }

    private static void placeBoneZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.BONE_BLOCK.defaultBlockState(), 2);

                if (random.nextFloat() < 0.08f) {
                    int h = 1 + random.nextInt(3);
                    for (int y = 1; y <= h; y++) {
                        level.setBlock(new BlockPos(bx, SURFACE_Y + y, bz), Blocks.BONE_BLOCK.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < 1 + strength / 2; i++) {
            int sx = ox + random.nextInt(size) - size / 2;
            int sz = oz + random.nextInt(size) - size / 2;
            level.setBlock(new BlockPos(sx, SURFACE_Y + 1, sz), Blocks.SKELETON_SKULL.defaultBlockState(), 2);
        }
    }

    private static void placeWindZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 5 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                if (random.nextFloat() < 0.3f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SAND.defaultBlockState(), 2);
                } else if (random.nextFloat() < 0.2f) {
                    level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SANDSTONE.defaultBlockState(), 2);
                }

                if (random.nextFloat() < 0.2f) {
                    BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                    if (level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.SHORT_GRASS.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int i = 0; i < strength; i++) {
            int wx = ox + random.nextInt(size) - size / 2;
            int wz = oz + random.nextInt(size) - size / 2;
            level.setBlock(new BlockPos(wx, SURFACE_Y + 1, wz), Blocks.WHITE_BANNER.defaultBlockState(), 2);
        }
    }

    private static void placeFormationZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 3 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;
                level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
            }
        }

        double angleStep = Math.PI * 2 / 8;
        int ringR = size - 1;
        for (int i = 0; i < 8; i++) {
            int px = ox + (int) (Math.cos(angleStep * i) * ringR);
            int pz = oz + (int) (Math.sin(angleStep * i) * ringR);
            level.setBlock(new BlockPos(px, SURFACE_Y + 1, pz),
                    ModBlocks.FORMATION_STONE.get().defaultBlockState(), 2);
        }

        level.setBlock(new BlockPos(ox, SURFACE_Y + 1, oz),
                ModBlocks.FORMATION_STONE.get().defaultBlockState(), 2);
    }

    private static void placeRefinementZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 3 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;
                level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
        }

        level.setBlock(new BlockPos(ox, SURFACE_Y + 1, oz), Blocks.BLAST_FURNACE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox + 1, SURFACE_Y + 1, oz), Blocks.SMOKER.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox - 1, SURFACE_Y + 1, oz), Blocks.FURNACE.defaultBlockState(), 2);

        for (int i = 0; i < strength; i++) {
            int fx = ox + random.nextInt(size) - size / 2;
            int fz = oz + random.nextInt(size) - size / 2;
            BlockPos pos = new BlockPos(fx, SURFACE_Y + 1, fz);
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 2);
            }
        }
    }

    private static void placeFoodZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 4 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                level.setBlock(new BlockPos(bx, SURFACE_Y, bz), Blocks.FARMLAND.defaultBlockState(), 2);

                if (random.nextFloat() < 0.3f) {
                    BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                    Block crop = switch (random.nextInt(4)) {
                        case 0 -> Blocks.WHEAT;
                        case 1 -> Blocks.CARROTS;
                        case 2 -> Blocks.POTATOES;
                        default -> Blocks.BEETROOTS;
                    };
                    level.setBlock(above, crop.defaultBlockState(), 2);
                }
            }
        }

        int waterSpots = 1 + strength / 2;
        for (int i = 0; i < waterSpots; i++) {
            int wx = ox + random.nextInt(size) - size / 2;
            int wz = oz + random.nextInt(size) - size / 2;
            level.setBlock(new BlockPos(wx, SURFACE_Y, wz), Blocks.WATER.defaultBlockState(), 2);
        }
    }

    private static void placeDefaultZone(ServerLevel level, BlockPos origin, int strength, Random random) {
        int ox = origin.getX();
        int oz = origin.getZ();
        int size = 3 + strength;

        for (int dx = -size; dx <= size; dx++) {
            for (int dz = -size; dz <= size; dz++) {
                if (dx * dx + dz * dz > size * size) continue;
                int bx = ox + dx;
                int bz = oz + dz;

                BlockPos above = new BlockPos(bx, SURFACE_Y + 1, bz);
                if (level.getBlockState(above.below()).is(Blocks.GRASS_BLOCK) && level.getBlockState(above).isAir()) {
                    if (random.nextFloat() < 0.25f) {
                        Block flower = switch (random.nextInt(8)) {
                            case 0 -> Blocks.DANDELION;
                            case 1 -> Blocks.POPPY;
                            case 2 -> Blocks.BLUE_ORCHID;
                            case 3 -> Blocks.ALLIUM;
                            case 4 -> Blocks.AZURE_BLUET;
                            case 5 -> Blocks.OXEYE_DAISY;
                            case 6 -> Blocks.CORNFLOWER;
                            default -> Blocks.LILY_OF_THE_VALLEY;
                        };
                        level.setBlock(above, flower.defaultBlockState(), 2);
                    } else if (random.nextFloat() < 0.3f) {
                        level.setBlock(above, Blocks.SHORT_GRASS.defaultBlockState(), 2);
                    }
                }
            }
        }
    }
}
