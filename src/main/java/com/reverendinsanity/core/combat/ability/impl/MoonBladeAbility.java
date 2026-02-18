package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.EssenceGrade;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.entity.MoonBladeEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 月光蛊技能：发射月刃投射物
public class MoonBladeAbility extends GuAbility {

    public MoonBladeAbility() {
        super(GuRegistry.id("moonlight_gu"), 10f, 40, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float baseDamage = 4.0f;
        float efficiencyMult = 1.0f;
        EssenceGrade grade = aperture.getEssenceGrade();
        if (grade != null) {
            efficiencyMult = grade.getEfficiency();
        }
        float pathMult = 1.0f + aperture.getPathRealm(DaoPath.MOON).getTier() * 0.15f;
        float damage = baseDamage * efficiencyMult * pathMult;

        Vec3 look = player.getLookAngle();
        MoonBladeEntity blade = new MoonBladeEntity(player.level(), player, damage);
        blade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        blade.shoot(look.x, look.y, look.z, 1.5f, 0.0f);
        player.level().addFreshEntity(blade);

        VfxHelper.spawn(player, VfxType.SLASH_ARC, 0xFF4488FF, 2.0f, 8);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.2f);
    }
}
