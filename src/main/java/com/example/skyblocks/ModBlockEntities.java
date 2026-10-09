package com.example.skyblocks;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SkyBlocksMod.MOD_ID);

    /** One block entity type for every Shadered+ skyblock. */
    public static final Supplier<BlockEntityType<SkyBlockEntity>> SKY_BLOCK = TYPES.register("sky_block",
            () -> BlockEntityType.Builder.of(SkyBlockEntity::new,
                    ModBlocks.SKY_BLOCKS.stream().map(b -> (Block) b.get()).toArray(Block[]::new)).build(null));

    private ModBlockEntities() {}
}
