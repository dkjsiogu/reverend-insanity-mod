package com.reverendinsanity.item;

import com.reverendinsanity.core.combat.buff.GuBuff;
import com.reverendinsanity.core.combat.buff.GuBuffManager;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.Aptitude;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.cultivation.Rank;
import com.reverendinsanity.core.cultivation.SubRank;
import com.reverendinsanity.core.cultivation.TribulationManager;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

// 突破石：消耗使用，尝试大境界突破，有失败风险；三转以上触发天劫
public class BreakthroughStoneItem extends Item {

    public BreakthroughStoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();

            if (!aperture.isOpened()) {
                player.displayClientMessage(
                        Component.literal("尚未开窍，无法使用突破石").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }

            if (aperture.getSubRank() != SubRank.PEAK) {
                player.displayClientMessage(
                        Component.literal("未达巅峰境界，无法进行大境界突破").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }

            Rank nextRank = aperture.getRank().next();
            if (nextRank == null) {
                player.displayClientMessage(
                        Component.literal("已达最高境界").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }

            if (!aperture.getAptitude().canAdvanceTo(nextRank)) {
                player.displayClientMessage(
                        Component.literal("资质不足，无法突破至" + nextRank.getDisplayName())
                                .withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }

            ServerPlayer sp = (ServerPlayer) player;

            if (TribulationManager.isInTribulation(sp)) {
                player.displayClientMessage(
                        Component.literal("天劫进行中，无法再次使用突破石").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }

            if (TribulationManager.requiresTribulation(nextRank)) {
                stack.shrink(1);
                TribulationManager.startTribulation(sp, nextRank);
                return InteractionResultHolder.success(stack);
            }

            float successRate = calculateSuccessRate(aperture.getAptitude());
            boolean success = player.getRandom().nextFloat() < successRate;

            stack.shrink(1);

            if (success && aperture.tryAdvanceRank()) {
                String msg = "大境界突破成功！晋升为 "
                        + aperture.getRank().getDisplayName() + "·"
                        + aperture.getSubRank().getDisplayName() + " 蛊师！";
                player.displayClientMessage(
                        Component.literal(msg).withStyle(ChatFormatting.GOLD), false);

                ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        200, 1.0, 2.0, 1.0, 0.3);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        100, 0.8, 1.5, 0.8, 0.1);

                level.playSound(null, player.blockPosition(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.0f);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);

                int rankLevel = aperture.getRank().getLevel();
                if (rankLevel >= 2) AdvancementHelper.grant(sp, "rank2");
                if (rankLevel >= 3) AdvancementHelper.grant(sp, "rank3");
            } else {
                player.displayClientMessage(
                        Component.literal("突破失败！空窍震荡...").withStyle(ChatFormatting.DARK_RED), false);
                player.displayClientMessage(
                        Component.literal("经脉受创，短时间内实力大减").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);

                ResourceLocation modId = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "breakthrough_failure");
                GuMasterData gData = sp.getData(ModAttachments.GU_MASTER_DATA.get());
                gData.getBuffManager().applyBuff(sp, new GuBuff(modId, 1200) {
                    @Override
                    protected void onApply(ServerPlayer p) {
                        var attr = p.getAttribute(Attributes.ATTACK_DAMAGE);
                        if (attr != null) attr.addTransientModifier(new AttributeModifier(modId, -4.0, AttributeModifier.Operation.ADD_VALUE));
                    }
                    @Override
                    protected void onRemove(ServerPlayer p) {
                        var attr = p.getAttribute(Attributes.ATTACK_DAMAGE);
                        if (attr != null) attr.removeModifier(modId);
                    }
                });

                ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        30, 0.5, 0.5, 0.5, 0.05);

                level.playSound(null, player.blockPosition(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ANVIL_DESTROY, SoundSource.PLAYERS, 0.6f, 0.8f);
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private float calculateSuccessRate(Aptitude aptitude) {
        float base = 0.70f;
        return switch (aptitude) {
            case EXTREME -> base + 0.25f;
            case A -> base + 0.15f;
            case B -> base + 0.05f;
            case C -> base;
            case D -> base - 0.10f;
            default -> base;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("右键使用尝试大境界突破").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("需要巅峰小境界").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("突破有失败风险").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal("三转以上突破将触发天劫").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
