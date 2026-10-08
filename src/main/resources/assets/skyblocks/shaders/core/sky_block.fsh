#version 150

uniform sampler2D Sampler0;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;
uniform vec4 ColorModulator;

out vec4 fragColor;

const float PI = 3.14159265359;

void main() {
    // Work out which way the camera is looking through THIS PIXEL, the same way
    // the real sky is drawn. ProjMat contains view bobbing, so using the pixel
    // (instead of the block's corner positions) keeps the sky steady while walking.
    vec2 ndc = gl_FragCoord.xy / ScreenSize * 2.0 - 1.0;
    vec4 p = inverse(ProjMat) * vec4(ndc, 1.0, 1.0);
    vec3 viewDir = p.xyz / p.w;

    // ModelViewMat is the camera rotation, so its transpose undoes it: view space -> world space.
    vec3 d = normalize(transpose(mat3(ModelViewMat)) * viewDir);

    // Equirectangular lookup: longitude -> u, latitude -> v
    float u = atan(d.z, d.x) / (2.0 * PI) + 0.5;
    float v = 0.5 - asin(clamp(d.y, -1.0, 1.0)) / PI;

    vec4 color = texture(Sampler0, vec2(u, v)) * ColorModulator;
    fragColor = vec4(color.rgb, 1.0);
}
