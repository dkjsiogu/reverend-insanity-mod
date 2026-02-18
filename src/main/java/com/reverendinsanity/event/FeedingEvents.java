package com.reverendinsanity.event;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

// 蛊虫喂养事件：蹲下右键手持喂养物品，喂养空窍中最饥饿的对应蛊虫
@EventBusSubscriber(modid = ReverendInsanity.MODID)
public class FeedingEvents {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!player.isCrouching()) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;

        ItemStack itemStack = event.getItemStack();
        if (itemStack.isEmpty()) return;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        String itemIdStr = itemId.toString();

        GuInstance hungriest = null;
        for (GuInstance gu : aperture.getStoredGu()) {
            GuType type = gu.getType();
            if (type == null) continue;
            if (type.feedItem().isEmpty()) continue;
            if (!type.feedItem().equals(itemIdStr)) continue;
            if (!gu.isAlive()) continue;
            if (hungriest == null || gu.getHunger() < hungriest.getHunger()) {
                hungriest = gu;
            }
        }

        if (hungriest == null) return;

        if (hungriest.feed()) {
            itemStack.shrink(1);
            GuType type = hungriest.getType();
            String name = type != null ? type.displayName() : hungriest.getTypeId().getPath();
            player.displayClientMessage(
                Component.literal(name + " 已喂养，饥饿度: " + String.format("%.0f", hungriest.getHunger()) + "%"),
                true
            );
        } else {
            player.displayClientMessage(
                Component.literal("蛊虫饱食，无需喂养"),
                true
            );
        }
    }
}
