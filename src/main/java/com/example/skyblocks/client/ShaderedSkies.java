package com.example.skyblocks.client;

import com.example.skyblocks.ModBlocks;
import com.example.skyblocks.SkyBlocksMod;
import com.noodlegamer76.shadered.client.renderer.ComplexPassRenderer;
import com.noodlegamer76.shadered.client.renderer.complexpasses.SkyboxRenderPass;
import com.noodlegamer76.shadered.client.util.RenderStage;
import com.noodlegamer76.shadered.client.util.skyblock.SkyblockBatchData;
import com.noodlegamer76.shadered.client.util.skyblock.SkyboxTranslation;
import net.minecraft.resources.ResourceLocation;

/**
 * Registers our skies with Shadered's renderer, the same way Shadered registers its own
 * (SkyblockRenderer): one SkyblockBatchData + one SkyboxRenderPass per sky.
 *
 * Every frame the skybox pass draws the sky (textures/environment/<name>/front|back|left|right|top|bottom.png)
 * into its own screen-sized target and registers that target in SkyblockRenderer.DATA_LIST. Our block entity
 * renderer adds each visible block to the sky's batch data, and Shadered's SkyblockRenderPass then draws all
 * of them with Shadered's skyblock shaders (including the filter passes).
 */
public final class ShaderedSkies {
    private static final SkyblockBatchData[] DATA = new SkyblockBatchData[ModBlocks.SKY_NAMES.length];
    private static final SkyboxRenderPass[] PASSES = new SkyboxRenderPass[ModBlocks.SKY_NAMES.length];
    private static boolean registered;

    static {
        for (int i = 0; i < DATA.length; i++) {
            DATA[i] = new SkyblockBatchData();
            ResourceLocation folder = ResourceLocation.fromNamespaceAndPath(
                    SkyBlocksMod.MOD_ID, "textures/environment/" + ModBlocks.SKY_NAMES[i]);
            // Default translation = the face layout tools/build_shadered_faces.py writes.
            PASSES[i] = new SkyboxRenderPass(folder, DATA[i], new SkyboxTranslation());
        }
    }

    /** Called once from client setup. */
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ComplexPassRenderer renderer = ComplexPassRenderer.getInstance();
        for (SkyboxRenderPass pass : PASSES) {
            renderer.add(RenderStage.AFTER_SKY, pass);
        }
    }

    public static SkyblockBatchData data(int skyIndex) {
        return DATA[clamp(skyIndex)];
    }

    /** GL id of the sky's screen-sized target, or 0 before it has been drawn once. */
    public static int skyboxTexture(int skyIndex) {
        SkyboxRenderPass pass = PASSES[clamp(skyIndex)];
        return pass.getSkyboxTarget() != null ? pass.getSkyboxTarget().getColorTextureId() : 0;
    }

    public static int count() {
        return PASSES.length;
    }

    private static int clamp(int i) {
        return Math.max(0, Math.min(PASSES.length - 1, i));
    }

    private ShaderedSkies() {}
}
