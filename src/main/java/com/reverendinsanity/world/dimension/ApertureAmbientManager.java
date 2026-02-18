package com.reverendinsanity.world.dimension;

import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

// 仙窍维度环境氛围：根据道路主题生成粒子效果
public class ApertureAmbientManager {

    private static final int AMBIENT_INTERVAL = 40;
    private static final int PARTICLE_RANGE_MIN = 8;
    private static final int PARTICLE_RANGE_MAX = 16;
    private static final int PARTICLES_PER_TICK = 6;

    private static int tickCounter = 0;

    public static void tickAmbient(ServerPlayer player) {
        tickCounter++;
        if (tickCounter % AMBIENT_INTERVAL != 0) return;

        ServerLevel level = (ServerLevel) player.level();
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        ImmortalAperture ap = data.getImmortalAperture();
        if (!ap.isFormed()) return;

        ApertureChunkGenerator.IslandData island = ApertureChunkGenerator.findIsland(
                (int) player.getX(), (int) player.getZ());

        DaoPath path = island != null ? island.primaryPath : null;
        ParticleOptions particle = getPathParticle(path);
        RandomSource rand = level.getRandom();

        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = PARTICLE_RANGE_MIN + rand.nextDouble() * (PARTICLE_RANGE_MAX - PARTICLE_RANGE_MIN);
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;
            double y = player.getY() + (rand.nextDouble() - 0.3) * 8;

            level.sendParticles(particle, x, y, z, 1, 0.2, 0.1, 0.2, 0.01);
        }

        if (path != null) {
            ParticleOptions secondary = getSecondaryParticle(path);
            if (secondary != null && rand.nextFloat() < 0.4f) {
                double x = player.getX() + (rand.nextDouble() - 0.5) * 20;
                double z = player.getZ() + (rand.nextDouble() - 0.5) * 20;
                double y = player.getY() + rand.nextDouble() * 5;
                level.sendParticles(secondary, x, y, z, 2, 0.5, 0.3, 0.5, 0.02);
            }
        }
    }

    private static ParticleOptions getPathParticle(DaoPath path) {
        if (path == null) return ParticleTypes.CHERRY_LEAVES;
        return switch (path) {
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case WATER -> ParticleTypes.DRIPPING_WATER;
            case SOUL -> ParticleTypes.SCULK_SOUL;
            case BLOOD -> ParticleTypes.CRIMSON_SPORE;
            case MOON -> ParticleTypes.END_ROD;
            case LIGHTNING -> ParticleTypes.ELECTRIC_SPARK;
            case POISON -> ParticleTypes.SPORE_BLOSSOM_AIR;
            case EARTH -> ParticleTypes.WAX_OFF;
            case WOOD -> ParticleTypes.HAPPY_VILLAGER;
            case METAL -> ParticleTypes.CRIT;
            case WIND -> ParticleTypes.CLOUD;
            case LIGHT -> ParticleTypes.END_ROD;
            case DARK, SHADOW -> ParticleTypes.SMOKE;
            case STAR -> ParticleTypes.ENCHANT;
            case DREAM -> ParticleTypes.ENCHANTED_HIT;
            case BONE -> ParticleTypes.ASH;
            case SOUND -> ParticleTypes.NOTE;
            case CLOUD -> ParticleTypes.CLOUD;
            default -> ParticleTypes.CHERRY_LEAVES;
        };
    }

    private static ParticleOptions getSecondaryParticle(DaoPath path) {
        return switch (path) {
            case FIRE -> ParticleTypes.LAVA;
            case ICE -> ParticleTypes.WHITE_ASH;
            case WATER -> ParticleTypes.SPLASH;
            case SOUL -> ParticleTypes.SOUL_FIRE_FLAME;
            case BLOOD -> ParticleTypes.DAMAGE_INDICATOR;
            case LIGHTNING -> ParticleTypes.FLASH;
            case MOON -> ParticleTypes.END_ROD;
            case POISON -> ParticleTypes.ITEM_SLIME;
            default -> null;
        };
    }
}
