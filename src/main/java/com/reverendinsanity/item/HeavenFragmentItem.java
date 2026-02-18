package com.reverendinsanity.item;

import com.reverendinsanity.core.heaven.HeavenType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

// 九天碎片：太古九天的残骸碎片，极其珍贵的修仙资源
public class HeavenFragmentItem extends Item {

    private final HeavenType heavenType;

    public HeavenFragmentItem(HeavenType heavenType) {
        super(new Properties().stacksTo(16).rarity(Rarity.EPIC));
        this.heavenType = heavenType;
    }

    public HeavenType getHeavenType() {
        return heavenType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(heavenType.getDisplayName() + "碎片").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("太古九天之" + heavenType.getDisplayName() + "的残片").withStyle(ChatFormatting.GRAY));
        String desc = switch (heavenType) {
            case WHITE -> "净化之力，可涤除蛊虫杂质";
            case RED -> "毁灭之炎，蕴含破坏性真元";
            case ORANGE -> "星萤之光，照亮前路";
            case YELLOW -> "宝黄天的商道气运";
            case GREEN -> "勃勃生机，万物生长之力";
            case CYAN -> "竹海灵韵，坚韧不拔之意";
            case BLUE -> "星辰之海，蕴含星道真意";
            case PURPLE -> "紫气东来，神秘莫测之力";
            case BLACK -> "虚无深渊，吞噬一切之力";
        };
        tooltip.add(Component.literal(desc).withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
