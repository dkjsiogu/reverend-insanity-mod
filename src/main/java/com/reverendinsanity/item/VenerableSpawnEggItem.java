package com.reverendinsanity.item;

import com.reverendinsanity.entity.VenerableEntity;
import com.reverendinsanity.entity.VenerableType;
import com.reverendinsanity.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.List;

// 尊者召唤蛋：右键放置对应类型的尊者Boss
public class VenerableSpawnEggItem extends Item {
    private final VenerableType venerableType;

    public VenerableSpawnEggItem(VenerableType venerableType) {
        super(new Item.Properties().stacksTo(1));
        this.venerableType = venerableType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            VenerableEntity entity = ModEntities.VENERABLE.get().create(level);
            if (entity != null) {
                entity.setVenerableType(venerableType);
                entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                serverLevel.addFreshEntity(entity);
                if (!context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("\u00A75\u4E5D\u8F6C\u5C0A\u8005").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("\u00A77" + venerableType.displayNameCN));
    }
}
