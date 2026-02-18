package com.reverendinsanity.item;

import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.economy.GamblingStoneManager;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

// 赌石物品：右键开石，随机获得蛊虫或空石
public class GamblingStoneItem extends Item {

    private final Grade grade;

    public GamblingStoneItem(Grade grade, Properties properties) {
        super(properties);
        this.grade = grade;
    }

    public enum Grade {
        LOW("低档蛊石", 0.75f, 0.18f, 0.07f, 0.00f),
        MEDIUM("中档蛊石", 0.65f, 0.20f, 0.13f, 0.02f),
        HIGH("高档蛊石", 0.55f, 0.20f, 0.20f, 0.05f);

        private final String displayName;
        private final float emptyChance;
        private final float deadChance;
        private final float aliveChance;
        private final float rareChance;

        Grade(String displayName, float emptyChance, float deadChance, float aliveChance, float rareChance) {
            this.displayName = displayName;
            this.emptyChance = emptyChance;
            this.deadChance = deadChance;
            this.aliveChance = aliveChance;
            this.rareChance = rareChance;
        }

        public String getDisplayName() { return displayName; }
        public float getEmptyChance() { return emptyChance; }
        public float getDeadChance() { return deadChance; }
        public float getAliveChance() { return aliveChance; }
        public float getRareChance() { return rareChance; }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.pass(stack);
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);

        GuMasterData data = sp.getData(ModAttachments.GU_MASTER_DATA.get());
        float fortuneBonus = Math.max(0, (data.getLuck() - 1.0f)) * 0.5f;

        float roll = sp.getRandom().nextFloat();

        float emptyThreshold = grade.emptyChance - fortuneBonus;
        float deadThreshold = emptyThreshold + grade.deadChance;
        float aliveThreshold = deadThreshold + grade.aliveChance + fortuneBonus * 0.5f;

        if (roll < emptyThreshold) {
            sp.displayClientMessage(Component.literal("\u00a77打开蛊石...一块普通石头。"), false);
        } else if (roll < deadThreshold) {
            int xp = 10 + sp.getRandom().nextInt(21);
            sp.giveExperiencePoints(xp);
            sp.displayClientMessage(Component.literal("\u00a7e打开蛊石...发现一只死蛊虫的残骸。"), false);
        } else if (roll < aliveThreshold) {
            int minRank = 1;
            int maxRank = grade == Grade.LOW ? 1 : 2;
            ItemStack result = GamblingStoneManager.rollGuItem(sp, minRank, maxRank);
            if (result != null && !result.isEmpty()) {
                if (!sp.getInventory().add(result)) {
                    sp.drop(result, false);
                }
                sp.displayClientMessage(Component.literal("\u00a7a打开蛊石...发现了一只" + result.getHoverName().getString() + "！"), false);
            } else {
                sp.displayClientMessage(Component.literal("\u00a77打开蛊石...一块普通石头。"), false);
            }
        } else {
            int minRank = grade == Grade.HIGH ? 3 : 2;
            int maxRank = 3;
            ItemStack result = GamblingStoneManager.rollGuItem(sp, minRank, maxRank);
            if (result != null && !result.isEmpty()) {
                if (!sp.getInventory().add(result)) {
                    sp.drop(result, false);
                }
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);
                sp.displayClientMessage(Component.literal("\u00a76\u00a7l打开蛊石...竟然是" + result.getHoverName().getString() + "！大赚！"), false);
            } else {
                int xp = 10 + sp.getRandom().nextInt(21);
                sp.giveExperiencePoints(xp);
                sp.displayClientMessage(Component.literal("\u00a7e打开蛊石...发现一只死蛊虫的残骸。"), false);
            }
        }

        stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.literal(grade.getDisplayName()).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.literal("右键开石，看看运气如何").withStyle(ChatFormatting.GRAY));
        switch (grade) {
            case LOW -> tooltipComponents.add(Component.literal("可能出1转蛊虫").withStyle(ChatFormatting.WHITE));
            case MEDIUM -> {
                tooltipComponents.add(Component.literal("可能出1-2转蛊虫").withStyle(ChatFormatting.GREEN));
                tooltipComponents.add(Component.literal("小概率珍稀2-3转").withStyle(ChatFormatting.AQUA));
            }
            case HIGH -> {
                tooltipComponents.add(Component.literal("可能出2转蛊虫").withStyle(ChatFormatting.GREEN));
                tooltipComponents.add(Component.literal("较高概率珍稀3转").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    public Grade getGrade() { return grade; }
}
