package com.example.skyblocks.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

/** Extends RenderType only to get access to its protected state shards. */
public final class SkyRenderTypes extends RenderType {
    /** Set by ClientSetup when shaders (re)load. */
    public static ShaderInstance skyShader;

    /** Shader for held / inventory items (block-local mapping). */
    public static ShaderInstance skyItemShader;

    private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ITEM_CACHE = new HashMap<>();

    private SkyRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        throw new UnsupportedOperationException("Utility class");
    }

    public static RenderType sky(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, tex -> RenderType.create(
                "skyblocks_sky_" + tex,
                DefaultVertexFormat.POSITION,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(() -> skyShader))
                        .setTextureState(new TextureStateShard(tex, true, false))
                        .setCullState(NO_CULL)
                        .createCompositeState(false)));
    }

    public static RenderType skyItem(ResourceLocation texture) {
        return ITEM_CACHE.computeIfAbsent(texture, tex -> RenderType.create(
                "skyblocks_sky_item_" + tex,
                DefaultVertexFormat.POSITION,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(() -> skyItemShader))
                        .setTextureState(new TextureStateShard(tex, true, false))
                        .setCullState(NO_CULL)
                        .createCompositeState(false)));
    }
}
