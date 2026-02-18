package com.reverendinsanity.core.combat;

import com.reverendinsanity.network.DamageNumberPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

// 伤害数字管理器：在伤害发生时向附近玩家发送浮动数字
public class DamageNumberManager {

    public enum DamageType {
        PHYSICAL,
        ESSENCE,
        DOT,
        CRITICAL,
        TRUE_DAMAGE
    }

    public static void sendDamageNumber(ServerPlayer viewer, Entity target, float damage, DamageType type) {
        if (damage <= 0) return;
        double x = target.getX() + (target.getRandom().nextFloat() - 0.5) * 0.5;
        double y = target.getY() + target.getBbHeight() + 0.2;
        double z = target.getZ() + (target.getRandom().nextFloat() - 0.5) * 0.5;
        PacketDistributor.sendToPlayer(viewer, new DamageNumberPayload(
            target.getId(), damage, type.ordinal(), x, y, z
        ));
    }

    public static void broadcastDamageNumber(Entity target, float damage, DamageType type) {
        if (damage <= 0 || target.level().isClientSide()) return;
        double x = target.getX() + (target.getRandom().nextFloat() - 0.5) * 0.5;
        double y = target.getY() + target.getBbHeight() + 0.2;
        double z = target.getZ() + (target.getRandom().nextFloat() - 0.5) * 0.5;
        DamageNumberPayload payload = new DamageNumberPayload(
            target.getId(), damage, type.ordinal(), x, y, z
        );
        for (ServerPlayer player : ((net.minecraft.server.level.ServerLevel) target.level()).players()) {
            if (player.distanceTo(target) <= 32.0) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
