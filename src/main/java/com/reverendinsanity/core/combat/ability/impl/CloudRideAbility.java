package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 腾云蛊技能：腾云驾雾——向前方跃起飞行
public class CloudRideAbility extends GuAbility {

    public CloudRideAbility() {
        super(GuRegistry.id("cloud_ride_gu"), 10f, 150, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 look = player.getLookAngle();
        float boost = 1.5f + aperture.getPathRealm(DaoPath.FLIGHT).getTier() * 0.3f;
        player.setDeltaMovement(look.x * boost, Math.max(look.y * boost, 0.8), look.z * boost);
        player.hurtMarked = true;
        player.fallDistance = 0;

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFFAADDFF, 2.0f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
