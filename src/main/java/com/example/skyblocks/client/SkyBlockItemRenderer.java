package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Draws our skyblock items (inventory, hand, dropped, item frame) as a cube with Shadered's skyblock
 * shader, so the item shows the sky the same way Shadered's own skyblock items do.
 */
public class SkyBlockItemRenderer extends BlockEntityWithoutLevelRenderer {

    // 4 corners + normal per face: DOWN, UP, NORTH, SOUTH, WEST, EAST
    private static final float[][][] FACES = {
            {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}, {0, -1, 0}},
            {{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}},
            {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, -1}},
            {{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}, {0, 0, 1}},
            {{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}, {-1, 0, 0}},
            {{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}, {1, 0, 0}}
    };
    private static final float[][] UVS = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};

    public SkyBlockItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof SkyBlock sky)) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(SkyItemRenderTypes.get(sky.getSkyIndex()));
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();

        for (float[][] face : FACES) {
            float[] n = face[4];
            for (int i = 0; i < 4; i++) {
                float[] v = face[i];
                consumer.addVertex(pose, v[0], v[1], v[2])
                        .setColor(255, 255, 255, 255)
                        .setUv(UVS[i][0], UVS[i][1])
                        .setOverlay(packedOverlay)
                        .setLight(packedLight)
                        .setNormal(last, n[0], n[1], n[2]);
            }
        }
    }
}
