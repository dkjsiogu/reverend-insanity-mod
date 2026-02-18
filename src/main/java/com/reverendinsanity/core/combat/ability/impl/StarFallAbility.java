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

// 星陨蛊技能：星辰陨落——10格AOE天降陨石伤害
public class StarFallAbility extends GuAbility {

    public StarFallAbility() {
        super(GuRegistry.id("star_fall_gu"), 22f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.STAR).getTier() * 0.2f;
        float damage = 9.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(10, 8, 10);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            target.setRemainingFireTicks(40);
        }

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 5.0, player.getZ(),
            0f, -1f, 0f,
            0xFFDDCCFF, 5.0f, 25);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5f, 1.0f);
    }
}
