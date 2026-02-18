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

// 雷电蛊技能：引雷术——召唤雷电劈击10格内最近敌人
public class LightningAbility extends GuAbility {

    public LightningAbility() {
        super(GuRegistry.id("lightning_gu"), 12f, 160, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        AABB area = player.getBoundingBox().inflate(10);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        targets.stream()
               .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
               .ifPresent(target -> {
                   LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.level());
                   if (bolt != null) {
                       bolt.moveTo(target.getX(), target.getY(), target.getZ());
                       bolt.setCause(player);
                       player.level().addFreshEntity(bolt);
                   }
               });

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFFFFFF44, 2.0f, 8);
    }
}
