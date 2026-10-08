#version 150

uniform sampler2D Sampler0;
uniform vec2 ScreenSize;
uniform vec4 ColorModulator;
uniform mat4 WorldProjInv;   // inverse of the WORLD projection (set once per frame)
uniform mat3 ViewToWorld;    // camera rotation: view space -> world space

out vec4 fragColor;

const float PI = 3.14159265359;

void main() {
    // Same idea as the placed block: find the world direction that the camera sees at this
    // pixel of the screen. The item is a window onto the sky, wherever it is drawn
    // (hand, inventory, ground, item frame), so the sky changes as you look around.
    vec2 ndc = gl_FragCoord.xy / ScreenSize * 2.0 - 1.0;
    vec4 p = WorldProjInv * vec4(ndc, 1.0, 1.0);
    vec3 d = normalize(ViewToWorld * (p.xyz / p.w));

    float u = atan(d.z, d.x) / (2.0 * PI) + 0.5;
    float v = 0.5 - asin(clamp(d.y, -1.0, 1.0)) / PI;

    vec4 color = texture(Sampler0, vec2(u, v)) * ColorModulator;
    fragColor = vec4(color.rgb, 1.0);
}
