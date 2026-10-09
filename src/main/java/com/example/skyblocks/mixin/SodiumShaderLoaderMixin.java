package com.example.skyblocks.mixin;

import com.example.skyblocks.compat.sodium.SodiumShaderPatch;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sodium reads its shaders straight from its own jar, so a resource override can't reach them.
 * This patches the terrain fragment shader source as Sodium loads it.
 * Only applies when Sodium is installed (@Pseudo, require = 0).
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader", remap = false)
public abstract class SodiumShaderLoaderMixin {

    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private static void skyblocks$patchTerrainShader(ResourceLocation name, CallbackInfoReturnable<String> cir) {
        if ("sodium".equals(name.getNamespace()) && SodiumShaderPatch.TARGET.equals(name.getPath())) {
            cir.setReturnValue(SodiumShaderPatch.patch(cir.getReturnValue(), SodiumShaderPatch.lookupSource()));
        }
    }
}
