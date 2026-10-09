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

    public static RenderType sky(ResourceLocation[] faces) {
        return CACHE.computeIfAbsent(faces[0], key -> RenderType.create(
                "skyblocks_sky_" + key,
                DefaultVertexFormat.POSITION,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(() -> skyShader))
                        .setTextureState(cubeFaces(faces))
                        .setCullState(NO_CULL)
                        .createCompositeState(false)));
    }

    public static RenderType skyItem(ResourceLocation[] faces) {
        return ITEM_CACHE.computeIfAbsent(faces[0], key -> RenderType.create(
                "skyblocks_sky_item_" + key,
                DefaultVertexFormat.POSITION,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new ShaderStateShard(() -> skyItemShader))
                        .setTextureState(cubeFaces(faces))
                        .setCullState(NO_CULL)
                        .createCompositeState(false)));
    }

    /** Binds the six cube faces to Sampler0..Sampler5 (linear filtering, no mipmaps). */
    private static EmptyTextureStateShard cubeFaces(ResourceLocation[] faces) {
        MultiTextureStateShard.Builder builder = MultiTextureStateShard.builder();
        for (ResourceLocation face : faces) {
            builder.add(face, true, false);
        }
        return builder.build();
    }
}
