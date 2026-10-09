package com.example.skyblocks.compat.sodium;

import com.example.skyblocks.client.SkyAtlas;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

/**
 * Called (through a mixin) right after Sodium has bound its terrain shader and set up its own textures.
 * Feeds our extra uniforms to the patched shader: the sky atlases, the screen size and an on/off switch.
 */
public final class SodiumSkyBinder {
    /** Texture units for the six atlases. Sodium itself uses units 0 (blocks) and 1 (lightmap). */
    private static final int FIRST_UNIT = 4;

    public static void afterSetupState() {
        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        if (program == 0) {
            return;
        }
        int enabled = GL20.glGetUniformLocation(program, "skyblocks_Enabled");
        if (enabled < 0) {
            return; // not our patched shader (patch skipped, or a shader pack is in use)
        }

        // The translucent pass also runs this shader. Only draw skies in passes without blending
        // (solid), so semi-transparent textures like stained glass are never mistaken for a sky.
        boolean solidPass = !GL11.glIsEnabled(GL11.GL_BLEND);
        GL20.glUniform1i(enabled, solidPass ? 1 : 0);
        if (!solidPass) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        int screen = GL20.glGetUniformLocation(program, "skyblocks_ScreenSize");
        if (screen >= 0) {
            GL20.glUniform2f(screen, window.getWidth(), window.getHeight());
        }

        int previousUnit = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        for (int i = 0; i < SkyAtlas.FACE_COUNT; i++) {
            int unit = FIRST_UNIT + i;
            GlStateManager._activeTexture(GL13.GL_TEXTURE0 + unit);
            int texture = SkyAtlas.textureId(i);
            GlStateManager._bindTexture(texture);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture); // in case GlStateManager's cache is stale
            int sampler = GL20.glGetUniformLocation(program, "Sampler" + (SkyAtlas.FIRST_SLOT + i));
            if (sampler >= 0) {
                GL20.glUniform1i(sampler, unit);
            }
        }
        GlStateManager._activeTexture(previousUnit);
    }

    private SodiumSkyBinder() {}
}
