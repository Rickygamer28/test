package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlock;
import com.example.skyblocks.SkyBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class SkyBlockRenderer implements BlockEntityRenderer<SkyBlockEntity> {

    // 4 corners per face, in Direction.values() order: DOWN, UP, NORTH, SOUTH, WEST, EAST
    public static final float[][][] FACES = {
            {{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}}, // DOWN
            {{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}}, // UP
            {{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}}, // NORTH
            {{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}}, // SOUTH
            {{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}}, // WEST
            {{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}}  // EAST
    };

    public SkyBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SkyBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof SkyBlock skyBlock) || SkyRenderTypes.skyShader == null) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(SkyRenderTypes.sky(skyBlock.getSkyTexture()));
        Matrix4f pose = poseStack.last().pose();
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();

        Direction[] dirs = Direction.values();
        for (int i = 0; i < dirs.length; i++) {
            if (level != null) {
                BlockPos neighborPos = pos.relative(dirs[i]);
                BlockState neighbor = level.getBlockState(neighborPos);
                // Skip faces that can't be seen (touching another sky block or any opaque cube)
                if (neighbor.getBlock() instanceof SkyBlock || neighbor.isSolidRender(level, neighborPos)) {
                    continue;
                }
            }
            for (float[] v : FACES[i]) {
                consumer.addVertex(pose, v[0], v[1], v[2]);
            }
        }
    }
}
