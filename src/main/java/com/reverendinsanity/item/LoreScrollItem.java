package com.reverendinsanity.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

// 蛊师手记：记录蛊师修炼心得的卷轴
public class LoreScrollItem extends Item {

    private static final String[] LORE_TEXTS = {
        "人是万物之灵，蛊是天地真精。",
        "所谓修炼，不过是人与天争命。",
        "真正的强者，从来不会抱怨命运的不公。",
        "弱肉强食，适者生存，这便是蛊师世界的法则。",
        "甲等资质又如何？没有机缘和努力，一切都是空谈。",
        "空窍之中，元海无边，修为无止境。",
        "每一只蛊虫，都是天地间的造化奇物。",
        "春秋蝉逆转时光，可世间哪有真正的回头路？",
        "福地之中别有洞天，仙家气象万千。",
        "大道三千，条条通天，不过殊途同归。",
        "蛊虫不是工具，它们也有自己的灵性。",
        "一转铜元翠绿，二转铁元绯红，三转银元白银，四转金元灿金。",
        "元石乃天地精华凝结，修炼之根本。",
        "蛊师之道，在于炼蛊、养蛊、用蛊三位一体。",
        "杀招之威，在于蛊虫配合、真元调控、时机把握。",
        "十万大山，蛊师世界的真正舞台。"
    };

    public LoreScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            String lore = LORE_TEXTS[level.getRandom().nextInt(LORE_TEXTS.length)];
            player.displayClientMessage(
                Component.literal("【蛊师手记】").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(lore).withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW)),
                false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("右键阅读蛊师先辈的修炼感悟").withStyle(ChatFormatting.GRAY));
    }
}
