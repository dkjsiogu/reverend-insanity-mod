package com.reverendinsanity.item;

import com.reverendinsanity.network.OpenCodexPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;

// 蛊虫图鉴：记录已发现的蛊虫百科
public class GuCodexItem extends Item {

    public GuCodexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            PacketDistributor.sendToServer(new OpenCodexPayload());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("右键打开蛊虫图鉴").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("记录所有炼化过的蛊虫").withStyle(ChatFormatting.GRAY));
    }
}
