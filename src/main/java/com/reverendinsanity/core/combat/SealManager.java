package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 封印系统：消耗真元封印目标实体，使其无法移动和攻击
public class SealManager {

    private static final Map<Integer, SealState> sealedEntities = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();

    private static final ResourceLocation SEAL_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "seal_slow");
    private static final int COOLDOWN = 1200;

    public static boolean sealTarget(ServerPlayer player, LivingEntity target) {
        UUID uuid = player.getUUID();
        Integer cd = cooldowns.get(uuid);
        if (cd != null && cd > 0) {
            player.displayClientMessage(Component.literal("封印冷却中...").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        int rank = aperture.getRank().getLevel();
        float cost = 30f + rank * 20f;
        if (!aperture.consumeEssence(cost)) {
            player.displayClientMessage(Component.literal("真元不足").withStyle(ChatFormatting.RED), true);
            return false;
        }

        int duration = 60 + rank * 40;

        if (target.getMaxHealth() > 100) {
            duration = (int) (duration * 0.5f);
        }

        sealedEntities.put(target.getId(), new SealState(player.getUUID(), duration));

        var speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SEAL_SPEED);
            speed.addTransientModifier(new AttributeModifier(SEAL_SPEED, -0.95, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        cooldowns.put(uuid, COOLDOWN);

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    25, 0.5, 0.5, 0.5, 0.1);
        }
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 0.5f);

        player.displayClientMessage(
                Component.literal("封印！" + target.getName().getString() + " 被封印 " + (duration / 20) + " 秒")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);

        com.reverendinsanity.core.heavenwill.HeavenWillManager.addAttention(player, 2f);

        return true;
    }

    public static void tickServer(ServerLevel level) {
        Iterator<Map.Entry<Integer, SealState>> it = sealedEntities.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, SealState> entry = it.next();
            SealState state = entry.getValue();
            state.remaining--;

            if (state.remaining <= 0) {
                var entity = level.getEntity(entry.getKey());
                if (entity instanceof LivingEntity living) {
                    var speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speed != null) speed.removeModifier(SEAL_SPEED);
                }
                it.remove();
                continue;
            }

            if (state.remaining % 10 == 0) {
                var entity = level.getEntity(entry.getKey());
                if (entity instanceof LivingEntity living) {
                    level.sendParticles(ParticleTypes.ENCHANT,
                            living.getX(), living.getY() + 0.5, living.getZ(),
                            3, 0.3, 0.5, 0.3, 0.05);
                }
            }
        }
    }

    public static void tickPlayerCooldown(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Integer cd = cooldowns.get(uuid);
        if (cd != null) {
            if (cd <= 0) cooldowns.remove(uuid);
            else cooldowns.put(uuid, cd - 1);
        }
    }

    public static boolean isSealed(LivingEntity entity) {
        return sealedEntities.containsKey(entity.getId());
    }

    public static void onPlayerLogout(ServerPlayer player) {
        cooldowns.remove(player.getUUID());
    }

    public static void clearAll(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity living)) {
                    continue;
                }
                var speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null) {
                    speed.removeModifier(SEAL_SPEED);
                }
            }
        }
        sealedEntities.clear();
        cooldowns.clear();
    }

    private static class SealState {
        UUID caster;
        int remaining;

        SealState(UUID caster, int remaining) {
            this.caster = caster;
            this.remaining = remaining;
        }
    }
}
