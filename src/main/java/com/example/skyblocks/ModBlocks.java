package com.example.skyblocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SkyBlocksMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SkyBlocksMod.MOD_ID);

    // The registry name decides which texture is projected:
    // assets/skyblocks/textures/sky/<registry_name>.png
    public static final DeferredBlock<SkyBlock> NEBULA_SKY = BLOCKS.registerBlock(
            "nebula_sky_block",
            SkyBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.GLASS)
                    .requiresCorrectToolForDrops());

    public static final DeferredItem<BlockItem> NEBULA_SKY_ITEM =
            ITEMS.registerSimpleBlockItem("nebula_sky_block", NEBULA_SKY);

    private ModBlocks() {}
}
