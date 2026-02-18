package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.EssenceGrade;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.entity.GoldBeamEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 一气金光虫技能：发射金色穿透光束
public class GoldLightAbility extends GuAbility {

    public GoldLightAbility() {
        super(GuRegistry.id("gold_light_worm"), 20f, 60, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float baseDamage = 6.0f;
        float efficiencyMult = 1.0f;
        EssenceGrade grade = aperture.getEssenceGrade();
        if (grade != null) {
            efficiencyMult = grade.getEfficiency();
        }
        float pathMult = 1.0f + aperture.getPathRealm(DaoPath.METAL).getTier() * 0.15f;
        float damage = baseDamage * efficiencyMult * pathMult;

        Vec3 look = player.getLookAngle();
        GoldBeamEntity beam = new GoldBeamEntity(player.level(), player, damage);
        beam.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        beam.shoot(look.x, look.y, look.z, 2.0f, 0.0f);
        player.level().addFreshEntity(beam);

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM, 0xFFFFD700, 1.5f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
