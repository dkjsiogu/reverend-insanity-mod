package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 气运系统：击杀夺运、气运衰减、气运影响各类判定
public class FortunePlunderManager {

    private static final Map<UUID, Integer> plunderCooldowns = new ConcurrentHashMap<>();
    private static final int TICK_INTERVAL = 100;

    public static void tick(ServerPlayer player) {
        if (player.tickCount % TICK_INTERVAL != 0) return;

        UUID uuid = player.getUUID();
        Integer cd = plunderCooldowns.get(uuid);
        if (cd != null) {
            if (cd <= 0) plunderCooldowns.remove(uuid);
            else plunderCooldowns.put(uuid, cd - TICK_INTERVAL);
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        if (!data.getAperture().isOpened()) return;

        float luck = data.getLuck();
        if (luck > 1.0f) {
            data.setLuck(luck - 0.001f);
        } else if (luck < 1.0f) {
            data.setLuck(luck + 0.0005f);
        }
    }

    public static void onKillEntity(ServerPlayer player, LivingEntity killed) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        if (!data.getAperture().isOpened()) return;

        float plunder = 0;
        String victimName = killed.getName().getString();

        if (killed instanceof com.reverendinsanity.entity.GuMasterEntity) {
            plunder = 0.08f + player.getRandom().nextFloat() * 0.07f;
        } else if (killed instanceof com.reverendinsanity.entity.AncientGuImmortalEntity) {
            plunder = 0.15f + player.getRandom().nextFloat() * 0.1f;
        } else if (killed.getMaxHealth() >= 40) {
            plunder = 0.02f + player.getRandom().nextFloat() * 0.02f;
        }

        if (plunder > 0) {
            data.setLuck(data.getLuck() + plunder);
            player.displayClientMessage(
                    Component.literal("夺取 " + victimName + " 气运 +" + String.format("%.1f%%", plunder * 100))
                            .withStyle(ChatFormatting.GOLD), true);
        }
    }

    public static float getRefinementBonus(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float luck = data.getLuck();
        if (luck > 1.2f) return 0.15f;
        if (luck > 1.1f) return 0.08f;
        if (luck > 1.0f) return 0.03f;
        if (luck < 0.8f) return -0.15f;
        if (luck < 0.9f) return -0.08f;
        return 0;
    }

    public static float getLootDropBonus(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        return (data.getLuck() - 1.0f) * 2.0f;
    }

    public static void onPlayerLogout(ServerPlayer player) {
        plunderCooldowns.remove(player.getUUID());
    }
}
