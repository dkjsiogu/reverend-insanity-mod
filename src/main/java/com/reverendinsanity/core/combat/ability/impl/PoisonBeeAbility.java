package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.PoisonCloudManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 毒蜂蛊技能：在前方释放毒云区域
public class PoisonBeeAbility extends GuAbility {

    public PoisonBeeAbility() {
        super(GuRegistry.id("poison_bee_gu"), 10f, 160, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 look = player.getLookAngle();
        Vec3 cloudCenter = player.position().add(look.x * 4, 1.0, look.z * 4);

        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float dps = 3.0f * efficiency;

        PoisonCloudManager.addCloud((ServerLevel) player.level(), cloudCenter, 2.5f, 80, dps, player.getUUID());

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            (float) cloudCenter.x, (float) cloudCenter.y, (float) cloudCenter.z,
            0f, 1f, 0f,
            0xFF44AA00, 3.0f, 40);
        player.level().playSound(null, cloudCenter.x, cloudCenter.y, cloudCenter.z,
            SoundEvents.BEEHIVE_EXIT, SoundSource.PLAYERS, 1.0f, 0.5f);
    }
}
