package com.reverendinsanity.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

// 灵泉方块，天地自然形成的灵气汇聚之地，附近蛊师真元恢复加速
public class SpiritSpringBlock extends Block {

    public static final MapCodec<SpiritSpringBlock> CODEC = simpleCodec(SpiritSpringBlock::new);

    public SpiritSpringBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < 3; i++) {
            double ox = (random.nextDouble() - 0.5) * 0.6;
            double oy = random.nextDouble() * 0.8;
            double oz = (random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.ENCHANTED_HIT,
                    x + ox, y + oy, z + oz,
                    0, 0.05, 0);
        }

        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WITCH,
                    x + (random.nextDouble() - 0.5) * 0.8,
                    y + random.nextDouble() * 0.5,
                    z + (random.nextDouble() - 0.5) * 0.8,
                    0, 0.02, 0);
        }
    }
}
