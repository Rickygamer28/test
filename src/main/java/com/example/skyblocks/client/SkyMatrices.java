package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlocksMod;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Held and inventory items are drawn with their own camera matrices, so the item shader cannot
 * read the world camera itself. Once per frame (right after the sky is drawn, when the world's
 * projection and camera are active) we hand the item shader the world's inverse projection and
 * the camera rotation. The item shader then shows the sky exactly as the world sees it.
 */
@EventBusSubscriber(modid = SkyBlocksMod.MOD_ID, value = Dist.CLIENT)
public final class SkyMatrices {
    private static final Matrix4f INV_PROJECTION = new Matrix4f();
    private static final Matrix3f VIEW_TO_WORLD = new Matrix3f();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        ShaderInstance shader = SkyRenderTypes.skyItemShader;
        if (shader == null) {
            return;
        }

        // Includes view bobbing, the same as the real sky, so item and sky stay in step.
        RenderSystem.getProjectionMatrix().invert(INV_PROJECTION);
        // Camera.rotation() turns view-space directions into world-space directions.
        VIEW_TO_WORLD.identity().rotation(event.getCamera().rotation());

        Uniform invProj = shader.getUniform("WorldProjInv");
        if (invProj != null) {
            invProj.set(INV_PROJECTION);
        }
        Uniform viewToWorld = shader.getUniform("ViewToWorld");
        if (viewToWorld != null) {
            viewToWorld.set(VIEW_TO_WORLD);
        }
    }

    private SkyMatrices() {}
}
