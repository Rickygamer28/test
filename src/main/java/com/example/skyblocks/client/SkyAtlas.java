package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlocksMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * The six sky atlases (textures/sky/atlas_px..nz.png, built by tools/build_sky_atlas.py).
 * Both the patched chunk shader and the item shader read them from Sampler3..Sampler8.
 */
public final class SkyAtlas {
    public static final int FIRST_SLOT = 3;
    private static final String[] FACES = {"px", "nx", "py", "ny", "pz", "nz"};
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[FACES.length];
    private static final AbstractTexture[] LAST = new AbstractTexture[FACES.length];

    static {
        for (int i = 0; i < FACES.length; i++) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(
                    SkyBlocksMod.MOD_ID, "textures/sky/atlas_" + FACES[i] + ".png");
        }
    }

    /** Puts the atlases in shader texture slots 3..8. Render thread only. */
    public static void bind() {
        var textureManager = Minecraft.getInstance().getTextureManager();
        for (int i = 0; i < TEXTURES.length; i++) {
            AbstractTexture texture = textureManager.getTexture(TEXTURES[i]);
            if (texture != LAST[i]) {
                // Linear filtering, no mipmaps (only needed once per loaded texture).
                texture.setFilter(true, false);
                LAST[i] = texture;
            }
            RenderSystem.setShaderTexture(FIRST_SLOT + i, texture.getId());
        }
    }

    private SkyAtlas() {}
}
