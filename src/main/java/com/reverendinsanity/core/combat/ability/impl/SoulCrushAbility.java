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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 碎魂蛊技能：碎魂冲击——10格AOE魔法伤害（无视护甲，二转）
public class SoulCrushAbility extends GuAbility {

    public SoulCrushAbility() {
        super(GuRegistry.id("soul_crush_gu"), 25f, 500, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.SOUL).getTier() * 0.2f;
        float damage = 8.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(10.0, 4.0, 10.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF8822FF, 5.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}
