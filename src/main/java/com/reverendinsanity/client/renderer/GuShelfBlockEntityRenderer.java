package com.reverendinsanity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.reverendinsanity.block.entity.GuShelfBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

// 蛊架方块实体渲染器，展示浮空旋转的蛊虫物品
public class GuShelfBlockEntityRenderer implements BlockEntityRenderer<GuShelfBlockEntity> {

    private final ItemRenderer itemRenderer;

    public GuShelfBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(GuShelfBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getDisplayItem();
        if (stack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);

        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        float angle = (gameTime + partialTick) * 2.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, buffer, blockEntity.getLevel(), 0);

        poseStack.popPose();
    }
}
