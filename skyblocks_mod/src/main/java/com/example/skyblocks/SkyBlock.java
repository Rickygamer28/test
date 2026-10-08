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

    private ResourceLocation skyTexture;

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

    /** textures/sky/<registry_name>.png (resolved lazily, after registration). */
    public ResourceLocation getSkyTexture() {
        if (skyTexture == null) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(this);
            skyTexture = ResourceLocation.fromNamespaceAndPath(
                    key.getNamespace(), "textures/sky/" + key.getPath() + ".png");
        }
        return skyTexture;
    }
}
