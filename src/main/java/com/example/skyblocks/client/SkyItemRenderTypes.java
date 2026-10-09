package com.example.skyblocks.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.noodlegamer76.shadered.client.util.skyblock.SkyblockPass;
import com.noodlegamer76.shadered.event.RegisterShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;

/**
 * Render types for our skyblock items, built the same way as Shadered's own item render types
 * (ModRenderTypes.getSkyblockRenderType): Shadered's skyblock shader, with its "Skybox" sampler set to
 * our sky's screen-sized skybox target.
 */
public final class SkyItemRenderTypes {
    private static final RenderType[] TYPES = new RenderType[ShaderedSkies.count()];

    public static RenderType get(int skyIndex) {
        int i = Math.max(0, Math.min(TYPES.length - 1, skyIndex));
        if (TYPES[i] == null) {
            TYPES[i] = create(i);
        }
        return TYPES[i];
    }

    private static RenderType create(int skyIndex) {
        String shaderName = SkyblockPass.NORMAL.getShaderName();
        String name = "skyblocks_sky_item_" + skyIndex;
        RenderStateShard.TexturingStateShard texturing = new RenderStateShard.TexturingStateShard(
                name + "_texturing",
                () -> {
                    int texture = ShaderedSkies.skyboxTexture(skyIndex);
                    ShaderInstance shader = RegisterShaders.get(shaderName);
                    if (shader != null) {
                        shader.setSampler("Skybox", texture);
                    }
                    RenderSystem.setShaderTexture(0, texture);
                },
                () -> {});

        return RenderType.create(
                name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(() -> RegisterShaders.get(shaderName)))
                        .setTexturingState(texturing)
                        .setDepthTestState(new RenderStateShard.DepthTestStateShard("<", GL11.GL_LESS))
                        .createCompositeState(false));
    }

    private SkyItemRenderTypes() {}
}
