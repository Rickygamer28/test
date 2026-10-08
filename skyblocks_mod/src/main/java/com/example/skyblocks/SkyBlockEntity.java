package com.example.skyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SkyBlockEntity extends BlockEntity {
    public SkyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SKY_BLOCK.get(), pos, state);
    }
}
