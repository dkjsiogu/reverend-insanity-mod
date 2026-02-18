package com.reverendinsanity.block;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.block.entity.RefinementCauldronBlockEntity;
import com.reverendinsanity.core.gu.RefinementRecipe;

import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 炼蛊炉方块，用于将低阶蛊虫升炼为高阶蛊虫
public class RefinementCauldronBlock extends BaseEntityBlock {

    public static final MapCodec<RefinementCauldronBlock> CODEC = simpleCodec(RefinementCauldronBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 12, 15);

    public RefinementCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefinementCauldronBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RefinementCauldronBlockEntity cauldron)) {
            return ItemInteractionResult.FAIL;
        }
        if (cauldron.addItem(stack)) {
            stack.shrink(1);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
            return ItemInteractionResult.SUCCESS;
        }
        player.displayClientMessage(Component.literal("\u70bc\u86ca\u7089\u5df2\u6ee1"), true);
        return ItemInteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RefinementCauldronBlockEntity cauldron)) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            List<ItemStack> stored = cauldron.getItems();
            for (ItemStack item : stored) {
                popResource(level, pos.above(), item.copy());
            }
            cauldron.clearItems();
            return InteractionResult.SUCCESS;
        }

        List<ItemStack> stored = cauldron.getItems();
        if (stored.isEmpty()) {
            player.displayClientMessage(Component.literal("\u70bc\u86ca\u7089\u4e2d\u7a7a\u65e0\u4e00\u7269"), true);
            return InteractionResult.CONSUME;
        }

        RefinementRecipe recipe = RefinementRecipe.findMatch(stored);
        if (recipe != null) {
            cauldron.removeMatchingItems(recipe);
            ItemStack output = recipe.getOutputStack();
            popResource(level, pos.above(), output);
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, 1.2f);
            for (int i = 0; i < 20; i++) {
                level.addParticle(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5 + level.random.nextGaussian() * 0.3,
                    pos.getY() + 1.0 + level.random.nextDouble() * 0.5,
                    pos.getZ() + 0.5 + level.random.nextGaussian() * 0.3,
                    0, 0.1, 0);
            }
            player.displayClientMessage(Component.literal("\u70bc\u86ca\u6210\u529f\uff01\u83b7\u5f97 " + recipe.getDisplayName()), true);
            if (player instanceof ServerPlayer sp) {
                AdvancementHelper.grant(sp, "refinement");
            }
            return InteractionResult.SUCCESS;
        }

        StringBuilder sb = new StringBuilder("\u70bc\u86ca\u7089\u4e2d: ");
        for (int i = 0; i < stored.size(); i++) {
            if (i > 0) sb.append(", ");
            ItemStack s = stored.get(i);
            sb.append(s.getHoverName().getString());
            if (s.getCount() > 1) {
                sb.append(" x").append(s.getCount());
            }
        }
        player.displayClientMessage(Component.literal(sb.toString()), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RefinementCauldronBlockEntity cauldron) {
                for (ItemStack item : cauldron.getItems()) {
                    popResource(level, pos, item.copy());
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
