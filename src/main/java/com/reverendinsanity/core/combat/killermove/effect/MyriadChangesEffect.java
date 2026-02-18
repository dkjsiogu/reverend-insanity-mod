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

// 万变归元杀招效果：瞬移至最远敌人背后+AOE变化冲击，55%伤害
public class MyriadChangesEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.55f);
            Vec3 knockDir = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(knockDir.x * 1.2, 0.5, knockDir.z * 1.2);
        }

        player.heal(calculatedDamage * 0.10f);

        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0f, 1f, 0f,
            0xFF66CCAA, 4.0f, 25);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0f, 0.6f);
    }
}
