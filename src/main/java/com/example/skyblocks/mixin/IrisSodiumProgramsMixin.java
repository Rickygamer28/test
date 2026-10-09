package com.example.skyblocks.mixin;

import com.example.skyblocks.compat.iris.IrisShaderPatch;
import com.example.skyblocks.compat.sodium.SodiumShaderPatch;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Iris (with a shader pack) builds Sodium terrain programs from the pack's gbuffers_terrain shaders.
 * Right before it compiles them, patch the solid and cutout terrain passes so sky blocks show their sky.
 * Only applies when Iris is installed (@Pseudo, require = 0). Any problem leaves the pack's shaders as they were.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.programs.SodiumPrograms", remap = false)
public abstract class IrisSodiumProgramsMixin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "createGlShaders", at = @At("HEAD"), require = 0, remap = false)
    private void skyblocks$patchTerrain(String passName, Map transformed, CallbackInfoReturnable<Map> cir) {
        if (!"terrain".equals(passName) && !"terrain_cutout".equals(passName)) {
            return;
        }
        try {
            Object vertexKey = null;
            Object fragmentKey = null;
            for (Object entry : transformed.entrySet()) {
                Map.Entry e = (Map.Entry) entry;
                String type = ((Enum<?>) e.getKey()).name();
                if (e.getValue() == null) {
                    continue;
                }
                switch (type) {
                    case "VERTEX" -> vertexKey = e.getKey();
                    case "FRAGMENT" -> fragmentKey = e.getKey();
                    default -> {
                        return; // geometry / tessellation stage: our varying wouldn't reach the fragment shader
                    }
                }
            }
            if (vertexKey == null || fragmentKey == null) {
                return;
            }
            String vertex = IrisShaderPatch.patchVertex((String) transformed.get(vertexKey));
            String fragment = IrisShaderPatch.patchFragment((String) transformed.get(fragmentKey),
                    SodiumShaderPatch.lookupSource());
            if (vertex != null && fragment != null) {
                transformed.put(vertexKey, vertex);
                transformed.put(fragmentKey, fragment);
            }
        } catch (RuntimeException ignored) {
            // e.g. an unmodifiable map in some Iris version: keep the pack's shaders unchanged
        }
    }
}
