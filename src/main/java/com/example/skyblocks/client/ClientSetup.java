package com.example.skyblocks.client;

import com.example.skyblocks.SkyBlocksMod;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import com.example.skyblocks.ModBlocks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = SkyBlocksMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        // Placed blocks need no shader of their own: they are drawn by the patched vanilla
        // assets/minecraft/shaders/core/rendertype_solid.* (a resource override, no code needed).

        // assets/skyblocks/shaders/core/sky_item.json (+ .vsh / .fsh)
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(SkyBlocksMod.MOD_ID, "sky_item"),
                        DefaultVertexFormat.POSITION_COLOR),
                shader -> SkyRenderTypes.skyItemShader = shader);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private SkyBlockItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new SkyBlockItemRenderer();
                }
                return renderer;
            }
        }, ModBlocks.SKY_BLOCK_ITEMS.stream().map(DeferredItem::get).toArray(Item[]::new));
    }

    private ClientSetup() {}
}
