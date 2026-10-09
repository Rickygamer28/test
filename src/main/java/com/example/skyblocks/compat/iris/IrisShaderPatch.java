package com.example.skyblocks.compat.iris;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Best-effort sky blocks for Iris shader packs. Pure string work, testable on its own.
 *
 * Iris turns the pack's gbuffers_terrain program into a Sodium terrain shader. After Iris has finished
 * transforming it, we:
 *  - vertex shader: rename the pack's main() and add a new main() that calls it, then passes the block
 *    atlas UV to the fragment shader in our own varying (skyblocks_uv);
 *  - fragment shader: the same, then if the atlas texel carries a sky marker, overwrite the pack's
 *    first colour output (location 0, normally albedo) with the sky.
 * The pack still lights that pixel as it likes, so how it looks depends on the pack.
 * Anything unexpected (geometry/tessellation stages, no location-0 output) leaves the shaders untouched.
 */
public final class IrisShaderPatch {
    public static final String MARKER = "skyblocks_packMain";

    private static final Pattern MAIN = Pattern.compile("\\bvoid\\s+main\\s*\\(\\s*(?:void\\s*)?\\)");
    private static final Pattern OUT0 = Pattern.compile(
            "layout\\s*\\(\\s*location\\s*=\\s*0\\s*\\)\\s*out\\s+(?:(?:highp|mediump|lowp)\\s+)?vec4\\s+(\\w+)\\s*;");

    /** Our shared sky lookup with every global name prefixed, so it can't clash with pack code. */
    public static String renameLookup(String lookup) {
        return lookup
                .replaceAll("\\bSampler([3-8])\\b", "skyblocks_Sampler$1")
                .replaceAll("\\bsky_", "skyblocks_sky_")
                .replace("SKY_SHARPEN", "SKYBLOCKS_SHARPEN");
    }

    /** @return patched vertex source, or null if it can't be patched. */
    public static String patchVertex(String vertex) {
        if (vertex == null || vertex.contains(MARKER)
                || !vertex.contains("_vert_tex_diffuse_coord") || !vertex.contains("u_TexCoordShrink")) {
            return null;
        }
        String renamed = renameMain(vertex);
        if (renamed == null) {
            return null;
        }
        return renamed + """

                // ---- Shadered+ (skyblocks) ----
                out vec2 skyblocks_uv;
                void main() {
                    skyblocks_packMain();
                    skyblocks_uv = (_vert_tex_diffuse_coord_bias * u_TexCoordShrink) + _vert_tex_diffuse_coord;
                }
                """;
    }

    /** @return patched fragment source, or null if it can't be patched. */
    public static String patchFragment(String fragment, String lookup) {
        if (fragment == null || lookup == null || fragment.contains(MARKER)) {
            return null;
        }
        Matcher out = OUT0.matcher(fragment);
        if (!out.find()) {
            return null;
        }
        String output = out.group(1);
        String renamed = renameMain(fragment);
        if (renamed == null) {
            return null;
        }
        return renamed + "\n// ---- Shadered+ (skyblocks) ----\n"
                + "uniform sampler2D skyblocks_BlockAtlas;\n"
                + "uniform mat4 skyblocks_ProjInv;\n"
                + "uniform mat3 skyblocks_ViewToWorld;\n"
                + "uniform vec2 skyblocks_ScreenSize;\n"
                + "uniform int skyblocks_Enabled;\n"
                + "in vec2 skyblocks_uv;\n"
                + renameLookup(lookup) + "\n"
                + "void main() {\n"
                + "    skyblocks_packMain();\n"
                + "    if (skyblocks_Enabled == 0) {\n"
                + "        return;\n"
                + "    }\n"
                + "    ivec2 atlasSize = textureSize(skyblocks_BlockAtlas, 0);\n"
                + "    ivec2 texel = clamp(ivec2(skyblocks_uv * vec2(atlasSize)), ivec2(0), atlasSize - ivec2(1));\n"
                + "    float marker = texelFetch(skyblocks_BlockAtlas, texel, 0).a * 255.0;\n"
                + "    int layer = int(floor((244.0 - marker) / 8.0));\n"
                + "    if (marker > 244.0 || layer < 0 || layer >= skyblocks_sky_layers()) {\n"
                + "        return;\n"
                + "    }\n"
                + "    vec2 ndc = gl_FragCoord.xy / skyblocks_ScreenSize * 2.0 - 1.0;\n"
                + "    vec4 p = skyblocks_ProjInv * vec4(ndc, 1.0, 1.0);\n"
                + "    vec3 d = normalize(skyblocks_ViewToWorld * (p.xyz / p.w));\n"
                + "    " + output + " = skyblocks_sky_color(d, layer);\n"
                + "}\n";
    }

    private static String renameMain(String source) {
        Matcher m = MAIN.matcher(source);
        if (!m.find()) {
            return null;
        }
        int start = m.start();
        if (m.find()) {
            return null; // more than one match (e.g. in a comment): too risky
        }
        return source.substring(0, start) + "void " + MARKER + "()"
                + source.substring(start + source.substring(start).indexOf(')') + 1);
    }

    private IrisShaderPatch() {}
}
