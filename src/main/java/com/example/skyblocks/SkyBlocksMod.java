package com.example.skyblocks;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(SkyBlocksMod.MOD_ID)
public class SkyBlocksMod {
    public static final String MOD_ID = "skyblocks";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    /** The "Shadered+" creative tab. Every item registered in ModBlocks.ITEMS is listed automatically. */
    public static final Supplier<CreativeModeTab> SHADERED_PLUS_TAB = CREATIVE_TABS.register("shadered_plus",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MOD_ID))
                    .icon(() -> new ItemStack(ModBlocks.JUPITER_SKY_ITEM.get()))
                    .displayItems((parameters, output) ->
                            ModBlocks.ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
                    .build());

    public SkyBlocksMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModBlockEntities.TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }
}
