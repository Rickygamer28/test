package com.example.skyblocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/**
 * A plain, solid, full cube with a normal block model. It has no block entity and no special
 * renderer: the sky is drawn by the patched vanilla chunk shader
 * (assets/minecraft/shaders/core/rendertype_solid.fsh), which recognises the block's texture by its
 * marker alpha. Because of that, anything that copies the block's model (for example Framed Blocks
 * camos) shows the sky too.
 */
public class SkyBlock extends Block {
    public static final MapCodec<SkyBlock> CODEC = simpleCodec(p -> new SkyBlock(p, 0));

    private final int skyIndex;

    public SkyBlock(Properties properties, int skyIndex) {
        super(properties);
        this.skyIndex = skyIndex;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** Which sky of the atlases this block shows (line of tools/skies/order.txt, from 0). */
    public int getSkyIndex() {
        return skyIndex;
    }
}
