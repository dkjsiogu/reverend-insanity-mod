package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.Comparator;
import java.util.List;

// 雷暴蛊技能：雷暴天降——召唤3道雷电劈击12格内最近的3个敌人（二转）
public class ThunderstormAbility extends GuAbility {

    public ThunderstormAbility() {
        super(GuRegistry.id("thunderstorm_gu"), 25f, 500, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = player.getBoundingBox().inflate(12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        targets.stream()
               .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
               .limit(3)
               .forEach(target -> {
                   LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
                   if (bolt != null) {
                       bolt.moveTo(target.getX(), target.getY(), target.getZ());
                       bolt.setCause(player);
                       player.level().addFreshEntity(bolt);
                   }
               });

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFFFFF00, 4.0f, 15);
    }
}
