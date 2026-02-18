package com.reverendinsanity.block;

import com.reverendinsanity.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 月桂花作物，在月光下生长
public class MoonOrchidBlock extends CropBlock {

    public static final int MAX_AGE = 5;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(4, 0, 4, 12, 3, 12),
        Block.box(3, 0, 3, 13, 5, 13),
        Block.box(2, 0, 2, 14, 8, 14),
        Block.box(2, 0, 2, 14, 10, 14),
        Block.box(1, 0, 1, 15, 12, 15),
        Block.box(1, 0, 1, 15, 14, 15)
    };

    public MoonOrchidBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.MOON_ORCHID_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[getAge(state)];
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isNight() && level.canSeeSky(pos.above())) {
            int age = getAge(state);
            if (age < getMaxAge()) {
                float growthChance = 0.3f;
                if (level.getMoonBrightness() > 0.5f) {
                    growthChance = 0.6f;
                }
                if (random.nextFloat() < growthChance) {
                    level.setBlock(pos, getStateForAge(age + 1), 2);
                }
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return getAge(state) < getMaxAge();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }
}
