package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.DotManager;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import java.util.Comparator;
import java.util.List;

// 三心合魂杀招效果：奴道+魂道精神支配，最多控制5个目标
public class ThreeHeartsSoulEffect implements MoveEffect {

    private static final ResourceLocation WEAKEN_ID = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "soul_enslave_weaken");

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12, 6, 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, area, e -> e != player && e.isAlive());

        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
        int count = Math.min(targets.size(), 5);

        for (int i = 0; i < count; i++) {
            LivingEntity target = targets.get(i);
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.35f);
            DotManager.applyWither(target, player, 2.0f, 100);

            if (target instanceof Mob mob) {
                mob.setTarget(null);
                AttributeInstance atkAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
                if (atkAttr != null) {
                    atkAttr.removeModifier(WEAKEN_ID);
                    atkAttr.addTransientModifier(new AttributeModifier(WEAKEN_ID, -6.0, AttributeModifier.Operation.ADD_VALUE));
                }
            }

            VfxHelper.spawn(player, VfxType.SHADOW_FADE,
                target.getX(), target.getY() + 1, target.getZ(),
                0f, -0.5f, 0f,
                0xFF223366, 2.0f, 20);
        }

        VfxHelper.spawn(player, VfxType.DOME_FIELD,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF6622AA, 4.0f, 35);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 2.0f, 0.4f);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.5f, 0.6f);
    }
}
