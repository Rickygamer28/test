package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.noodlegamer76.shadered.client.util.skyblock.SkyblockPass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

/**
 * Same job as Shadered's SkyblockEntityRenderer: it draws nothing itself, it queues the block for
 * Shadered's skyblock renderer, which draws it later this frame with the block's filter pass.
 */
public class SkyBlockRenderer implements BlockEntityRenderer<SkyBlockEntity> {

    public SkyBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SkyBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        SkyblockPass pass = be.getPass() != null ? be.getPass() : SkyblockPass.NORMAL;
        ShaderedSkies.data(be.getSkyIndex())
                .add(pass, be.getBlockPos(), new Matrix4f(poseStack.last().pose()), false, 1.0F);
    }
}
