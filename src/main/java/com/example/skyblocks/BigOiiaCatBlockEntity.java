package com.example.skyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The big cat's master part (bottom centre of the cube): draws the cat at 3x size. */
public class BigOiiaCatBlockEntity extends OiiaCatBlockEntity {
    public BigOiiaCatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BIG_OIIA_CAT.get(), pos, state);
    }
}
