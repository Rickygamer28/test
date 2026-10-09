#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform sampler2D Sampler4;
uniform sampler2D Sampler5;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;
uniform vec4 ColorModulator;

out vec4 fragColor;

// Sample one cube face. UVs are pulled half a texel inwards so linear filtering never
// reads across the face border (that would show seams between faces).
vec4 face(sampler2D s, vec2 sctc, float ma) {
    vec2 uv = sctc / ma * 0.5 + 0.5;
    vec2 e = 0.5 / vec2(textureSize(s, 0));
    return texture(s, clamp(uv, e, 1.0 - e));
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
    // Which way the camera looks through THIS PIXEL (ProjMat includes view bobbing, so using
    // the pixel keeps the sky steady while walking).
    vec2 ndc = gl_FragCoord.xy / ScreenSize * 2.0 - 1.0;
    vec4 p = inverse(ProjMat) * vec4(ndc, 1.0, 1.0);
    vec3 viewDir = p.xyz / p.w;

    // ModelViewMat is the camera rotation; its transpose takes view space -> world space.
    vec3 d = normalize(transpose(mat3(ModelViewMat)) * viewDir);

    vec4 color = sampleCube(d) * ColorModulator;
    fragColor = vec4(color.rgb, 1.0);
}
