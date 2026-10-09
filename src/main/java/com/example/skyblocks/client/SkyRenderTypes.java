package com.example.skyblocks.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

/** Extends RenderType only to get access to its protected state shards. */
public final class SkyRenderTypes extends RenderType {
    /** Shader for held / inventory items. Set by ClientSetup when shaders (re)load. */
    public static ShaderInstance skyItemShader;

    /** One render type for every sky item; the vertex color's red channel carries the sky index. */
    public static final RenderType SKY_ITEM = RenderType.create(
            "skyblocks_sky_item",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> skyItemShader))
                    .setTextureState(new EmptyTextureStateShard(SkyAtlas::bind, () -> {}))
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    private SkyRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        throw new UnsupportedOperationException("Utility class");
    }
}
