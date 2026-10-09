package com.example.skyblocks.mixin;

import com.example.skyblocks.compat.iris.IrisSkyBinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After Iris sets up a shader-pack terrain program, bind our sky textures and uniforms.
 * Only applies when Iris is installed (@Pseudo, require = 0).
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.programs.SodiumShader", remap = false)
public abstract class IrisSodiumShaderMixin {

    @Inject(method = "setupState", at = @At("TAIL"), require = 0, remap = false)
    private void skyblocks$bindSky(CallbackInfo ci) {
        IrisSkyBinder.afterSetupState();
    }
}
