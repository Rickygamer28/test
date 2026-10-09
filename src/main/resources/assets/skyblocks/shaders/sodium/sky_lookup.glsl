// ---------------------------------------------------------------------------------------------
// Shadered+ sky lookup. The SAME block of code is in skyblocks:shaders/core/sky_item.fsh and
// minecraft:shaders/core/rendertype_solid.fsh
// and skyblocks:shaders/sodium/sky_lookup.glsl (spliced into Sodium's chunk shader); keep them identical.
//
// Sampler3..Sampler8 are the sky atlases for the cube faces +X -X +Y -Y +Z -Z.
// Each atlas is S wide and S*N tall: sky number k is the square at rows [k*S, (k+1)*S).
// ---------------------------------------------------------------------------------------------
uniform sampler2D Sampler3;
uniform sampler2D Sampler4;
uniform sampler2D Sampler5;
uniform sampler2D Sampler6;
uniform sampler2D Sampler7;
uniform sampler2D Sampler8;

// Catmull-Rom weights for a fractional texel position t in [0,1).
vec4 sky_catmullRom(float t) {
    float t2 = t * t;
    float t3 = t2 * t;
    return vec4(-0.5 * t3 + t2 - 0.5 * t,
                 1.5 * t3 - 2.5 * t2 + 1.0,
                -1.5 * t3 + 2.0 * t2 + 0.5 * t,
                 0.5 * t3 - 0.5 * t2);
}

// One texel of sky `layer`, clamped to that sky's square (never reads a neighbouring sky).
vec3 sky_fetch(sampler2D s, ivec2 p, int layer, int size) {
    p = clamp(p, ivec2(0), ivec2(size - 1));
    return texelFetch(s, ivec2(p.x, p.y + layer * size), 0).rgb;
}

// Bicubic (Catmull-Rom) lookup, 16 texel fetches.
vec3 sky_bicubic(sampler2D s, vec2 uv, int layer, int size) {
    vec2 pos = uv * float(size) - 0.5;
    vec2 base = floor(pos);
    vec2 f = pos - base;
    vec4 wx = sky_catmullRom(f.x);
    vec4 wy = sky_catmullRom(f.y);
    vec3 sum = vec3(0.0);
    for (int j = 0; j < 4; j++) {
        vec3 row = vec3(0.0);
        for (int i = 0; i < 4; i++) {
            row += sky_fetch(s, ivec2(base) + ivec2(i - 1, j - 1), layer, size) * wx[i];
        }
        sum += row * wy[j];
    }
    return max(sum, vec3(0.0));
}

// Bilinear lookup inside sky `layer` (clamped to texel centres so it never bleeds into another sky).
vec3 sky_linear(sampler2D s, vec2 uv, int layer, int size, int layers) {
    float lo = 0.5 / float(size);
    uv = clamp(uv, vec2(lo), vec2(1.0 - lo));
    return textureLod(s, vec2(uv.x, (uv.y + float(layer)) / float(layers)), 0.0).rgb;
}

const float SKY_SHARPEN = 0.6;   // 0 = off. Raise for crisper edges, lower if you see halos.

// One cube face: bicubic reconstruction plus a light unsharp mask.
vec3 sky_face(sampler2D s, vec2 sctc, float ma, int layer) {
    ivec2 atlas = textureSize(s, 0);
    int size = max(atlas.x, 1);
    int layers = max(atlas.y / size, 1);
    vec2 uv = sctc / ma * 0.5 + 0.5;
    float t = 1.0 / float(size);
    vec3 c = sky_bicubic(s, uv, layer, size);
    vec3 soft = 0.25 * (sky_linear(s, uv + vec2(t, 0.0), layer, size, layers)
                      + sky_linear(s, uv - vec2(t, 0.0), layer, size, layers)
                      + sky_linear(s, uv + vec2(0.0, t), layer, size, layers)
                      + sky_linear(s, uv - vec2(0.0, t), layer, size, layers));
    return max(c + SKY_SHARPEN * (c - soft), vec3(0.0));
}

// d = world-space direction. Same cube-map convention as tools/equirect_to_cubemap.py.
vec3 sky_cube(vec3 d, int layer) {
    vec3 a = abs(d);
    if (a.x >= a.y && a.x >= a.z) {
        return d.x > 0.0 ? sky_face(Sampler3, vec2(-d.z, -d.y), a.x, layer)
                         : sky_face(Sampler4, vec2( d.z, -d.y), a.x, layer);
    } else if (a.y >= a.z) {
        return d.y > 0.0 ? sky_face(Sampler5, vec2( d.x,  d.z), a.y, layer)
                         : sky_face(Sampler6, vec2( d.x, -d.z), a.y, layer);
    }
    return d.z > 0.0 ? sky_face(Sampler7, vec2( d.x, -d.y), a.z, layer)
                     : sky_face(Sampler8, vec2(-d.x, -d.y), a.z, layer);
}

// Number of skies in the atlases (0 if they are not bound).
int sky_layers() {
    ivec2 atlas = textureSize(Sampler3, 0);
    return atlas.x > 0 ? atlas.y / atlas.x : 0;
}

// Final sky colour for this pixel, with a tiny dither (about 1.5/255) against banding.
vec4 sky_color(vec3 d, int layer) {
    vec3 c = sky_cube(d, layer);
    float noise = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453);
    return vec4(c + (noise - 0.5) * (1.5 / 255.0), 1.0);
}
// ------------------------------- end of shared sky lookup ------------------------------------
