package com.reverendinsanity.core.clone;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 分身系统：虚影分身增强战斗，概率闪避+额外伤害+速度提升
public class CloneManager {

    private static final Map<UUID, Integer> cloneTicks = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();

    private static final ResourceLocation CLONE_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "clone_speed");
    private static final ResourceLocation CLONE_ATTACK = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "clone_attack");

    private static final int COOLDOWN = 2400;
    private static final float ESSENCE_COST_RATIO = 0.3f;
    private static final float DODGE_CHANCE = 0.2f;
    private static final float EXTRA_DAMAGE_RATIO = 0.4f;

    public static boolean tryActivate(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (cloneTicks.containsKey(uuid)) {
            cancelClone(player);
            return true;
        }

        Integer cd = cooldowns.get(uuid);
        if (cd != null && cd > 0) {
            player.displayClientMessage(Component.literal("分身杀招冷却中...").withStyle(ChatFormatting.GRAY), true);
            return false;
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        float cost = aperture.getMaxEssence() * ESSENCE_COST_RATIO;
        if (!aperture.consumeEssence(cost)) {
            player.displayClientMessage(Component.literal("真元不足").withStyle(ChatFormatting.RED), true);
            return false;
        }

        int duration = switch (aperture.getRank().getLevel()) {
            case 1 -> 300;
            case 2 -> 400;
            case 3 -> 600;
            case 4 -> 800;
            default -> 1200;
        };

        cloneTicks.put(uuid, duration);
        applyModifiers(player);

        player.displayClientMessage(
                Component.literal("分身！").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);

        return true;
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();

        Integer cd = cooldowns.get(uuid);
        if (cd != null) {
            if (cd <= 0) cooldowns.remove(uuid);
            else cooldowns.put(uuid, cd - 1);
        }

        Integer remaining = cloneTicks.get(uuid);
        if (remaining == null) return;

        if (remaining <= 0) {
            cancelClone(player);
            return;
        }

        cloneTicks.put(uuid, remaining - 1);

        if (player.tickCount % 5 == 0 && player.level() instanceof ServerLevel level) {
            double angle = (player.tickCount * 0.15) % (2 * Math.PI);
            double ox = Math.cos(angle) * 1.2;
            double oz = Math.sin(angle) * 1.2;
            level.sendParticles(
                    new DustParticleOptions(new Vector3f(0.5f, 0.8f, 1.0f), 1.0f),
                    player.getX() + ox, player.getY() + 0.5, player.getZ() + oz,
                    3, 0.1, 0.5, 0.1, 0);

            double angle2 = angle + Math.PI;
            level.sendParticles(
                    new DustParticleOptions(new Vector3f(0.5f, 0.8f, 1.0f), 0.8f),
                    player.getX() + Math.cos(angle2) * 1.0, player.getY() + 0.3, player.getZ() + Math.sin(angle2) * 1.0,
                    2, 0.1, 0.4, 0.1, 0);
        }
    }

    public static boolean onIncomingDamage(ServerPlayer player) {
        if (!cloneTicks.containsKey(player.getUUID())) return false;
        if (player.getRandom().nextFloat() < DODGE_CHANCE) {
            player.displayClientMessage(
                    Component.literal("分身替身！").withStyle(ChatFormatting.AQUA), true);
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(
                        new DustParticleOptions(new Vector3f(0.3f, 0.6f, 1.0f), 1.5f),
                        player.getX(), player.getY() + 1, player.getZ(),
                        15, 0.5, 0.8, 0.5, 0.05);
            }
            return true;
        }
        return false;
    }

    public static float getExtraDamage(ServerPlayer player, float baseDamage) {
        if (!cloneTicks.containsKey(player.getUUID())) return 0;
        return baseDamage * EXTRA_DAMAGE_RATIO;
    }

    public static boolean isActive(ServerPlayer player) {
        return cloneTicks.containsKey(player.getUUID());
    }

    private static void cancelClone(ServerPlayer player) {
        UUID uuid = player.getUUID();
        cloneTicks.remove(uuid);
        removeModifiers(player);
        cooldowns.put(uuid, COOLDOWN);
        player.displayClientMessage(
                Component.literal("分身消散").withStyle(ChatFormatting.GRAY), true);
    }

    private static void applyModifiers(ServerPlayer player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(CLONE_SPEED);
            speed.addTransientModifier(new AttributeModifier(CLONE_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(CLONE_ATTACK);
            atk.addTransientModifier(new AttributeModifier(CLONE_ATTACK, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void removeModifiers(ServerPlayer player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(CLONE_SPEED);
        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.removeModifier(CLONE_ATTACK);
    }

    public static void onPlayerDeath(ServerPlayer player) {
        cloneTicks.remove(player.getUUID());
        removeModifiers(player);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        cloneTicks.remove(player.getUUID());
        removeModifiers(player);
        cooldowns.remove(player.getUUID());
    }
}
