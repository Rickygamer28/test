package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlocksMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * The six sky atlases (textures/sky/atlas_px..nz.png, built by tools/build_sky_atlas.py).
 * The patched chunk shaders (vanilla and Sodium) and the item shader read them as Sampler3..Sampler8.
 */
public final class SkyAtlas {
    public static final int FIRST_SLOT = 3;
    private static final String[] FACES = {"px", "nx", "py", "ny", "pz", "nz"};
    public static final int FACE_COUNT = FACES.length;
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[FACES.length];
    private static final AbstractTexture[] LAST = new AbstractTexture[FACES.length];

    static {
        for (int i = 0; i < FACES.length; i++) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(
                    SkyBlocksMod.MOD_ID, "textures/sky/atlas_" + FACES[i] + ".png");
        }
    }

    /** OpenGL id of atlas {@code face} (0..5 = px nx py ny pz nz). Loads it if needed. Render thread only. */
    public static int textureId(int face) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(TEXTURES[face]);
        if (texture != LAST[face]) {
            // Linear filtering, no mipmaps (only needed once per loaded texture).
            texture.setFilter(true, false);
            LAST[face] = texture;
        }
        return texture.getId();
    }

    /** Puts the atlases in shader texture slots 3..8 (for vanilla shaders). Render thread only. */
    public static void bind() {
        for (int i = 0; i < FACE_COUNT; i++) {
            RenderSystem.setShaderTexture(FIRST_SLOT + i, textureId(i));
        }
    }

    private SkyAtlas() {}
}
