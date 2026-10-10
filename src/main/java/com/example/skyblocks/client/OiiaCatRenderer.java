package com.example.skyblocks.client;

import com.example.skyblocks.OiiaCatBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import com.mojang.math.Axis;
import com.noodlegamer76.shadered.client.renderer.assimp.AssimpRenderer;
import com.noodlegamer76.shadered.client.renderer.assimp.RenderableModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Same approach as Shadered's MaxwellRenderer: hands the model, with its world matrix, to Shadered's model renderer.
 * Draws the small cat and the big cat (from the big cat's bottom-centre block, at the cube's size).
 */
public class OiiaCatRenderer implements BlockEntityRenderer<OiiaCatBlockEntity> {

    public OiiaCatRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(OiiaCatBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int size = be.size();
        if (size <= 0) {
            return; // big cat whose size hasn't reached this client yet
        }
        boolean powered = be.powered();
        double center = be.centerOffset();

        // Without a signal: the standing cat. With a signal: the loaf pose, spinning (like the original animation).
        if (!(be.clientModel instanceof RenderableModel)) {
            be.clientModel = OiiaCatModel.create(OiiaCatModel.MODEL);
        }
        if (!(be.clientSpinModel instanceof RenderableModel)) {
            be.clientSpinModel = OiiaCatModel.create(OiiaCatModel.SPIN_MODEL);
        }
        Object chosen = powered ? be.clientSpinModel : be.clientModel;
        if (!(chosen instanceof RenderableModel model)) {
            return; // Shadered hasn't loaded the model (yet)
        }

        // Shadered's model renderer works in world coordinates (like MaxwellRenderer).
        BlockPos pos = be.getBlockPos();
        PoseStack pose = new PoseStack();
        pose.translate(pos.getX() + center, pos.getY(), pos.getZ() + center);
        pose.mulPose(Axis.YP.rotationDegrees(yaw(be.facing())));
        float scale = OiiaCatModel.SCALE * size;
        pose.scale(scale, scale, scale);
        pose.translate(0, OiiaCatModel.FEET, 0);

        // Spin only while powered by redstone, starting from the first frame each time the signal turns on.
        float seconds = 0;
        if (powered && be.getLevel() != null) {
            long now = be.getLevel().getGameTime();
            if (be.animationStart < 0) {
                be.animationStart = now;
            }
            seconds = (now - be.animationStart + partialTick) / 20.0F;
        } else {
            be.animationStart = -1;
        }
        float offsetY = OiiaCatModel.animate(pose, seconds); // 0 = the first frame = standing still

        model.modelMatrix = new Matrix4f(pose.last().pose());
        AssimpRenderer.getInstance().addModel(model);

        // How high the cat's lowest point is above the floor, in blocks (the loaf floats and bobs while spinning).
        float lift = powered ? (OiiaCatModel.SPIN_BOTTOM + offsetY) * scale : 0;
        renderShadow(be, poseStack, buffer, Math.max(0, lift), size, (float) center);
    }

    private static final ResourceLocation SHADOW = ResourceLocation.withDefaultNamespace("textures/misc/shadow.png");
    private static final RenderType SHADOW_TYPE = RenderType.entityShadow(SHADOW);
    /** Same size as a cat's (ocelot's) entity shadow. */
    private static final float SHADOW_RADIUS = 0.4F;

    /**
     * A round shadow on the floor under the cat, drawn the same way Minecraft draws entity shadows (same texture,
     * render type and vertex values). Fainter the higher the cat floats; off when "Entity Shadows" is off or there
     * is no solid floor.
     */
    private static void renderShadow(OiiaCatBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, float lift,
                                     float size, float center) {
        Minecraft mc = Minecraft.getInstance();
        Level level = be.getLevel();
        if (level == null || !mc.options.entityShadows().get()) {
            return;
        }
        BlockPos below = be.getBlockPos().below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return;
        }
        float alpha = 0.5F * Math.max(0, 1 - lift / size * 2.0F);
        if (alpha <= 0.01F) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(SHADOW_TYPE);
        PoseStack.Pose last = poseStack.last();
        float y = 0.002F; // just above the floor (the bottom of our block)
        float r = SHADOW_RADIUS * size;
        shadowVertex(consumer, last, center - r, y, center - r, 0, 0, alpha);
        shadowVertex(consumer, last, center - r, y, center + r, 0, 1, alpha);
        shadowVertex(consumer, last, center + r, y, center + r, 1, 1, alpha);
        shadowVertex(consumer, last, center + r, y, center - r, 1, 0, alpha);
    }

    private static void shadowVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
                                     float u, float v, float alpha) {
        consumer.addVertex(pose, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
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
        int size = Math.max(1, be.size());
        BlockPos pos = be.getBlockPos();
        double c = be.centerOffset();
        // around the cat's feet, reaching up and out past the cat (the spinning tail sticks out)
        return new AABB(pos.getX() + c, pos.getY(), pos.getZ() + c, pos.getX() + c, pos.getY() + size, pos.getZ() + c)
                .inflate(1.5 * size);
    }

    /** Big cats can be seen from further away than block entities usually are. */
    @Override
    public int getViewDistance() {
        return 256;
    }
}
