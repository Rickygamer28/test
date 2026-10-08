#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec3 worldDir;

out vec4 fragColor;

const float PI = 3.14159265359;

void main() {
    vec3 d = normalize(worldDir);

    // Equirectangular lookup: longitude -> u, latitude -> v
    float u = atan(d.z, d.x) / (2.0 * PI) + 0.5;
    float v = 0.5 - asin(clamp(d.y, -1.0, 1.0)) / PI;

    vec4 color = texture(Sampler0, vec2(u, v)) * ColorModulator;
    fragColor = vec4(color.rgb, 1.0);
}
