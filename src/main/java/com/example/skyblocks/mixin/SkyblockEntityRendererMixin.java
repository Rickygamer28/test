package com.example.skyblocks.mixin;

import com.example.skyblocks.SkyTypes;
import com.example.skyblocks.client.ShaderedSkies;
import com.mojang.blaze3d.vertex.PoseStack;
import com.noodlegamer76.shadered.client.renderer.block.SkyblockEntityRenderer;
import com.noodlegamer76.shadered.client.util.skyblock.SkyblockPass;
import com.noodlegamer76.shadered.entity.block.SkyblockHolderEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Shadered's skyblock renderer has a switch over its own sky types; queue our sky types the same way. */
@Mixin(SkyblockEntityRenderer.class)
public abstract class SkyblockEntityRendererMixin {

    @Inject(method = "render(Lcom/noodlegamer76/shadered/entity/block/SkyblockHolderEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"), cancellable = true)
    private void skyblocks$renderOurSky(SkyblockHolderEntity entity, float partialTick, PoseStack poseStack,
                                        MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        int index = SkyTypes.index(entity.getBlockType());
        if (index < 0) {
            return;
        }
        SkyblockPass pass = entity.getPass() != null ? entity.getPass() : SkyblockPass.NORMAL;
        ShaderedSkies.data(index).add(pass, entity.getBlockPos(), new Matrix4f(poseStack.last().pose()), false, 1.0F);
        ci.cancel();
    }
}
