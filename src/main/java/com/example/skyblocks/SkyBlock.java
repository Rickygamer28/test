package com.example.skyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A full cube that is drawn by a BlockEntityRenderer instead of the chunk mesh,
 * so the renderer can use our custom skybox shader.
 */
public class SkyBlock extends Block implements EntityBlock {
    public static final MapCodec<SkyBlock> CODEC = simpleCodec(SkyBlock::new);

    /** Cube-map face suffixes, in the order of the shader's Sampler0..Sampler5. */
    public static final String[] FACE_SUFFIXES = {"px", "nx", "py", "ny", "pz", "nz"};

    private ResourceLocation[] skyFaces;

    public SkyBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkyBlockEntity(pos, state);
    }

    /** textures/sky/<registry_name>_px|nx|py|ny|pz|nz.png (resolved lazily, after registration). */
    public ResourceLocation[] getSkyFaces() {
        if (skyFaces == null) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(this);
            ResourceLocation[] faces = new ResourceLocation[FACE_SUFFIXES.length];
            for (int i = 0; i < faces.length; i++) {
                faces[i] = ResourceLocation.fromNamespaceAndPath(
                        key.getNamespace(), "textures/sky/" + key.getPath() + "_" + FACE_SUFFIXES[i] + ".png");
            }
            skyFaces = faces;
        }
        return skyFaces;
    }
}
