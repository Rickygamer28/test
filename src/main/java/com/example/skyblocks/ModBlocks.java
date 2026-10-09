package com.example.skyblocks;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * The skies, IN THE ORDER of tools/skies/order.txt (position = sky index).
     * Registry name of each block: <name>_sky_block.
     */
    private static final String[] SKY_NAMES = {"jupiter", "cat", "twilight", "hell"};
    private static final MapColor[] SKY_COLORS = {
            MapColor.COLOR_BROWN, MapColor.COLOR_ORANGE, MapColor.COLOR_PINK, MapColor.COLOR_RED};

    public static final List<DeferredBlock<SkyBlock>> SKY_BLOCKS = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> SKY_BLOCK_ITEMS = new ArrayList<>();

    static {
        for (int i = 0; i < SKY_NAMES.length; i++) {
            final int index = i;
            String id = SKY_NAMES[i] + "_sky_block";
            DeferredBlock<SkyBlock> block =
                    BLOCKS.registerBlock(id, p -> new SkyBlock(p, index), skyProps(SKY_COLORS[i]));
            SKY_BLOCKS.add(block);
            SKY_BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(id, block));
        }
    }

    private static BlockBehaviour.Properties skyProps(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(1.5F, 6.0F)
                .sound(SoundType.GLASS)
                .requiresCorrectToolForDrops();
    }

    private ModBlocks() {}
}
