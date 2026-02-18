package com.reverendinsanity.client.vfx;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;

// 自定义RenderType，用于VFX着色器渲染
public class ModRenderTypes extends RenderType {

    public ModRenderTypes(String n, VertexFormat f, VertexFormat.Mode m, int b, boolean a, boolean s, Runnable su, Runnable cu) {
        super(n, f, m, b, a, s, su, cu);
    }

    public static final RenderType VFX_GLOW = create(
        "reverend_insanity_vfx_glow",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1024, false, true,
        CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(ADDITIVE_TRANSPARENCY)
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );

    public static final RenderType VFX_TRANSLUCENT = create(
        "reverend_insanity_vfx_translucent",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1024, false, true,
        CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setCullState(NO_CULL)
            .createCompositeState(false)
    );
}
