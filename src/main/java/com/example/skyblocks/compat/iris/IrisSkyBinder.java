package com.example.skyblocks.compat.iris;

import com.example.skyblocks.client.SkyAtlas;
import com.example.skyblocks.client.SkyMatrices;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

/**
 * Called (through a mixin) after Iris has set up a shader-pack terrain program for a render pass.
 * Feeds the uniforms that IrisShaderPatch added. Iris hands out texture units from 0 upwards for the
 * pack's own samplers, so ours use the highest units the GPU has.
 */
public final class IrisSkyBinder {
    private static final int TEXTURES = 1 + SkyAtlas.FACE_COUNT; // block atlas + six sky atlases
    private static int firstUnit = -1;

    public static void afterSetupState() {
        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        if (program == 0) {
            return;
        }
        int enabled = GL20.glGetUniformLocation(program, "skyblocks_Enabled");
        if (enabled < 0) {
            return; // not a program we patched
        }

        if (firstUnit < 0) {
            int maxUnits = GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS);
            firstUnit = maxUnits >= 24 ? maxUnits - TEXTURES : 0;
        }
        // Skip passes with blending (translucent), and GPUs with too few texture units to spare.
        boolean on = firstUnit > 0 && !GL11.glIsEnabled(GL11.GL_BLEND);
        GL20.glUniform1i(enabled, on ? 1 : 0);
        if (!on) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        setFloat2(program, "skyblocks_ScreenSize", window.getWidth(), window.getHeight());
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int projInv = GL20.glGetUniformLocation(program, "skyblocks_ProjInv");
            if (projInv >= 0) {
                FloatBuffer buffer = stack.mallocFloat(16);
                SkyMatrices.inverseProjection().get(buffer);
                GL20.glUniformMatrix4fv(projInv, false, buffer);
            }
            int viewToWorld = GL20.glGetUniformLocation(program, "skyblocks_ViewToWorld");
            if (viewToWorld >= 0) {
                FloatBuffer buffer = stack.mallocFloat(9);
                SkyMatrices.viewToWorld().get(buffer);
                GL20.glUniformMatrix3fv(viewToWorld, false, buffer);
            }
        }

        int previousUnit = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int blockAtlas = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getId();
        bind(program, "skyblocks_BlockAtlas", firstUnit, blockAtlas);
        for (int i = 0; i < SkyAtlas.FACE_COUNT; i++) {
            bind(program, "skyblocks_Sampler" + (SkyAtlas.FIRST_SLOT + i), firstUnit + 1 + i, SkyAtlas.textureId(i));
        }
        GlStateManager._activeTexture(previousUnit);
    }

    private static void bind(int program, String name, int unit, int texture) {
        GlStateManager._activeTexture(GL13.GL_TEXTURE0 + unit);
        GlStateManager._bindTexture(texture);
        // Iris binds some textures without going through GlStateManager, so its cache may be stale: bind for real.
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        int location = GL20.glGetUniformLocation(program, name);
        if (location >= 0) {
            GL20.glUniform1i(location, unit);
        }
    }

    private static void setFloat2(int program, String name, float x, float y) {
        int location = GL20.glGetUniformLocation(program, name);
        if (location >= 0) {
            GL20.glUniform2f(location, x, y);
        }
    }

    private IrisSkyBinder() {}
}
