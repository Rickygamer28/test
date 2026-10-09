package com.example.skyblocks.compat.sodium;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splices the sky lookup into Sodium's terrain fragment shader (sodium:blocks/block_layer_opaque.fsh).
 * Pure string work, no Minecraft or Sodium classes, so it can be tested on its own.
 *
 * The patch does what our vanilla rendertype_solid.fsh does: if the block-atlas texel under this pixel
 * carries a sky marker alpha (240 - 8*k) and the material is solid, the pixel shows sky k instead.
 * Works with the Sodium 0.6 and 0.8 shader layouts for 1.21.1. If the shader does not look like either,
 * it is returned unchanged (Sodium keeps working, sky blocks just show their icon).
 */
public final class SodiumShaderPatch {
    public static final String TARGET = "blocks/block_layer_opaque.fsh";
    public static final String MARKER = "skyblocks_sky(";

    private static final Pattern MAIN = Pattern.compile("void\\s+main\\s*\\(\\s*\\)\\s*\\{");

    private static String lookupSource;

    /** @return the patched source, or {@code source} unchanged if it can't be patched safely. */
    public static String patch(String source, String lookup) {
        if (source == null || lookup == null || source.contains(MARKER)
                || !source.contains("u_BlockTex") || !source.contains("v_TexCoord")
                || !source.contains("fragColor")) {
            return source;
        }
        Matcher main = MAIN.matcher(source);
        if (!main.find()) {
            return source;
        }

        // Only solid materials (alpha cutoff 0). Translucent passes are also switched off from Java.
        String cutoff;
        if (source.contains("_material_alpha_cutoff") && source.contains("v_Material")) {
            cutoff = "_material_alpha_cutoff(v_Material)";        // Sodium 0.8
        } else if (source.contains("v_MaterialAlphaCutoff")) {
            cutoff = "v_MaterialAlphaCutoff";                     // Sodium 0.6
        } else {
            cutoff = "0.0";
        }

        StringBuilder decl = new StringBuilder();
        decl.append("\n// ---- Shadered+ (skyblocks) sky blocks ----\n");
        if (!source.contains("uniform mat4 u_ProjectionMatrix")) {
            decl.append("uniform mat4 u_ProjectionMatrix;\n");
        }
        if (!source.contains("uniform mat4 u_ModelViewMatrix")) {
            decl.append("uniform mat4 u_ModelViewMatrix;\n");
        }
        decl.append("uniform vec2 skyblocks_ScreenSize;\n");
        decl.append("uniform int skyblocks_Enabled;\n");
        decl.append(lookup).append('\n');
        decl.append("""
                bool skyblocks_sky(vec2 uv, float alphaCutoff, out vec4 skyOut) {
                    skyOut = vec4(0.0);
                    if (skyblocks_Enabled == 0 || alphaCutoff > 0.0) {
                        return false;
                    }
                    ivec2 atlasSize = textureSize(u_BlockTex, 0);
                    ivec2 texel = clamp(ivec2(uv * vec2(atlasSize)), ivec2(0), atlasSize - ivec2(1));
                    float marker = texelFetch(u_BlockTex, texel, 0).a * 255.0;
                    int layer = int(floor((244.0 - marker) / 8.0));
                    if (marker > 244.0 || layer < 0 || layer >= sky_layers()) {
                        return false;
                    }
                    vec2 ndc = gl_FragCoord.xy / skyblocks_ScreenSize * 2.0 - 1.0;
                    vec4 p = inverse(u_ProjectionMatrix) * vec4(ndc, 1.0, 1.0);
                    vec3 d = normalize(transpose(mat3(u_ModelViewMatrix)) * (p.xyz / p.w));
                    skyOut = sky_color(d, layer);
                    return true;
                }
                // ---- end of Shadered+ ----

                """);

        String call = "\n    vec4 skyblocks_color;\n"
                + "    if (skyblocks_sky(v_TexCoord, " + cutoff + ", skyblocks_color)) {\n"
                + "        fragColor = skyblocks_color;\n"
                + "        return;\n"
                + "    }\n";

        return source.substring(0, main.start())
                + decl
                + source.substring(main.start(), main.end())
                + call
                + source.substring(main.end());
    }

    /** The shared GLSL sky lookup from our own jar (assets/skyblocks/shaders/sodium/sky_lookup.glsl). */
    public static String lookupSource() {
        if (lookupSource == null) {
            try (InputStream in = SodiumShaderPatch.class.getResourceAsStream(
                    "/assets/skyblocks/shaders/sodium/sky_lookup.glsl")) {
                lookupSource = in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                lookupSource = null;
            }
        }
        return lookupSource;
    }

    private SodiumShaderPatch() {}
}
