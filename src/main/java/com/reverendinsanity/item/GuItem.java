package com.reverendinsanity.item;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import com.reverendinsanity.util.AdvancementHelper;
import java.util.List;

// 蛊虫物品基类，右键炼化存入空窍
public class GuItem extends Item {

    private final ResourceLocation guTypeId;

    public GuItem(ResourceLocation guTypeId, Properties properties) {
        super(properties);
        this.guTypeId = guTypeId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();

            if (!aperture.isOpened()) {
                player.displayClientMessage(Component.literal("空窍未开，无法炼化蛊虫"), true);
                return InteractionResultHolder.fail(stack);
            }

            GuType type = GuRegistry.get(guTypeId);
            if (type == null) {
                return InteractionResultHolder.fail(stack);
            }

            if (!type.canBeUsedBy(aperture.getRank())) {
                player.displayClientMessage(Component.literal("境界不足，无法炼化此蛊"), true);
                return InteractionResultHolder.fail(stack);
            }

            if (!aperture.hasGuCapacity()) {
                player.displayClientMessage(Component.literal("窍穴容量已满（" + aperture.getMaxGuCapacity() + "/" + aperture.getMaxGuCapacity() + "），需提升境界扩容"), true);
                return InteractionResultHolder.fail(stack);
            }

            float refineCost = type.essenceCost() * 2;
            if (!aperture.consumeEssence(refineCost)) {
                player.displayClientMessage(Component.literal("真元不足，无法炼化"), true);
                return InteractionResultHolder.fail(stack);
            }

            GuInstance instance = new GuInstance(guTypeId);
            instance.refine(refineCost + 1);
            aperture.addGu(instance);
            data.discoverGu(guTypeId);

            stack.shrink(1);
            player.displayClientMessage(Component.literal("成功炼化 " + type.displayName()), true);

            if (player instanceof ServerPlayer sp) {
                AdvancementHelper.grant(sp, "first_gu");
                AdvancementHelper.grant(sp, "first_codex_discovery");
                int discoveredCount = data.getDiscoveredGu().size();
                if (discoveredCount >= 50) AdvancementHelper.grant(sp, "codex_50");
                if (discoveredCount >= 100) AdvancementHelper.grant(sp, "codex_100");
                if (aperture.getStoredGu().size() >= 10) {
                    AdvancementHelper.grant(sp, "collect_10_gu");
                }
            }

            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        GuType type = GuRegistry.get(guTypeId);
        if (type != null) {
            ChatFormatting rankColor = getRankColor(type.rank());
            tooltipComponents.add(Component.literal(type.rank() + "转 " + type.path().getDisplayName()).withStyle(rankColor));
            tooltipComponents.add(Component.literal(type.category().getDisplayName() + "蛊").withStyle(ChatFormatting.DARK_PURPLE));
            if (type.essenceCost() > 0) {
                tooltipComponents.add(Component.literal("催动: " + (int) type.essenceCost() + " 真元").withStyle(ChatFormatting.BLUE));
                tooltipComponents.add(Component.literal("炼化: " + (int) (type.essenceCost() * 2) + " 真元").withStyle(ChatFormatting.DARK_AQUA));
            }
            if (type.feedItem() != null && !type.feedItem().isEmpty()) {
                String feedName = type.feedItem().contains(":") ? type.feedItem().split(":")[1].replace("_", " ") : type.feedItem();
                tooltipComponents.add(Component.literal("喂养: " + feedName).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    private static ChatFormatting getRankColor(int rank) {
        return switch (rank) {
            case 1 -> ChatFormatting.WHITE;
            case 2 -> ChatFormatting.GREEN;
            case 3 -> ChatFormatting.AQUA;
            case 4 -> ChatFormatting.LIGHT_PURPLE;
            case 5 -> ChatFormatting.GOLD;
            case 6 -> ChatFormatting.RED;
            default -> ChatFormatting.DARK_RED;
        };
    }

    public ResourceLocation getGuTypeId() {
        return guTypeId;
    }
}
