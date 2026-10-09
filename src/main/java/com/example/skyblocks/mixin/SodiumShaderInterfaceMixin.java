package com.example.skyblocks.mixin;

import com.example.skyblocks.compat.sodium.SodiumSkyBinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After Sodium sets up its terrain shader for a render pass, bind our sky atlases and uniforms.
 * Only applies when Sodium is installed (@Pseudo, require = 0).
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.shader.DefaultShaderInterface", remap = false)
public abstract class SodiumShaderInterfaceMixin {

    @Inject(method = "setupState", at = @At("TAIL"), require = 0, remap = false)
    private void skyblocks$bindSky(CallbackInfo ci) {
        SodiumSkyBinder.afterSetupState();
    }
}
