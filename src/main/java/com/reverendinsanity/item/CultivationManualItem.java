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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// 修炼入门手册：右键翻页阅读蛊师入门知识
public class CultivationManualItem extends Item {

    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    private static final String[][] PAGES = {
        {
            "\u3010\u7b2c\u4e00\u7ae0\uff1a\u5f00\u7a8d\u4e4b\u8def\u3011",
            "\u5bfb\u627e\u5e0c\u671b\u86ca\uff0c\u53f3\u952e\u4f7f\u7528\u5373\u53ef\u5f00\u7a8d\u3002",
            "\u8d44\u8d28\u968f\u673a\uff1a\u4e01\u7b49(40%)\u3001\u4e19\u7b49(30%)\u3001\u4e59\u7b49(20%)\u3001\u7532\u7b49(8%)\u3001\u5341\u7edd\u4f53(2%)\u3002",
            "\u8d44\u8d28\u51b3\u5b9a\u771f\u5143\u6062\u590d\u901f\u5ea6\u548c\u4fee\u70bc\u4e0a\u9650\u3002"
        },
        {
            "\u3010\u7b2c\u4e8c\u7ae0\uff1a\u4fee\u70bc\u57fa\u7840\u3011",
            "\u771f\u5143(\u7eff\u6761)\uff1a\u91ca\u653e\u86ca\u672f\u7684\u80fd\u91cf\uff0c\u81ea\u7136\u6062\u590d\u3002",
            "\u5ff5\u529b(\u84dd\u6761)\uff1a\u63a7\u5236\u86ca\u866b\u7684\u7cbe\u795e\u529b\uff0c\u81ea\u7136\u6062\u590d\u3002",
            "\u8e72\u4e0b\u9759\u6b62\u4e0d\u52a8\u53ef\u5195\u60f3\u7a81\u7834\u5c0f\u5883\u754c\u3002",
            "\u4f7f\u7528\u7a81\u7834\u77f3\u53ef\u7a81\u7834\u5927\u5883\u754c(\u4e09\u8f6c\u2192\u56db\u8f6c\u9700\u5929\u52ab)\u3002"
        },
        {
            "\u3010\u7b2c\u4e09\u7ae0\uff1a\u86ca\u866b\u4e4b\u9053\u3011",
            "\u91ce\u5916\u53ef\u9047\u89c1\u91ce\u86ca(\u5f69\u8272\u8424\u706b\u866b)\uff0c\u51fb\u6740\u6709\u51e0\u7387\u83b7\u5f97\u86ca\u866b\u3002",
            "\u86ca\u866b\u9700\u8981\u5b9a\u671f\u5582\u517b(\u6309Tab\u6253\u5f00\u7a8d\u7a74\u754c\u9762\uff0c\u70b9\u51fb\u5582\u98df)\u3002",
            "\u86ca\u866b\u9965\u997f\u5ea6\u964d\u81f30\u4f1a\u6b7b\u4ea1\uff0c\u8bf7\u53ca\u65f6\u5582\u517b\uff01",
            "\u70bc\u86ca\u7089\u53ef\u4ee5\u5c06\u86ca\u866b+\u6750\u6599\u5408\u6210\u66f4\u5f3a\u86ca\u866b\u3002"
        },
        {
            "\u3010\u7b2c\u56db\u7ae0\uff1a\u6218\u6597\u4e4b\u672f\u3011",
            "\u88c5\u5907\u86ca\u866b\u540e\uff0c\u62591-5\u952e\u91ca\u653e\u5bf9\u5e94\u86ca\u672f\u3002",
            "\u6740\u62db\uff1a\u7ec4\u5408\u591a\u53ea\u86ca\u866b\u7684\u86ca\u672f\u91ca\u653e\u5a01\u529b\u66f4\u5927\u7684\u62db\u5f0f\u3002",
            "\u6309Z/X/C\u952e\u91ca\u653e\u5df2\u88c5\u5907\u7684\u6740\u62db\u3002",
            "\u4e0d\u540c\u9053\u7684\u86ca\u672f\u6709\u4e0d\u540c\u6548\u679c\uff1a\u653b\u51fb\u3001\u9632\u5fa1\u3001\u8f85\u52a9\u3001\u79fb\u52a8\u7b49\u3002"
        },
        {
            "\u3010\u7b2c\u4e94\u7ae0\uff1a\u63a2\u7d22\u4e16\u754c\u3011",
            "\u86ca\u7a9f\uff1a\u5730\u4e0b\u6d1e\u7a74\uff0c\u6709\u5b9d\u7bb1\u548c\u86ca\u866b\u3002",
            "\u4f20\u627f\u79d8\u5883\uff1a\u5927\u578b\u5730\u4e0b\u9057\u8ff9\uff0c\u6709\u7a00\u6709\u86ca\u866b\u548c\u4f20\u627f\u3002",
            "\u6708\u5170\u82b1\u6d77\uff1a\u58ee\u4e3d\u7684\u5730\u4e0b\u82b1\u6d77\u6eb6\u6d1e\u3002",
            "\u53e4\u6708\u5c71\u5be8\uff1a\u86ca\u5e08\u805a\u843d\uff0c\u6709\u86ca\u5546\u4eba\u548c\u86ca\u5ba4\u3002",
            "\u7075\u6cc9\u9644\u8fd1\u4fee\u70bc\u901f\u5ea6\u5927\u5e45\u63d0\u5347\uff01"
        },
        {
            "\u3010\u7b2c\u516d\u7ae0\uff1a\u8fdb\u9636\u4e4b\u8def\u3011",
            "\u4e00\u8f6c\u9752\u94dc\u2192\u4e8c\u8f6c\u8d64\u94c1\u2192\u4e09\u8f6c\u767d\u94f6\u2192\u56db\u8f6c\u9ec4\u91d1\u2192\u4e94\u8f6c\u6781\u9650\u3002",
            "\u4e09\u8f6c\u540e\u771f\u5143\u7c92\u5b50\u53ef\u89c1\u3002\u56db\u8f6c\u9700\u6e21\u5929\u52ab\u3002",
            "\u9053\u75d5\uff1a\u4f7f\u7528\u86ca\u672f\u79ef\u7d2f\u9053\u75d5\uff0c\u63d0\u5347\u5bf9\u5e94\u9053\u7684\u86ca\u672f\u5a01\u529b\u3002",
            "\u798f\u5730\u4e4b\u79cd\uff1a\u653e\u7f6e\u540e\u6fc0\u6d3b\u5f62\u6210\u798f\u5730\uff0c\u5927\u5e45\u52a0\u901f\u4fee\u70bc\u3002",
            "\u6625\u79cb\u8749\uff1a\u9006\u8f6c\u65f6\u5149\u4e4b\u86ca\uff0c\u6b7b\u4ea1\u540e\u53ef\u91cd\u751f\u3002"
        }
    };

    public CultivationManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            UUID uuid = player.getUUID();
            int page = playerPages.getOrDefault(uuid, 0);

            String[] content = PAGES[page % PAGES.length];

            player.displayClientMessage(
                Component.literal(content[0]).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

            for (int i = 1; i < content.length; i++) {
                player.displayClientMessage(
                    Component.literal("  " + content[i]).withStyle(ChatFormatting.YELLOW), false);
            }

            player.displayClientMessage(
                Component.literal("--- \u7b2c " + (page % PAGES.length + 1) + "/" + PAGES.length + " \u9875 (\u53f3\u952e\u7ffb\u9875) ---")
                    .withStyle(ChatFormatting.GRAY), false);

            playerPages.put(uuid, (page + 1) % PAGES.length);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("\u86ca\u5e08\u4fee\u70bc\u5165\u95e8\u6307\u5357").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.literal("\u53f3\u952e\u7ffb\u9875\u9605\u8bfb").withStyle(ChatFormatting.GRAY));
    }
}
