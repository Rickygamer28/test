package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlocksMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.noodlegamer76.shadered.client.assimp.AssimpModel;
import com.noodlegamer76.shadered.client.assimp.McModel;
import com.noodlegamer76.shadered.client.assimp.load.AssimpModels;
import com.noodlegamer76.shadered.client.renderer.assimp.RenderableModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

/**
 * The OIIA cat model (assets/skyblocks/models/complex/oiia_cat.glb), loaded by Shadered's model loader, which
 * reads every models/complex/ file of every mod. Also plays the model's own animation on a PoseStack, because
 * Shadered's animator only animates skeletons and this model moves as a whole.
 */
final class OiiaCatModel {
    static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(SkyBlocksMod.MOD_ID, "models/complex/oiia_cat.glb");

    /** Model units -> blocks. The model is about 38.7 units tall, so this makes it about 0.8 blocks tall. */
    static final float SCALE = 0.02F;
    /** The model's feet are 1.1 units below its origin. */
    static final float FEET = 1.10F;

    /** A fresh renderable for the loaded model, or null while Shadered hasn't loaded it (yet). */
    @Nullable
    static RenderableModel create() {
        McModel model = AssimpModels.getModel(MODEL);
        if (model == null) {
            return null;
        }
        RenderableModel renderable = new RenderableModel();
        for (AssimpModel part : model.getModels()) {
            renderable.add(part);
        }
        return renderable;
    }

    /** Applies the "Take 001" animation (looping) at the given time in seconds, in model units. */
    static void animate(PoseStack pose, float seconds) {
        float[] time = OiiaCatAnimation.TIME;
        int last = time.length - 1;
        float start = time[0];
        float length = time[last] - start;
        float t = start + (((seconds % length) + length) % length);

        int i = 0;
        while (i < last - 1 && time[i + 1] <= t) {
            i++;
        }
        float span = time[i + 1] - time[i];
        float f = span > 0 ? Math.max(0, Math.min(1, (t - time[i]) / span)) : 0;

        float y = OiiaCatAnimation.POS_Y[i] + (OiiaCatAnimation.POS_Y[i + 1] - OiiaCatAnimation.POS_Y[i]) * f;
        Quaternionf a = new Quaternionf(0, OiiaCatAnimation.ROT_Y[i], 0, OiiaCatAnimation.ROT_W[i]);
        Quaternionf b = new Quaternionf(0, OiiaCatAnimation.ROT_Y[i + 1], 0, OiiaCatAnimation.ROT_W[i + 1]);

        pose.translate(0, y, 0);
        pose.mulPose(a.slerp(b, f).normalize());
    }

    private OiiaCatModel() {}
}
