package com.reverendinsanity.core.transformation;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.path.DaoPathSounds;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 变身系统：变化道蛊虫触发形态变化，获得对应生物能力
public class TransformationManager {

    private static final Map<UUID, TransformState> activeTransforms = new ConcurrentHashMap<>();

    private static final ResourceLocation MORPH_GU = GuRegistry.id("morph_gu");
    private static final ResourceLocation HEAVEN_CHANGE_GU = GuRegistry.id("heaven_change_gu");
    private static final ResourceLocation SHRINK_GROUND_GU = GuRegistry.id("shrink_ground_gu");

    private static final ResourceLocation MOD_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "transform_speed");
    private static final ResourceLocation MOD_ATTACK = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "transform_attack");
    private static final ResourceLocation MOD_ARMOR = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "transform_armor");
    private static final ResourceLocation MOD_KNOCKBACK_RES = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "transform_kb_res");
    private static final ResourceLocation MOD_JUMP = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "transform_jump");

    private static final float ESSENCE_PER_SECOND = 2.0f;

    public static boolean tryTransform(ServerPlayer player, TransformForm form) {
        UUID uuid = player.getUUID();

        if (activeTransforms.containsKey(uuid)) {
            cancelTransform(player);
            return true;
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        if (aperture.getCurrentEssence() < ESSENCE_PER_SECOND * 5) return false;

        ResourceLocation requiredGu = switch (form) {
            case WOLF -> MORPH_GU;
            case BEAR -> HEAVEN_CHANGE_GU;
            case SHRINK -> SHRINK_GROUND_GU;
        };
        if (aperture.findGuInstance(requiredGu) == null) return false;

        TransformState state = new TransformState();
        state.form = form;
        state.startTick = player.tickCount;
        state.remainingTicks = form.durationTicks;
        activeTransforms.put(uuid, state);

        applyModifiers(player, form);

        player.displayClientMessage(
                Component.literal("变身——" + form.displayName + "！")
                        .withStyle(net.minecraft.ChatFormatting.GOLD),
                true
        );
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                DaoPathSounds.getAbilitySound(DaoPath.TRANSFORMATION), SoundSource.PLAYERS, 0.7f, 0.8f);

        return true;
    }

    public static void tick(ServerPlayer player) {
        TransformState state = activeTransforms.get(player.getUUID());
        if (state == null) return;

        state.remainingTicks--;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        if (player.tickCount % 20 == 0) {
            if (!aperture.consumeEssence(ESSENCE_PER_SECOND)) {
                cancelTransform(player);
                player.displayClientMessage(
                        Component.literal("真元耗尽，变身解除！").withStyle(net.minecraft.ChatFormatting.RED), true);
                return;
            }
        }

        if (state.remainingTicks <= 0) {
            cancelTransform(player);
            player.displayClientMessage(
                    Component.literal("变身时间结束。").withStyle(net.minecraft.ChatFormatting.YELLOW), true);
            return;
        }

        if (player.tickCount % 15 == 0) {
            spawnFormParticles(player, state.form);
        }
    }

    public static void cancelTransform(ServerPlayer player) {
        TransformState state = activeTransforms.remove(player.getUUID());
        if (state == null) return;

        removeModifiers(player);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                DaoPathSounds.getAbilitySound(DaoPath.TRANSFORMATION), SoundSource.PLAYERS, 0.5f, 1.6f);
    }

    public static void onPlayerAttack(ServerPlayer player, float damage) {
        TransformState state = activeTransforms.get(player.getUUID());
        if (state != null && state.form == TransformForm.BEAR) {
            player.heal(1.0f);
        }
    }

    public static boolean isTransformed(ServerPlayer player) {
        return activeTransforms.containsKey(player.getUUID());
    }

    public static TransformForm getCurrentForm(UUID playerId) {
        TransformState state = activeTransforms.get(playerId);
        return state != null ? state.form : null;
    }

    public static void onPlayerLogout(ServerPlayer player) {
        TransformState state = activeTransforms.remove(player.getUUID());
        if (state != null) {
            removeModifiers(player);
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        TransformState state = activeTransforms.remove(player.getUUID());
        if (state != null) {
            removeModifiers(player);
        }
    }

    private static void applyModifiers(ServerPlayer player, TransformForm form) {
        switch (form) {
            case WOLF -> {
                addMod(player, Attributes.MOVEMENT_SPEED, MOD_SPEED, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 3.0, AttributeModifier.Operation.ADD_VALUE);
                addMod(player, Attributes.JUMP_STRENGTH, MOD_JUMP, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
            case BEAR -> {
                addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, 6.0, AttributeModifier.Operation.ADD_VALUE);
                addMod(player, Attributes.KNOCKBACK_RESISTANCE, MOD_KNOCKBACK_RES, 0.8, AttributeModifier.Operation.ADD_VALUE);
                addMod(player, Attributes.ARMOR, MOD_ARMOR, 6.0, AttributeModifier.Operation.ADD_VALUE);
                addMod(player, Attributes.MOVEMENT_SPEED, MOD_SPEED, -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
            case SHRINK -> {
                addMod(player, Attributes.MOVEMENT_SPEED, MOD_SPEED, 0.6, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                addMod(player, Attributes.JUMP_STRENGTH, MOD_JUMP, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                addMod(player, Attributes.ATTACK_DAMAGE, MOD_ATTACK, -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }
        }
    }

    private static void addMod(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                               ResourceLocation id, double amount, AttributeModifier.Operation op) {
        var instance = player.getAttribute(attr);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, amount, op));
        }
    }

    private static void removeModifiers(ServerPlayer player) {
        ResourceLocation[] mods = { MOD_SPEED, MOD_ATTACK, MOD_ARMOR, MOD_KNOCKBACK_RES, MOD_JUMP };
        for (ResourceLocation modId : mods) {
            removeMod(player, Attributes.MOVEMENT_SPEED, modId);
            removeMod(player, Attributes.ATTACK_DAMAGE, modId);
            removeMod(player, Attributes.ARMOR, modId);
            removeMod(player, Attributes.KNOCKBACK_RESISTANCE, modId);
            removeMod(player, Attributes.JUMP_STRENGTH, modId);
        }
    }

    private static void removeMod(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                                  ResourceLocation id) {
        var instance = player.getAttribute(attr);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static void spawnFormParticles(ServerPlayer player, TransformForm form) {
        ServerLevel level = (ServerLevel) player.level();
        int color = form.particleColor;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), 1.2f);
        for (int i = 0; i < 4; i++) {
            double angle = (player.tickCount * 0.15 + i * Math.PI / 2) % (Math.PI * 2);
            double px = player.getX() + Math.cos(angle) * 0.8;
            double pz = player.getZ() + Math.sin(angle) * 0.8;
            level.sendParticles(dust, px, player.getY() + 0.5 + Math.sin(player.tickCount * 0.1) * 0.3, pz,
                    1, 0, 0, 0, 0);
        }
    }

    public enum TransformForm {
        WOLF("狼形", 600, 0xAAAA00),
        BEAR("熊形", 600, 0x884422),
        SHRINK("缩地", 400, 0x88CCFF);

        final String displayName;
        final int durationTicks;
        final int particleColor;

        TransformForm(String displayName, int durationTicks, int particleColor) {
            this.displayName = displayName;
            this.durationTicks = durationTicks;
            this.particleColor = particleColor;
        }

        public String getDisplayName() { return displayName; }
    }

    private static class TransformState {
        TransformForm form;
        int startTick;
        int remainingTicks;
    }
}
