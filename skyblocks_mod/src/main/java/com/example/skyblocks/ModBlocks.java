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

    private static BlockBehaviour.Properties props(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(1.5F, 6.0F)
                .sound(SoundType.GLASS)
                .requiresCorrectToolForDrops();
    }

    // The registry name decides which texture is projected:
    // assets/skyblocks/textures/sky/<registry_name>.png
    public static final DeferredBlock<SkyBlock> NIGHT_SKY =
            BLOCKS.registerBlock("night_sky_block", SkyBlock::new, props(MapColor.COLOR_BLUE));
    public static final DeferredBlock<SkyBlock> SUNSET_SKY =
            BLOCKS.registerBlock("sunset_sky_block", SkyBlock::new, props(MapColor.COLOR_ORANGE));
    public static final DeferredBlock<SkyBlock> NEBULA_SKY =
            BLOCKS.registerBlock("nebula_sky_block", SkyBlock::new, props(MapColor.COLOR_PURPLE));

    public static final DeferredItem<BlockItem> NIGHT_SKY_ITEM =
            ITEMS.registerSimpleBlockItem("night_sky_block", NIGHT_SKY);
    public static final DeferredItem<BlockItem> SUNSET_SKY_ITEM =
            ITEMS.registerSimpleBlockItem("sunset_sky_block", SUNSET_SKY);
    public static final DeferredItem<BlockItem> NEBULA_SKY_ITEM =
            ITEMS.registerSimpleBlockItem("nebula_sky_block", NEBULA_SKY);

    private ModBlocks() {}
}
