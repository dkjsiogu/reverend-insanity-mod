package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 偷袭系统：潜行状态下首击造成额外伤害，偷道蛊师特化
public class AmbushManager {

    private static final Map<UUID, Integer> stealthTicks = new ConcurrentHashMap<>();

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();

        if (player.isShiftKeyDown() && !player.isSprinting()) {
            int current = stealthTicks.getOrDefault(uuid, 0);
            stealthTicks.put(uuid, current + 1);

            if (current == 60) {
                player.displayClientMessage(
                        Component.literal("潜行就绪...下一击将造成偷袭伤害")
                                .withStyle(ChatFormatting.DARK_GRAY), true);
            }
        } else {
            stealthTicks.remove(uuid);
        }
    }

    public static float processAmbushDamage(ServerPlayer attacker, LivingEntity target, float baseDamage) {
        UUID uuid = attacker.getUUID();
        Integer stealth = stealthTicks.get(uuid);
        if (stealth == null || stealth < 40) return baseDamage;

        stealthTicks.remove(uuid);

        GuMasterData data = attacker.getData(ModAttachments.GU_MASTER_DATA.get());
        float multiplier = 1.8f;

        if (data.getAperture().isOpened() && data.getAperture().getPrimaryPath() != null) {
            var path = data.getAperture().getPrimaryPath();
            if (path == com.reverendinsanity.core.path.DaoPath.STEAL ||
                    path == com.reverendinsanity.core.path.DaoPath.SHADOW ||
                    path == com.reverendinsanity.core.path.DaoPath.DARK) {
                multiplier = 2.5f;
            }
        }

        float ambushDamage = baseDamage * multiplier;

        attacker.displayClientMessage(
                Component.literal("偷袭！" + String.format("%.0f", ambushDamage) + " 伤害 (x" + String.format("%.1f", multiplier) + ")")
                        .withStyle(ChatFormatting.DARK_RED), true);

        if (attacker.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    15, 0.3, 0.3, 0.3, 0.2);
        }
        attacker.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2f, 0.7f);

        com.reverendinsanity.core.heavenwill.HeavenWillManager.addAttention(attacker, 1f);

        return ambushDamage;
    }

    public static void onPlayerLogout(ServerPlayer player) {
        stealthTicks.remove(player.getUUID());
    }
}
