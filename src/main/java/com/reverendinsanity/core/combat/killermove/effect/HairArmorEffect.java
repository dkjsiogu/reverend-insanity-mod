package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.ReverendInsanity;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 发甲杀招效果：青丝蛊系发丝缠绕铠甲+8格发丝鞭笞30%伤害
public class HairArmorEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getBuffManager().applyBuff(player, new HairArmorBuff());

        AABB area = player.getBoundingBox().inflate(8, 4, 8);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.30f);
        }

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF228B22, 3.5f, 30);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1.5f, 0.6f);
    }

    private static class HairArmorBuff extends GuBuff {

        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "hair_armor_buff");
        private static final ResourceLocation ARMOR_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "hair_armor_armor");
        private static final ResourceLocation TOUGHNESS_MOD = ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "hair_armor_toughness");

        HairArmorBuff() {
            super(ID, 200);
        }

        @Override
        protected void onApply(ServerPlayer player) {
            player.getAttribute(Attributes.ARMOR).addTransientModifier(
                new AttributeModifier(ARMOR_MOD, 8.0, AttributeModifier.Operation.ADD_VALUE));
            player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
                new AttributeModifier(TOUGHNESS_MOD, 4.0, AttributeModifier.Operation.ADD_VALUE));
        }

        @Override
        protected void onRemove(ServerPlayer player) {
            player.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MOD);
            player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(TOUGHNESS_MOD);
        }
    }
}
