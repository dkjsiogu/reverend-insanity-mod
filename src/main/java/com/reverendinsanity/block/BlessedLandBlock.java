package com.reverendinsanity.block;

import com.mojang.serialization.MapCodec;
import com.reverendinsanity.block.entity.BlessedLandBlockEntity;
import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.network.ServerPayloadHandler;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModBlockEntities;
import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

// 福地之种，四转以上蛊师的个人修炼空间核心方块
public class BlessedLandBlock extends BaseEntityBlock {

    public static final MapCodec<BlessedLandBlock> CODEC = simpleCodec(BlessedLandBlock::new);

    public BlessedLandBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlessedLandBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlessedLandBlockEntity blessed)) return InteractionResult.PASS;

        if (blessed.getOwnerUUID() == null || !blessed.getOwnerUUID().equals(sp.getUUID())) {
            sp.displayClientMessage(Component.literal("这不是你的福地").withStyle(ChatFormatting.RED), true);
            return InteractionResult.SUCCESS;
        }

        if (!blessed.isActive()) {
            sp.displayClientMessage(Component.literal("此福地已失效").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.SUCCESS;
        }

        GuMasterData data = sp.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        ImmortalAperture immortalAp = data.getImmortalAperture();

        if (aperture.getRank().isImmortal()) {
            if (!immortalAp.isFormed()) {
                immortalAp.form(aperture, data);
                sp.displayClientMessage(
                    Component.literal("仙窍开辟成功！" + immortalAp.getGrade().getDisplayName() + "！")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                AdvancementHelper.grant(sp, "form_immortal_aperture");
            }
            ServerPayloadHandler.syncImmortalApertureToClient(sp, data);
        } else {
            sp.displayClientMessage(
                Component.literal("[ 福地状态 ]").withStyle(ChatFormatting.GOLD), false);
            sp.displayClientMessage(
                Component.literal("真元回复: 5倍加速").withStyle(ChatFormatting.GREEN), false);
            sp.displayClientMessage(
                Component.literal("范围: " + BlessedLandBlockEntity.RADIUS + "格").withStyle(ChatFormatting.AQUA), false);
            sp.displayClientMessage(
                Component.literal("效果: 周围敌对生物缓慢衰弱").withStyle(ChatFormatting.YELLOW), false);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || !(placer instanceof ServerPlayer player)) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        if (!aperture.isOpened() || aperture.getRank().getLevel() < 4) {
            player.displayClientMessage(Component.literal("境界不足，需四转以上方可开辟福地"), true);
            level.destroyBlock(pos, true);
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlessedLandBlockEntity blessed) {
            blessed.setOwner(player.getUUID());
            blessed.deactivateOtherBlessedLands((ServerLevel) level);
        }

        AdvancementHelper.grant(player, "create_blessed_land");
        player.displayClientMessage(Component.literal("福地开辟成功！灵气开始汇聚..."), false);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.BLESSED_LAND_CORE.get(), BlessedLandBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        for (int i = 0; i < 2; i++) {
            double ox = (random.nextDouble() - 0.5) * 0.8;
            double oy = random.nextDouble() * 0.6;
            double oz = (random.nextDouble() - 0.5) * 0.8;
            level.addParticle(ParticleTypes.END_ROD,
                    x + ox, y + oy, z + oz,
                    0, 0.03, 0);
        }

        if (random.nextInt(4) == 0) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 1.0 + random.nextDouble() * 2.0;
            level.addParticle(ParticleTypes.ENCHANTED_HIT,
                    x + Math.cos(angle) * radius,
                    y + random.nextDouble() * 0.5,
                    z + Math.sin(angle) * radius,
                    0, 0.02, 0);
        }
    }
}
