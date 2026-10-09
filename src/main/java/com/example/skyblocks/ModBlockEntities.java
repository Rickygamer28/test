package com.example.skyblocks;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SkyBlocksMod.MOD_ID);

    public static final Supplier<BlockEntityType<SkyBlockEntity>> SKY_BLOCK = TYPES.register("sky_block",
            () -> BlockEntityType.Builder.of(SkyBlockEntity::new,
                    ModBlocks.JUPITER_SKY.get(),
                    ModBlocks.CAT_SKY.get(),
                    ModBlocks.TWILIGHT_SKY.get(),
                    ModBlocks.HELL_SKY.get()).build(null));

    private ModBlockEntities() {}
}
