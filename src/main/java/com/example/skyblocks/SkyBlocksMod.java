package com.example.skyblocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(SkyBlocksMod.MOD_ID)
public class SkyBlocksMod {
    public static final String MOD_ID = "skyblocks";

    /** Shadered's creative tab (shadered:shadered_tab). Our blocks are listed there, after Shadered's items. */
    public static final ResourceKey<CreativeModeTab> SHADERED_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath("shadered", "shadered_tab"));

    public SkyBlocksMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.TYPES.register(modEventBus);
        modEventBus.addListener(SkyBlocksMod::addToShaderedTab);
    }

    /** Every item registered in ModBlocks.ITEMS is added to Shadered's tab automatically. */
    private static void addToShaderedTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(SHADERED_TAB)) {
            ModBlocks.ITEMS.getEntries().forEach(entry -> event.accept(entry.get()));
        }
    }
}
