package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 玄气轰天杀招效果：12格AOE气爆，55%伤害+回复10%
public class ProfoundQiExplosionEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        float totalDamageDealt = 0;
        Vec3 center = player.position();
        for (LivingEntity target : targets) {
            float dmg = calculatedDamage * 0.55f;
            target.hurt(player.damageSources().magic(), dmg);
            totalDamageDealt += dmg;
            Vec3 away = target.position().subtract(center).normalize().scale(0.5);
            target.setDeltaMovement(away.x, 0.3, away.z);
        }

        player.heal(totalDamageDealt * 0.10f);

        VfxHelper.spawn(player, VfxType.IMPACT_BURST,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFF66CCDD, 4.0f, 25);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.0f, 1.2f);
    }
}
