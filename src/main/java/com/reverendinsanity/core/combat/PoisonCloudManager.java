package com.reverendinsanity.core.combat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;

// 毒云管理器：追踪活跃的毒云区域并对区域内敌人造成持续伤害
public class PoisonCloudManager {

    private static final List<PoisonCloud> activeClouds = new ArrayList<>();

    public static void addCloud(ServerLevel level, Vec3 center, float radius, int durationTicks, float damagePerSecond, UUID ownerUUID) {
        activeClouds.add(new PoisonCloud(level, center, radius, durationTicks, damagePerSecond, ownerUUID));
    }

    public static void tick() {
        Iterator<PoisonCloud> it = activeClouds.iterator();
        while (it.hasNext()) {
            PoisonCloud cloud = it.next();
            cloud.remainingTicks--;
            if (cloud.remainingTicks <= 0) {
                it.remove();
                continue;
            }
            if (cloud.tickCounter++ % 20 == 0) {
                AABB area = new AABB(
                    cloud.center.x - cloud.radius, cloud.center.y - 1, cloud.center.z - cloud.radius,
                    cloud.center.x + cloud.radius, cloud.center.y + cloud.radius, cloud.center.z + cloud.radius
                );
                List<LivingEntity> targets = cloud.level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e.isAlive() && !e.getUUID().equals(cloud.ownerUUID));
                for (LivingEntity target : targets) {
                    target.hurt(cloud.level.damageSources().magic(), cloud.damagePerSecond);
                }
            }
            if (cloud.tickCounter % 5 == 0) {
                spawnParticles(cloud);
            }
        }
    }

    private static void spawnParticles(PoisonCloud cloud) {
        for (int i = 0; i < 8; i++) {
            double ox = (cloud.level.random.nextDouble() - 0.5) * cloud.radius * 2;
            double oy = cloud.level.random.nextDouble() * cloud.radius;
            double oz = (cloud.level.random.nextDouble() - 0.5) * cloud.radius * 2;
            cloud.level.sendParticles(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                cloud.center.x + ox, cloud.center.y + oy, cloud.center.z + oz,
                1, 0, 0.02, 0, 0.01);
        }
    }

    public static void clearAll() {
        activeClouds.clear();
    }

    private static class PoisonCloud {
        final ServerLevel level;
        final Vec3 center;
        final float radius;
        int remainingTicks;
        final float damagePerSecond;
        final UUID ownerUUID;
        int tickCounter;

        PoisonCloud(ServerLevel level, Vec3 center, float radius, int ticks, float dps, UUID owner) {
            this.level = level;
            this.center = center;
            this.radius = radius;
            this.remainingTicks = ticks;
            this.damagePerSecond = dps;
            this.ownerUUID = owner;
            this.tickCounter = 0;
        }
    }
}
