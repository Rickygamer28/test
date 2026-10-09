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
 * Draws the sky block as an item (inventory, hand, dropped, item frame).
 * Items are rendered with different camera matrices than the world, so the item shader gets the
 * world camera from SkyMatrices and shows the sky as seen from where the player is looking.
 */
public class SkyBlockItemRenderer extends BlockEntityWithoutLevelRenderer {

    public SkyBlockItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (SkyRenderTypes.skyItemShader == null
                || !(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof SkyBlock skyBlock)) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(SkyRenderTypes.skyItem(skyBlock.getSkyFaces()));
        Matrix4f pose = poseStack.last().pose();

        for (float[][] face : SkyBlockRenderer.FACES) {
            for (float[] v : face) {
                consumer.addVertex(pose, v[0], v[1], v[2]);
            }
        }
    }
}
