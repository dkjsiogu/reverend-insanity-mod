package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.entity.FireBoltEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 火种蛊技能：发射火弹，命中引燃敌人
public class FireSeedAbility extends GuAbility {

    public FireSeedAbility() {
        super(GuRegistry.id("fire_seed_gu"), 8f, 80, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.FIRE).getTier() * 0.15f;
        float damage = 5.0f * efficiency * pathBonus;

        FireBoltEntity bolt = new FireBoltEntity(player.level(), player, damage);
        bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.2f, 0.5f);
        player.level().addFreshEntity(bolt);

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFFFF6600, 1.5f, 15);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
