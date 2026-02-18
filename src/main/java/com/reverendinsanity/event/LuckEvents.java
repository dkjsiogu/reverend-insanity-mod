package com.reverendinsanity.event;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// 气运系统事件：钓鱼、方块掉落、气运自然恢复
@EventBusSubscriber(modid = ReverendInsanity.MODID)
public class LuckEvents {

    private static final int LUCK_RECOVERY_INTERVAL = 12000;
    private static final float LUCK_RECOVERY_AMOUNT = 0.01f;
    private static final float LUCK_NATURAL_CAP = 1.0f;

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float luck = data.getLuck();

        if (luck > 1.0f && player.getRandom().nextFloat() < (luck - 1.0f)) {
            event.getDrops().add(new ItemStack(Items.NAUTILUS_SHELL));
        }

        if (luck > 1.5f && player.getRandom().nextFloat() < 0.1f) {
            event.getDrops().add(new ItemStack(Items.NAME_TAG));
        }

        if (luck < 0.7f && player.getRandom().nextFloat() < (1.0f - luck) * 0.5f) {
            if (!event.getDrops().isEmpty()) {
                event.getDrops().remove(event.getDrops().size() - 1);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getBreaker() instanceof ServerPlayer player)) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float luck = data.getLuck();

        if (luck > 1.2f && player.getRandom().nextFloat() < (luck - 1.0f) * 0.3f) {
            event.getDrops().forEach(item -> {
                ItemStack stack = item.getItem();
                if (stack.getCount() < stack.getMaxStackSize()) {
                    stack.grow(1);
                }
            });
        }

        if (luck < 0.5f && player.getRandom().nextFloat() < (1.0f - luck) * 0.2f) {
            if (!event.getDrops().isEmpty()) {
                event.getDrops().remove(event.getDrops().size() - 1);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.tickCount % LUCK_RECOVERY_INTERVAL != 0) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float luck = data.getLuck();

        if (luck < LUCK_NATURAL_CAP) {
            float newLuck = Math.min(luck + LUCK_RECOVERY_AMOUNT, LUCK_NATURAL_CAP);
            data.setLuck(newLuck);
        }
    }
}
