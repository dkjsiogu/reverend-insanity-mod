package com.reverendinsanity.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// 彩晶钟乳石，洞穴中自然形成的发光装饰方块
public class RainbowStalactiteBlock extends Block {

    public static final MapCodec<RainbowStalactiteBlock> CODEC = simpleCodec(RainbowStalactiteBlock::new);

    public RainbowStalactiteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.ENCHANTED_HIT,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.02,
                    0.02,
                    (random.nextDouble() - 0.5) * 0.02);
        }
    }
}
