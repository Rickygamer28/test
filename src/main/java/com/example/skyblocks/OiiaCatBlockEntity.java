package com.example.skyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Holds the client-side renderable model (like Shadered's MaxwellEntity); nothing is saved. */
public class OiiaCatBlockEntity extends BlockEntity {
    /** Client only: Shadered RenderableModels (walking cat / loaf pose), kept as Object so the server never loads client classes. */
    public Object clientModel;
    public Object clientSpinModel;

    /** Client only: game time when the current redstone signal started (-1 = not powered), to start the animation from frame 0. */
    public long animationStart = -1;

    public OiiaCatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OIIA_CAT.get(), pos, state);
    }
}
