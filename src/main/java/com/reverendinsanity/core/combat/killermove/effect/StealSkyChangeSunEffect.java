package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.buff.GuBuff;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import java.util.Comparator;
import java.util.List;

// 偷天换日杀招效果：偷取最近目标属性为己用，攻击力与速度互换
public class StealSkyChangeSunEffect implements MoveEffect {

    private static final ResourceLocation DEBUFF_DMG = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "steal_sky_debuff_dmg");
    private static final ResourceLocation DEBUFF_SPD = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "steal_sky_debuff_spd");
    private static final ResourceLocation BUFF_ID = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "steal_sky_buff");

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        AABB area = player.getBoundingBox().inflate(12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, area, e -> e != player && e.isAlive());

        LivingEntity target = targets.stream()
            .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
            .orElse(null);

        if (target == null) return;

        target.hurt(player.damageSources().magic(), calculatedDamage * 0.40f);

        AttributeInstance targetDmg = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (targetDmg != null) {
            targetDmg.removeModifier(DEBUFF_DMG);
            targetDmg.addTransientModifier(new AttributeModifier(DEBUFF_DMG, -4.0, AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance targetSpd = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (targetSpd != null) {
            targetSpd.removeModifier(DEBUFF_SPD);
            targetSpd.addTransientModifier(new AttributeModifier(DEBUFF_SPD, -0.03, AttributeModifier.Operation.ADD_VALUE));
        }

        GuMasterData gData = player.getData(ModAttachments.GU_MASTER_DATA.get());
        gData.getBuffManager().applyBuff(player, new GuBuff(BUFF_ID, 200) {
            @Override
            protected void onApply(ServerPlayer p) {
                AttributeInstance dmg = p.getAttribute(Attributes.ATTACK_DAMAGE);
                if (dmg != null) {
                    dmg.addTransientModifier(new AttributeModifier(BUFF_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
                }
                AttributeInstance spd = p.getAttribute(Attributes.MOVEMENT_SPEED);
                if (spd != null) {
                    spd.addTransientModifier(new AttributeModifier(BUFF_ID, 0.03, AttributeModifier.Operation.ADD_VALUE));
                }
            }

            @Override
            protected void onRemove(ServerPlayer p) {
                AttributeInstance dmg = p.getAttribute(Attributes.ATTACK_DAMAGE);
                if (dmg != null) dmg.removeModifier(BUFF_ID);
                AttributeInstance spd = p.getAttribute(Attributes.MOVEMENT_SPEED);
                if (spd != null) spd.removeModifier(BUFF_ID);
            }
        });

        VfxHelper.spawn(player, VfxType.BLACK_HOLE,
            target.getX(), target.getY() + 1, target.getZ(),
            0f, 1f, 0f,
            0xFF8800AA, 3.0f, 30);
        VfxHelper.spawn(player, VfxType.GLOW_BURST,
            player.getX(), player.getY() + 1, player.getZ(),
            0f, 1f, 0f,
            0xFFFFDD00, 2.5f, 25);

        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 2.0f, 0.5f);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.2f);
    }
}
