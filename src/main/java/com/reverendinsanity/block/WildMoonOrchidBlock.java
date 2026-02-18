package com.reverendinsanity.block;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

// 野月兰花丛，洞穴中自然生成的发光植物，剪刀可完整采集
public class WildMoonOrchidBlock extends BushBlock {

    public static final MapCodec<WildMoonOrchidBlock> CODEC = simpleCodec(WildMoonOrchidBlock::new);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    public WildMoonOrchidBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.MOSS_REPLACEABLE) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        if (random.nextInt(3) == 0) {
            double ox = (random.nextDouble() - 0.5) * 0.6;
            double oy = random.nextDouble() * 0.6;
            double oz = (random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.ENCHANT,
                    x + ox, y + oy, z + oz,
                    0, 0.05, 0);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(ModItems.MOON_ORCHID_PETAL.get(), 1 + params.getLevel().getRandom().nextInt(2)));
    }
}
