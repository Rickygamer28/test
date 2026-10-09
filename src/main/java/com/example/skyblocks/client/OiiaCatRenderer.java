package com.example.skyblocks.client;

import com.example.skyblocks.OiiaCatBlock;
import com.example.skyblocks.OiiaCatBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noodlegamer76.shadered.client.renderer.assimp.AssimpRenderer;
import com.noodlegamer76.shadered.client.renderer.assimp.RenderableModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/** Same approach as Shadered's MaxwellRenderer: hands the model, with its world matrix, to Shadered's model renderer. */
public class OiiaCatRenderer implements BlockEntityRenderer<OiiaCatBlockEntity> {

    public OiiaCatRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(OiiaCatBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(be.clientModel instanceof RenderableModel)) {
            be.clientModel = OiiaCatModel.create();
            if (be.clientModel == null) {
                return;
            }
        }
        RenderableModel model = (RenderableModel) be.clientModel;

        // Shadered's model renderer works in world coordinates (like MaxwellRenderer).
        BlockPos pos = be.getBlockPos();
        PoseStack pose = new PoseStack();
        pose.translate(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(yaw(be.getBlockState().getValue(HorizontalDirectionalBlock.FACING))));
        pose.scale(OiiaCatModel.SCALE, OiiaCatModel.SCALE, OiiaCatModel.SCALE);
        pose.translate(0, OiiaCatModel.FEET, 0);

        // Animate only while powered by redstone, starting from the first frame each time the signal turns on.
        float seconds = 0;
        if (be.getBlockState().getValue(OiiaCatBlock.POWERED) && be.getLevel() != null) {
            long now = be.getLevel().getGameTime();
            if (be.animationStart < 0) {
                be.animationStart = now;
            }
            seconds = (now - be.animationStart + partialTick) / 20.0F;
        } else {
            be.animationStart = -1;
        }
        OiiaCatModel.animate(pose, seconds); // 0 = the first frame = standing still

        model.modelMatrix = new Matrix4f(pose.last().pose());
        AssimpRenderer.getInstance().addModel(model);
    }

    /** The model faces +Z (south); turn it to face the block's FACING. */
    static float yaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case EAST -> 90.0F;
            case WEST -> -90.0F;
            default -> 0.0F;
        };
    }

    @Override
    public AABB getRenderBoundingBox(OiiaCatBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(1.5); // the spinning tail reaches past the block
    }
}
