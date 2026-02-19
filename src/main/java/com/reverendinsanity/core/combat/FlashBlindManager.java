package com.reverendinsanity.core.combat;

import com.reverendinsanity.ReverendInsanity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

// Tracks flash-blind timers by entity key to avoid full-world scans every tick.
public final class FlashBlindManager {

    private static final ResourceLocation FOLLOW_RANGE_MOD_ID =
        ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "flash_gu_blind");
    private static final double FOLLOW_RANGE_REDUCTION = -0.95d;
    private static final Map<EntityKey, BlindState> ACTIVE = new HashMap<>();

    private FlashBlindManager() {
    }

    public static void apply(LivingEntity target, int durationTicks) {
        AttributeInstance followRange = target.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) {
            followRange.removeModifier(FOLLOW_RANGE_MOD_ID);
            followRange.addTransientModifier(new AttributeModifier(
                FOLLOW_RANGE_MOD_ID,
                FOLLOW_RANGE_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
        if (target instanceof Mob mob) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        target.setInvisible(false);

        EntityKey key = new EntityKey(target.level().dimension(), target.getId());
        ACTIVE.put(key, new BlindState(target.getUUID(), Math.max(1, durationTicks)));
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<EntityKey, BlindState>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<EntityKey, BlindState> entry = it.next();
            EntityKey key = entry.getKey();
            BlindState state = entry.getValue();

            ServerLevel level = server.getLevel(key.dimensionKey());
            if (level == null) {
                it.remove();
                continue;
            }

            Entity entity = level.getEntity(key.entityId());
            if (!(entity instanceof LivingEntity living) || !living.isAlive()
                || !living.getUUID().equals(state.uuid())) {
                it.remove();
                continue;
            }

            int remaining = state.remainingTicks() - 1;
            if (remaining <= 0) {
                clearModifier(living);
                it.remove();
            } else {
                entry.setValue(new BlindState(state.uuid(), remaining));
            }
        }
    }

    public static void clearAll() {
        ACTIVE.clear();
    }

    public static void clearAll(MinecraftServer server) {
        for (Map.Entry<EntityKey, BlindState> entry : ACTIVE.entrySet()) {
            EntityKey key = entry.getKey();
            BlindState state = entry.getValue();
            ServerLevel level = server.getLevel(key.dimensionKey());
            if (level == null) {
                continue;
            }
            Entity entity = level.getEntity(key.entityId());
            if (entity instanceof LivingEntity living && living.getUUID().equals(state.uuid())) {
                clearModifier(living);
            }
        }
        ACTIVE.clear();
    }

    private static void clearModifier(LivingEntity living) {
        AttributeInstance followRange = living.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) {
            followRange.removeModifier(FOLLOW_RANGE_MOD_ID);
        }
    }

    private record EntityKey(ResourceKey<Level> dimensionKey, int entityId) {
    }

    private record BlindState(UUID uuid, int remainingTicks) {
    }
}
