package com.example.skyblocks;

import com.noodlegamer76.shadered.entity.block.SkyblockHolderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extends Shadered's skyblock block entity, so it stores a Shadered filter pass (normal, inverted,
 * grayscale, ...) and Shadered's filter items work on our blocks too. Shadered's own sky type field is
 * not used: the sky comes from the block (SkyBlock.getSkyIndex()).
 */
public class SkyBlockEntity extends SkyblockHolderEntity {
    public SkyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SKY_BLOCK.get(), pos, state);
    }

    public int getSkyIndex() {
        return getBlockState().getBlock() instanceof SkyBlock sky ? sky.getSkyIndex() : 0;
    }
}
