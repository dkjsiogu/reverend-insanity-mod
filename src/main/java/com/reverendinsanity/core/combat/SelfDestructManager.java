package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.heavenwill.HeavenWillManager;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 自爆系统：牺牲蛊虫造成大范围爆炸伤害，蛊虫永久销毁
public class SelfDestructManager {

    private static final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();
    private static final int COOLDOWN = 600;

    public static boolean selfDestruct(ServerPlayer player, int guSlotIndex) {
        UUID uuid = player.getUUID();
        Integer cd = cooldowns.get(uuid);
        if (cd != null && cd > 0) {
            player.displayClientMessage(Component.literal("自爆冷却中...").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        List<GuInstance> stored = aperture.getStoredGu();
        if (guSlotIndex < 0 || guSlotIndex >= stored.size()) return false;

        GuInstance sacrificed = stored.get(guSlotIndex);
        int rank = sacrificed.getType() != null ? sacrificed.getType().rank() : 1;
        ResourceLocation guId = sacrificed.getTypeId();

        aperture.removeGuAt(guSlotIndex);

        float damage = switch (rank) {
            case 1 -> 12f;
            case 2 -> 25f;
            case 3 -> 50f;
            case 4 -> 80f;
            default -> 120f;
        };

        float radius = switch (rank) {
            case 1 -> 4f;
            case 2 -> 6f;
            case 3 -> 8f;
            case 4 -> 10f;
            default -> 12f;
        };

        ServerLevel level = player.serverLevel();

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0f, 0.5f + rank * 0.1f);

        for (int i = 0; i < 5 + rank * 3; i++) {
            double ox = (player.getRandom().nextFloat() - 0.5) * radius;
            double oy = player.getRandom().nextFloat() * 2;
            double oz = (player.getRandom().nextFloat() - 0.5) * radius;
            level.sendParticles(ParticleTypes.EXPLOSION, player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(),
                30 + rank * 15, radius * 0.3, 1.0, radius * 0.3, 0.1);

        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && e.distanceTo(player) <= radius);

        for (LivingEntity target : targets) {
            float dist = target.distanceTo(player);
            float falloff = 1.0f - (dist / radius) * 0.5f;
            target.hurt(player.damageSources().explosion(null, player), damage * falloff);
        }

        player.hurt(player.damageSources().explosion(null, player), damage * 0.15f);

        HeavenWillManager.addAttention(player, rank * 3f);

        cooldowns.put(uuid, COOLDOWN);

        player.displayClientMessage(
                Component.literal("蛊虫自爆！" + guId.getPath() + " 永久销毁，造成 " + (int) damage + " 范围伤害")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);

        return true;
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Integer cd = cooldowns.get(uuid);
        if (cd != null) {
            if (cd <= 0) cooldowns.remove(uuid);
            else cooldowns.put(uuid, cd - 1);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        cooldowns.remove(player.getUUID());
    }
}
