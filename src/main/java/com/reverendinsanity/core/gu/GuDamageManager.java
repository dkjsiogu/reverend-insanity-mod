package com.reverendinsanity.core.gu;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

// 蛊虫损伤系统：战斗/死亡可能导致蛊虫受损，降低效果
public class GuDamageManager {

    public static void onPlayerHurt(ServerPlayer player, float damageAmount) {
        if (damageAmount < 5f) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;

        List<GuInstance> stored = aperture.getStoredGu();
        if (stored.isEmpty()) return;

        float damageChance = Math.min(0.15f, damageAmount / player.getMaxHealth() * 0.2f);

        for (GuInstance gu : stored) {
            if (gu.isDamaged()) continue;
            if (player.getRandom().nextFloat() < damageChance) {
                gu.setDamaged(true);
                GuType type = gu.getType();
                if (type != null) {
                    player.displayClientMessage(
                            Component.literal(type.displayName() + " 受损！效果降低50%")
                                    .withStyle(ChatFormatting.RED), false);
                }
                break;
            }
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;

        for (GuInstance gu : aperture.getStoredGu()) {
            if (!gu.isDamaged() && player.getRandom().nextFloat() < 0.25f) {
                gu.setDamaged(true);
            }
        }
    }

    public static boolean repairGu(ServerPlayer player, int guIndex) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        List<GuInstance> stored = aperture.getStoredGu();
        if (guIndex < 0 || guIndex >= stored.size()) return false;

        GuInstance gu = stored.get(guIndex);
        if (!gu.isDamaged()) return false;

        GuType type = gu.getType();
        float cost = type != null ? type.rank() * 50f : 50f;
        if (!aperture.consumeEssence(cost)) {
            player.displayClientMessage(
                    Component.literal("真元不足，修复需要 " + (int) cost + " 真元")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        gu.setDamaged(false);
        if (type != null) {
            player.displayClientMessage(
                    Component.literal(type.displayName() + " 修复成功！")
                            .withStyle(ChatFormatting.GREEN), false);
        }
        return true;
    }
}
