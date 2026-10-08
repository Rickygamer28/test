package com.example.skyblocks;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(SkyBlocksMod.MOD_ID)
public class SkyBlocksMod {
    public static final String MOD_ID = "skyblocks";

    public SkyBlocksMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.TYPES.register(modEventBus);
        modEventBus.addListener(this::addToCreativeTab);
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.NIGHT_SKY_ITEM);
            event.accept(ModBlocks.SUNSET_SKY_ITEM);
            event.accept(ModBlocks.NEBULA_SKY_ITEM);
        }
    }
}
