package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.entity.BloodBoltEntity;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 血蛊技能：消耗HP发射自追踪血弹
public class BloodBoltAbility extends GuAbility {

    public BloodBoltAbility() {
        super(GuRegistry.id("blood_gu"), 8f, 80, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        if (player.getHealth() <= 4.0f) return;
        player.hurt(player.damageSources().magic(), 4.0f);

        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(com.reverendinsanity.core.path.DaoPath.BLOOD).getTier() * 0.15f;
        float damage = 6.0f * efficiency * pathBonus;

        BloodBoltEntity bolt = new BloodBoltEntity(player.level(), player, damage);
        bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 0.8f, 0.5f);
        player.level().addFreshEntity(bolt);

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFFCC0000, 1.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
