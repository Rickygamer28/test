#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform sampler2D Sampler4;
uniform sampler2D Sampler5;
uniform vec2 ScreenSize;
uniform vec4 ColorModulator;
uniform mat4 WorldProjInv;   // inverse of the WORLD projection (set once per frame by SkyMatrices)
uniform mat3 ViewToWorld;    // camera rotation: view space -> world space

out vec4 fragColor;

// Catmull-Rom weights for a fractional texel position t in [0,1).
vec4 catmullRom(float t) {
    float t2 = t * t;
    float t3 = t2 * t;
    return vec4(-0.5 * t3 + t2 - 0.5 * t,
                 1.5 * t3 - 2.5 * t2 + 1.0,
                -1.5 * t3 + 2.0 * t2 + 0.5 * t,
                 0.5 * t3 - 0.5 * t2);
}

// Bicubic (Catmull-Rom) lookup with 16 texel fetches. Smoother and crisper than plain bilinear
// when a small texture is stretched over many screen pixels. Texel coordinates are clamped to the
// face, so it never reads across a face border (no seams).
vec3 bicubic(sampler2D s, vec2 uv) {
    ivec2 size = textureSize(s, 0);
    vec2 pos = uv * vec2(size) - 0.5;
    vec2 base = floor(pos);
    vec2 f = pos - base;
    vec4 wx = catmullRom(f.x);
    vec4 wy = catmullRom(f.y);
    vec3 sum = vec3(0.0);
    for (int j = 0; j < 4; j++) {
        vec3 row = vec3(0.0);
        for (int i = 0; i < 4; i++) {
            ivec2 p = clamp(ivec2(base) + ivec2(i - 1, j - 1), ivec2(0), size - 1);
            row += texelFetch(s, p, 0).rgb * wx[i];
        }
        sum += row * wy[j];
    }
    return max(sum, vec3(0.0));
}

const float SHARPEN = 0.6;   // 0 = off. Raise for crisper edges, lower if you see halos.

// Sample one cube face: bicubic reconstruction plus a light unsharp mask.
vec4 face(sampler2D s, vec2 sctc, float ma) {
    vec2 uv = sctc / ma * 0.5 + 0.5;
    vec2 texel = 1.0 / vec2(textureSize(s, 0));
    vec2 lo = 0.5 * texel;
    vec2 hi = 1.0 - lo;
    vec3 c = bicubic(s, uv);
    vec3 soft = 0.25 * (texture(s, clamp(uv + vec2(texel.x, 0.0), lo, hi)).rgb
                      + texture(s, clamp(uv - vec2(texel.x, 0.0), lo, hi)).rgb
                      + texture(s, clamp(uv + vec2(0.0, texel.y), lo, hi)).rgb
                      + texture(s, clamp(uv - vec2(0.0, texel.y), lo, hi)).rgb);
    return vec4(max(c + SHARPEN * (c - soft), vec3(0.0)), 1.0);
}

// d = world-space direction. Same cube-map convention as tools/equirect_to_cubemap.py.
vec4 sampleCube(vec3 d) {
    vec3 a = abs(d);
    if (a.x >= a.y && a.x >= a.z) {
        return d.x > 0.0 ? face(Sampler0, vec2(-d.z, -d.y), a.x)
                         : face(Sampler1, vec2( d.z, -d.y), a.x);
    } else if (a.y >= a.z) {
        return d.y > 0.0 ? face(Sampler2, vec2( d.x,  d.z), a.y)
                         : face(Sampler3, vec2( d.x, -d.z), a.y);
    }
    return d.z > 0.0 ? face(Sampler4, vec2( d.x, -d.y), a.z)
                     : face(Sampler5, vec2(-d.x, -d.y), a.z);
}

void main() {
    vec2 ndc = gl_FragCoord.xy / ScreenSize * 2.0 - 1.0;
    vec4 p = WorldProjInv * vec4(ndc, 1.0, 1.0);
    vec3 d = normalize(ViewToWorld * (p.xyz / p.w));

    vec4 color = sampleCube(d) * ColorModulator;

    // Tiny per-pixel noise (about 1.5/255) hides color banding in smooth gradients.
    float noise = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453);
    color.rgb += (noise - 0.5) * (1.5 / 255.0);
    fragColor = vec4(color.rgb, 1.0);
}
