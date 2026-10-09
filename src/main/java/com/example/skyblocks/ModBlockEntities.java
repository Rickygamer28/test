package com.example.skyblocks;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SkyBlocksMod.MOD_ID);

    public static final Supplier<BlockEntityType<OiiaCatBlockEntity>> OIIA_CAT = TYPES.register("oiia_cat",
            () -> BlockEntityType.Builder.of(OiiaCatBlockEntity::new, ModBlocks.OIIA_CAT.get()).build(null));

    public static final Supplier<BlockEntityType<BigOiiaCatBlockEntity>> BIG_OIIA_CAT = TYPES.register("big_oiia_cat",
            () -> BlockEntityType.Builder.of(BigOiiaCatBlockEntity::new, ModBlocks.BIG_OIIA_CAT.get()).build(null));

    private ModBlockEntities() {}
}
