package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.entity.IceBoltEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 寒冰蛊技能：发射冰弹，命中减速
public class ColdIceAbility extends GuAbility {

    public ColdIceAbility() {
        super(GuRegistry.id("cold_ice_gu"), 8f, 80, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.ICE).getTier() * 0.15f;
        float damage = 4.0f * efficiency * pathBonus;

        IceBoltEntity bolt = new IceBoltEntity(player.level(), player, damage);
        bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.2f, 0.5f);
        player.level().addFreshEntity(bolt);

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFF88CCFF, 1.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.5f);
    }
}
